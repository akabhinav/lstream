@echo off
echo ====================================
echo  LStream - YouTube Live Streaming
echo  Starting all services...
echo ====================================
echo.

echo Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed or not running!
    echo Please install Docker Desktop from: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo Docker is installed!
echo.

echo Starting all services with Docker Compose...
docker-compose up -d

echo.
echo Waiting for services to start (30 seconds)...
timeout /t 30 /nobreak

echo.
echo ====================================
echo  Checking service status...
echo ====================================
docker-compose ps

echo.
echo ====================================
echo  LStream is starting!
echo ====================================
echo.
echo Services will be available at:
echo   - Application API: http://localhost:8080
echo   - Health Check:    http://localhost:8080/actuator/health
echo   - HLS Streams:     http://localhost/hls
echo   - RTMP Ingest:     rtmp://localhost:1935/live
echo.
echo Wait 1-2 minutes for all services to be fully ready.
echo.
echo To view logs, run: docker-compose logs -f
echo To stop services, run: stop.bat
echo.
echo ====================================
pause
