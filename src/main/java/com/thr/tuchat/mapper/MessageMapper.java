package com.thr.tuchat.mapper;

import com.thr.tuchat.pojo.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Select("select * from message where conversation_id = #{conversationId}")
    List<Message> getAllMessageByConversationId (String conversationId);

    @Insert("insert into message (conversation_id, role, content, error_message, attachment) " +
            "values (#{conversationId}, #{role}, #{content}, #{errorMessage}, #{attachment});")
    void addNewMessage (Message message);
}
