package com.streaming.content_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.streaming.content_service.dto.MovieRequest;
import com.streaming.content_service.dto.MovieResponse;
import com.streaming.content_service.model.Genre;
import com.streaming.content_service.service.ContenetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/movies") @Slf4j @RequiredArgsConstructor
public class ContentController {

    private final ContenetService contentService;

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest movieRequest) {
        log.info("Creating a new movie: {}", movieRequest);
        MovieResponse createdMovie = contentService.createMovie(movieRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
    }

    // Get All the movies
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        log.info("Fetching all movies");
        List<MovieResponse> movies = contentService.getAllMovies();
        return ResponseEntity.ok(movies);
    }

    // Get movie by Genre
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponse>> getMoviesByGenre(@PathVariable Genre genre) {
        log.info("Fetching movies by genre: {}", genre);
        List<MovieResponse> movies = contentService.getMoviesByGenre(genre);
        return ResponseEntity.ok(movies);
    }

    // Get movie by ID
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable String movieId) {
        log.info("Fetching movie by ID: {}", movieId);
        MovieResponse movie = contentService.getMovieById(movieId);
        return ResponseEntity.ok(movie);    
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMoviesByTitle(@RequestParam String title) {
        log.info("Searching movies by title: {}", title);
        List<MovieResponse> movies = contentService.searchMoviesByTitle(title);
        return ResponseEntity.ok(movies);
    }
}
