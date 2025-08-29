@echo off
setlocal enabledelayedexpansion

REM Mshando Port Debugger for Windows
REM This script helps debug and manage port conflicts for microservices

title Mshando Port Debugger

echo.
echo =================================
echo    Mshando Port Debugger
echo =================================
echo.

REM Define ports
set "ports[eureka]=8761"
set "ports[gateway]=8080"
set "ports[user]=8081"
set "ports[task]=8082"
set "ports[bidding]=8083"
set "ports[payment]=8084"
set "ports[notification]=8085"
set "ports[postgres]=5432"
set "ports[frontend]=5173"

REM Function to check if port is in use
:check_port
set port=%1
set service=%2

echo.
echo Checking %service% (Port %port%)
echo ----------------------------------------

netstat -ano | findstr ":%port%" | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo [WARNING] Port %port% is in use
    
    REM Get process ID
    for /f "tokens=5" %%i in ('netstat -ano ^| findstr ":%port%" ^| findstr "LISTENING"') do (
        set pid=%%i
        echo Process ID: !pid!
        
        REM Get process name
        for /f "tokens=1" %%j in ('tasklist ^| findstr "!pid!"') do (
            echo Process Name: %%j
        )
        
        echo To kill this process: taskkill /PID !pid! /F
        goto :eof
    )
) else (
    echo [OK] Port %port% is available
)
goto :eof

REM Function to kill process on port
:kill_port
set port=%1
echo.
echo Killing process on port %port%...

for /f "tokens=5" %%i in ('netstat -ano ^| findstr ":%port%" ^| findstr "LISTENING"') do (
    set pid=%%i
    echo Killing process !pid!...
    taskkill /PID !pid! /F >nul 2>&1
    if !errorlevel!==0 (
        echo [SUCCESS] Killed process !pid!
    ) else (
        echo [ERROR] Failed to kill process !pid!
    )
)

timeout /t 2 >nul

REM Check if port is now free
netstat -ano | findstr ":%port%" | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo [ERROR] Port %port% is still in use
) else (
    echo [SUCCESS] Port %port% is now available
)
goto :eof

REM Function to show all ports
:show_all_ports
echo.
echo All Mshando Ports Status:
echo =============================

call :check_status eureka 8761
call :check_status gateway 8080
call :check_status user 8081
call :check_status task 8082
call :check_status bidding 8083
call :check_status payment 8084
call :check_status notification 8085
call :check_status postgres 5432
call :check_status frontend 5173
goto :eof

:check_status
set service=%1
set port=%2

netstat -ano | findstr ":%port%" | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo %service% ^(%port%^): [IN USE]
) else (
    echo %service% ^(%port%^): [FREE]
)
goto :eof

REM Function to kill all Mshando processes
:kill_all
echo.
echo [WARNING] This will kill all Mshando services!
set /p confirm="Are you sure? (y/N): "
if /i "!confirm!"=="y" (
    echo Killing all Mshando processes...
    call :kill_port 8761
    call :kill_port 8080
    call :kill_port 8081
    call :kill_port 8082
    call :kill_port 8083
    call :kill_port 8084
    call :kill_port 8085
    call :kill_port 5173
    echo All processes terminated.
) else (
    echo Operation cancelled.
)
goto :eof

REM Function to show system resources
:show_resources
echo.
echo System Resources:
echo ==================

echo Memory Usage:
wmic OS get TotalVisibleMemorySize,FreePhysicalMemory /format:list | findstr "="

echo.
echo Java Processes:
tasklist | findstr "java.exe"

echo.
echo Node.js Processes:
tasklist | findstr "node.exe"
goto :eof

REM Function to show logs
:show_logs
echo.
echo Available Service Logs:
echo =======================

if exist "logs" (
    dir /b logs\*.log
    echo.
    set /p service="Enter service name to view logs: "
    if exist "logs\!service!.log" (
        echo.
        echo === !service!.log (last 20 lines) ===
        powershell "Get-Content logs\!service!.log | Select-Object -Last 20"
    ) else (
        echo [ERROR] Log file not found: logs\!service!.log
    )
) else (
    echo [ERROR] Logs directory not found
)
goto :eof

REM Main menu
:main_menu
echo.
echo What would you like to do?
echo 1. Check all ports
echo 2. Check specific port
echo 3. Kill process on specific port
echo 4. Kill all Mshando processes
echo 5. Show system resources
echo 6. Show service logs
echo 7. Quick port overview
echo 0. Exit
echo.

set /p choice="Choose an option: "

if "%choice%"=="1" (
    call :show_all_ports
    goto :continue
)
if "%choice%"=="2" (
    set /p port="Enter port number: "
    call :check_port !port! "Custom"
    goto :continue
)
if "%choice%"=="3" (
    set /p port="Enter port number: "
    call :kill_port !port!
    goto :continue
)
if "%choice%"=="4" (
    call :kill_all
    goto :continue
)
if "%choice%"=="5" (
    call :show_resources
    goto :continue
)
if "%choice%"=="6" (
    call :show_logs
    goto :continue
)
if "%choice%"=="7" (
    echo.
    echo Quick Port Overview:
    netstat -ano | findstr ":8080\|:8081\|:8082\|:8083\|:8084\|:8085\|:8761\|:5432\|:5173" | findstr "LISTENING"
    goto :continue
)
if "%choice%"=="0" (
    echo Goodbye!
    exit /b 0
)

echo Invalid option. Please try again.

:continue
echo.
pause
call :show_all_ports
goto :main_menu
