package com.thr.tuchat;

import com.thr.tuchat.service.AIService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
class TuchatApplicationTests {

    @Resource
    private AIService aiService;

    @Test
    void contextLoads() {
        System.out.println(System.getenv("TEST_KEY"));
    }

    @Test
    void TestAIStreamingResponse () {
        Flux<String> response = aiService.getAIResponse("给我一段python判断素数的代码", "gpt-4.1");
        response.doOnNext(System.out::print).blockLast(); // 简明调试用
    }

    @Test
    void testCPUCores () {
        System.out.println(Runtime.getRuntime().availableProcessors());
    }

}
