package com.lstream.dto;

import com.lstream.model.Stream;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StreamCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private String category;

    private Stream.StreamPrivacy privacy = Stream.StreamPrivacy.PUBLIC;

    private Boolean chatEnabled = true;

    private Boolean recordingEnabled = true;

    private Boolean lowLatencyMode = false;
}
