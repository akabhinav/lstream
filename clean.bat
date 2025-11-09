@echo off
echo ====================================
echo  LStream - Clean All Data
echo  WARNING: This will delete all data!
echo ====================================
echo.

set /p confirm="Are you sure you want to delete all data? (yes/no): "
if /i not "%confirm%"=="yes" (
    echo Cancelled.
    pause
    exit /b 0
)

echo.
echo Stopping all services...
docker-compose down -v

echo.
echo Removing Docker images...
docker-compose down --rmi all

echo.
echo Cleaning Docker system...
docker system prune -f

echo.
echo ====================================
echo  All data cleaned!
echo ====================================
echo.
echo To start fresh, run: start.bat
echo.
pause
