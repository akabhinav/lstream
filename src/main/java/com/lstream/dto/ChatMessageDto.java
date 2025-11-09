package com.lstream.dto;

import com.lstream.model.ChatMessage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDto {

    private Long id;
    private Long streamId;
    private Long userId;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private String message;
    private ChatMessage.MessageType type;
    private LocalDateTime timestamp;

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .streamId(chatMessage.getStream().getId())
                .userId(chatMessage.getUser().getId())
                .username(chatMessage.getUser().getUsername())
                .displayName(chatMessage.getUser().getDisplayName())
                .profileImageUrl(chatMessage.getUser().getProfileImageUrl())
                .message(chatMessage.getMessage())
                .type(chatMessage.getType())
                .timestamp(chatMessage.getTimestamp())
                .build();
    }
}
