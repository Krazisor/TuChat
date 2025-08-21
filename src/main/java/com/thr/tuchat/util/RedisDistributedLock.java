package com.thr.tuchat.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisDistributedLock {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisDistributedLock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 加锁
     *
     * @param lockKey       锁的key
     * @param expireSeconds 过期时间（秒）
     * @return 唯一标识（解锁时需要用到），加锁失败返回null
     */
    public String tryLock(String lockKey, long expireSeconds) {
        if (lockKey.isEmpty() || expireSeconds <= 0) {
            return null;
        }
        String uuid = UUID.randomUUID().toString();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, uuid, expireSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success) ? uuid : null;
    }

    /**
     * 解锁
     *
     * @param lockKey 锁的key
     * @param uuid    加锁时返回的唯一标识
     * @return 是否解锁成功
     */
    public boolean unlock(String lockKey, String uuid) {
        if (lockKey == null || uuid == null) return false;
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        long result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(lockKey),
                uuid
        );
        if(result != 1L){
             log.warn("Unlock failed for {}, uuid={}", lockKey, uuid);
        }
        return result == 1L;
    }
}
