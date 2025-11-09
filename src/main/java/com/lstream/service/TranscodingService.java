package com.lstream.service;

import com.lstream.config.StreamingConfig;
import com.lstream.model.Stream;
import com.lstream.model.StreamQuality;
import com.lstream.repository.StreamQualityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscodingService {

    private final StreamingConfig streamingConfig;
    private final StreamQualityRepository streamQualityRepository;

    // Map to store running transcoding processes
    private final Map<Long, List<Process>> transcodingProcesses = new ConcurrentHashMap<>();

    public void startTranscoding(Stream stream) throws IOException {
        String streamKey = stream.getStreamKey();
        String streamBasePath = streamingConfig.getStreamBasePath() + "/" + streamKey;

        // Create directories
        Path streamDir = Paths.get(streamBasePath);
        Files.createDirectories(streamDir);

        // RTMP input from stream
        String rtmpInput = "rtmp://localhost:" + streamingConfig.getRtmpPort() + "/live/" + streamKey;

        List<Process> processes = new ArrayList<>();

        // Create transcoding for each quality
        for (StreamingConfig.QualityConfig quality : streamingConfig.getQualities()) {
            try {
                String qualityPath = streamBasePath + "/" + quality.getName();
                Files.createDirectories(Paths.get(qualityPath));

                String hlsPlaylist = qualityPath + "/playlist.m3u8";

                // Build FFmpeg command
                FFmpegBuilder builder = new FFmpegBuilder()
                        .setInput(rtmpInput)
                        .addOutput(hlsPlaylist)
                        .setVideoCodec("libx264")
                        .setVideoResolution(quality.getWidth(), quality.getHeight())
                        .setVideoBitRate(quality.getBitrate() * 1000L)
                        .setVideoFrameRate(30)
                        .setAudioCodec("aac")
                        .setAudioBitRate(128_000)
                        .setAudioChannels(2)
                        .setAudioSampleRate(44_100)
                        .setFormat("hls")
                        .addExtraArgs("-hls_time", streamingConfig.getHlsSegmentDuration().toString())
                        .addExtraArgs("-hls_list_size", streamingConfig.getHlsPlaylistLength().toString())
                        .addExtraArgs("-hls_flags", "delete_segments+append_list")
                        .addExtraArgs("-preset", "veryfast")
                        .addExtraArgs("-g", "60")  // GOP size
                        .addExtraArgs("-sc_threshold", "0")
                        .done();

                // Start FFmpeg process (in production, use FFmpegExecutor)
                // For now, we'll use ProcessBuilder to run ffmpeg command
                List<String> command = buildFFmpegCommand(rtmpInput, hlsPlaylist, quality);
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                processes.add(process);

                // Create StreamQuality record
                StreamQuality streamQuality = StreamQuality.builder()
                        .stream(stream)
                        .name(quality.getName())
                        .width(quality.getWidth())
                        .height(quality.getHeight())
                        .bitrate(quality.getBitrate())
                        .hlsPlaylistUrl("/hls/" + streamKey + "/" + quality.getName() + "/playlist.m3u8")
                        .available(true)
                        .build();

                streamQualityRepository.save(streamQuality);

                log.info("Started transcoding for stream {} quality {}", stream.getId(), quality.getName());
            } catch (Exception e) {
                log.error("Failed to start transcoding for quality {}", quality.getName(), e);
            }
        }

        transcodingProcesses.put(stream.getId(), processes);

        // Also create master playlist
        createMasterPlaylist(stream);
    }

    public void stopTranscoding(Stream stream) {
        List<Process> processes = transcodingProcesses.remove(stream.getId());
        if (processes != null) {
            for (Process process : processes) {
                process.destroy();
            }
            log.info("Stopped transcoding for stream {}", stream.getId());
        }

        // Mark qualities as unavailable
        List<StreamQuality> qualities = streamQualityRepository.findByStream(stream);
        qualities.forEach(q -> q.setAvailable(false));
        streamQualityRepository.saveAll(qualities);
    }

    private List<String> buildFFmpegCommand(String input, String output, StreamingConfig.QualityConfig quality) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(input);
        command.add("-c:v");
        command.add("libx264");
        command.add("-s");
        command.add(quality.getWidth() + "x" + quality.getHeight());
        command.add("-b:v");
        command.add(quality.getBitrate() + "k");
        command.add("-r");
        command.add("30");
        command.add("-c:a");
        command.add("aac");
        command.add("-b:a");
        command.add("128k");
        command.add("-f");
        command.add("hls");
        command.add("-hls_time");
        command.add(streamingConfig.getHlsSegmentDuration().toString());
        command.add("-hls_list_size");
        command.add(streamingConfig.getHlsPlaylistLength().toString());
        command.add("-hls_flags");
        command.add("delete_segments+append_list");
        command.add("-preset");
        command.add("veryfast");
        command.add("-g");
        command.add("60");
        command.add(output);
        return command;
    }

    private void createMasterPlaylist(Stream stream) {
        String streamKey = stream.getStreamKey();
        String streamBasePath = streamingConfig.getStreamBasePath() + "/" + streamKey;
        Path masterPlaylistPath = Paths.get(streamBasePath, "master.m3u8");

        StringBuilder masterPlaylist = new StringBuilder();
        masterPlaylist.append("#EXTM3U\n");
        masterPlaylist.append("#EXT-X-VERSION:3\n\n");

        for (StreamingConfig.QualityConfig quality : streamingConfig.getQualities()) {
            masterPlaylist.append("#EXT-X-STREAM-INF:BANDWIDTH=")
                    .append(quality.getBitrate() * 1000)
                    .append(",RESOLUTION=")
                    .append(quality.getWidth())
                    .append("x")
                    .append(quality.getHeight())
                    .append(",NAME=\"")
                    .append(quality.getName())
                    .append("\"\n");
            masterPlaylist.append(quality.getName())
                    .append("/playlist.m3u8\n\n");
        }

        try {
            Files.writeString(masterPlaylistPath, masterPlaylist.toString());
            stream.setHlsUrl("/hls/" + streamKey + "/master.m3u8");
            log.info("Created master playlist for stream {}", stream.getId());
        } catch (IOException e) {
            log.error("Failed to create master playlist", e);
        }
    }
}
