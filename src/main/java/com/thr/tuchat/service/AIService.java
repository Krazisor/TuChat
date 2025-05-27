package com.thr.tuchat.service;


import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.constant.AIMessageType;
import com.thr.tuchat.dto.AIRequestDTO;
import com.thr.tuchat.pojo.Conversation;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AIService {

    @Resource
    private OpenAiChatModel baseChatModel;

    @Resource
    private OpenAiApi openAiApi;

    @Resource
    private MessageService messageService;

    @Resource
    private ConversationService conversationService;

    public Flux<String> getAIResponse (String question, String model) {
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(model).temperature(0.5).build();
        OpenAiChatModel openAiChatModel = baseChatModel.mutate()
                .openAiApi(openAiApi)
                .defaultOptions(openAiChatOptions)
                .build();
        Prompt prompt = new Prompt(new UserMessage(question));
        ChatClient chatClient = ChatClient.builder(openAiChatModel).build();
        return chatClient.prompt(prompt).user(question).stream().content();
    }


    public Flux<String> getAIResponseWithAncient (AIRequestDTO aiRequestDTO) {
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(aiRequestDTO.getModel()).temperature(0.5).build();
        OpenAiChatModel openAiChatModel = baseChatModel.mutate()
                .openAiApi(openAiApi)
                .defaultOptions(openAiChatOptions)
                .build();
        List<Message> messages = new ArrayList<>();
//        TODO:如下
//        messages.add(new SystemMessage(systemPrompt));
        String userId = StpUtil.getLoginIdAsString();
/*        AtomicReference<String> userId = new AtomicReference<>();
        SaTokenContextMockUtil.setMockContext(()->{
            userId.set(StpUtil.getLoginIdAsString());
        });*/
        List<Conversation> conversations = conversationService.getConversationByUserId_Safe(userId);
        // 严格的权限控制，只有自己的conversation历史可以被获取
        if (conversations.stream().anyMatch(s -> s.getConversationId().equals(aiRequestDTO.getConversationId()))) {
            List<com.thr.tuchat.pojo.Message> history = messageService.getAllMessageByConversationId(aiRequestDTO.getConversationId());
            history.forEach(item -> {
                if (Objects.equals(item.getRole(), AIMessageType.USER.getRole())) {
                    messages.add(new UserMessage(item.getContent()));
                }
                else if (Objects.equals(item.getRole(), AIMessageType.ASSISTANT.getRole())) {
                    messages.add(new AssistantMessage(item.getContent()));
                }
            });
        }
        Prompt prompt = new Prompt(messages);
        ChatClient chatClient = ChatClient.builder(openAiChatModel).build();
        return chatClient.prompt(prompt).user(aiRequestDTO.getQuestion()).stream().content();
    }
}
