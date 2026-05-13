@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul
cd /d "%~dp0"

set "OUT=target\classes"
if not exist "%OUT%" mkdir "%OUT%"

set "FILES="
for /r "src\main\java" %%f in (*.java) do set "FILES=!FILES! "%%f""

echo Java kaynaklari derleniyor...
javac --release 17 -encoding UTF-8 -d "%OUT%" !FILES!
set ERR=!ERRORLEVEL!

if !ERR! neq 0 (
    echo.
    echo [HATA] Derleme basarisiz. JDK 17+ kurulu oldugundan emin olun: javac -version
    pause
    exit /b 1
)

echo.
echo [OK] Derleme tamam. Siniflar: %OUT%
echo Sunucu: 2-sunucu.bat   Istemci: 3-istemci.bat
echo.
pause
