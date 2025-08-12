package com.thr.tuchat.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thr.tuchat.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}