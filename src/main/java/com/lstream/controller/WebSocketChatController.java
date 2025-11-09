package com.lstream.controller;

import com.lstream.dto.ChatMessageDto;
import com.lstream.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final ChatService chatService;

    @MessageMapping("/stream/{streamId}/chat")
    public void sendChatMessage(@DestinationVariable Long streamId,
                                 @Payload ChatMessageRequest message,
                                 Principal principal) {
        try {
            String username = principal != null ? principal.getName() : "Anonymous";
            chatService.sendMessage(streamId, message.getMessage(), username);
        } catch (Exception e) {
            log.error("Error sending chat message", e);
        }
    }

    @MessageMapping("/stream/{streamId}/join")
    public void joinStream(@DestinationVariable Long streamId,
                           SimpMessageHeaderAccessor headerAccessor) {
        log.info("User joined stream {}", streamId);
    }

    @MessageMapping("/stream/{streamId}/leave")
    public void leaveStream(@DestinationVariable Long streamId,
                            SimpMessageHeaderAccessor headerAccessor) {
        log.info("User left stream {}", streamId);
    }

    @Data
    public static class ChatMessageRequest {
        private String message;
    }
}
