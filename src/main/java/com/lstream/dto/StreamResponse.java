package com.lstream.dto;

import com.lstream.model.Stream;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class StreamResponse {

    private Long id;
    private String streamKey;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Long streamerId;
    private String streamerName;
    private String streamerDisplayName;
    private String streamerProfileImage;
    private Stream.StreamStatus status;
    private Stream.StreamPrivacy privacy;
    private String category;
    private Set<String> tags;
    private String hlsUrl;
    private String recordingUrl;
    private Long currentViewers;
    private Long peakViewers;
    private Long totalViews;
    private Long likeCount;
    private Boolean chatEnabled;
    private Boolean recordingEnabled;
    private Boolean lowLatencyMode;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private Set<QualityDto> availableQualities;

    public static StreamResponse fromEntity(Stream stream) {
        return StreamResponse.builder()
                .id(stream.getId())
                .streamKey(stream.getStreamKey())
                .title(stream.getTitle())
                .description(stream.getDescription())
                .thumbnailUrl(stream.getThumbnailUrl())
                .streamerId(stream.getStreamer().getId())
                .streamerName(stream.getStreamer().getUsername())
                .streamerDisplayName(stream.getStreamer().getDisplayName())
                .streamerProfileImage(stream.getStreamer().getProfileImageUrl())
                .status(stream.getStatus())
                .privacy(stream.getPrivacy())
                .category(stream.getCategory())
                .tags(stream.getTags())
                .hlsUrl(stream.getHlsUrl())
                .recordingUrl(stream.getRecordingUrl())
                .currentViewers(stream.getCurrentViewers())
                .peakViewers(stream.getPeakViewers())
                .totalViews(stream.getTotalViews())
                .likeCount(stream.getLikeCount())
                .chatEnabled(stream.getChatEnabled())
                .recordingEnabled(stream.getRecordingEnabled())
                .lowLatencyMode(stream.getLowLatencyMode())
                .startedAt(stream.getStartedAt())
                .endedAt(stream.getEndedAt())
                .createdAt(stream.getCreatedAt())
                .availableQualities(stream.getAvailableQualities().stream()
                        .map(q -> new QualityDto(q.getName(), q.getWidth(), q.getHeight(), q.getBitrate(), q.getHlsPlaylistUrl()))
                        .collect(Collectors.toSet()))
                .build();
    }

    @Data
    @Builder
    public static class QualityDto {
        private String name;
        private Integer width;
        private Integer height;
        private Integer bitrate;
        private String hlsPlaylistUrl;

        public QualityDto(String name, Integer width, Integer height, Integer bitrate, String hlsPlaylistUrl) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.bitrate = bitrate;
            this.hlsPlaylistUrl = hlsPlaylistUrl;
        }
    }
}
