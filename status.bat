@echo off
echo ====================================
echo  LStream - Service Status
echo ====================================
echo.

echo Docker containers:
echo.
docker-compose ps

echo.
echo ====================================
echo  Health Checks
echo ====================================
echo.

echo Checking Application...
curl -s http://localhost:8080/actuator/health
echo.

echo.
echo ====================================
echo  Resource Usage
echo ====================================
echo.

docker stats --no-stream

echo.
pause
