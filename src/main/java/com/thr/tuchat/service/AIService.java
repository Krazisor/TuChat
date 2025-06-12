package com.thr.tuchat.service;


import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.constant.AIMessageType;
import com.thr.tuchat.dto.AIRequestDTO;
import com.thr.tuchat.exception.ServiceDeniedException;
import com.thr.tuchat.pojo.Conversation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    public Flux<String> getAIResponse(String question, String model) {
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(model).temperature(0.5).build();
        OpenAiChatModel openAiChatModel = baseChatModel.mutate()
                .openAiApi(openAiApi)
                .defaultOptions(openAiChatOptions)
                .build();
        Prompt prompt = new Prompt(new UserMessage(question));
        ChatClient chatClient = ChatClient.builder(openAiChatModel).build();
        return chatClient.prompt(prompt).user(question).stream().content();
    }


    public Flux<String> getAIResponseWithAncient(AIRequestDTO aiRequestDTO) {
        try {
            // 将数据question存入数据库中
            com.thr.tuchat.pojo.Message message = new com.thr.tuchat.pojo.Message();
            message.setConversationId(aiRequestDTO.getConversationId());
            message.setRole(AIMessageType.USER.getRole());
            message.setContent(aiRequestDTO.getQuestion());
            messageService.addNewMessage(message);
            // 初始化模型
            OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(aiRequestDTO.getModel()).temperature(0.5).build();
            OpenAiChatModel openAiChatModel = baseChatModel.mutate()
                    .openAiApi(openAiApi)
                    .defaultOptions(openAiChatOptions)
                    .build();
            List<Message> messages = new ArrayList<>();
//        TODO:如下
//        messages.add(new SystemMessage(systemPrompt));
            String userId = StpUtil.getLoginIdAsString();
            List<Conversation> conversations = conversationService.getConversationByUserId_Safe(userId);
            // 严格的权限控制，只有自己的conversation历史可以被获取
            if (conversations.stream().anyMatch(s -> s.getConversationId().equals(aiRequestDTO.getConversationId()))) {
                List<com.thr.tuchat.pojo.Message> history = messageService.getAllMessageByConversationId(aiRequestDTO.getConversationId());
                history.forEach(item -> {
                    if (Objects.equals(item.getRole(), AIMessageType.USER.getRole())) {
                        messages.add(new UserMessage(item.getContent()));
                    } else if (Objects.equals(item.getRole(), AIMessageType.ASSISTANT.getRole())) {
                        messages.add(new AssistantMessage(item.getContent()));
                    }
                });
            }
            Prompt prompt = new Prompt(messages);
            ChatClient chatClient = ChatClient.builder(openAiChatModel).build();
            Flux<String> aiFlux = chatClient.prompt(prompt)
                    .user(aiRequestDTO.getQuestion())
                    .stream()
                    .content();

            // 下面处理：边推流/边收集，流结束时入库
            AtomicReference<StringBuilder> replyBuilder = new AtomicReference<>(new StringBuilder());

            return aiFlux
                    .concatMap(token -> {
                        List<String> chars = new ArrayList<>();
                        token.codePoints().forEach(cp -> {
                            if (cp == ' ') {
                                chars.add("[[SPACE]]");
                            } else if (cp == '\n' || cp == '\r') {
                                chars.add("[[LINEBREAKS]]");
                            } else {
                                chars.add(new String(Character.toChars(cp)));
                            }
                        });
                        return Flux.fromIterable(chars);
                    })
                    .doOnNext(charToken -> {
                        // 拼接入库内容，遇到[[SPACE]]还原成空格
                        if ("[[SPACE]]".equals(charToken)) {
                            replyBuilder.get().append(" ");
                        } else if ("[[LINEBREAKS]]".equals(charToken)) {
                            replyBuilder.get().append("\n");
                        } else {
                            replyBuilder.get().append(charToken);
                        }
                    })
                    .doOnComplete(() -> {
                        log.info("AI回复数据#{}", replyBuilder.get().toString());
                        // 入库AI消息
                        com.thr.tuchat.pojo.Message aiMessage = new com.thr.tuchat.pojo.Message();
                        aiMessage.setConversationId(aiRequestDTO.getConversationId());
                        aiMessage.setRole(AIMessageType.ASSISTANT.getRole());
                        aiMessage.setContent(replyBuilder.get().toString());
                        messageService.addNewMessage(aiMessage);
                    });
        } catch (Exception e) {
            com.thr.tuchat.pojo.Message aiMessage = new com.thr.tuchat.pojo.Message();
            aiMessage.setConversationId(aiRequestDTO.getConversationId());
            aiMessage.setRole(AIMessageType.ASSISTANT.getRole());
            aiMessage.setContent(e.getMessage());
            messageService.addNewMessage(aiMessage);
            throw new ServiceDeniedException(e.getMessage());
        }
    }
}
