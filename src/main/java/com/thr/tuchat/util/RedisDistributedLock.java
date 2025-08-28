package com.thr.tuchat.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Deprecated
@Slf4j
public class RedisDistributedLock {

    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> unlockScript;
    // 用于存储每个锁的续期任务，避免重复创建和便于取消
    private final ConcurrentHashMap<String, ScheduledFuture<?>> renewalTasks;
    // 定时任务执行器，用于锁续期
    private final ScheduledExecutorService renewalExecutor;

    public RedisDistributedLock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        // 初始化解锁脚本
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptText(UNLOCK_LUA_SCRIPT);
        this.unlockScript.setResultType(Long.class);
        // 初始化续期任务存储
        this.renewalTasks = new ConcurrentHashMap<>();
        // 初始化续期任务执行器，线程池大小可根据业务需求调整
        this.renewalExecutor = new ScheduledThreadPoolExecutor(4,
                r -> new Thread(r, "RedisLockRenewal-" + UUID.randomUUID()));
    }

    /**
     * 尝试加锁
     *
     * @param lockKey       锁的key
     * @param expire        过期时间
     * @param timeUnit      时间单位
     * @return 唯一标识（解锁时需要用到），加锁失败返回null
     */
    public String tryLock(String lockKey, long expire, TimeUnit timeUnit) {
        if (!StringUtils.hasText(lockKey) || expire <= 0) {
            log.warn("Invalid parameters for tryLock: lockKey={}, expire={}", lockKey, expire);
            return null;
        }
        String uuid = UUID.randomUUID().toString();
        try {
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, uuid, expire, timeUnit);
            if (Boolean.TRUE.equals(success)) {
                log.debug("Lock acquired for key={}, uuid={}", lockKey, uuid);
                // 成功获取锁后，启动续期任务
                startRenewalTask(lockKey, uuid, expire, timeUnit);
                return uuid;
            } else {
                log.debug("Lock acquire failed for key={}", lockKey);
                return null;
            }
        } catch (Exception e) {
            log.error("Error while acquiring lock for key={}: {}", lockKey, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 尝试加锁（带重试机制）
     *
     * @param lockKey        锁的key
     * @param expire         过期时间
     * @param timeUnit       时间单位
     * @param maxRetryTimes  最大重试次数
     * @param retryInterval  重试间隔（毫秒）
     * @return 唯一标识（解锁时需要用到），加锁失败返回null
     */
    public String tryLockWithRetry(String lockKey, long expire, TimeUnit timeUnit, int maxRetryTimes, long retryInterval) {
        if (!StringUtils.hasText(lockKey) || expire <= 0 || maxRetryTimes <= 0 || retryInterval <= 0) {
            log.warn("Invalid parameters for tryLockWithRetry: lockKey={}, expire={}, maxRetryTimes={}, retryInterval={}",
                    lockKey, expire, maxRetryTimes, retryInterval);
            return null;
        }

        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < maxRetryTimes; i++) {
            try {
                Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, uuid, expire, timeUnit);
                if (Boolean.TRUE.equals(success)) {
                    log.debug("Lock acquired for key={}, uuid={}, attempt={}", lockKey, uuid, i + 1);
                    // 成功获取锁后，启动续期任务
                    startRenewalTask(lockKey, uuid, expire, timeUnit);
                    return uuid;
                }
                log.debug("Lock acquire failed for key={}, attempt={}", lockKey, i + 1);
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Thread interrupted while trying to acquire lock for key={}", lockKey);
                    return null;
                }
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                log.warn("Thread interrupted while trying to acquire lock for key={}: {}", lockKey, e.getMessage());
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                log.error("Error while acquiring lock for key={}: {}", lockKey, e.getMessage(), e);
                return null;
            }
        }
        log.info("Failed to acquire lock for key={} after {} attempts", lockKey, maxRetryTimes);
        return null;
    }

    /**
     * 解锁
     *
     * @param lockKey 锁的key
     * @param uuid    加锁时返回的唯一标识
     * @return 是否解锁成功
     */
    public boolean unlock(String lockKey, String uuid) {
        if (!StringUtils.hasText(lockKey) || !StringUtils.hasText(uuid)) {
            log.warn("Invalid parameters for unlock: lockKey={}, uuid={}", lockKey, uuid);
            return false;
        }
        try {
            Long result = stringRedisTemplate.execute(
                    unlockScript,
                    Collections.singletonList(lockKey),
                    uuid
            );
            if (result == 1L) {
                log.debug("Lock released for key={}, uuid={}", lockKey, uuid);
                // 解锁成功，取消续期任务
                cancelRenewalTask(lockKey);
                return true;
            } else {
                log.warn("Unlock failed for key={}, uuid={}, possibly lock expired or not owned", lockKey, uuid);
                // 如果锁已过期或不属于当前线程，也尝试取消续期任务（以防万一）
                cancelRenewalTask(lockKey);
                return false;
            }
        } catch (Exception e) {
            log.error("Error while releasing lock for key={}: {}", lockKey, e.getMessage(), e);
            // 发生异常时也尝试取消续期任务
            cancelRenewalTask(lockKey);
            return false;
        }
    }

    /**
     * 启动锁续期任务
     *
     * @param lockKey  锁的key
     * @param uuid     锁的唯一标识
     * @param expire   过期时间
     * @param timeUnit 时间单位
     */
    private void startRenewalTask(String lockKey, String uuid, long expire, TimeUnit timeUnit) {
        // 计算续期间隔，通常是过期时间的1/3或1/2，确保在锁过期前有足够时间续期
        long renewalIntervalMillis = timeUnit.toMillis(expire) / 3;
        // 确保间隔至少为1秒，避免过于频繁
        renewalIntervalMillis = Math.max(renewalIntervalMillis, 1000);

        // 创建续期任务
        ScheduledFuture<?> future = renewalExecutor.scheduleAtFixedRate(() -> {
            try {
                // 检查锁是否仍然由当前线程持有
                String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
                if (uuid.equals(currentValue)) {
                    // 锁仍然由当前线程持有，执行续期
                    Boolean renewed = stringRedisTemplate.expire(lockKey, expire, timeUnit);
                    if (renewed) {
                        log.debug("Lock renewed for key={}, uuid={}, new expire={} {}", lockKey, uuid, expire, timeUnit);
                    } else {
                        log.warn("Failed to renew lock for key={}, uuid={}", lockKey, uuid);
                    }
                } else {
                    // 锁已不属于当前线程（可能被删除或被其他线程覆盖），停止续期
                    log.debug("Lock no longer owned for key={}, stopping renewal, currentValue={}", lockKey, currentValue);
                    cancelRenewalTask(lockKey);
                }
            } catch (Exception e) {
                log.error("Error while renewing lock for key={}: {}", lockKey, e.getMessage(), e);
            }
        }, renewalIntervalMillis, renewalIntervalMillis, TimeUnit.MILLISECONDS);

        // 将任务存入Map，方便解锁时取消
        renewalTasks.put(lockKey, future);
        log.debug("Renewal task started for key={}, interval={}ms", lockKey, renewalIntervalMillis);
    }

    /**
     * 取消锁续期任务
     *
     * @param lockKey 锁的key
     */
    private void cancelRenewalTask(String lockKey) {
        ScheduledFuture<?> future = renewalTasks.remove(lockKey);
        if (future != null) {
            future.cancel(false); // 取消任务，不中断正在执行的任务
            log.debug("Renewal task cancelled for key={}", lockKey);
        }
    }

    /**
     * 在应用关闭时清理资源
     */
    @org.springframework.context.event.EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void shutdown() {
        log.info("Shutting down RedisDistributedLock, cancelling all renewal tasks");
        renewalTasks.forEach((key, future) -> {
            future.cancel(false);
            log.debug("Cancelled renewal task for key={}", key);
        });
        renewalTasks.clear();
        renewalExecutor.shutdown();
        try {
            if (!renewalExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                renewalExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while shutting down renewal executor", e);
            renewalExecutor.shutdownNow();
        }
        log.info("RedisDistributedLock shutdown completed");
    }
}
