package com.lstream.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "streams", indexes = {
    @Index(name = "idx_stream_key", columnList = "streamKey"),
    @Index(name = "idx_stream_status", columnList = "status"),
    @Index(name = "idx_stream_streamer", columnList = "streamer_id"),
    @Index(name = "idx_stream_started_at", columnList = "startedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String streamKey;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 500)
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "streamer_id", nullable = false)
    private User streamer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StreamStatus status = StreamStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StreamPrivacy privacy = StreamPrivacy.PUBLIC;

    @Column(length = 100)
    private String category;

    @ElementCollection
    @CollectionTable(name = "stream_tags", joinColumns = @JoinColumn(name = "stream_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Column(length = 500)
    private String hlsUrl;

    @Column(length = 500)
    private String recordingUrl;

    @Builder.Default
    private Long currentViewers = 0L;

    @Builder.Default
    private Long peakViewers = 0L;

    @Builder.Default
    private Long totalViews = 0L;

    @Builder.Default
    private Long likeCount = 0L;

    @Builder.Default
    private Long chatMessageCount = 0L;

    @Builder.Default
    private Boolean chatEnabled = true;

    @Builder.Default
    private Boolean recordingEnabled = true;

    @Builder.Default
    private Boolean lowLatencyMode = false;

    @Column(length = 20)
    private String inputResolution;

    @Column(length = 10)
    private String inputFrameRate;

    @Builder.Default
    private Integer inputBitrate = 0;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "stream", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<StreamQuality> availableQualities = new HashSet<>();

    public enum StreamStatus {
        CREATED,        // Stream created but not started
        STARTING,       // Transcoding and setup in progress
        LIVE,           // Currently streaming
        PAUSED,         // Temporarily paused
        ENDING,         // Shutting down
        ENDED,          // Stream finished
        ERROR           // Error occurred
    }

    public enum StreamPrivacy {
        PUBLIC,         // Anyone can watch
        UNLISTED,       // Only with link
        PRIVATE         // Only invited users
    }

    public void incrementViewers() {
        this.currentViewers++;
        if (this.currentViewers > this.peakViewers) {
            this.peakViewers = this.currentViewers;
        }
    }

    public void decrementViewers() {
        if (this.currentViewers > 0) {
            this.currentViewers--;
        }
    }
}
