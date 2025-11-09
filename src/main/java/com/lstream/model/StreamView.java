package com.lstream.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stream_views", indexes = {
    @Index(name = "idx_view_stream", columnList = "stream_id"),
    @Index(name = "idx_view_user", columnList = "user_id"),
    @Index(name = "idx_view_started", columnList = "startedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private Stream stream;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // Nullable for anonymous viewers

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Builder.Default
    private Long watchDurationSeconds = 0L;

    @Column(length = 20)
    private String quality;  // Quality level watched most
}
