package com.thr.tuchat.mapper;

import com.thr.tuchat.pojo.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Select("select * from tuchat.message where conversation_id = #{conversationId} order by create_time")
    List<Message> getAllMessageByConversationId (String conversationId);

    @Insert("insert into tuchat.message (conversation_id, role, content, error_message, attachment) " +
            "values (#{conversationId}, #{role}, #{content}, #{errorMessage}, #{attachment});")
    void addNewMessage (Message message);

    @Delete("delete from tuchat.message where conversation_id = #{conversationId}")
    void deleteMessagesByConversationId (String conversationId);
}
