@echo off
REM Mshando Microservices Setup Script for Windows
REM This script helps set up the development environment

echo 🚀 Setting up Mshando Microservices...

REM Function to check if command exists
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java is required but not found
    exit /b 1
) else (
    echo [SUCCESS] Java is installed
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven is required but not found
    exit /b 1
) else (
    echo [SUCCESS] Maven is installed
)

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker is required but not found
    exit /b 1
) else (
    echo [SUCCESS] Docker is installed
)

where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose is required but not found
    exit /b 1
) else (
    echo [SUCCESS] Docker Compose is installed
)

echo [INFO] Building services...

REM Build Eureka Server
if exist "eureka-server" (
    echo [INFO] Building eureka-server...
    cd eureka-server
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo [ERROR] Failed to build eureka-server
        exit /b 1
    )
    cd ..
    echo [SUCCESS] eureka-server built successfully
)

REM Build API Gateway
if exist "api-gateway" (
    echo [INFO] Building api-gateway...
    cd api-gateway
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo [ERROR] Failed to build api-gateway
        exit /b 1
    )
    cd ..
    echo [SUCCESS] api-gateway built successfully
)

REM Build User Service
if exist "user-service" (
    echo [INFO] Building user-service...
    cd user-service
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo [ERROR] Failed to build user-service
        exit /b 1
    )
    cd ..
    echo [SUCCESS] user-service built successfully
)

echo [INFO] Starting infrastructure services...

REM Start PostgreSQL
echo [INFO] Starting PostgreSQL database...
docker-compose up -d postgres

REM Wait for PostgreSQL
echo [INFO] Waiting for PostgreSQL to be ready...
timeout /t 10 /nobreak >nul

echo [INFO] Starting Eureka Server...
docker-compose up -d eureka-server

REM Wait for Eureka
echo [INFO] Waiting for Eureka Server to be ready...
timeout /t 15 /nobreak >nul

echo [INFO] Starting all services...
docker-compose up -d

echo [INFO] Waiting for services to start...
timeout /t 30 /nobreak >nul

echo [INFO] Setup completed! 🎉
echo.
echo 📊 Eureka Dashboard: http://localhost:8761
echo 🌐 API Gateway: http://localhost:8080
echo 👤 User Service: http://localhost:8081
echo 📖 API Documentation: http://localhost:8080/swagger-ui.html
echo.
echo To view logs: docker-compose logs -f [service-name]
echo To stop services: docker-compose down

pause
