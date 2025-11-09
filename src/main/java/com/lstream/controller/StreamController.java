package com.lstream.controller;

import com.lstream.dto.StreamCreateRequest;
import com.lstream.dto.StreamResponse;
import com.lstream.service.StreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @PostMapping
    public ResponseEntity<?> createStream(@Valid @RequestBody StreamCreateRequest request,
                                          Authentication authentication) {
        try {
            StreamResponse stream = streamService.createStream(request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(stream);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/live")
    public ResponseEntity<Page<StreamResponse>> getLiveStreams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StreamResponse> streams = streamService.getLiveStreams(PageRequest.of(page, size));
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStream(@PathVariable Long id) {
        try {
            StreamResponse stream = streamService.getStream(id);
            return ResponseEntity.ok(stream);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<StreamResponse>> getStreamsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StreamResponse> streams = streamService.getStreamsByCategory(category, PageRequest.of(page, size));
        return ResponseEntity.ok(streams);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startStream(@PathVariable Long id, Authentication authentication) {
        try {
            StreamResponse stream = streamService.getStream(id);
            // Verify ownership
            if (!stream.getStreamerName().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Not authorized to start this stream"));
            }

            streamService.startStream(stream.getStreamKey());
            return ResponseEntity.ok(Map.of("message", "Stream started successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<?> stopStream(@PathVariable Long id, Authentication authentication) {
        try {
            StreamResponse stream = streamService.getStream(id);
            // Verify ownership
            if (!stream.getStreamerName().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Not authorized to stop this stream"));
            }

            streamService.stopStream(stream.getStreamKey());
            return ResponseEntity.ok(Map.of("message", "Stream stopped successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/watch")
    public ResponseEntity<?> watchStream(@PathVariable Long id) {
        try {
            streamService.incrementViewers(id);
            return ResponseEntity.ok(Map.of("message", "Joined stream"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveStream(@PathVariable Long id) {
        try {
            streamService.decrementViewers(id);
            return ResponseEntity.ok(Map.of("message", "Left stream"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
