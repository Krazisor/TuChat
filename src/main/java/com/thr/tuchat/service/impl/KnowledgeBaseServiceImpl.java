package com.thr.tuchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.constant.RoleEnum;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.KnowledgeBaseMapper;
import com.thr.tuchat.mapper.KnowledgeBaseMemberMapper;
import com.thr.tuchat.model.dto.KnowledgeBaseListResponse;
import com.thr.tuchat.model.entity.KnowledgeBase;
import com.thr.tuchat.model.entity.KnowledgeBaseMember;
import com.thr.tuchat.service.KnowledgeBaseService;
import com.thr.tuchat.util.RedisDistributedLock;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private KnowledgeBaseMemberMapper knowledgeBaseMemberMapper;

    @Resource
    private RedisDistributedLock redisDistributedLock;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addNewKnowledgeBase(String name, String description, String ownerId) {
        ThrowUtils.throwIf(name == null || ownerId == null, ResultCode.PARAMS_ERROR, "name或ownerId为空");
        ThrowUtils.throwIf(name.length() > 10, ResultCode.PARAMS_ERROR, "name长度不得大于10");
        String redisKeyForAdd = "knowledgeBase:add:" + name;
        String lockValue = redisDistributedLock.tryLock(redisKeyForAdd, 5);
        ThrowUtils.throwIf(lockValue == null, ResultCode.PARAMS_ERROR, "当前知识库name正在被创建，请稍后重试");

        try {
            // 1. 检查name是否可用，redis预查
            String redisKey = "knowledgeBase:name:" + name;
            if (stringRedisTemplate.hasKey(redisKey)) {
                throw new BusinessException(ResultCode.PARAMS_ERROR, "当前知识库name已存在[缓存]");
            }
            LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KnowledgeBase::getName, name);
            boolean exists = knowledgeBaseMapper.selectCount(queryWrapper) > 0;
            if (exists) {
                stringRedisTemplate.opsForValue().set(redisKey, "1");
                throw new BusinessException(ResultCode.PARAMS_ERROR, "当前知识库name已存在");
            }

            // 2. 插入知识库（插入数据库生成ID和createTime等）
            KnowledgeBase newKnowledgeBase = new KnowledgeBase();
            newKnowledgeBase.setName(name);
            newKnowledgeBase.setDescription(description);
            newKnowledgeBase.setOwnerId(ownerId);
            newKnowledgeBase.setCreateTime(new Timestamp(System.currentTimeMillis()));
            knowledgeBaseMapper.insert(newKnowledgeBase);

            // 3. 插入成员权限行
            KnowledgeBaseMember knowledgeBaseMember = new KnowledgeBaseMember();
            knowledgeBaseMember.setKnowledgeBaseId(newKnowledgeBase.getKnowledgeBaseId()); // 注意是知识库ID
            knowledgeBaseMember.setUserId(ownerId);
            knowledgeBaseMember.setRole(RoleEnum.owner);
            knowledgeBaseMember.setJoinTime(newKnowledgeBase.getCreateTime());
            knowledgeBaseMemberMapper.insert(knowledgeBaseMember);

            // 4. 所有写库成功再写缓存
            stringRedisTemplate.opsForValue().set(redisKey, "1");
            return newKnowledgeBase.getKnowledgeBaseId();
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "已经存在同名的知识库");
        } finally {
            if (!redisDistributedLock.unlock(redisKeyForAdd, lockValue)) {
                log.warn("分布式锁没有被正常释放: {}", redisKeyForAdd);
            }
        }
    }


    @Override
    public List<KnowledgeBaseListResponse> getKnowledgeBaseList(String ownerId) {
        ThrowUtils.throwIf(ownerId == null, ResultCode.PARAMS_ERROR, "ownerId为空");
        return knowledgeBaseMapper.selectKnowledgeBaseListByUser(ownerId);
    }




}
