package com.lstream.service;

import com.lstream.config.StreamingConfig;
import com.lstream.dto.StreamCreateRequest;
import com.lstream.dto.StreamResponse;
import com.lstream.event.StreamEvent;
import com.lstream.model.Stream;
import com.lstream.model.User;
import com.lstream.repository.StreamRepository;
import com.lstream.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamService {

    private final StreamRepository streamRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StreamingConfig streamingConfig;
    private final TranscodingService transcodingService;

    private static final String STREAM_KEY_PREFIX = "stream:";
    private static final String LIVE_STREAMS_KEY = "streams:live";

    @Transactional
    public StreamResponse createStream(StreamCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String streamKey = generateStreamKey();

        Stream stream = Stream.builder()
                .streamKey(streamKey)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .streamer(user)
                .privacy(request.getPrivacy())
                .chatEnabled(request.getChatEnabled())
                .recordingEnabled(request.getRecordingEnabled())
                .lowLatencyMode(request.getLowLatencyMode())
                .status(Stream.StreamStatus.CREATED)
                .build();

        stream = streamRepository.save(stream);

        // Cache stream key mapping
        redisTemplate.opsForValue().set(STREAM_KEY_PREFIX + streamKey, stream.getId(), 24, TimeUnit.HOURS);

        log.info("Created stream {} for user {}", stream.getId(), username);
        return StreamResponse.fromEntity(stream);
    }

    @Transactional
    public void startStream(String streamKey) {
        Stream stream = streamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        if (stream.getStatus() == Stream.StreamStatus.LIVE) {
            log.warn("Stream {} is already live", stream.getId());
            return;
        }

        stream.setStatus(Stream.StreamStatus.STARTING);
        stream.setStartedAt(LocalDateTime.now());
        streamRepository.save(stream);

        // Start transcoding for multiple qualities
        try {
            transcodingService.startTranscoding(stream);

            stream.setStatus(Stream.StreamStatus.LIVE);
            streamRepository.save(stream);

            // Add to live streams set in Redis
            redisTemplate.opsForSet().add(LIVE_STREAMS_KEY, stream.getId());

            // Publish stream started event
            publishStreamEvent(stream, StreamEvent.EventType.STREAM_STARTED);

            log.info("Stream {} started successfully", stream.getId());
        } catch (Exception e) {
            log.error("Failed to start stream {}", stream.getId(), e);
            stream.setStatus(Stream.StreamStatus.ERROR);
            streamRepository.save(stream);
            throw new RuntimeException("Failed to start stream", e);
        }
    }

    @Transactional
    public void stopStream(String streamKey) {
        Stream stream = streamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        stream.setStatus(Stream.StreamStatus.ENDING);
        streamRepository.save(stream);

        try {
            // Stop transcoding
            transcodingService.stopTranscoding(stream);

            stream.setStatus(Stream.StreamStatus.ENDED);
            stream.setEndedAt(LocalDateTime.now());
            streamRepository.save(stream);

            // Remove from live streams
            redisTemplate.opsForSet().remove(LIVE_STREAMS_KEY, stream.getId());

            // Publish stream ended event
            publishStreamEvent(stream, StreamEvent.EventType.STREAM_ENDED);

            log.info("Stream {} stopped successfully", stream.getId());
        } catch (Exception e) {
            log.error("Failed to stop stream {}", stream.getId(), e);
            stream.setStatus(Stream.StreamStatus.ERROR);
            streamRepository.save(stream);
        }
    }

    public StreamResponse getStream(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));
        return StreamResponse.fromEntity(stream);
    }

    public Stream getStreamByKey(String streamKey) {
        // Try to get from cache first
        Long streamId = (Long) redisTemplate.opsForValue().get(STREAM_KEY_PREFIX + streamKey);
        if (streamId != null) {
            return streamRepository.findById(streamId)
                    .orElseThrow(() -> new RuntimeException("Stream not found"));
        }

        // Fall back to database
        return streamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new RuntimeException("Stream not found"));
    }

    public Page<StreamResponse> getLiveStreams(Pageable pageable) {
        return streamRepository.findLiveStreams(Stream.StreamStatus.LIVE, pageable)
                .map(StreamResponse::fromEntity);
    }

    public Page<StreamResponse> getStreamsByCategory(String category, Pageable pageable) {
        return streamRepository.findLiveStreamsByCategory(category, pageable)
                .map(StreamResponse::fromEntity);
    }

    @Transactional
    public void incrementViewers(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));
        stream.incrementViewers();
        streamRepository.save(stream);

        // Update Redis cache
        redisTemplate.opsForValue().set("stream:viewers:" + streamId, stream.getCurrentViewers());
    }

    @Transactional
    public void decrementViewers(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));
        stream.decrementViewers();
        streamRepository.save(stream);

        // Update Redis cache
        redisTemplate.opsForValue().set("stream:viewers:" + streamId, stream.getCurrentViewers());
    }

    private String generateStreamKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void publishStreamEvent(Stream stream, StreamEvent.EventType eventType) {
        StreamEvent event = StreamEvent.builder()
                .streamId(stream.getId())
                .streamKey(stream.getStreamKey())
                .streamerId(stream.getStreamer().getId())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("stream-events", event);
    }
}
