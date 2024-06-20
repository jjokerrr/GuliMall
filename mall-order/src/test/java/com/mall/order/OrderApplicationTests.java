package com.mall.order;

import com.alibaba.fastjson.TypeReference;
import com.mall.common.utils.R;
import com.mall.order.client.CartClient;
import com.mall.order.client.MemberClient;
import com.mall.order.entity.OrderEntity;
import com.mall.order.vo.MemberAddressVo;
import com.mall.order.vo.OrderItemVo;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class OrderApplicationTests {

    @Resource
    private MemberClient memberClient;

    @Resource
    private CartClient cartClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    void testMemberAddress() {
        R r = memberClient.queryListByUserId(1L);
        List<MemberAddressVo> data = r.getData("data", new TypeReference<List<MemberAddressVo>>() {
        });
        System.out.println(data);
    }

    @Test
    public void testCartItem() {
        R r = cartClient.queryCartById(3L);
        List<OrderItemVo> data = r.getData("data", new TypeReference<List<OrderItemVo>>() {
        });
        System.out.println(data);
    }

    @Test
    public void testLuaScript() {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]); " +
                "else " +
                "return 0; " +
                "end; ";
        RedisScript<Long> redisScript = new DefaultRedisScript<Long>(script, Long.class);
        Long execute = stringRedisTemplate.execute(redisScript, Collections.singletonList("1"), "1");
        System.out.println(execute);
    }

    @Test
    public void testSendMessage() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn("123456");

        rabbitTemplate.convertAndSend("order.delay.queue", orderEntity);
    }
}
