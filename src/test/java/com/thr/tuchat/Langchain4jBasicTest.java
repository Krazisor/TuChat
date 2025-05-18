package com.thr.tuchat;

import com.thr.tuchat.ai.tool.CalculatorTool;
import com.thr.tuchat.bean.LangChain;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import dev.langchain4j.service.tool.ToolExecution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

@SpringBootTest
public class Langchain4jBasicTest {

    @Autowired
    private LangChain langChain;

    interface Assistant {
        Flux<String> chat(String userMessage);
    }

    @Test
    void chatStreamingTest () {
        Assistant assistant = AiServices.create(Assistant.class, langChain.streamingChatModel());
        Flux<String> answer = assistant.chat("tell me the something about java");
        answer.doOnNext(System.out::print)
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> System.out.println("\n[All done!]"))
                .blockLast();
    }


    interface Assistant2 extends ChatMemoryAccess {
        String chat(@MemoryId int memoryId, @UserMessage String message);
    }

    @Test
    void chatHistoryTest () {
        Assistant2 assistant = AiServices.builder(Assistant2.class)
                .chatModel(langChain.chatModel())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String answerToKlaus = assistant.chat(1, "Hello, my name is Klaus");
        String answerToFrancine = assistant.chat(2, "Hello, my name is Francine");

        List<ChatMessage> messagesWithKlaus = assistant.getChatMemory(1).messages();
        boolean chatMemoryWithFrancineEvicted = assistant.evictChatMemory(2);

        System.out.println("###");
        System.out.println(answerToKlaus);
        System.out.println(answerToFrancine);
        System.out.println(messagesWithKlaus);
        System.out.println(chatMemoryWithFrancineEvicted);
    }

    @Test
    void chatToolTest () {
        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(langChain.streamingChatModel())
                .tools(new CalculatorTool())
                .build();
        Flux<String> answer = assistant.chat("475695037565的平方根是多少");
        answer.doOnNext(System.out::print)
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> System.out.println("\n[All done!]"))
                .blockLast();
    }

    interface Assistant3 {
        Result<String> chat(String userMessage);
    }

    @Test
    void getExecutedAnswer () {
        Assistant3 assistant = AiServices.builder(Assistant3.class)
                .chatModel(langChain.chatModel())
                .tools(new CalculatorTool())
                // 制定幻觉策略
                .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()))
                .build();
        Result<String> result = assistant.chat("475695037565的平方根是多少");

        String answer = result.content();
        List<ToolExecution> toolExecutions = result.toolExecutions();
        System.out.println(answer);
        System.out.println(toolExecutions);
    }

}
