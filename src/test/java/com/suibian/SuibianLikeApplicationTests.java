package com.suibian;

import com.suibian.constant.ThumbConstant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class SuibianLikeApplicationTests {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {

        redisTemplate.opsForHash().keys(ThumbConstant.USER_THUMB_KEY_PREFIX + 1)
                .forEach(System.out::println);
    }

}
