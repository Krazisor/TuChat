package com.thr.tuchat.config;


import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Value("${spring.ai.openai.base-url}")
    private String BASE_URL;

    @Value("${spring.ai.openai.api-key}")
    private String API_KEY;

    // 构造基本API
    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(this.API_KEY)
                .baseUrl(this.BASE_URL)
                .build();
    }

}
