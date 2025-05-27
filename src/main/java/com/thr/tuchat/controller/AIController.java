package com.thr.tuchat.controller;


import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import com.thr.tuchat.dto.AIRequestDTO;
import com.thr.tuchat.service.AIService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private AIService aiService;

    @PostMapping(value = "/getAIResponse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getAIResponse (@RequestBody AIRequestDTO aiRequestDTO) {
        try {
            log.info("用户发起AI问答，参数:#{}", aiRequestDTO);
            return aiService.getAIResponseWithAncient(aiRequestDTO);
        } catch (Exception e) {
            return Flux.just("ERROR!!!!oops~");
        }
    }
}
