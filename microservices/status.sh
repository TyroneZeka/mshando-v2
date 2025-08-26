#!/bin/bash

# Script to check the status of all services
echo "🔍 Checking Mshando Microservices Status..."

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

echo ""
print_info "Service Status Check:"
echo "=========================="

# Check PostgreSQL
if docker-compose ps postgres | grep -q "Up"; then
    print_success "✅ PostgreSQL is running (Docker)"
else
    print_error "❌ PostgreSQL is not running"
fi

# Check services
services=("eureka:8761:Eureka Server" "gateway:8080:API Gateway" "user:8081:User Service" "task:8082:Task Service" "bidding:8083:Bidding Service" "payment:8084:Payment Service" "notification:8085:Notification Service")

for service_info in "${services[@]}"; do
    IFS=':' read -r service port name <<< "$service_info"
    
    if [ -f "${service}.pid" ]; then
        pid=$(cat "${service}.pid")
        if kill -0 $pid 2>/dev/null; then
            # Check if service is responding
            if curl -s "http://localhost:$port/actuator/health" | grep -q '"status"'; then
                print_success "✅ $name is running and healthy (PID: $pid, Port: $port)"
            else
                print_warning "⚠️  $name is running but not responding (PID: $pid, Port: $port)"
            fi
        else
            print_error "❌ $name PID file exists but process is dead (Port: $port)"
            rm "${service}.pid"
        fi
    else
        print_error "❌ $name is not running (Port: $port)"
    fi
done

echo ""
print_info "Access Points:"
echo "==============="
echo "  📊 Eureka Dashboard: http://localhost:8761"
echo "  🌐 API Gateway: http://localhost:8080"
echo "  👤 User Service: http://localhost:8081"
echo "  📋 Task Service: http://localhost:8082"
echo "  💰 Bidding Service: http://localhost:8083"
echo "  💳 Payment Service: http://localhost:8084"
echo "  📧 Notification Service: http://localhost:8085"
echo ""
echo "  📖 API Documentation:"
echo "    - API Gateway: http://localhost:8080/swagger-ui.html"
echo "    - User Service: http://localhost:8081/swagger-ui.html"
echo "    - Task Service: http://localhost:8082/swagger-ui/index.html"
echo "    - Bidding Service: http://localhost:8083/swagger-ui.html"
echo "    - Payment Service: http://localhost:8084/swagger-ui.html"
echo "    - Notification Service: http://localhost:8085/swagger-ui.html"
echo ""
print_info "Logs: tail -f logs/[service].log"
print_info "Stop: ./stop-all.sh"
