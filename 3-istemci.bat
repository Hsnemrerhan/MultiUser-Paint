@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist "target\classes\com\multipaint\client\PaintClientApp.class" (
    echo Once 1-derle.bat dosyasini calistirin.
    pause
    exit /b 1
)

echo Istemci aciliyor...
java -cp "target\classes" com.multipaint.client.PaintClientApp
if errorlevel 1 (
    echo.
    echo [HATA] java calistirilamadi. JDK kurulu mu kontrol edin: java -version
    pause
)
