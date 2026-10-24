# Api Gatway
   Single point entry

# Content service (movie catelog)
   DB of all database content

# Video Service --> Kafka (video uploaded) --> Encoding Service (ffmpg) 
    - Encoding service - once video uploaded it convert it into multiple qualities
    - convert it into HLS format (Small chunks of 10sec each)

   Receives raw video file and upload to S3

# Streaming Service (HLS Playback)

# AWS S3 (Video Storage)

=========================================================================================

# HLS (HTTP Live Streaming)
  - Raw video file (50GB) -> 2hrs long
  - FFmpeg Encoding (chunk1.ts  chunk2.ts  .. chunkn.ts) combing all the chunks we get 2 hrs long
  - For all the qualities the chunk will be created i.e 1080p , 720p 
  - All the chunks will be stored in the S3
  - A master playlist is created --> master.m3u8 (contain all the table of content)
  - When the user clicks play or download, first it will download master 
  - Adaptive bitrate streaming