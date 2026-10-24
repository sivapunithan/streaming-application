package com.streaming.videoservice.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.streaming.videoservice.service.VideoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/videos")
@Slf4j @RequiredArgsConstructor
public class VideoController {
    
    private final VideoService videoService;

    /**
     * Upload video file for a movie
     * Accepts multipart file upload
     */

    @PostMapping("/upload/{movieId}")
    public ResponseEntity<String> uploadVideo(
        @PathVariable String movieId, 
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info("Video Upload request for movieId: {} file size: {}", movieId, file.getSize() / (1024 * 1024)); // Convert to MB
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        String videoKey = videoService.uploadVideo(movieId, file);
        log.info("Video uploaded successfully for movieId: {}", movieId);
        return ResponseEntity.ok("Video uploaded successfully: "+ videoKey + "- Encoding started automatically via Kafka");
    }
}
