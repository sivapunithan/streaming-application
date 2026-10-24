package com.streaming.encodingservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.streaming.encodingservice.event.VideoUploadedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j @RequiredArgsConstructor
public class VideoEventConsumer {
    

    private final EncodingService encodingService;

    /**
     * Listens  to video.uploaded kafka topic
     * Triggered when the raw video service uploads a raw video to s3
     * 
     * FLOW: 
     *     video servic -> s3 upload -> kafka (video.upload)
     *                               -> This Consumer
     *                               -> Encoding Service -> FFmpeg -> S3
     *                               -> Kafka (video.encoded)
     */

    @KafkaListener(
        topics = "video.uploaded",
        groupId = "encoding-service-group"
    )
    public void consumeVideoUploadedEvent(VideoUploadedEvent event){
        log.info("Consumed Video uploaded event for movie: {} - {}", event.getMovieId(), event.getOriginalFileName());
        try {
            encodingService.encodeVideo(event);
        } catch (Exception e) {
            log.error("Failed to process encoding for the movie: {} - {}", event.getMovieId(), e.getMessage());
            throw new IllegalStateException("Video encoding failed", e);
        }
    }
}
