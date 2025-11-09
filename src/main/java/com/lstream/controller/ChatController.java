package com.lstream.controller;

import com.lstream.dto.ChatMessageDto;
import com.lstream.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/streams/{streamId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<?> sendMessage(@PathVariable Long streamId,
                                         @RequestBody Map<String, String> request,
                                         Authentication authentication) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
            }

            ChatMessageDto chatMessage = chatService.sendMessage(streamId, message, authentication.getName());
            return ResponseEntity.ok(chatMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ChatMessageDto>> getMessages(@PathVariable Long streamId,
                                                             @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessageDto> messages = chatService.getRecentMessages(streamId, limit);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long streamId,
                                           @PathVariable Long messageId,
                                           Authentication authentication) {
        try {
            chatService.deleteMessage(messageId, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Message deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
