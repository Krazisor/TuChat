package com.thr.tuchat.bean;

import com.thr.tuchat.config.ModelConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

@Configuration
public class LangChain {

    @Resource
    private ModelConfig openAIConfig;

    @Bean
    public ChatModel chatModel() {
        String customAPIKey = openAIConfig.getApiKey();
        String customBaseURL = openAIConfig.getBaseUrl();
        return OpenAiChatModel.builder()
                .apiKey(customAPIKey)
                .modelName(GPT_4_O_MINI)
                .baseUrl(customBaseURL)
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        String customAPIKey = openAIConfig.getApiKey();
        String customBaseURL = openAIConfig.getBaseUrl();
        return OpenAiStreamingChatModel.builder()
                .apiKey(customAPIKey)
                .modelName(GPT_4_O_MINI)
                .baseUrl(customBaseURL)
                .build();
    }
}
