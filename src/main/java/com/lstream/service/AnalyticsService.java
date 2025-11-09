package com.lstream.service;

import com.lstream.model.Stream;
import com.lstream.model.StreamAnalytics;
import com.lstream.model.StreamView;
import com.lstream.model.User;
import com.lstream.repository.StreamAnalyticsRepository;
import com.lstream.repository.StreamRepository;
import com.lstream.repository.StreamViewRepository;
import com.lstream.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final StreamAnalyticsRepository analyticsRepository;
    private final StreamViewRepository viewRepository;
    private final StreamRepository streamRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ChatService chatService;

    @Transactional
    public StreamView recordStreamView(Long streamId, Long userId, String ipAddress, String userAgent) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        StreamView streamView = StreamView.builder()
                .stream(stream)
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        streamView = viewRepository.save(streamView);

        // Increment total views
        stream.setTotalViews(stream.getTotalViews() + 1);
        streamRepository.save(stream);

        log.debug("Recorded view for stream {} from {}", streamId, ipAddress);
        return streamView;
    }

    @Transactional
    public void updateStreamViewDuration(Long viewId, Long durationSeconds) {
        StreamView view = viewRepository.findById(viewId)
                .orElseThrow(() -> new RuntimeException("Stream view not found"));

        view.setWatchDurationSeconds(durationSeconds);
        view.setEndedAt(LocalDateTime.now());
        viewRepository.save(view);
    }

    @Transactional
    public void recordStreamAnalytics(Stream stream) {
        Long chatMessagesPerMinute = chatService.getChatMessagesPerMinute(stream.getId());

        StreamAnalytics analytics = StreamAnalytics.builder()
                .stream(stream)
                .currentViewers(stream.getCurrentViewers())
                .chatMessagesPerMinute(chatMessagesPerMinute)
                .averageWatchTime(viewRepository.getAverageWatchTime(stream))
                .build();

        analyticsRepository.save(analytics);

        // Publish to Kafka for real-time dashboard
        kafkaTemplate.send("analytics-events", analytics);

        log.debug("Recorded analytics for stream {}", stream.getId());
    }

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void collectAnalyticsForLiveStreams() {
        List<Stream> liveStreams = streamRepository.findByStatus(Stream.StreamStatus.LIVE);

        for (Stream stream : liveStreams) {
            try {
                recordStreamAnalytics(stream);
            } catch (Exception e) {
                log.error("Failed to record analytics for stream {}", stream.getId(), e);
            }
        }

        log.debug("Collected analytics for {} live streams", liveStreams.size());
    }

    public Map<String, Object> getStreamStatistics(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        Long uniqueViewers = viewRepository.countUniqueViewers(stream);
        Double averageWatchTime = viewRepository.getAverageWatchTime(stream);
        Long peakViewers = analyticsRepository.findPeakViewers(stream);
        Double averageViewers = analyticsRepository.findAverageViewers(stream);

        return Map.of(
                "streamId", streamId,
                "currentViewers", stream.getCurrentViewers(),
                "peakViewers", peakViewers != null ? peakViewers : 0,
                "totalViews", stream.getTotalViews(),
                "uniqueViewers", uniqueViewers != null ? uniqueViewers : 0,
                "averageViewers", averageViewers != null ? averageViewers : 0.0,
                "averageWatchTime", averageWatchTime != null ? averageWatchTime : 0.0,
                "chatMessageCount", stream.getChatMessageCount(),
                "likeCount", stream.getLikeCount()
        );
    }

    public List<StreamAnalytics> getStreamAnalyticsHistory(Long streamId, LocalDateTime since) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        return analyticsRepository.findByStreamSince(stream, since);
    }
}
