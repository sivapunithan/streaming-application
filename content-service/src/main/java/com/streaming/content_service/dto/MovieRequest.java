package com.streaming.content_service.dto;

import com.streaming.content_service.model.Genre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Genre is required")
    private Genre genre;
    private String director;
    private String cast;
    private int releaseYear;
    private double rating;
    private String thumbnailUrl;
    private int durationMinutes;
}
