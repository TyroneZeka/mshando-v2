#!/bin/bash

# Simple script to start all services in the correct order
echo "🚀 Starting Mshando Microservices..."

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if PostgreSQL is running
print_info "Checking PostgreSQL..."
if docker-compose ps postgres | grep -q "Up"; then
    print_success "PostgreSQL is running"
else
    print_info "Starting PostgreSQL..."
    docker-compose up -d postgres
    sleep 10
fi

# Start Eureka Server first
print_info "Starting Eureka Server..."
cd eureka-server
java -jar target/eureka-server-1.0.0.jar --spring.profiles.active=local > ../logs/eureka.log 2>&1 &
echo $! > ../eureka.pid
cd ..
sleep 15

# Start API Gateway
print_info "Starting API Gateway..."
cd api-gateway
java -jar target/api-gateway-1.0.0.jar --spring.profiles.active=local > ../logs/gateway.log 2>&1 &
echo $! > ../gateway.pid
cd ..
sleep 10

# Start User Service
print_info "Starting User Service..."
cd user-service
java -jar target/user-service-1.0.0.jar --spring.profiles.active=local > ../logs/user.log 2>&1 &
echo $! > ../user.pid
cd ..
sleep 10

# Start Task Service
print_info "Starting Task Service..."
cd task-service
java -jar target/task-service-1.0.0.jar --spring.profiles.active=local > ../logs/task.log 2>&1 &
echo $! > ../task.pid
cd ..
sleep 10

# Start Bidding Service
print_info "Starting Bidding Service..."
cd bidding-service
java -jar target/bidding-service-1.0.0.jar --spring.profiles.active=local > ../logs/bidding.log 2>&1 &
echo $! > ../bidding.pid
cd ..
sleep 10

# Start Payment Service
print_info "Starting Payment Service..."
cd payment-service
java -jar target/payment-service-1.0.0.jar --spring.profiles.active=local > ../logs/payment.log 2>&1 &
echo $! > ../payment.pid
cd ..
sleep 10

print_success "All services started!"
print_info "Access points:"
echo "  📊 Eureka Dashboard: http://localhost:8761"
echo "  🌐 API Gateway: http://localhost:8080"
echo "  👤 User Service: http://localhost:8081"
echo "  📋 Task Service: http://localhost:8082"
echo "  💰 Bidding Service: http://localhost:8083"
echo "  💳 Payment Service: http://localhost:8084"
echo ""
print_info "To view logs: tail -f logs/[service].log"
print_info "To stop all services: ./stop-all.sh"
