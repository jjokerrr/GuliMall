package com.mall.cart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mall.cart.client.ProductClient;
import com.mall.cart.service.CartService;
import com.mall.common.utils.UserHolder;
import com.mall.cart.vo.CartItemVo;
import com.mall.cart.vo.CartVo;
import com.mall.common.constant.RedisConstant;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberEntityVO;
import com.mall.common.vo.SkuInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ProductClient productClient;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 添加商品到用户购物车
     */
    @Override
    public void addToCart(Long skuId, Integer num) {
        MemberEntityVO loginUser = UserHolder.getUser();
        if (loginUser == null) {
            log.error("用户尚未登入");
            throw new RuntimeException("用户尚未登入");
        }
        Long userId = loginUser.getId();

        // redis中存在商品数据，则购物车数量加一
        BoundHashOperations<String, Object, Object> userCartRedisOption = getUserCartRedisOption(userId);
        if (Boolean.TRUE.equals(userCartRedisOption.hasKey(String.valueOf(skuId)))) {
            updateCartItemInfo(skuId, num, userCartRedisOption);
            return;
        }

        // 不存在商品数据，将商品存入购物车
        CartItemVo cartItemVo = generateCartItem(skuId, num);
        userCartRedisOption.put(String.valueOf(skuId), JSON.toJSONString(cartItemVo));
    }

    private void updateCartItemInfo(Long skuId, Integer count, BoundHashOperations<String, Object, Object> userCartRedisOption) {
        // 存在该商品，将购物车数量加一
        String cartJSON = (String) userCartRedisOption.get(String.valueOf(skuId));
        CartItemVo cartItemVo = JSON.parseObject(cartJSON, CartItemVo.class);
        cartItemVo.setCount(cartItemVo.getCount() + count);
        cartItemVo.setTotalPrice(cartItemVo.getTotalPrice());
        // 保存之前需要手动序列化成json
        userCartRedisOption.put(String.valueOf(skuId), JSON.toJSONString(cartItemVo));
    }

    /**
     * 异步查询商品数据封装成购物车对象
     */
    private CartItemVo generateCartItem(Long skuId, Integer count) {
        CartItemVo cartItemVo = new CartItemVo();
        // 检索商品数据
        CompletableFuture<Void> setBase = CompletableFuture.runAsync(() -> {
            R skuInfoResponse = productClient.info(skuId);
            if (!skuInfoResponse.getCode().equals("0")) {
                log.error("查询sku信息失败");
                throw new RuntimeException("查询sku信息失败");
            }
            SkuInfoVO skuInfo = skuInfoResponse.getData("skuInfo", new TypeReference<SkuInfoVO>() {
            });
            cartItemVo.setTitle(skuInfo.getSkuTitle());
            cartItemVo.setPrice(skuInfo.getPrice());
            cartItemVo.setSkuId(skuInfo.getSkuId());
            cartItemVo.setImage(skuInfo.getSkuDefaultImg());
            cartItemVo.setCount(count);
            cartItemVo.setTotalPrice(cartItemVo.getTotalPrice());
        }, threadPoolExecutor);
        // sku的全部组合信息
        CompletableFuture<Void> setSaleAttrs = CompletableFuture.runAsync(() -> {
            R response = productClient.getSaleAttrsBySkuId(skuId);
            cartItemVo.setSkuAttrValues(response.getData("data", new TypeReference<List<String>>() {
            }));
        }, threadPoolExecutor);

        try {
            CompletableFuture.allOf(setSaleAttrs, setBase).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return cartItemVo;
    }

    private BoundHashOperations<String, Object, Object> getUserCartRedisOption(Long userId) {
        String cartKey = RedisConstant.USER_CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> stringObjectObjectBoundHashOperations = stringRedisTemplate.boundHashOps(cartKey);
        return stringObjectObjectBoundHashOperations;
    }

    /**
     * 获取购物车数据对象
     */
    @Override
    public CartItemVo getCartItem(Long skuId) {
        // 获取登录用户
        MemberEntityVO user = UserHolder.getUser();
        Long userId = user.getId();
        // 获取redis数据
        String cartStrValue = (String) stringRedisTemplate.opsForHash()
                .get(RedisConstant.USER_CART_PREFIX + userId, String.valueOf(skuId));


        if (StrUtil.isBlank(cartStrValue)) {
            return null;
        }
        return JSON.parseObject(cartStrValue, CartItemVo.class);
    }

    /**
     * 获取用户购物车列表
     */
    @Override
    public CartVo getCartList() {
        // 检查用户登录状态
        MemberEntityVO loginUser = UserHolder.getUser();
        if (loginUser == null) {
            log.error("用户尚未登入");
            throw new RuntimeException("用户尚未登入");
        }

        // 获取全部的购物车信息
        Long userId = UserHolder.getUser().getId();
        return getCartByUserId(userId);
    }

    private CartVo getCartByUserId(Long userId) {
        Map<Object, Object> cartEntries = stringRedisTemplate.opsForHash()
                .entries(RedisConstant.USER_CART_PREFIX + userId);

        // 获取全部的value数据，将value转换成List<CartItemVO>

        List<CartItemVo> cartItems = new ArrayList<>();

        // 遍历购物车数据，转换为 CartItemVO 对象
        for (Map.Entry<Object, Object> entry : cartEntries.entrySet()) {
            String cartItemJson = (String) entry.getValue();
            CartItemVo cartItem = JSON.parseObject(cartItemJson, CartItemVo.class);
            cartItems.add(cartItem);
        }
        CartVo cartVo = new CartVo();
        cartVo.setItems(cartItems);
        return cartVo;
    }

    /**
     * 修改购物车中商品的数量
     */
    @Override
    public void updateCartItemCount(Long skuId, Integer num) {
        // 检查用户登录状态
        MemberEntityVO loginUser = UserHolder.getUser();
        if (loginUser == null) {
            log.error("用户尚未登入");
            throw new RuntimeException("用户尚未登入");
        }
        // 获取用户数据
        String cartItemJSON = (String) stringRedisTemplate.opsForHash()
                .get(RedisConstant.USER_CART_PREFIX + loginUser.getId(), String.valueOf(skuId));

        CartItemVo cartItem = JSON.parseObject(cartItemJSON, CartItemVo.class);

        if (cartItem != null) {
            cartItem.setCount(num);
        }

        stringRedisTemplate.opsForHash()
                .put(RedisConstant.USER_CART_PREFIX + loginUser.getId(), String.valueOf(skuId), JSON.toJSONString(cartItem));
    }

    /**
     * 删除购物车商品
     */
    @Override
    public void deleteCartItem(Long skuId) {
        // 检查用户登录状态
        MemberEntityVO loginUser = UserHolder.getUser();
        if (loginUser == null) {
            log.error("用户尚未登入");
            throw new RuntimeException("用户尚未登入");
        }
        // 获取用户数据
        stringRedisTemplate.opsForHash()
                .delete(RedisConstant.USER_CART_PREFIX + loginUser.getId(), String.valueOf(skuId));

    }

    /**
     * 根据用户id查询最新的购物车信息
     */
    @Override
    public List<CartItemVo> queryCartByUserId(Long userId) {
        CartVo cartVo = getCartByUserId(userId);
        List<CartItemVo> items = cartVo.getItems();

        List<Long> cartItemIds = items.stream().map(CartItemVo::getSkuId).collect(Collectors.toList());
        // 查询最新价格
        R currentPriceResponse = productClient.getCurrentPrice(cartItemIds);
        List<SkuInfoVO> currentPrice = currentPriceResponse.getData("data", new TypeReference<List<SkuInfoVO>>() {
        });
        Map<Long, BigDecimal> skuIdPriceMap = currentPrice.stream().collect(Collectors.toMap(SkuInfoVO::getSkuId, SkuInfoVO::getPrice));
        items = items.stream()
                .peek(item -> item.setPrice(skuIdPriceMap.get(item.getSkuId())))
                .collect(Collectors.toList());
        return items;
    }
}
