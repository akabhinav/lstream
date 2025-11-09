package com.lstream.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.streaming")
@Data
public class StreamingConfig {

    private Integer rtmpPort;
    private Integer hlsSegmentDuration;
    private Integer hlsPlaylistLength;
    private String streamBasePath;
    private String recordingsPath;
    private Integer maxBitrate;
    private List<QualityConfig> qualities;

    @Data
    public static class QualityConfig {
        private String name;
        private Integer width;
        private Integer height;
        private Integer bitrate;
    }
}
