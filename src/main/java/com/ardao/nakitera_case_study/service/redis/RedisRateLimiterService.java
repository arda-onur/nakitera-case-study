package com.ardao.nakitera_case_study.service.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisRateLimiterService {

    private final StringRedisTemplate redisTemplate;
    @Value("${app.rate-limit.limit}")
    private long LIMIT;

    @Value("${app.rate-limit.window-second}")
    private long SECOND;

    public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key){
        try{
            Long currentCount = this.redisTemplate.opsForValue().increment(key);
            if(currentCount !=null && currentCount == 1){
                this.redisTemplate.expire(key, Duration.ofSeconds(SECOND));
            }

            return currentCount == null || (currentCount <= LIMIT);
        }catch (Exception ex){
            return true;
        }

    }
}
