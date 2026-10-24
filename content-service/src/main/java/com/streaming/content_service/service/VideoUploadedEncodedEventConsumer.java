package com.streaming.content_service.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.streaming.content_service.model.VideoStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoUploadedEncodedEventConsumer {
    
    private final ContenetService contenetService;

    @KafkaListener(
        topics = "video.uploaded"
    )
    public void consumerVideoUpladedEvent(
        @Payload Map<String, Object> payload
    ){
        String movieId = payload.get("movieId").toString();
        String videoKey = payload.get("videoKey").toString();

        log.info("Video uploaded for movie; {} key: {}", movieId, videoKey);
        
        contenetService.updateVideoKey(movieId, videoKey);
        
    }

    @KafkaListener(
        topics = "video.encoded"
    )
    public void consumeVideoEncodedEvent(
        @Payload Map<String, Object> payload
    ){
        String movieId = payload.get("movieId").toString();
        String hlsUrl = payload.get("hlsUrl").toString();
        boolean success = (boolean) payload.get("success");

        if(success){
            contenetService.updateHlsUrl(movieId, hlsUrl);
        } else{
            String errorMessage = payload.get("errorMessage").toString();
            log.error("Error encoding video for movie: {}. Error: {}", movieId, errorMessage);
            contenetService.updateVideoStatus(movieId, VideoStatus.FAILED);
        }
        
    }
}
