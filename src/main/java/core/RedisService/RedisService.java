package core.RedisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class RedisService {
    @Autowired
    RedisTemplate<String,String> redisTemplate;

    public void setValue(String key, String value, long timeout) throws Exception {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw e;
        }
    }

    public String get(String key) throws Exception{
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw e;
        }
    }

    public void unset(String key) throws Exception{
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw e;
        }
    }

    public long incrementValue(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            throw e;
        }
    }
}
