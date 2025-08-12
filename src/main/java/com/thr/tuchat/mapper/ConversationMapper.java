package com.thr.tuchat.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thr.tuchat.model.entity.Conversation;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
