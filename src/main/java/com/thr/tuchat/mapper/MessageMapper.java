package com.thr.tuchat.mapper;

import com.thr.tuchat.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Select("select * from tuchat.message where conversation_id = #{conversationId}")
    List<Message> getAllMessageByConversationId (String conversationId);
}
