@echo off
REM =====================================================
REM Script cai dat moi truong cho du an
REM Quan ly Mua sam Tai lieu Thu vien
REM =====================================================

REM Kiem tra neu dang chay tu double-click
echo %CMDCMDLINE% | findstr /i "%~0" >nul
if %errorlevel% equ 0 set DOUBLECLICKED=1

chcp 65001 >nul
setlocal enabledelayedexpansion

echo ======================================
echo  SETUP MOI TRUONG DU AN THU VIEN
echo ======================================
echo.

REM Kiem tra Java
echo [1/3] Kiem tra Java 17...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo X Java chua duoc cai dat
    echo.
    echo   Vui long tai va cai dat Java 17:
    echo   https://adoptium.net/temurin/releases/?version=17
    echo.
    echo   Hoac dung Chocolatey:
    echo   choco install temurin17
    echo.
    goto :error
) else (
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VERSION=%%g
        set JAVA_VERSION=!JAVA_VERSION:"=!
    )
    echo   Java version: !JAVA_VERSION!
    echo   JAVA_HOME: %JAVA_HOME%
)

REM Kiem tra Maven
echo.
echo [2/3] Kiem tra Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo X Maven chua duoc cai dat
    echo.
    echo   Vui long tai va cai dat Maven:
    echo   https://maven.apache.org/download.cgi
    echo.
    echo   Hoac dung Chocolatey:
    echo   choco install maven
    echo.
    pause
    goto :error
)

for /f "tokens=*" %%g in ('mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do (
    echo   %%g
)

REM Kiem tra MySQL
echo.
echo [3/3] Kiem tra MySQL...
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo ! MySQL CLI khong co trong PATH, nhung co the da cai dat
    echo   Kiem tra: MySQL Workbench co ket noi duoc khong?
    echo   Neu da ket noi duoc thi bo qua buoc nay
) else (
    mysql --version
)

echo.
echo ======================================
echo  SETUP XONG! BAT DAU CHAY UNG DUNG
echo ======================================
echo.
echo Dang nhap: admin / admin123
echo.
echo Dang compile va chay chuong trinh...
echo.

cd "%~dp0"
mvn clean compile exec:java

if %errorlevel% neq 0 (
    echo.
    echo ======================================
    echo  LOI: Khong the chay ung dung
    echo ======================================
    echo.
    pause
    exit /b 1
)

exit /b 0

:error
echo.
echo ======================================
echo  LOI: Thieu cong cu can thiet
echo ======================================
echo.
echo Cach cai dat nhanh bang Chocolatey:
echo   1. Mo PowerShell (Admin) va chay:
echo      Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
echo.
echo   2. Sau do chay:
echo      choco install temurin17 maven mysql -y
echo.
echo   3. Khoi dong lai terminal va chay lai setup.bat
echo.
if defined DOUBLECLICKED (
    echo Nhan phim bat ky de dong cua so...
    pause >nul
)
pause
exit /b 1
