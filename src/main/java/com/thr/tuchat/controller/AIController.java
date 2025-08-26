package com.thr.tuchat.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import com.thr.tuchat.model.dto.AIRequest;
import com.thr.tuchat.ai.AIService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private AIService aiService;

    @SaCheckLogin
    @PostMapping(value = "/getAIResponse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getAIResponse(@RequestBody AIRequest aiRequestDTO) {
        try {
            log.info("用户发起AI问答，参数:#{}", aiRequestDTO);
            return aiService.getAIResponseWithRAGAndAncient(aiRequestDTO);
        } catch (Exception e) {
            return Flux.just("ERROR!!!!oops~");
        }
    }

    @SaCheckLogin
    @PostMapping(value = "/getAIResponseWithFile", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getAIResponseWithFile(@RequestParam("files") List<MultipartFile> files,
                                              @RequestParam String conversationId,
                                              @RequestParam String question,
                                              @RequestParam String model) {
        try {
            log.info("用户发起了带文件的AI问答，文件:{}, 参数:{}{}{}", files, question, model, conversationId);
            return Flux.just("ERROR!!!!oopsFiles~");
        } catch (Exception e) {
            return Flux.just("ERROR!!!!oopsFiles~");
        }
    }
}
