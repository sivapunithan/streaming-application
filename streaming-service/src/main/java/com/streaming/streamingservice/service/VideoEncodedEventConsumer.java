package com.streaming.streamingservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.streaming.streamingservice.event.VideoEncodedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoEncodedEventConsumer {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String MASTER_PLAYLIST_KEY_PREFIX = "streaming:playlist:";

    /**
     * Listens to video.encodes kafka yopic
     * Stores master playlist key i redis when encoding is complete 
     * This allows streamingservice to quickly find the playlist key by id
     */

    @KafkaListener(
        topics = "video.encoded",
        groupId = "streaming-service-group"
    )
    public void consumerVideoEncodedEvent(VideoEncodedEvent event){
        log.info("Consumed VideoEncodedEvent for movie: {} success: {}",
            event.getMovieId(), event.isSuccess()
        );

        if (event.isSuccess()){
            String cacheKey = MASTER_PLAYLIST_KEY_PREFIX + event.getMovieId();
            redisTemplate.opsForValue().set(cacheKey, event.getMasterPlayListKey());
            log.info("Master playlist key stored in redis for movie : {}", event.getMovieId());
        } else{
            log.error("Encoding failed for movie : {} - {}",event.getMovieId(), event.getErrorMessage() );
        }
    }
}
