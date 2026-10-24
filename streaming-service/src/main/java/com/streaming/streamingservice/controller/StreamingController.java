package com.streaming.streamingservice.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.streaming.streamingservice.dto.StreamingResponse;
import com.streaming.streamingservice.service.StreamingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/stream")
@Slf4j @RequiredArgsConstructor
public class StreamingController {
    
    private final StreamingService streamingService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String MASTER_PLAYLIST_KEY_PREFIX = "streaming:playlist:";


    /**
     * Get Streaming URL for a movie
     * Returns pre-signed HLS master playlist URL 
     * 
     */

    @GetMapping("/{movieId}")
    public ResponseEntity<StreamingResponse> getStreamingURL(
        @PathVariable String movieId
    ){
        log.info("Streaming request for movie: {}", movieId);
        String playlistKey = redisTemplate.opsForValue()
                 .get(MASTER_PLAYLIST_KEY_PREFIX + movieId);

        if (playlistKey == null){
            return ResponseEntity.notFound().build();
        }
        StreamingResponse response = streamingService.getStreamingUrl(movieId, playlistKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{movieId}/playlist")
    public ResponseEntity<String> getSignedPlayList(@PathVariable String movieId, @RequestParam String path){

        String signedPlaylist = streamingService.getSignedPlaylist(movieId, path);
        return ResponseEntity.ok()
                .header("content-Type", "application/x-mpegURL")
                .body(signedPlaylist);
    }

    @GetMapping("/{movieId}/master.m3u8")
    public ResponseEntity<String> getSignedMasterPlaylist(@PathVariable String movieId){
        String masterKey = redisTemplate.opsForValue()
                 .get(MASTER_PLAYLIST_KEY_PREFIX + movieId);

        if (masterKey == null){
            return ResponseEntity.notFound().build();
        }

        String signedMasterPlaylist = streamingService.getSignedMasterPlaylist(movieId, masterKey);
        return ResponseEntity.ok()
                .header("content-Type", "application/x-mpegURL")
                .body(signedMasterPlaylist);
    }
}
