package com.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.OrderConstant;
import com.mall.common.constant.RedisConstant;
import com.mall.common.excption.InConsistentPriceException;
import com.mall.common.excption.NoStockException;
import com.mall.common.excption.VerifyTokenException;
import com.mall.common.to.WareSkuStockTO;
import com.mall.common.to.mq.SeckillOrderTo;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.common.utils.UserHolder;
import com.mall.common.vo.MemberEntityVO;
import com.mall.order.client.CartClient;
import com.mall.order.client.MemberClient;
import com.mall.order.client.ProductClient;
import com.mall.order.client.WareClient;
import com.mall.common.constant.OrderMqConstant;
import com.mall.order.entity.OrderEntity;
import com.mall.order.entity.OrderItemEntity;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.service.OrderItemService;
import com.mall.order.service.OrderService;
import com.mall.order.utils.RedisIdWorker;
import com.mall.order.vo.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private MemberClient memberClient;
    @Resource
    private CartClient cartClient;
    @Resource
    private WareClient wareClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private ProductClient productClient;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 确认订单，异步获取信息
     */
    @Override
    public OrderConfirmVo confirmOrder() {
        MemberEntityVO user = UserHolder.getUser();
        Long userId = user.getId();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 获取用户收获地址
            List<MemberAddressVo> memberAddressVos = buildUserAddress(userId);
            orderConfirmVo.setMemberAddressVos(memberAddressVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 获取有库存的购物车项
            List<OrderItemVo> orderItemVos = buildUserCart(userId);
            orderConfirmVo.setItems(orderItemVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> integrationFuture = CompletableFuture.runAsync(() -> {
            // 获取会员积分
            Integer userIntegration = buildUserIntegration(user);
            orderConfirmVo.setIntegration(userIntegration);
        }, threadPoolExecutor);

        // 库存状态查询
        CompletableFuture<Void> stockFuture = cartFuture.thenRunAsync(() ->
                        buildStocks(orderConfirmVo)
                , threadPoolExecutor);


        try {
            CompletableFuture.allOf(addressFuture, stockFuture, integrationFuture).get();
            orderConfirmVo.setTotal(orderConfirmVo.getTotal());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);

        }
        // 创建幂等性token，将幂等性token添加到redis中
        String token = generateOrderToken(userId);
        orderConfirmVo.setOrderToken(token);
        return orderConfirmVo;
    }

    /**
     * 创建幂等性token
     */
    @NotNull
    private String generateOrderToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(RedisConstant.USER_ORDER_TOKEN_PREFIX + userId, token);
        return token;
    }

    /**
     * 提交订单，加入幂等性验证
     */
//    @GlobalTransactional
    @Override
    public SubmitOrderResponseVo submit(OrderSubmitVo orderSubmitVo) {
        MemberEntityVO user = UserHolder.getUser();
        Long userId = user.getId();
        SubmitOrderResponseVo orderResponseVo = new SubmitOrderResponseVo();
        // 加入幂等性验证（验证token，删除token需要是一个原子性操作）
        String orderToken = orderSubmitVo.getOrderToken();
        if (!verifyToken(orderToken, userId)) {
            // token验证失败，说明当前订单已经创建
            log.error("订单令牌校验失败");
            throw new VerifyTokenException();
        }

        //1、创建订单、订单项等信息
        OrderCreateVO orderCreateVO = createOrder(orderSubmitVo);

        //2、验证价格
        OrderEntity order = orderCreateVO.getOrder();
        BigDecimal payAmount = order.getPayAmount();
        BigDecimal payPrice = orderSubmitVo.getPayPrice();
        if (!priceEqual(payAmount, payPrice)) {
            // 金额校验失败，返回失败信息
            log.error("金额校验失败");
            throw new InConsistentPriceException();
        }

        // 保存订单
        saveOrder(orderCreateVO);
        // 锁库存
        List<OrderItemEntity> orderItems = orderCreateVO.getOrderItems();
        lockWares(order, orderItems);
        // 创建订单成功，将订单添加到mq中
        rabbitTemplate.convertAndSend(OrderMqConstant.ORDER_EVENT_EXCHANGE, OrderMqConstant.ORDER_CREATE_ORDER_ROUTING_KEY, order);
        orderResponseVo.setOrder(order);
        orderResponseVo.setCode(0);
        return orderResponseVo;
    }

    private void lockWares(OrderEntity order, List<OrderItemEntity> orderItems) {
        WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
        wareSkuLockVo.setOrderSn(order.getOrderSn());
        List<OrderItemVo> orderLockVos = orderItems.stream().map(orderItemEntity -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setCount(orderItemEntity.getSkuQuantity());
            orderItemVo.setSkuId(orderItemEntity.getSkuId());
            orderItemVo.setTitle(orderItemEntity.getSkuName());
            return orderItemVo;
        }).collect(Collectors.toList());
        wareSkuLockVo.setLocks(orderLockVos);
        R lockResponse = wareClient.orderLocks(wareSkuLockVo);
        if (!lockResponse.getCode().equals("0")) {
            // 库存锁定失败
            log.error("库存锁定失败");
            throw new NoStockException();
        }
    }

    @Override
    public OrderEntity queryOrderByOrdersn(String orderSn) {
        return this.lambdaQuery().eq(OrderEntity::getOrderSn, orderSn).one();
    }

    /**
     * 根据订单号查询订单
     */
    @Override
    public OrderEntity getByOrderSn(String orderSn) {
        return this.lambdaQuery().eq(OrderEntity::getOrderSn, orderSn).one();
    }

    /**
     * 根据用户id查询订单列表
     */
    @Override
    public List<OrderEntity> queryOrderByUserId(Long userId) {
        return this.lambdaQuery().eq(OrderEntity::getMemberId, userId).list();
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params, Long userId) {

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getMemberId, userId).orderByDesc(OrderEntity::getCreateTime)
        );

        //遍历所有订单集合
        List<OrderEntity> orderEntityList = page.getRecords().stream().map(order -> {
            //根据订单号查询订单项里的数据
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>()
                    .eq("order_sn", order.getOrderSn()));
            order.setOrderItemEntityList(orderItemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderEntityList);

        return new PageUtils(page);
    }

    /**
     * 将订单转为已支付状态同时修改库存
     */
    @Override
    public void processOrderPayment(String orderSn) {
        this.lambdaUpdate().eq(OrderEntity::getOrderSn, orderSn).set(OrderEntity::getStatus, 1).update();
        wareClient.deductWare(orderSn);
    }

    /**
     * 创建秒杀单
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = orderTo.getSeckillPrice().multiply(BigDecimal.valueOf(orderTo.getNum()));
        orderEntity.setPayAmount(totalPrice);
        orderEntity.setStatus(0);

        //保存订单
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(orderTo.getOrderSn());
        orderItem.setRealAmount(totalPrice);
        orderItem.setSkuId(orderTo.getSkuId());
        orderItem.setSkuQuantity(orderTo.getNum());


        //保存商品的spu信息
        R spuInfo = productClient.getSpuInfoBySkuId(orderTo.getSkuId());
        SpuInfoVo spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoVo>() {
        });
        orderItem.setSpuId(spuInfoData.getId());
        orderItem.setSpuName(spuInfoData.getSpuName());
        orderItem.setSpuBrand(spuInfoData.getBrandName());
        orderItem.setCategoryId(spuInfoData.getCatalogId());

        //保存订单项数据
        orderItemService.save(orderItem);

        // 锁定库存
        lockWares(orderEntity, Collections.singletonList(orderItem));
        // 创建订单成功，将订单添加到mq中
        rabbitTemplate.convertAndSend(OrderMqConstant.ORDER_EVENT_EXCHANGE, OrderMqConstant.ORDER_CREATE_ORDER_ROUTING_KEY, orderEntity);

    }

    /**
     * 保存订单
     */
    private void saveOrder(OrderCreateVO orderCreateTo) {

        //获取订单信息
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        //保存订单
        this.save(order);


        //获取订单项信息
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems().stream().peek(orderItemEntity -> orderItemEntity.setOrderId(order.getId())).collect(Collectors.toList());
        //批量保存订单项数据
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 价格比较
     */
    private static boolean priceEqual(BigDecimal payAmount, BigDecimal payPrice) {
        return Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01;
    }

    /**
     * 创建订单实体
     */
    private OrderCreateVO createOrder(OrderSubmitVo orderSubmitVo) {

        OrderCreateVO createVO = new OrderCreateVO();

        //1、生成订单号
        String orderSn = String.valueOf(redisIdWorker.nextId("order"));
        // 构建订单对象
        OrderEntity orderEntity = builderOrderFromSubmit(orderSn, orderSubmitVo);

        //2、获取到所有的订单项
        List<OrderItemEntity> orderItemEntities = builderOrderItems(orderSn);

        //3、验价(计算价格、积分等信息)
        computePrice(orderEntity, orderItemEntities);

        createVO.setOrder(orderEntity);
        createVO.setOrderItems(orderItemEntities);


        return createVO;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            intergration = intergration.add(orderItem.getIntegrationAmount());

            //总价
            total = total.add(orderItem.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();

        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 构建订单项数据
     */
    private List<OrderItemEntity> builderOrderItems(String orderSn) {
        // 获取最新的购物车数据
        MemberEntityVO user = UserHolder.getUser();
        Long userId = user.getId();
        List<OrderItemVo> orderItemVos = buildUserCart(userId);

        //最后确定每个购物项的价格
        if (CollectionUtils.isEmpty(orderItemVos)) {
            return Collections.emptyList();
        }
        List<OrderItemEntity> orderItemEntities = orderItemVos.stream().map((items) -> {
            //构建订单项数据
            OrderItemEntity orderItemEntity = builderOrderItem(items);
            orderItemEntity.setOrderSn(orderSn);

            return orderItemEntity;
        }).collect(Collectors.toList());
        return orderItemEntities;
    }

    /**
     * 构建某一个订单项的数据
     *
     * @param items
     * @return
     */
    private OrderItemEntity builderOrderItem(OrderItemVo items) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //1、商品的spu信息
        Long skuId = items.getSkuId();
        //获取spu的信息
        R spuInfoResponse = productClient.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoData = spuInfoResponse.getData("data", new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoData.getId());
        orderItemEntity.setSpuName(spuInfoData.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoData.getBrandName());
        orderItemEntity.setCategoryId(spuInfoData.getCatalogId());

        //2、商品的sku信息
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(items.getTitle());
        orderItemEntity.setSkuPic(items.getImage());
        orderItemEntity.setSkuPrice(items.getPrice());
        orderItemEntity.setSkuQuantity(items.getCount());

        //使用StringUtils.collectionToDelimitedString将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(items.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);

        //3、商品的优惠信息

        //4、商品的积分信息
        orderItemEntity.setGiftGrowth(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());
        orderItemEntity.setGiftIntegration(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());

        //5、订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        //当前订单项的实际金额.总额 - 各种优惠价格
        //原来的价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //原价减去优惠价得到最终的价格
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }

    /**
     * 构建订单数据
     */
    private OrderEntity builderOrderFromSubmit(String orderSn, OrderSubmitVo orderSubmitVo) {

        //获取当前用户登录信息
        MemberEntityVO user = UserHolder.getUser();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(user.getId());
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberUsername(user.getUsername());


        Long addrId = orderSubmitVo.getAddrId();
        //远程获取收货地址和运费信息
        R fareResponse = wareClient.getFare(addrId);
        FareVo fareResp = fareResponse.getData("data", new TypeReference<FareVo>() {
        });

        //获取到运费信息
        BigDecimal fare = fareResp.getFare();
        orderEntity.setFreightAmount(fare);

        //获取到收货地址信息
        MemberAddressVo address = fareResp.getAddress();
        //设置收货人信息
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());

        //设置订单相关的状态信息
        orderEntity.setStatus(OrderConstant.ORDER_CREATE_NEW_STATE);
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setConfirmStatus(0);
        return orderEntity;
    }

    @NotNull
    private Boolean verifyToken(String orderToken, Long userId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]); " +
                "else " +
                "return 0; " +
                "end; ";
        RedisScript<Long> redisScript = new DefaultRedisScript<Long>(script, Long.class);
        Long res = stringRedisTemplate.execute(redisScript
                , Collections.singletonList(RedisConstant.USER_ORDER_TOKEN_PREFIX + userId)
                , orderToken);
        return res != null && res == 1L;
    }

    private void buildStocks(OrderConfirmVo orderConfirmVo) {
        List<Long> skuIds = orderConfirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        List<WareSkuStockTO> wareSkuStockTOS = wareClient.queryStockBySkuIds(skuIds);
        Map<Long, Boolean> stocks = wareSkuStockTOS.stream().collect(Collectors.toMap(WareSkuStockTO::getSkuId, item -> item.getStock() > 0));
        orderConfirmVo.setStocks(stocks);
    }

    private Integer buildUserIntegration(MemberEntityVO user) {
        Integer userIntegration = user.getIntegration();
        return userIntegration == null ? 0 : userIntegration;

    }

    private List<OrderItemVo> buildUserCart(Long userId) {
        R cartResponse = cartClient.queryCartById(userId);
        if (!cartResponse.getCode().equals("0")) {
            log.error("查询购物车失败");
            throw new RuntimeException("查询购物车失败");
        }
        List<OrderItemVo> orderItemVos = cartResponse.getData("data", new TypeReference<List<OrderItemVo>>() {
        });
        orderItemVos = orderItemVos.stream().filter(OrderItemVo::getCheck).collect(Collectors.toList());
        return orderItemVos;

    }

    private List<MemberAddressVo> buildUserAddress(Long userId) {
        // 构建会员收获地址列表
        R memberResponse = memberClient.queryListByUserId(userId);
        if (!memberResponse.getCode().equals("0")) {
            log.error("查询用户地址列表");
            throw new RuntimeException("查询用户地址列表");
        }

        return memberResponse.getData("data", new TypeReference<List<MemberAddressVo>>() {
        });

    }

}