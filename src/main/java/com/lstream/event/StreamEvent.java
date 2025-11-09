package com.lstream.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StreamEvent {

    private Long streamId;
    private String streamKey;
    private Long streamerId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private String metadata;

    public enum EventType {
        STREAM_CREATED,
        STREAM_STARTED,
        STREAM_ENDED,
        STREAM_ERROR,
        VIEWER_JOINED,
        VIEWER_LEFT,
        QUALITY_CHANGED
    }
}
