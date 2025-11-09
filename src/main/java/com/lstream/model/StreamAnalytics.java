package com.lstream.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stream_analytics", indexes = {
    @Index(name = "idx_analytics_stream", columnList = "stream_id"),
    @Index(name = "idx_analytics_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private Stream stream;

    @Column(nullable = false)
    private Long currentViewers;

    @Column(nullable = false)
    private Long chatMessagesPerMinute;

    @Builder.Default
    private Double averageWatchTime = 0.0;

    @Builder.Default
    private Integer inputBitrate = 0;

    @Builder.Default
    private Double cpuUsage = 0.0;

    @Builder.Default
    private Double memoryUsage = 0.0;

    @Builder.Default
    private Long networkBytesIn = 0L;

    @Builder.Default
    private Long networkBytesOut = 0L;

    @Builder.Default
    private Integer droppedFrames = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
