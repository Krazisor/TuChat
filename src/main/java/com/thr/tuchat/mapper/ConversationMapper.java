package com.thr.tuchat.mapper;


import com.thr.tuchat.pojo.Conversation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ConversationMapper {

    @Select("select * from conversation where user_id = #{userId} order by create_time desc")
    List<Conversation> getConversationByUserId (String userId);

    @Select("select * from conversation where conversation_id = #{conversationId}")
    Conversation getConversationById (String conversationId);

    @Select("select user_id from conversation where conversation_id = #{conversationId}")
    String getUserIdByConversationId (String conversationId);

    @Insert("insert into conversation (conversation_id, user_id, title, is_marked)" +
            " values (#{conversationId}, #{userId}, #{title}, #{isMarked});")
    void addNewConversation (Conversation conversation);

    @Update("update conversation set title = #{title} where conversation_id = #{conversationId}")
    void renameConversation (Conversation conversation);
}
