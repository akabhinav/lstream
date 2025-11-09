# 🚀 Quick Start Guide for Windows

Get LStream running on Windows in just **3 steps**!

## Prerequisites

✅ **Docker Desktop** installed and running
Download: https://www.docker.com/products/docker-desktop

## Step 1: Open PowerShell or Command Prompt

Press `Win + X` and select **Windows PowerShell** or **Command Prompt**

## Step 2: Navigate to Project

```powershell
cd C:\path\to\lstream
```

Replace `C:\path\to\lstream` with your actual project path.

## Step 3: Start Everything

```powershell
start.bat
```

That's it! Wait 2-3 minutes for all services to start.

## Verify It's Working

Open your browser and visit:

**http://localhost:8080/actuator/health**

You should see: `{"status":"UP"}`

## What's Running?

- ✅ PostgreSQL Database (port 5432)
- ✅ Redis Cache (port 6379)
- ✅ Apache Kafka (port 9092)
- ✅ Application Server (port 8080)
- ✅ Nginx Load Balancer (port 80)

## Test the API

### 1. Register a User

Run:
```powershell
test-api.bat
```

### 2. Start Streaming with OBS

1. **Open OBS Studio**
2. **Settings** → **Stream**
3. **Server**: `rtmp://localhost:1935/live`
4. **Stream Key**: Get from creating a stream via API
5. **Start Streaming**!

## Useful Commands

| Command | Description |
|---------|-------------|
| `start.bat` | Start all services |
| `stop.bat` | Stop all services |
| `restart.bat` | Restart all services |
| `status.bat` | Check service status |
| `logs.bat` | View application logs |
| `test-api.bat` | Test API endpoints |
| `clean.bat` | Remove all data (fresh start) |

## API Endpoints

Base URL: **http://localhost:8080**

### Authentication
- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login and get JWT token

### Streams
- `GET /api/streams/live` - Get all live streams
- `POST /api/streams` - Create new stream (requires auth)
- `GET /api/streams/{id}` - Get stream details

### Chat
- `POST /api/streams/{id}/chat` - Send chat message (requires auth)
- `GET /api/streams/{id}/chat` - Get recent messages

### Analytics
- `GET /api/analytics/streams/{id}/statistics` - Get stream stats

## Example: Create and Start a Stream

### 1. Register
```powershell
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"username\":\"mystreamer\",\"email\":\"me@example.com\",\"password\":\"pass123\",\"displayName\":\"My Channel\"}"
```

### 2. Login
```powershell
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"mystreamer\",\"password\":\"pass123\"}"
```

Copy the **token** from the response.

### 3. Create Stream
```powershell
curl -X POST http://localhost:8080/api/streams -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN_HERE" -d "{\"title\":\"My First Stream\",\"category\":\"Gaming\",\"privacy\":\"PUBLIC\"}"
```

Copy the **streamKey** from the response.

### 4. Configure OBS
- Server: `rtmp://localhost:1935/live`
- Stream Key: [Your stream key from step 3]
- Start Streaming!

### 5. Watch Stream
Use VLC Player or any HLS player:
```
http://localhost/hls/{YOUR_STREAM_KEY}/master.m3u8
```

## Troubleshooting

### Problem: "Docker is not running"
**Solution**: Open Docker Desktop and wait for it to start

### Problem: "Port already in use"
**Solution**:
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Problem: Services won't start
**Solution**:
```powershell
clean.bat
start.bat
```

### Problem: Can't access from browser
**Solution**: Try `127.0.0.1` instead of `localhost`

## Need Help?

- Check logs: `logs.bat`
- Check status: `status.bat`
- View full docs: `WINDOWS_SETUP.md`

## Stop Everything

When done:
```powershell
stop.bat
```

---

**That's it! You're ready to stream! 🎥**
