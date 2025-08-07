package com.thr.tuchat.ai;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.ai.sub.AIChatHandler;
import com.thr.tuchat.ai.sub.MyPromptTemplate;
import com.thr.tuchat.dto.AIRequest;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.pojo.Conversation;
import com.thr.tuchat.service.ConversationService;
import com.thr.tuchat.service.MessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.VectorStore;
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

    @Resource
    private VectorStore vectorStore;

    @Resource
    private AIChatHandler aiChatHandler;

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

    public Flux<String> getAIResponseWithRAGAndAncient(AIRequest aiRequestDTO) {
        try {
            // 将数据question存入数据库中
            aiChatHandler.initAndInsertUserMessageWithConversationId(
                    aiRequestDTO.conversationId(),
                    aiRequestDTO.question()
            );

            // 初始化模型
            OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                    .model(aiRequestDTO.model())
                    .temperature(0.5)
                    .build();
            OpenAiChatModel openAiChatModel = baseChatModel.mutate()
                    .openAiApi(openAiApi)
                    .defaultOptions(openAiChatOptions)
                    .build();
            ChatClient chatClient = ChatClient.builder(openAiChatModel).build();

            // 提示词模板构建
            PromptTemplate customPromptTemplate = PromptTemplate.builder()
                    .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                    .template(MyPromptTemplate.promptTemplate)
                    .build();

            // RAG综合配置
            RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                    // 问题重写
                    .queryTransformers(RewriteQueryTransformer.builder()
                            .chatClientBuilder(ChatClient.builder(openAiChatModel))
                            .build())
                    // 获取RAG相关知识
                    .documentRetriever(VectorStoreDocumentRetriever.builder()
                            .vectorStore(vectorStore)
                            .similarityThreshold(0.50)
                            .topK(5)
                            .build())
                    // 绑定提示词模板
                    .queryAugmenter(ContextualQueryAugmenter.builder()
                            .promptTemplate(customPromptTemplate)
                            .allowEmptyContext(true)
                            .build())
                    .build();

            // 获取历史对话信息
            List<Message> messages = new ArrayList<>();
            String userId = StpUtil.getLoginIdAsString();
            // 严格的权限控制，只有自己的conversation历史可以被获取
            Conversation conversation = conversationService.getConversationById(aiRequestDTO.conversationId());
            ThrowUtils.throwIf(!Objects.equals(conversation.getUserId(), userId), ResultCode.NO_AUTH_ERROR,
                    "用户试图获取不属于自己的conversation");
            aiChatHandler.getAiAncientHistory(messages, aiRequestDTO.conversationId());

            // 设定prompt
            Prompt prompt = new Prompt(messages);

            Flux<String> aiFlux = chatClient.prompt(prompt)
                    .advisors(retrievalAugmentationAdvisor)
                    .user(aiRequestDTO.question())
                    .stream()
                    .content();

            // 下面处理：边推流/边收集，流结束时入库
            AtomicReference<StringBuilder> replyBuilder = new AtomicReference<>(new StringBuilder());

            return aiFlux
                    // 1. 将 token 拆分为字符流，并处理特殊字符
                    .concatMap(aiChatHandler::transformTokenToCharacterStream)
                    // 2. 将字符流中的 token 重新拼接为完整的回复
                    .doOnNext(charToken -> aiChatHandler.appendTokenToBuilder(replyBuilder.get(), charToken))
                    .doOnComplete(() -> {
                        // 入库AI消息
                        aiChatHandler.initAndInsertAssistantMessageWithConversationId(
                                aiRequestDTO.conversationId(),
                                replyBuilder.get().toString()
                        );
                        log.info("AI回复数据:{}", replyBuilder.get().toString());
                    });
        } catch (Exception e) {
            aiChatHandler.initAndInsertAssistantMessageWithConversationId(
                    aiRequestDTO.conversationId(),
                    e.getMessage()
            );
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
