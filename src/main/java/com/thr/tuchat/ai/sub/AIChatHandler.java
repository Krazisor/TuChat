package com.thr.tuchat.ai.sub;


import com.thr.tuchat.constant.AIMessageType;
import com.thr.tuchat.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

@Component
public class AIChatHandler {

    @Resource
    private MessageService messageService;

    public Flux<String> transformTokenToCharacterStream(String token) {
        if (token == null || token.isEmpty()) {
            return Flux.empty();
        }

        // 使用 Stream API 可以让代码更紧凑
        return Flux.fromStream(token.codePoints().mapToObj(cp -> {
            if (cp == ' ') {
                return "[[SPACE]]";
            } else if (cp == '\n' || cp == '\r') {
                return "[[LINEBREAKS]]";
            } else {
                return new String(Character.toChars(cp));
            }
        }));
    }

    /**
     * 将单个字符 token 追加到 StringBuilder 中。
     * 如果 token 是特殊占位符，则将其转换回原始字符（空格或换行符）再追加。
     *
     * @param builder   用于构建最终回复的 StringBuilder
     * @param charToken 来自流中的单个字符或占位符
     */
    public void appendTokenToBuilder(StringBuilder builder, String charToken) {
        switch (charToken) {
            case "[[SPACE]]":
                builder.append(" ");
                break;
            case "[[LINEBREAKS]]":
                // 统一使用 \n 作为换行符
                builder.append("\n");
                break;
            default:
                builder.append(charToken);
                break;
        }
    }

    public void getAiAncientHistory(List<Message> messageList, String conversationId) {
        List<com.thr.tuchat.pojo.Message> history = messageService.getAllMessageByConversationId(conversationId);
        history.forEach(item -> {
            if (Objects.equals(item.getRole(), AIMessageType.USER.getRole())) {
                messageList.add(new UserMessage(item.getContent()));
            } else if (Objects.equals(item.getRole(), AIMessageType.ASSISTANT.getRole())) {
                messageList.add(new AssistantMessage(item.getContent()));
            }
        });
    }

    public void initAndInsertUserMessageWithConversationId(String conversationId, String question) {
        com.thr.tuchat.pojo.Message message = new com.thr.tuchat.pojo.Message();
        message.setConversationId(conversationId);
        message.setRole(AIMessageType.USER.getRole());
        message.setContent(question);
        messageService.addNewMessage(message);
    }

    public void initAndInsertAssistantMessageWithConversationId(String conversationId, String question) {
        com.thr.tuchat.pojo.Message aiMessage = new com.thr.tuchat.pojo.Message();
        aiMessage.setConversationId(conversationId);
        aiMessage.setRole(AIMessageType.ASSISTANT.getRole());
        aiMessage.setContent(question);
        messageService.addNewMessage(aiMessage);
    }
}
