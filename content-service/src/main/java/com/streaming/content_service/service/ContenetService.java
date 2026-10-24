package com.streaming.content_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.streaming.content_service.dto.MovieRequest;
import com.streaming.content_service.dto.MovieResponse;
import com.streaming.content_service.model.Movie;
import com.streaming.content_service.model.VideoStatus;
import com.streaming.content_service.repository.ContentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContenetService {
    private final ContentRepository contentRepository;

    public MovieResponse createMovie(MovieRequest movieRequest) {
        log.info("Creating a new movie: {}", movieRequest);
        Movie movie = new Movie();
        movie.setTitle(movieRequest.getTitle());
        movie.setDescription(movieRequest.getDescription());
        movie.setGenre(movieRequest.getGenre());
        movie.setDirector(movieRequest.getDirector());
        movie.setCast(movieRequest.getCast());
        movie.setRating(movieRequest.getRating());
        movie.setThumbnailUrl(movieRequest.getThumbnailUrl());
        movie.setDurationMinutes(movieRequest.getDurationMinutes());
        movie.setReleaseYear(movieRequest.getReleaseYear());
        movie.setVideoStatus(VideoStatus.PENDING);

        Movie savedMovie = contentRepository.save(movie);
        log.info("Movie created successfully: {}", savedMovie);

        return mapToResponse(savedMovie);
    }

    private MovieResponse mapToResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setGenre(movie.getGenre());
        response.setDirector(movie.getDirector());
        response.setCast(movie.getCast());
        response.setRating(movie.getRating());
        response.setThumbnailUrl(movie.getThumbnailUrl());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setReleaseYear(movie.getReleaseYear());
        response.setVideoStatus(movie.getVideoStatus());
        response.setCreatedAt(movie.getCreatedAt());
        response.setUpdatedAt(movie.getUpdatedAt());
        response.setVideoKey(movie.getVideoKey());
        response.setHlsUrl(movie.getHlsUrl());
        return response;
    }

    public List<MovieResponse> getAllMovies() {
        log.info("Fetching all movies");
        List<Movie> movies = contentRepository.findAll();
        return movies.stream().map(this::mapToResponse).toList();
    }

    public List<MovieResponse> getMoviesByGenre(com.streaming.content_service.model.Genre genre) {
        log.info("Fetching movies by genre: {}", genre);
        List<Movie> movies = contentRepository.findByGenre(genre);
        return movies.stream().map(this::mapToResponse).toList();
    }

    public MovieResponse getMovieById(String movieId) {
        log.info("Fetching movie by ID: {}", movieId);
        Movie movie = contentRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with ID: " + movieId));
        return mapToResponse(movie);
    }

    public List<MovieResponse> searchMovies(String query) {
        log.info("Searching movies with query: {}", query);
        List<Movie> movies = contentRepository.findAll().stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                 movie.getDescription().toLowerCase().contains(query.toLowerCase()))
                .toList();
        return movies.stream().map(this::mapToResponse).toList();
    }

    public List<MovieResponse> searchMoviesByTitle(String title) {
        log.info("Searching movies by title: {}", title);
        List<Movie> movies = contentRepository.findAll().stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
        return movies.stream().map(this::mapToResponse).toList();
    }

    public void updateVideoKey(String movieId, String videoKey) {
        log.info("Updating video key for movie ID: {}", movieId);
        Movie movie = contentRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with ID: " + movieId));
        movie.setVideoKey(videoKey);
        movie.setVideoStatus(VideoStatus.UPLOADED);
        contentRepository.save(movie);
        log.info("Video key updated successfully for movie ID: {}", movieId);
    }

    public void updateHlsUrl(String movieId, String hlsUrl) {
        log.info("Updating HLS URL for movie ID: {}", movieId);
        Movie movie = contentRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with ID: " + movieId));
        movie.setHlsUrl(hlsUrl);
        movie.setVideoStatus(VideoStatus.READY);
        contentRepository.save(movie);
        log.info("HLS URL updated successfully for movie ID: {}", movieId);
    }

    public void updateVideoStatus(String movieId, VideoStatus videoStatus){
         Movie movie = contentRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with ID: " + movieId));
        movie.setVideoStatus(videoStatus);
        contentRepository.save(movie);
        log.info("Video status updated successfully for movie ID: {}", movieId);
}
}
