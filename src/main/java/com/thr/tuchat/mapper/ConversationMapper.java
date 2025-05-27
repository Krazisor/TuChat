package com.thr.tuchat.mapper;


import com.thr.tuchat.pojo.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationMapper {

    @Select("select * from tuchat.conversation where user_id = #{userId}")
    List<Conversation> getConversationByUserId (String userId);

    @Select("select * from tuchat.conversation where conversation_id = #{conversationId}")
    Conversation getConversationById (String conversationId);

    @Select("select user_id from tuchat.conversation where conversation_id = #{conversationId}")
    String getUserIdByConversationId (String conversationId);
}
