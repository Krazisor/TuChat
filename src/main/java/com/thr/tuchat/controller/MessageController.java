package com.thr.tuchat.controller;

import com.thr.tuchat.pojo.Message;
import com.thr.tuchat.pojo.ResponseResult;
import com.thr.tuchat.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @GetMapping("/list")
    public ResponseResult<List<Message>> getListMessageByConversationId(@RequestParam String conversationId) {
        try {
            List<Message> messageList =  messageService.getAllMessageByConversationId(conversationId);
            return ResponseResult.success(messageList);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }
}
