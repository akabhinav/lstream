package com.lstream.service;

import com.lstream.dto.ChatMessageDto;
import com.lstream.model.ChatMessage;
import com.lstream.model.Stream;
import com.lstream.model.User;
import com.lstream.repository.ChatMessageRepository;
import com.lstream.repository.StreamRepository;
import com.lstream.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StreamRepository streamRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "chat:rate:";
    private static final int MAX_MESSAGES_PER_MINUTE = 30;

    @Transactional
    public ChatMessageDto sendMessage(Long streamId, String message, String username) {
        // Check rate limit
        if (!checkRateLimit(username)) {
            throw new RuntimeException("Rate limit exceeded. Please slow down.");
        }

        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        if (!stream.getChatEnabled()) {
            throw new RuntimeException("Chat is disabled for this stream");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .stream(stream)
                .user(user)
                .message(message)
                .type(ChatMessage.MessageType.REGULAR)
                .deleted(false)
                .build();

        chatMessage = chatMessageRepository.save(chatMessage);

        // Increment chat message count
        stream.setChatMessageCount(stream.getChatMessageCount() + 1);
        streamRepository.save(stream);

        ChatMessageDto dto = ChatMessageDto.fromEntity(chatMessage);

        // Broadcast to WebSocket subscribers
        messagingTemplate.convertAndSend("/topic/stream/" + streamId + "/chat", dto);

        // Publish to Kafka for analytics
        kafkaTemplate.send("chat-events", dto);

        log.debug("Sent chat message for stream {} from user {}", streamId, username);
        return dto;
    }

    public List<ChatMessageDto> getRecentMessages(Long streamId, int limit) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        return chatMessageRepository.findRecentMessages(stream, PageRequest.of(0, limit))
                .stream()
                .map(ChatMessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMessage(Long messageId, String username) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only allow moderators, stream owner, or message author to delete
        if (!message.getUser().getId().equals(user.getId()) &&
            !message.getStream().getStreamer().getId().equals(user.getId()) &&
            user.getRole() != User.UserRole.MODERATOR &&
            user.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("Not authorized to delete this message");
        }

        message.setDeleted(true);
        chatMessageRepository.save(message);

        // Notify via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/stream/" + message.getStream().getId() + "/chat/deleted",
                messageId
        );
    }

    private boolean checkRateLimit(String username) {
        String key = RATE_LIMIT_KEY_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        return count != null && count <= MAX_MESSAGES_PER_MINUTE;
    }

    public Long getChatMessagesPerMinute(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        return chatMessageRepository.countRecentMessages(stream, oneMinuteAgo);
    }
}
