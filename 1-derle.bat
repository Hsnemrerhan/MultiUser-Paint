@echo off
chcp 65001 >nul
cd /d "%~dp0"

where mvn >nul 2>&1
if errorlevel 1 (
    echo [HATA] gRPC derlemesi icin Maven gerekli.
    echo Kurulum: https://maven.apache.org/
    pause
    exit /b 1
)

echo gRPC protobuf kodu uretiliyor ve proje derleniyor...
call mvn -q compile dependency:copy-dependencies -DoutputDirectory=target/lib
if errorlevel 1 (
    echo.
    echo [HATA] Maven derleme basarisiz.
    pause
    exit /b 1
)

echo.
echo [OK] Derleme tamam.
echo Siniflar: target\classes
echo Kutuphaneler: target\lib
echo Sunucu: 2-sunucu.bat   Istemci: 3-istemci.bat
echo.
pause
