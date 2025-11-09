@echo off
setlocal EnableDelayedExpansion

echo ====================================
echo  LStream - API Testing Script
echo ====================================
echo.

set BASE_URL=http://localhost:8080

echo Testing if server is running...
curl -s -o nul -w "%%{http_code}" %BASE_URL%/actuator/health > temp.txt
set /p STATUS=<temp.txt
del temp.txt

if not "%STATUS%"=="200" (
    echo ERROR: Server is not running or not healthy!
    echo Please start the server with start.bat first.
    pause
    exit /b 1
)

echo Server is running!
echo.

echo ====================================
echo  1. Register a new user
echo ====================================
echo.

curl -X POST %BASE_URL%/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"teststreamer\",\"email\":\"test@example.com\",\"password\":\"password123\",\"displayName\":\"Test Streamer\"}"

echo.
echo.
echo ====================================
echo  2. Login and get token
echo ====================================
echo.

curl -X POST %BASE_URL%/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"teststreamer\",\"password\":\"password123\"}" > token_response.json

echo.
echo Token saved to token_response.json
echo.

echo ====================================
echo  3. Get current user info
echo ====================================
echo.
echo NOTE: Copy the token from token_response.json and use it in the next commands
echo Example: "Authorization: Bearer eyJhbGc..."
echo.

echo ====================================
echo  4. Get live streams
echo ====================================
echo.

curl -X GET %BASE_URL%/api/streams/live

echo.
echo.
echo ====================================
echo  Testing complete!
echo ====================================
echo.
echo To create a stream, use the token from token_response.json
echo.
echo Example:
echo curl -X POST %BASE_URL%/api/streams ^
echo   -H "Content-Type: application/json" ^
echo   -H "Authorization: Bearer YOUR_TOKEN" ^
echo   -d "{\"title\":\"My Stream\",\"description\":\"Test\",\"category\":\"Gaming\",\"privacy\":\"PUBLIC\"}"
echo.
pause
