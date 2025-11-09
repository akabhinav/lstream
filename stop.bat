@echo off
echo ====================================
echo  LStream - YouTube Live Streaming
echo  Stopping all services...
echo ====================================
echo.

docker-compose down

echo.
echo ====================================
echo  All services stopped!
echo ====================================
echo.
echo To start again, run: start.bat
echo To remove all data, run: clean.bat
echo.
pause
