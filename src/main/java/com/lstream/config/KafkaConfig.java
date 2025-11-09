package com.lstream.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String STREAM_EVENTS_TOPIC = "stream-events";
    public static final String VIEWER_EVENTS_TOPIC = "viewer-events";
    public static final String CHAT_EVENTS_TOPIC = "chat-events";
    public static final String ANALYTICS_TOPIC = "analytics-events";

    @Bean
    public NewTopic streamEventsTopic() {
        return TopicBuilder.name(STREAM_EVENTS_TOPIC)
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic viewerEventsTopic() {
        return TopicBuilder.name(VIEWER_EVENTS_TOPIC)
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatEventsTopic() {
        return TopicBuilder.name(CHAT_EVENTS_TOPIC)
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic analyticsTopic() {
        return TopicBuilder.name(ANALYTICS_TOPIC)
                .partitions(5)
                .replicas(1)
                .build();
    }
}
