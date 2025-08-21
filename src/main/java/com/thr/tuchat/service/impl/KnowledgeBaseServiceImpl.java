package com.thr.tuchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.KnowledgeBaseMapper;
import com.thr.tuchat.model.entity.KnowledgeBase;
import com.thr.tuchat.service.KnowledgeBaseService;
import com.thr.tuchat.util.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;

public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private RedisDistributedLock redisDistributedLock;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    /*
     * 我们希望数据库不是同名的
     * 数据库唯一约束确实可以保证数据最终一致性，防止重复插入。但分布式锁的意义在于优化并发场景下的用户体验和系统性能，具体体现在：
        （1）减少数据库冲突和异常
        如果没有分布式锁，多个并发请求会同时尝试插入同名数据，数据库会抛出唯一约束异常（如 DuplicateKeyException）。
        频繁的异常会导致数据库压力增大，影响性能，甚至可能导致事务回滚、锁表等问题。
        （2）提升用户体验
        有分布式锁时，只有一个请求会真正走到插入逻辑，其他请求会被提前拦截（拿不到锁），可以直接返回“已存在”或“操作失败”，而不是等数据库报错。
        这样用户不会遇到“系统异常”或“插入失败”的提示，而是明确的业务提示。
        （3）减少无效操作和资源浪费
        没有分布式锁时，所有并发请求都要查库、尝试插入，最后只有一个成功，其他都失败，造成大量无效数据库写操作。
        有分布式锁时，只有一个请求会执行插入，其他请求直接返回，减少数据库压力。
        （4）业务流程可控
        某些业务场景下，插入前后还有其他操作（比如发通知、写日志等），如果依赖数据库唯一约束，异常处理流程会变复杂。
        分布式锁可以让业务流程更清晰，只有拿到锁的请求才会走完整流程。
     */
    public Boolean addNewKnowledgeBase(String name, String description, String ownerId) {
        ThrowUtils.throwIf(name == null || ownerId == null, ResultCode.PARAMS_ERROR,
                "name或ownerId为空");
        if (!this.checkKnowledgeBaseName(name)) {
            return false;
        }
        String redisKeyForAdd = "knowledgeBase:add:" + name;
        String lockValue = redisDistributedLock.tryLock(redisKeyForAdd, 5);
        if (lockValue == null) {
            return false;
        }
        try {
            if (!this.checkKnowledgeBaseName(name)) {
                return false;
            }
            KnowledgeBase newKnowledgeBase = new KnowledgeBase();
            newKnowledgeBase.setName(name);
            newKnowledgeBase.setDescription(description);
            newKnowledgeBase.setOwnerId(ownerId);
            try {
                knowledgeBaseMapper.insert(newKnowledgeBase);
                stringRedisTemplate.opsForValue().set("knowledgeBase:name:" + name, "1");
                return true; // 插入成功
            } catch (DuplicateKeyException e) {
                // 数据库唯一约束异常，说明已存在
                return false;
            }
        } finally {
            redisDistributedLock.unlock(redisKeyForAdd, lockValue);
        }
    }

    public Boolean checkKnowledgeBaseName(String name) {
        String redisKey = "knowledgeBase:name:" + name;
        if (stringRedisTemplate.hasKey(redisKey)) {
            // 缓存命中，提前拦截
            return false;
        }
        // 缓存没命中，查库
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeBase::getName, name);
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectOne(queryWrapper);
        if (knowledgeBase != null) {
            // 数据库有，写入缓存，提前拦截
            stringRedisTemplate.opsForValue().set(redisKey, "1");
            return false;
        } else {
            return true;
        }
    }
}
