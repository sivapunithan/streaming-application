package com.streaming.content_service.dto;

import java.time.LocalDateTime;

import com.streaming.content_service.model.Genre;
import com.streaming.content_service.model.VideoStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class MovieResponse {
    
    private String id;
    private String title;
    private String description;
    private Genre genre;
    private String director;
    private String cast;
    private int releaseYear;
    private double rating;
    private String thumbnailUrl;
    private int durationMinutes;
    // S3 key for the video file
    private String videoKey;
    private String hlsUrl;

    private VideoStatus videoStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
