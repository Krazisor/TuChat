package com.thr.tuchat.mapper;


import com.thr.tuchat.pojo.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConversationMapper {

    @Select("select * from tuchat.conversation where user_id = #{userId} order by create_time desc")
    List<Conversation> getConversationByUserId (String userId);

    @Select("select * from tuchat.conversation where conversation_id = #{conversationId}")
    Conversation getConversationById (String conversationId);

    @Select("select user_id from tuchat.conversation where conversation_id = #{conversationId}")
    String getUserIdByConversationId (String conversationId);

    @Insert("insert into tuchat.conversation (conversation_id, user_id, title, is_marked)" +
            " values (#{conversationId}, #{userId}, #{title}, #{isMarked});")
    void addNewConversation (Conversation conversation);

    @Delete("delete from tuchat.conversation where conversation_id = #{conversationId}")
    void deleteConversationById (String conversationId);

    @Update("update tuchat.conversation set title = #{title}, is_marked = #{isMarked}, user_id = #{userId} where conversation_id = conversation_id")
    void updateConversationById (Conversation conversation);
}
