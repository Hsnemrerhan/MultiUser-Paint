@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist "target\classes\com\multipaint\server\PaintServer.class" (
    echo Once 1-derle.bat dosyasini calistirin.
    pause
    exit /b 1
)

set "PORT=9100"
if not "%~1"=="" set "PORT=%~1"

echo Sunucu baslatiliyor (port %PORT%)...
echo Durdurmak icin bu pencerede Ctrl+C basin.
echo.
java -cp "target\classes" com.multipaint.server.PaintServer %PORT%
echo.
pause
