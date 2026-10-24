package com.streaming.videoservice.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.streaming.videoservice.event.VideoUploadedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j @RequiredArgsConstructor
public class VideoService {
    
    private final S3Client s3Client;
    private final KafkaTemplate<String, VideoUploadedEvent> kafkaTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final String VIDEO_UPLOADED_TOPIC = "video.uploaded";

    /**
     * FLOW
     * 
     * 1, Receive video upload request 
     * 2, Generate unique s3 key
     * 3, Upload video to S3
     * 4, Publish VideoUploadedEvent to Kafka
     * 5, Encoding service picks up and start FFmpeg
     */

    public String uploadVideo(String movieId, MultipartFile file) throws IOException {

        log.info("Uploading video for movieId: {}", movieId);
        // Generate unique S3 key for raw video
        // Format: raw/movieId/uuid_filename

        String videoKey = "raw/" + movieId + "/" + java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(videoKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        log.info("Video uploaded to S3 with key: {}", videoKey);

        // Publish VideoUploadedEvent to Kafka
        // Encoding Service will consume this and start FFmpeg processing

        VideoUploadedEvent event = new VideoUploadedEvent(
            movieId,
            videoKey,
            bucketName,
            file.getOriginalFilename(),
            file.getSize()  
        );

        kafkaTemplate.send(VIDEO_UPLOADED_TOPIC, event);
        log.info("Published VideoUploadedEvent to Kafka for movieId: {}", movieId);

        return videoKey;
    }
}

