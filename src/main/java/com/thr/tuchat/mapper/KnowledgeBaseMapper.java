package com.thr.tuchat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thr.tuchat.model.dto.KnowledgeBaseListResponse;
import com.thr.tuchat.model.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /**
     *
     * @param userId 用户Id
     * @return 用户所有的knowledgeBase
     */
    @Select("""
        SELECT
            kb.knowledge_base_id,
            kb.name,
            kb.description,
            kb.create_time,
            kbm.role,
            kbm.join_time
        FROM
            knowledge_base kb
        JOIN
            knowledge_base_member kbm
        ON kb.knowledge_base_id = kbm.knowledge_base_id
        WHERE
            kbm.user_id = #{userId}
        ORDER BY
            kb.create_time DESC
    """)
    List<KnowledgeBaseListResponse> selectKnowledgeBaseListByUser(@Param("userId") String userId);
}
