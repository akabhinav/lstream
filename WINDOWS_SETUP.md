# LStream - Windows Setup Guide

This guide will help you run the YouTube-like live streaming platform on your Windows system using Docker.

## Prerequisites

### 1. Install Required Software

Before starting, ensure you have:

- ✅ **Docker Desktop for Windows** (Download from: https://www.docker.com/products/docker-desktop)
- ✅ **Git for Windows** (Download from: https://git-scm.com/download/win)
- ✅ **OBS Studio** (Optional, for testing streaming: https://obsproject.com/)

### 2. Verify Docker Installation

Open **PowerShell** or **Command Prompt** and run:

```powershell
docker --version
docker-compose --version
```

You should see version information for both commands.

### 3. Configure Docker Desktop

1. Open **Docker Desktop**
2. Go to **Settings** → **Resources**
3. Allocate resources:
   - **CPUs**: At least 4 cores
   - **Memory**: At least 8 GB RAM
   - **Disk**: At least 20 GB
4. Click **Apply & Restart**

## Quick Start (Easiest Method)

### Step 1: Clone the Repository

Open **PowerShell** and run:

```powershell
git clone <repository-url>
cd lstream
```

### Step 2: Start All Services

Simply run the start script:

```powershell
.\start.bat
```

This will:
- Start PostgreSQL database
- Start Redis cache
- Start Kafka event streaming
- Start the application server
- Start Nginx load balancer

Wait 2-3 minutes for all services to start.

### Step 3: Verify Services Are Running

Open your browser and check:

- **Application Health**: http://localhost:8080/actuator/health
- **Swagger API Docs** (if enabled): http://localhost:8080/swagger-ui.html

You should see a healthy status!

### Step 4: Stop All Services

When done, run:

```powershell
.\stop.bat
```

## Manual Setup (Step by Step)

If you prefer to run commands manually:

### 1. Start Services

```powershell
# Navigate to project directory
cd C:\path\to\lstream

# Start all services in detached mode
docker-compose up -d
```

### 2. Check Service Status

```powershell
# View running containers
docker-compose ps

# View logs
docker-compose logs -f app
```

### 3. Stop Services

```powershell
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## Testing the Platform

### 1. Register a User

Open **PowerShell** and run:

```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"teststreamer\",\"email\":\"test@example.com\",\"password\":\"password123\",\"displayName\":\"Test Streamer\"}'
```

Or use the test script:

```powershell
.\test-api.bat
```

### 2. Login and Get Token

```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"teststreamer\",\"password\":\"password123\"}'
```

Save the token from the response.

### 3. Create a Stream

```powershell
curl -X POST http://localhost:8080/api/streams `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_TOKEN_HERE" `
  -d '{\"title\":\"My First Stream\",\"description\":\"Testing live streaming\",\"category\":\"Gaming\",\"privacy\":\"PUBLIC\"}'
```

You'll receive a **streamKey** in the response - save this!

### 4. Configure OBS Studio

1. Open **OBS Studio**
2. Go to **Settings** → **Stream**
3. Select **Custom** service
4. Set **Server**: `rtmp://localhost:1935/live`
5. Set **Stream Key**: Your stream key from step 3
6. Click **OK**
7. Click **Start Streaming**

### 5. Watch the Stream

Open your browser:
- **HLS URL**: `http://localhost/hls/{YOUR_STREAM_KEY}/master.m3u8`

Use a video player like:
- **VLC Player**: File → Open Network Stream → Paste HLS URL
- **Web Browser**: Use HLS.js or Video.js player

### 6. View Live Streams

```powershell
curl http://localhost:8080/api/streams/live
```

## Windows-Specific Configuration

### File Paths

The docker-compose.yml uses Linux paths inside containers, but Docker Desktop handles the mapping automatically.

If you need to access streaming files from Windows:

1. Open Docker Desktop
2. Go to **Settings** → **Resources** → **File Sharing**
3. Add your project directory if not already added

### Firewall Configuration

If you have issues connecting:

1. Open **Windows Defender Firewall**
2. Click **Allow an app through firewall**
3. Find **Docker Desktop** and ensure both Private and Public are checked
4. If using OBS from another computer, allow ports:
   - 1935 (RTMP)
   - 8080 (API)
   - 80 (HLS)

### Network Issues

If localhost doesn't work, try:
- `127.0.0.1` instead of `localhost`
- Check Docker network: `docker network ls`
- Restart Docker Desktop

## Troubleshooting

### Issue: Containers won't start

**Solution:**
```powershell
# Stop all containers
docker-compose down

# Remove old volumes
docker-compose down -v

# Rebuild and start
docker-compose up -d --build
```

### Issue: Port already in use

**Error**: "Bind for 0.0.0.0:8080 failed: port is already allocated"

**Solution:**
```powershell
# Find what's using the port
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or change the port in docker-compose.yml
```

### Issue: Out of disk space

**Solution:**
```powershell
# Remove unused Docker resources
docker system prune -a

# Remove all volumes (WARNING: deletes all data)
docker volume prune
```

### Issue: Slow performance

**Solution:**
1. Increase Docker Desktop resources (Settings → Resources)
2. Close other heavy applications
3. Ensure Windows is not running updates

### Issue: Can't access from another device

**Solution:**
```powershell
# Find your Windows IP address
ipconfig

# Use that IP instead of localhost:
# From another device: rtmp://192.168.1.100:1935/live
```

### Issue: Database connection errors

**Solution:**
```powershell
# Check PostgreSQL logs
docker-compose logs postgres

# Restart just the database
docker-compose restart postgres

# Wait 10 seconds then restart app
Start-Sleep -Seconds 10
docker-compose restart app
```

## Development Mode (Optional)

If you want to modify code and run locally (without Docker):

### Prerequisites
- Install **Java 17 JDK** (https://adoptium.net/)
- Install **Maven** (https://maven.apache.org/download.cgi)
- Install **FFmpeg** (https://ffmpeg.org/download.html)

### Run Infrastructure Only

```powershell
# Start only databases (not the app)
docker-compose up -d postgres redis kafka zookeeper
```

### Run Application from Source

```powershell
# Build the project
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

## Useful Commands

### View Logs

```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f nginx
```

### Restart a Service

```powershell
docker-compose restart app
docker-compose restart nginx
```

### Access PostgreSQL Database

```powershell
docker-compose exec postgres psql -U postgres -d lstream
```

### Access Redis CLI

```powershell
docker-compose exec redis redis-cli
```

### Clean Everything

```powershell
# Stop and remove everything
docker-compose down -v

# Remove all Docker images
docker rmi $(docker images -q)

# Clean Docker system
docker system prune -a
```

## Performance Tips

1. **Enable WSL 2 Backend** (Docker Desktop Settings)
   - Better performance than Hyper-V
   - Faster file I/O

2. **Exclude Docker from Antivirus**
   - Add Docker directories to Windows Defender exclusions
   - Improves disk performance

3. **Use SSD for Docker Storage**
   - Docker Desktop Settings → Resources → Disk image location

4. **Allocate Sufficient Resources**
   - Minimum: 4 CPU, 8 GB RAM
   - Recommended for 20K users: 8 CPU, 16 GB RAM

## Video Player Integration

### HTML5 Player (for web)

Create a simple HTML file:

```html
<!DOCTYPE html>
<html>
<head>
    <title>LStream Player</title>
    <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
</head>
<body>
    <video id="video" controls width="800"></video>

    <script>
        var video = document.getElementById('video');
        var videoSrc = 'http://localhost/hls/YOUR_STREAM_KEY/master.m3u8';

        if (Hls.isSupported()) {
            var hls = new Hls();
            hls.loadSource(videoSrc);
            hls.attachMedia(video);
        }
    </script>
</body>
</html>
```

### VLC Player

1. Open **VLC Player**
2. **Media** → **Open Network Stream**
3. Paste: `http://localhost/hls/YOUR_STREAM_KEY/master.m3u8`
4. Click **Play**

## Next Steps

1. ✅ Start the platform: `.\start.bat`
2. ✅ Register a user via API
3. ✅ Create a stream
4. ✅ Configure OBS with your stream key
5. ✅ Start broadcasting
6. ✅ Watch your stream in VLC or browser

## Support

If you encounter issues:
- Check Docker Desktop is running
- Verify all containers are healthy: `docker-compose ps`
- Check logs: `docker-compose logs -f`
- Restart Docker Desktop
- Review this troubleshooting guide

## Production Deployment

For production on Windows Server:
- Use **Windows Server 2019/2022**
- Install **Docker Enterprise**
- Use **IIS** as reverse proxy (alternative to Nginx)
- Enable **Windows Firewall** rules
- Use **SSL/TLS certificates**

---

**Happy Streaming! 🎥**
