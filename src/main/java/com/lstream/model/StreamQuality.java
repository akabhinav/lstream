package com.lstream.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stream_qualities", indexes = {
    @Index(name = "idx_quality_stream", columnList = "stream_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private Stream stream;

    @Column(nullable = false, length = 20)
    private String name;  // 1080p, 720p, 480p, 360p

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false)
    private Integer bitrate;  // in kbps

    @Column(nullable = false, length = 500)
    private String hlsPlaylistUrl;

    @Builder.Default
    private Boolean available = false;
}
