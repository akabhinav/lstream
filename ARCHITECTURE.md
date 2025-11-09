# LStream Architecture Documentation

## System Overview

LStream is a distributed live streaming platform designed to handle 20,000+ concurrent viewers with low latency and high reliability.

## Core Components

### 1. Application Layer (Spring Boot)

#### Stream Service
- **Responsibility**: Core streaming logic
- **Functions**:
  - Create and manage streams
  - Start/stop transcoding
  - Handle stream lifecycle
  - Manage viewer counts
  - Generate stream keys

#### Transcoding Service
- **Responsibility**: Video processing
- **Functions**:
  - Accept RTMP input
  - Transcode to multiple qualities (ABR)
  - Generate HLS playlists
  - Manage FFmpeg processes
  - Quality: 1080p (5000kbps), 720p (2800kbps), 480p (1400kbps), 360p (800kbps)

#### Chat Service
- **Responsibility**: Real-time messaging
- **Functions**:
  - WebSocket message handling
  - Rate limiting (30 msg/min per user)
  - Message persistence
  - Moderation capabilities

#### Analytics Service
- **Responsibility**: Metrics and insights
- **Functions**:
  - Record viewer sessions
  - Track watch duration
  - Aggregate statistics
  - Real-time metrics collection (every 60s)

#### Auth Service
- **Responsibility**: User management
- **Functions**:
  - User registration/login
  - JWT token generation
  - Role-based access control

### 2. Data Layer

#### PostgreSQL
- **Purpose**: Primary data store
- **Schema**:
  - users (authentication, profiles)
  - streams (metadata)
  - chat_messages (chat history)
  - stream_views (viewer tracking)
  - stream_analytics (time-series data)
  - stream_qualities (available qualities)

#### Redis
- **Purpose**: Caching and real-time data
- **Use Cases**:
  - Stream key → Stream ID mapping
  - Current viewer counts
  - Live stream sets
  - Chat rate limiting
  - Session management

#### Apache Kafka
- **Purpose**: Event streaming
- **Topics**:
  - stream-events (lifecycle events)
  - viewer-events (join/leave)
  - chat-events (messages)
  - analytics-events (metrics)

### 3. Media Layer

#### RTMP Ingestion
- **Port**: 1935
- **Format**: RTMP (Real-Time Messaging Protocol)
- **Source**: OBS, Streamlabs, FFmpeg

#### HLS Output
- **Format**: HTTP Live Streaming
- **Segment Duration**: 6 seconds
- **Playlist Length**: 5 segments
- **Delivery**: Nginx static file serving

#### Storage
- **Live Streams**: `/var/streaming/{streamKey}/`
- **Recordings**: `/var/recordings/{streamKey}/`
- **Structure**:
  ```
  /var/streaming/
  ├── {streamKey}/
  │   ├── master.m3u8 (master playlist)
  │   ├── 1080p/
  │   │   ├── playlist.m3u8
  │   │   ├── segment0.ts
  │   │   ├── segment1.ts
  │   │   └── ...
  │   ├── 720p/
  │   ├── 480p/
  │   └── 360p/
  ```

### 4. Infrastructure Layer

#### Docker Containers
- `lstream-app`: Application server
- `lstream-postgres`: Database
- `lstream-redis`: Cache
- `lstream-kafka`: Event streaming
- `lstream-zookeeper`: Kafka coordination
- `lstream-nginx`: Load balancer & static serving

#### Nginx Configuration
- **Reverse Proxy**: Routes API requests to app servers
- **WebSocket Proxy**: Handles WebSocket upgrades
- **HLS Serving**: Serves .m3u8 and .ts files
- **Load Balancing**: Distributes load across app instances
- **CORS**: Configured for cross-origin requests

## Data Flow

### Stream Broadcast Flow

```
1. Streamer starts OBS
   └→ RTMP stream to rtmp://server:1935/live/{streamKey}

2. Application receives RTMP
   └→ Validates stream key
   └→ Updates stream status to STARTING

3. Transcoding Service
   └→ Spawns FFmpeg processes (one per quality)
   └→ Generates HLS segments
   └→ Creates master playlist
   └→ Updates stream status to LIVE

4. Redis Update
   └→ Adds stream to "streams:live" set
   └→ Caches stream metadata

5. Kafka Event
   └→ Publishes STREAM_STARTED event
   └→ Analytics consumers process event
```

### Viewer Watch Flow

```
1. Viewer opens stream page
   └→ GET /api/streams/{id}
   └→ Retrieves stream metadata

2. Viewer clicks play
   └→ POST /api/streams/{id}/watch
   └→ Increments viewer count (DB + Redis)
   └→ Records StreamView entry

3. Video player requests HLS
   └→ GET /hls/{streamKey}/master.m3u8
   └→ Nginx serves from /var/streaming
   └→ Player selects quality
   └→ GET /hls/{streamKey}/720p/playlist.m3u8
   └→ Player downloads segments

4. WebSocket connection
   └→ ws://server/ws
   └→ Subscribe to /topic/stream/{id}/chat
   └→ Receive real-time chat messages

5. Viewer leaves
   └→ POST /api/streams/{id}/leave
   └→ Decrements viewer count
   └→ Updates StreamView with watch duration
```

### Chat Message Flow

```
1. User sends message
   └→ WebSocket: /app/stream/{id}/chat
   OR
   └→ REST: POST /api/streams/{id}/chat

2. ChatService processes
   └→ Rate limit check (Redis)
   └→ Validate stream and user
   └→ Save to PostgreSQL

3. Broadcast
   └→ WebSocket: /topic/stream/{id}/chat
   └→ All connected viewers receive message

4. Analytics
   └→ Kafka: chat-events topic
   └→ Increment stream.chatMessageCount
```

## Scaling Architecture

### For 20,000 Concurrent Viewers

#### Application Tier (Horizontal Scaling)
```
Load Balancer (Nginx)
    ├→ App Server 1 (5,000 viewers)
    ├→ App Server 2 (5,000 viewers)
    ├→ App Server 3 (5,000 viewers)
    └→ App Server 4 (5,000 viewers)
```

#### Database Tier (Read Replicas)
```
PostgreSQL Primary (Writes)
    ├→ Read Replica 1
    ├→ Read Replica 2
    └→ Read Replica 3
```

#### CDN Integration
```
Origin Server (HLS files)
    └→ CDN Edge Locations
        ├→ US East
        ├→ US West
        ├→ Europe
        └→ Asia Pacific
```

### Resource Requirements

#### For 20K Viewers (Single Stream)
- **Bandwidth**: ~150 Gbps (assuming 720p avg at 2.8 Mbps)
- **CPU**: Transcoding: 8 cores, API: 4 cores/server
- **Memory**: 16GB/app server
- **Storage**: 1TB for recordings

#### For 100 Concurrent Streams
- **Bandwidth**: 15 Tbps (with CDN)
- **CPU**: 800 cores for transcoding
- **Memory**: 160GB total
- **Database**: 500GB SSD

## Security Architecture

### Authentication Flow
```
1. User → POST /api/auth/login
2. Server validates credentials (BCrypt)
3. Server generates JWT (HS256)
4. Client stores JWT
5. Client → API Request + Bearer Token
6. JwtAuthenticationFilter validates token
7. Request proceeds to controller
```

### Security Layers
- **Network**: TLS/HTTPS, firewall rules
- **Application**: JWT authentication, CORS, rate limiting
- **Database**: Encrypted connections, prepared statements
- **Stream**: Stream key validation, IP restrictions (optional)

## Monitoring & Observability

### Metrics (Prometheus)
- `stream_current_viewers{stream_id}`
- `stream_transcoding_bitrate{quality}`
- `chat_messages_per_second`
- `api_request_duration_seconds`
- `ffmpeg_process_count`

### Health Checks
- Application: `/actuator/health`
- Database: Connection pool status
- Redis: Ping command
- Kafka: Broker connectivity

### Logging
- **Application**: SLF4J → Logback
- **Access Logs**: Nginx
- **Streaming**: FFmpeg stderr
- **Centralized**: (Optional) ELK Stack

## Failure Scenarios & Recovery

### Stream Crash
1. FFmpeg process dies
2. Stream status → ERROR
3. Viewers notified via WebSocket
4. Auto-retry mechanism (3 attempts)

### Database Failure
1. Connection pool exhausted
2. Fallback to Redis cache for reads
3. Write operations queued in Kafka
4. Replay after recovery

### Redis Failure
1. Cache misses → Database queries
2. Performance degradation
3. No data loss (cache only)

### Kafka Failure
1. Analytics delayed
2. Core functionality maintained
3. Events queued in app memory
4. Replay on reconnection

## Future Enhancements

1. **Geo-Distributed Streaming**: Regional ingest points
2. **WebRTC**: Lower latency (< 1 second)
3. **AI Moderation**: Automatic content filtering
4. **Adaptive Encoding**: Quality based on viewer devices
5. **P2P Delivery**: Reduce bandwidth costs
6. **Edge Computing**: Transcoding at CDN edge

## Performance Optimization

### Database
- Indexes on frequently queried fields
- Batch inserts for analytics
- Partitioning on timestamp columns

### Caching
- Stream metadata: 24-hour TTL
- Viewer counts: No TTL (updated real-time)
- API responses: 60-second TTL

### Streaming
- Preset: veryfast (low CPU, good quality)
- GOP size: 60 frames (2 seconds at 30fps)
- Segment duration: 6 seconds (balance latency/overhead)

---

**Last Updated**: 2025-11-09
