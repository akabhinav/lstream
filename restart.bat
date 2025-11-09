@echo off
echo ====================================
echo  LStream - Restart Services
echo ====================================
echo.

echo Restarting all services...
docker-compose restart

echo.
echo Waiting for services to restart (20 seconds)...
timeout /t 20 /nobreak

echo.
echo ====================================
echo  Services restarted!
echo ====================================
echo.
docker-compose ps
echo.
pause
