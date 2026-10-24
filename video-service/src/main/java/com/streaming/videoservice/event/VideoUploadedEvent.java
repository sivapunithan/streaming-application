package com.streaming.videoservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to Kafka when a video is uploaded to S3.
 * Encoding service will consume this event and start FFmpeg processing.
 * 
 * TOPIC : video.uploaded
 */

@Data
@AllArgsConstructor @NoArgsConstructor
public class VideoUploadedEvent {
    
    private String movieId;
    private String videoKey;
    private String bucketName;
    private String originalFileName;
    private long fileSizeBytes;
}
