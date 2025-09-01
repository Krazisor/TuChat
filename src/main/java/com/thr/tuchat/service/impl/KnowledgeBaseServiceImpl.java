package com.thr.tuchat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.constant.ChangeTypeEnum;
import com.thr.tuchat.constant.RoleEnum;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.KnowledgeBaseMapper;
import com.thr.tuchat.mapper.KnowledgeBaseMemberMapper;
import com.thr.tuchat.model.dto.KnowledgeBaseListResponse;
import com.thr.tuchat.model.entity.KnowledgeBase;
import com.thr.tuchat.model.entity.KnowledgeBaseMember;
import com.thr.tuchat.service.FileService;
import com.thr.tuchat.service.KnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private KnowledgeBaseMemberMapper knowledgeBaseMemberMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FileService fileService;

    private static final long LOCK_WAIT_TIMEOUT_SECONDS = 5;
    private static final long LOCK_LEASE_TIMEOUT_SECONDS = 30;
    private static final long CACHE_EXPIRY_MINUTES = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addNewKnowledgeBase(String name, String description, String ownerId) {
        ThrowUtils.throwIf(name == null || ownerId == null, ResultCode.PARAMS_ERROR, "name或ownerId为空");
        ThrowUtils.throwIf(name.length() > 10, ResultCode.PARAMS_ERROR, "name长度不得大于10");

        String redisKeyForAdd = "knowledgeBase:add:" + name;
        String redisKey = "knowledgeBase:name:" + name;

        // 使用 Redisson 获取分布式锁
        RLock lock = redissonClient.getLock(redisKeyForAdd);
        try {
            boolean locked = lock.tryLock(LOCK_WAIT_TIMEOUT_SECONDS, LOCK_LEASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            ThrowUtils.throwIf(!locked, ResultCode.PARAMS_ERROR, "当前知识库name正在被创建，请稍后重试");
            // 检查名称是否重复
            ThrowUtils.throwIf(!checkRepeatKnowledgeBaseName(name), ResultCode.PARAMS_ERROR, "当前知识库name已存在");
            // 插入知识库
            KnowledgeBase newKnowledgeBase = new KnowledgeBase();
            newKnowledgeBase.setName(name);
            newKnowledgeBase.setDescription(description);
            newKnowledgeBase.setOwnerId(ownerId);
            newKnowledgeBase.setCreateTime(new Timestamp(System.currentTimeMillis()));
            knowledgeBaseMapper.insert(newKnowledgeBase);
            // 插入成员权限
            KnowledgeBaseMember knowledgeBaseMember = new KnowledgeBaseMember();
            knowledgeBaseMember.setKnowledgeBaseId(newKnowledgeBase.getKnowledgeBaseId());
            knowledgeBaseMember.setUserId(ownerId);
            knowledgeBaseMember.setRole(RoleEnum.OWNER);
            knowledgeBaseMember.setJoinTime(newKnowledgeBase.getCreateTime());
            knowledgeBaseMemberMapper.insert(knowledgeBaseMember);
            // 更新缓存
            stringRedisTemplate.opsForValue().set(redisKey, "1", CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES);
            return newKnowledgeBase.getKnowledgeBaseId();
        } catch (InterruptedException e) {
            log.error("获取锁时线程被中断, name: {}", name, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "已经存在同名的知识库");
        } catch (Exception e) {
            log.error("创建知识库失败, name: {}", name, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建知识库失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("分布式锁(新增知识库)释放成功, key: {}", redisKeyForAdd);
            }
        }
    }

    @Override
    public List<KnowledgeBaseListResponse> getKnowledgeBaseList(String ownerId) {
        ThrowUtils.throwIf(ownerId == null, ResultCode.PARAMS_ERROR, "ownerId为空");
        return knowledgeBaseMapper.selectKnowledgeBaseListByUser(ownerId);
    }

    @Override
    public Map<RoleEnum, List<String>> getKnowledgeBaseMember(String knowledgeBaseId) {
        String userId = StpUtil.getLoginIdAsString();
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = Wrappers.<KnowledgeBase>lambdaQuery()
                .eq(KnowledgeBase::getKnowledgeBaseId, knowledgeBaseId);
        List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectList(queryWrapper);
        ThrowUtils.throwIf(!userId.equals(knowledgeBases.getFirst().getOwnerId()), ResultCode.PARAMS_ERROR,
                "非知识库拥有者想要获取知识库成员");
        return this.getCurrentMembersGroupedByRole(knowledgeBaseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteKnowledgeBaseWithFile(String knowledgeBaseId) {
        // 验证权限：当前用户是否为知识库拥有者
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeBase::getKnowledgeBaseId, knowledgeBaseId);
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectOne(queryWrapper);
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(knowledgeBase == null, ResultCode.NOT_FOUND_ERROR, "知识库不存在");
        ThrowUtils.throwIf(!userId.equals(knowledgeBase.getOwnerId()), ResultCode.NO_AUTH_ERROR, "用户无权限删除该知识库");

        String redisKeyForDelete = "knowledgeBase:delete:" + knowledgeBaseId;

        // 使用 Redisson 获取分布式锁
        RLock lock = redissonClient.getLock(redisKeyForDelete);
        try {
            boolean locked = lock.tryLock(LOCK_WAIT_TIMEOUT_SECONDS, LOCK_LEASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            ThrowUtils.throwIf(!locked, ResultCode.PARAMS_ERROR, "当前知识库正在被删除，请不要重复提交请求");
            // 删除关联文件
            List<String> fileIds = fileService.getFileIdsByKnowledgeBaseId(knowledgeBaseId);
            if (!fileIds.isEmpty()) {
                fileService.removeByIds(fileIds);
            }
            // 删除权限记录
            LambdaQueryWrapper<KnowledgeBaseMember> memberQueryWrapper = new LambdaQueryWrapper<>();
            memberQueryWrapper.eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId);
            List<KnowledgeBaseMember> members = knowledgeBaseMemberMapper.selectList(memberQueryWrapper);
            if (!members.isEmpty()) {
                List<Long> memberIds = members.stream().map(KnowledgeBaseMember::getId).collect(Collectors.toList());
                knowledgeBaseMemberMapper.deleteByIds(memberIds);
            }
            // 删除知识库本身
            knowledgeBaseMapper.deleteById(knowledgeBaseId);
            // 清理缓存
            String redisKey = "knowledgeBase:name:" + knowledgeBase.getName();
            stringRedisTemplate.delete(redisKey);
            return true;
        } catch (InterruptedException e) {
            log.error("获取锁时线程被中断, knowledgeBaseId: {}", knowledgeBaseId, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("删除知识库失败, knowledgeBaseId: {}", knowledgeBaseId, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除知识库失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("分布式锁(删除知识库)释放成功, key: {}", redisKeyForDelete);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeKnowledgeBaseOwner(String knowledgeBaseId, String newOwnerId, RoleEnum newRole) {
        ThrowUtils.throwIf(knowledgeBaseId.isEmpty(), ResultCode.PARAMS_ERROR, "knowledgeBaseId为空");
        ThrowUtils.throwIf(newOwnerId.isEmpty(), ResultCode.PARAMS_ERROR, "newOwnerId为空");
        ThrowUtils.throwIf(RoleEnum.OWNER.equals(newRole), ResultCode.OPERATION_ERROR, "用户易主后还相当主人");
        // 首先查到知识库，查看操作人是否是知识库的主人
        String redisKeyForOwner = "knowledgeBase:owner:" + knowledgeBaseId;
        RLock lock = redissonClient.getLock(redisKeyForOwner);
        try {
            boolean locked = lock.tryLock(LOCK_WAIT_TIMEOUT_SECONDS, LOCK_LEASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            ThrowUtils.throwIf(!locked, ResultCode.PARAMS_ERROR, "当前知识库正在被易主，请不要重复提交请求");
            String operationUserId = StpUtil.getLoginIdAsString();
            LambdaQueryWrapper<KnowledgeBase> knowledgeBaseQueryWrapper = new LambdaQueryWrapper<>();
            knowledgeBaseQueryWrapper.eq(KnowledgeBase::getKnowledgeBaseId, knowledgeBaseId);
            KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectOne(knowledgeBaseQueryWrapper);
            ThrowUtils.throwIf(knowledgeBase == null, ResultCode.NOT_FOUND_ERROR, "不存在的用户Id");
            ThrowUtils.throwIf(!knowledgeBase.getOwnerId().equals(operationUserId), ResultCode.NO_AUTH_ERROR,
                    "用户企图易主不是自己的知识库");
            ThrowUtils.throwIf(knowledgeBase.getOwnerId().equals(newOwnerId), ResultCode.OPERATION_ERROR,
                    "用户想把自己的知识库传给自己");
            // 接下来在知识库表中修改主人的id
            knowledgeBase.setOwnerId(newOwnerId);
            knowledgeBaseMapper.updateById(knowledgeBase);
            // 接下来操作member表中的数据。首先，删除新用户原先可能留存的member记录
            LambdaQueryWrapper<KnowledgeBaseMember> memberQueryWrapper = new LambdaQueryWrapper<>();
            memberQueryWrapper.select(KnowledgeBaseMember::getId);
            memberQueryWrapper.eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId);
            memberQueryWrapper.eq(KnowledgeBaseMember::getUserId, newOwnerId);
            KnowledgeBaseMember member = knowledgeBaseMemberMapper.selectOne(memberQueryWrapper);
            knowledgeBaseMemberMapper.deleteById(member.getId());
            // 接下来，找到OWNER的记录，修改对应的ID
            LambdaQueryWrapper<KnowledgeBaseMember> ownerQueryWrapper = new LambdaQueryWrapper<>();
            ownerQueryWrapper.select(KnowledgeBaseMember::getId);
            ownerQueryWrapper.eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId);
            ownerQueryWrapper.eq(KnowledgeBaseMember::getRole, RoleEnum.OWNER.getRole());
            KnowledgeBaseMember ownerMember = knowledgeBaseMemberMapper.selectOne(ownerQueryWrapper);
            ownerMember.setUserId(newOwnerId);
            knowledgeBaseMemberMapper.updateById(ownerMember);
            // 最后,把原先的主人赋予新的职位
            if (RoleEnum.NONE.equals(newRole)) {
                return true;
            } else {
                KnowledgeBaseMember newMember = new KnowledgeBaseMember();
                newMember.setKnowledgeBaseId(knowledgeBaseId).setUserId(operationUserId).setRole(newRole);
                knowledgeBaseMemberMapper.insert(newMember);
            }
        } catch (InterruptedException e) {
            log.error("获取锁时线程被中断, knowledgeBaseId: {}", knowledgeBaseId, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("分布式锁(易主知识库)释放成功, key: {}", redisKeyForOwner);
            }
        }
        return true;
    }

    /**
     * @param knowledgeBaseId 知识库Id
     * @param name            知识库名称
     * @param description     知识库描述
     * @param editorList      编辑者名单
     * @param viewList        浏览者名单
     * @return 操作是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateKnowledgeBaseInfo(
            String knowledgeBaseId, String name, String description,
            List<String> editorList, List<String> viewList) {
        String redisKeyForUpdate = "knowledgeBase:update:" + knowledgeBaseId;

        // 使用 Redisson 获取分布式锁
        RLock lock = redissonClient.getLock(redisKeyForUpdate);
        try {
            boolean locked = lock.tryLock(LOCK_WAIT_TIMEOUT_SECONDS, LOCK_LEASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            ThrowUtils.throwIf(!locked, ResultCode.PARAMS_ERROR, "当前知识库正在被修改，请不要重复提交请求");
            // 获取当前知识库信息
            KnowledgeBase knowledgeBaseInfo = knowledgeBaseMapper.selectById(knowledgeBaseId);
            ThrowUtils.throwIf(knowledgeBaseInfo == null, ResultCode.NOT_FOUND_ERROR, "知识库不存在");
            // 更新基本信息
            boolean updated = this.updateBasicInfo(knowledgeBaseId, name, description);
            ThrowUtils.throwIf(!updated, ResultCode.SYSTEM_ERROR, "更新知识库基本信息失败");
            // 更新成员权限
            this.updateMembers(knowledgeBaseId, editorList, viewList);
            // 清理缓存
            String redisKey = "knowledgeBase:name:" + knowledgeBaseInfo.getName();
            stringRedisTemplate.delete(redisKey);
            // 新增缓存
            if (!name.equals(knowledgeBaseInfo.getName())) {
                String newRedisKey = "knowledgeBase:name:" + name;
                stringRedisTemplate.opsForValue().set(newRedisKey, "1", CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES);
            }
            return true;
        } catch (InterruptedException e) {
            log.error("获取锁时线程被中断, knowledgeBaseId: {}", knowledgeBaseId, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("更新知识库失败, knowledgeBaseId: {}", knowledgeBaseId, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新知识库失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("分布式锁(更新知识库)释放成功, key: {}", redisKeyForUpdate);
            }
        }
    }

    /**
     * 检查知识库名称是否重复
     *
     * @param name 知识库名称
     * @return 是否可用，true 表示名称可用，false 表示名称已存在
     */
    private boolean checkRepeatKnowledgeBaseName(String name) {
        ThrowUtils.throwIf(name == null || name.trim().isEmpty(), ResultCode.PARAMS_ERROR, "知识库名称不能为空");
        String redisKey = "knowledgeBase:name:" + name;
        if (stringRedisTemplate.hasKey(redisKey)) {
            log.info("从 Redis 缓存检测到知识库名称已存在: {}", name);
            return false;
        }
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeBase::getName, name);
        long count = knowledgeBaseMapper.selectCount(queryWrapper);
        if (count > 0) {
            stringRedisTemplate.opsForValue().set(redisKey, "1", CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES);
            log.info("从数据库检测到知识库名称已存在: {}", name);
            return false;
        }
        log.info("知识库名称可用: {}", name);
        return true;
    }

    /**
     * 知识库基本信息更新
     *
     * @param knowledgeBaseId 知识库ID
     * @param name            知识库名称
     * @param description     知识库描述
     * @return 更新是否成功
     */
    private boolean updateBasicInfo(String knowledgeBaseId, String name, String description) {
        LambdaUpdateWrapper<KnowledgeBase> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KnowledgeBase::getKnowledgeBaseId, knowledgeBaseId)
                .set(KnowledgeBase::getName, name)
                .set(KnowledgeBase::getDescription, description);
        return knowledgeBaseMapper.update(updateWrapper) > 0;
    }

    /**
     * 知识库角色名单更新
     *
     * @param knowledgeBaseId 知识库ID
     * @param editorList      新的编辑者名单
     * @param viewList        新的浏览者名单
     */
    private void updateMembers(String knowledgeBaseId, List<String> editorList, List<String> viewList) {
        Map<RoleEnum, List<String>> roleListMap = getCurrentMembersGroupedByRole(knowledgeBaseId);
        Map<ChangeTypeEnum, List<String>> editorChangeMap = computeUserListChanges(editorList, roleListMap.get(RoleEnum.EDITOR));
        Map<ChangeTypeEnum, List<String>> viewerChangeMap = computeUserListChanges(viewList, roleListMap.get(RoleEnum.VIEWER));
        // 处理添加用户
        List<KnowledgeBaseMember> adders = createMembersFromChanges(knowledgeBaseId, editorChangeMap.get(ChangeTypeEnum.ADDERS), RoleEnum.EDITOR);
        adders.addAll(createMembersFromChanges(knowledgeBaseId, viewerChangeMap.get(ChangeTypeEnum.ADDERS), RoleEnum.VIEWER));
        if (!adders.isEmpty()) {
            knowledgeBaseMemberMapper.insert(adders);
        }
        // 处理移除用户
        List<String> removers = new ArrayList<>(editorChangeMap.get(ChangeTypeEnum.REMOVERS));
        removers.addAll(viewerChangeMap.get(ChangeTypeEnum.REMOVERS));
        if (!removers.isEmpty()) {
            LambdaQueryWrapper<KnowledgeBaseMember> removeQuery = new LambdaQueryWrapper<>();
            removeQuery.eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId)
                    .in(KnowledgeBaseMember::getUserId, removers);
            List<KnowledgeBaseMember> toRemove = knowledgeBaseMemberMapper.selectList(removeQuery);
            if (!toRemove.isEmpty()) {
                List<Long> removeIds = toRemove.stream().map(KnowledgeBaseMember::getId).collect(Collectors.toList());
                knowledgeBaseMemberMapper.deleteByIds(removeIds);
            }
        }
    }

    /**
     * 查询知识库ID中所有成员，按照身份区分
     *
     * @param knowledgeBaseId 知识库ID
     * @return 以 RoleEnum 为键的成员ID清单
     */
    private Map<RoleEnum, List<String>> getCurrentMembersGroupedByRole(String knowledgeBaseId) {
        LambdaQueryWrapper<KnowledgeBaseMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId)
                .select(KnowledgeBaseMember::getUserId, KnowledgeBaseMember::getRole);
        List<KnowledgeBaseMember> members = knowledgeBaseMemberMapper.selectList(queryWrapper);
        return filterAndGroupByRole(members);
    }

    /**
     * 构造 KnowledgeBaseMember 列表
     *
     * @param knowledgeBaseId 知识库ID
     * @param userIds         用户ID列表
     * @param role            身份
     * @return KnowledgeBaseMember 列表
     */
    private List<KnowledgeBaseMember> createMembersFromChanges(String knowledgeBaseId, List<String> userIds, RoleEnum role) {
        return userIds.stream()
                .map(userId -> {
                    KnowledgeBaseMember member = new KnowledgeBaseMember();
                    member.setKnowledgeBaseId(knowledgeBaseId);
                    member.setUserId(userId);
                    member.setRole(role);
                    member.setJoinTime(new Timestamp(System.currentTimeMillis()));
                    return member;
                })
                .collect(Collectors.toList());
    }

    /**
     * 按角色分组成员
     *
     * @param members 某个知识库所有的成员清单
     * @return 以 RoleEnum 为键的成员ID清单
     */
    private Map<RoleEnum, List<String>> filterAndGroupByRole(List<KnowledgeBaseMember> members) {
        Map<RoleEnum, List<String>> groupedMembers = new EnumMap<>(RoleEnum.class);
        List<String> editors = new ArrayList<>();
        List<String> viewers = new ArrayList<>();
        members.forEach(member -> {
            RoleEnum role = member.getRole();
            if (role == RoleEnum.EDITOR) {
                editors.add(member.getUserId());
            } else if (role == RoleEnum.VIEWER) {
                viewers.add(member.getUserId());
            }
        });
        groupedMembers.put(RoleEnum.EDITOR, editors);
        groupedMembers.put(RoleEnum.VIEWER, viewers);
        return groupedMembers;
    }

    /**
     * 计算用户列表的变化，区分需要添加和移除的用户
     *
     * @param newUserList      新用户列表
     * @param existingUserList 老用户列表
     * @return 以 ChangeTypeEnum 为键，包含待删除和新增 userId 列表的 Map
     */
    private Map<ChangeTypeEnum, List<String>> computeUserListChanges(List<String> newUserList, List<String> existingUserList) {
        boolean isNewEmpty = newUserList == null || newUserList.isEmpty();
        boolean isExistingEmpty = existingUserList == null || existingUserList.isEmpty();
        if (isNewEmpty && isExistingEmpty) {
            return Map.of(ChangeTypeEnum.ADDERS, List.of(), ChangeTypeEnum.REMOVERS, List.of());
        }
        if (isNewEmpty) {
            return Map.of(ChangeTypeEnum.ADDERS, List.of(), ChangeTypeEnum.REMOVERS, List.copyOf(existingUserList));
        }
        if (isExistingEmpty) {
            return Map.of(ChangeTypeEnum.ADDERS, List.copyOf(newUserList), ChangeTypeEnum.REMOVERS, List.of());
        }
        Set<String> newSet = new HashSet<>(newUserList);
        Set<String> existingSet = new HashSet<>(existingUserList);
        List<String> adders = new ArrayList<>(newSet);
        adders.removeAll(existingSet);
        List<String> removers = new ArrayList<>(existingSet);
        removers.removeAll(newSet);
        Map<ChangeTypeEnum, List<String>> changes = new EnumMap<>(ChangeTypeEnum.class);
        changes.put(ChangeTypeEnum.ADDERS, Collections.unmodifiableList(adders));
        changes.put(ChangeTypeEnum.REMOVERS, Collections.unmodifiableList(removers));
        return changes;
    }
}
