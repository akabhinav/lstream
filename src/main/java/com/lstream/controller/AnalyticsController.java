package com.lstream.controller;

import com.lstream.model.StreamAnalytics;
import com.lstream.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/streams/{streamId}/statistics")
    public ResponseEntity<Map<String, Object>> getStreamStatistics(@PathVariable Long streamId) {
        Map<String, Object> stats = analyticsService.getStreamStatistics(streamId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/streams/{streamId}/history")
    public ResponseEntity<List<StreamAnalytics>> getStreamAnalyticsHistory(
            @PathVariable Long streamId,
            @RequestParam(required = false) String since) {
        LocalDateTime sinceTime = since != null ?
                LocalDateTime.parse(since) : LocalDateTime.now().minusHours(24);

        List<StreamAnalytics> history = analyticsService.getStreamAnalyticsHistory(streamId, sinceTime);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/streams/{streamId}/view")
    public ResponseEntity<?> recordView(@PathVariable Long streamId,
                                        @RequestParam(required = false) Long userId,
                                        @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
                                        @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        try {
            analyticsService.recordStreamView(streamId, userId, ipAddress, userAgent);
            return ResponseEntity.ok(Map.of("message", "View recorded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
