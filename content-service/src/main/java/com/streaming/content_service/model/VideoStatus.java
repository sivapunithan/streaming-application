package com.streaming.content_service.model;


/**
   PENDING --> UPLODED --> ENCODING --> ENCODED --> READY --> FAILED
 */
public enum VideoStatus {
   PENDING,
   UPLOADED,
   ENCODING, //FFmpeg is encoding the video
    ENCODED,
   READY, // HLS playlist is generated and ready for streaming
   FAILED 
}
