package com.streaming.streamingservice.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.streaming.streamingservice.dto.StreamingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingService {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiry}")
    private long presignedUrlExpiry;

    @Value("${app.base-url}")
    private String baseUrl;

    // Redis key for caching streaing url

    private final static String STREAMING_URL_CACHE_PREFIX = "streaming:url:";

    /**
     * FLOW 
     * 
     * 1. Check redis cache for existing presigned URL
     * 2. If cached return immediately
     * 3. If not cached = generate new presigned URL 
     * 4. Cache the URL in redis
     * 5. Return streaming URL
     */

    public StreamingResponse getStreamingUrl(String movieId, String playlistKey){
        log.info("Getting streaming url for movie: {}", movieId);

        String cacheKey =  STREAMING_URL_CACHE_PREFIX + movieId;

        // Check redis cache first
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
        if (cachedUrl != null){
            log.info("Returning cached streaming URL for movie: {}", movieId);
            return new StreamingResponse(
                movieId, cachedUrl, "1080p, 720p, 480p, 360p", presignedUrlExpiry
            );
        }

        // Build a stable URL to our own master-playlist endpoint, which signs
        // each nested quality playlist/segment on the fly. A raw presigned S3
        // URL for the master file isn't enough - its quality playlist lines
        // are relative S3 keys, which would be fetched unsigned by the player.
        log.info("Generating streaming URL for movie: {}", movieId);

        String masterUrl = baseUrl + "/api/v1/stream/" + movieId + "/master.m3u8";

        // Cache in redis for 55 minutes
        redisTemplate.opsForValue().set(cacheKey, masterUrl, 55, TimeUnit.MINUTES);
        log.info("Streaming URL generated and cached for movie: {}", movieId);

        return new StreamingResponse(
            movieId,
            masterUrl,
            "1080p, 720p, 480p, 360p",
            presignedUrlExpiry
        );

    }

    /**
     * Get the master playlist for a movie, with nested quality-playlist
     * references rewritten to point back at this service's own signed
     * playlist endpoint instead of raw (unsigned) S3 keys.
     */
    public String getSignedMasterPlaylist(String movieId, String masterKey){
        log.info("Getting signed master playlist for movie: {}", movieId);

        String basePath = masterKey.substring(0, masterKey.lastIndexOf('/') + 1);
        String m3u8Content = readFromS3(masterKey);

        return reWriteMasterPlaylistUrls(movieId, m3u8Content, basePath);
    }

    private String reWriteMasterPlaylistUrls(
        String movieId, String m3u8Content, String basePath
    ){
        StringBuilder reWritten = new StringBuilder();
        for(String line : m3u8Content.split("\n")){
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")){
                reWritten.append(line).append("\n");
                continue;
            }

            String fullKey = basePath + trimmed;

            if (trimmed.endsWith(".m3u8")){
                // Nested quality playlist - route back through our own signing
                // endpoint so its segment references get presigned too.
                String signedPlaylistUrl = baseUrl + "/api/v1/stream/" + movieId
                        + "/playlist?path=" + URLEncoder.encode(fullKey, StandardCharsets.UTF_8);
                reWritten.append(signedPlaylistUrl).append("\n");
            } else {
                reWritten.append(generatePresignedUrl(fullKey)).append("\n");
            }
        }

        return reWritten.toString();
    }

    /**
     * Generate pre-signed url for s3 object
     * URL expires at configured time
     */

    private String generatePresignedUrl(String key){
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpiry))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url().toString();
        
    }

    /**
     * Invalid cache streaming url
     * 
     * called when video is re-encoded or updated
     */

    public void invalidateCache(String movieId){
        String cacheKey = STREAMING_URL_CACHE_PREFIX + movieId;
        redisTemplate.delete(cacheKey);
        log.info("Invalidated streaming cache for movie: {}", movieId);
    }

    /**
     * This is the key method that make everything secure
     * @param movieId
     * @param playListPath
     * @return
     */

    public String getSignedPlaylist(String movieId, String playListPath){
        log.info("Getting signed playlist for movie: {}", movieId);
        
        //Get base path for the playlist
        String basePath = playListPath.substring(0,
            playListPath.lastIndexOf('/') + 1);

        // read m3u8 content from S3
        String m3u8Content = readFromS3(playListPath);

        // Re-write each line that is a segment or playlist reference
        String signedContent = reWriteM3U8SignedUrls(m3u8Content, basePath);

        return signedContent;
        
    }

    private String reWriteM3U8SignedUrls(
        String m3u8Content, String basePath
    ){

        StringBuilder reWritten = new StringBuilder();
        for(String line : m3u8Content.split("\n")){
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")){
                reWritten.append(line).append("\n");
                continue;
            }

            String fullKey = basePath + trimmed;
            String signedUrl = generatePresignedUrl(fullKey);

            reWritten.append(signedUrl).append("\n");
        }

        return reWritten.toString();
    }

    /**
     * 
     * read file content from s3
     */
    private String readFromS3(String key){

        log.info("Reading key: {}", key);

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);

        return new BufferedReader(new InputStreamReader(response))
                .lines()
                .collect(Collectors.joining("\n"));
    }
                
}
