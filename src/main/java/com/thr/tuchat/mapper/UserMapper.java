package com.thr.tuchat.mapper;

import com.thr.tuchat.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO user(user_id, user_name, email, avatar) VALUES (#{userId},#{userName},#{email},#{avatar})")
    void addUser(User user);

    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User getUserById(String userId);

    @Update("UPDATE user SET user_name = #{userName}, email = #{email}, avatar = #{avatar} WHERE user_id = #{userId}")
    void updateUser(User user);
}
