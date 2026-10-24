package com.streaming.content_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.streaming.content_service.model.Genre;
import com.streaming.content_service.model.Movie;

public interface ContentRepository extends JpaRepository<Movie, String> {
    List<Movie> findByGenre(Genre genre);
    
}
