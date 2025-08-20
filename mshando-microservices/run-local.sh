#!/bin/bash

# Local Development Script - Run services without Docker
echo "🏠 Running Mshando Microservices Locally..."

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_status() {
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

# Check if PostgreSQL is running (Docker or local)
check_postgres() {
    print_status "Checking PostgreSQL..."
    
    # Try Docker first
    if docker-compose ps postgres | grep -q "Up"; then
        print_success "PostgreSQL is running in Docker"
        return 0
    fi
    
    # Try local PostgreSQL
    if pg_isready -h localhost -p 5432 -U postgres 2>/dev/null; then
        print_success "PostgreSQL is running locally"
        return 0
    fi
    
    print_error "PostgreSQL is not running. Starting with Docker..."
    docker-compose up -d postgres
    sleep 10
    
    if docker-compose ps postgres | grep -q "Up"; then
        print_success "PostgreSQL started in Docker"
        return 0
    else
        print_error "Failed to start PostgreSQL"
        return 1
    fi
}

# Function to run a service
run_service() {
    local service_name=$1
    local service_port=$2
    local service_dir=$3
    
    print_status "Starting $service_name on port $service_port..."
    
    if [ ! -d "$service_dir" ]; then
        print_error "$service_dir directory not found"
        return 1
    fi
    
    cd "$service_dir"
    
    if [ ! -f "target/${service_dir}-1.0.0.jar" ]; then
        print_warning "JAR file not found, building $service_name..."
        mvn clean package -DskipTests
    fi
    
    print_status "Starting $service_name..."
    java -jar "target/${service_dir}-1.0.0.jar" --spring.profiles.active=local &
    
    local pid=$!
    echo $pid > "../${service_name}.pid"
    
    print_success "$service_name started with PID $pid"
    cd ..
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    print_status "Waiting for $service_name to be ready on port $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:$port/actuator/health | grep -q "UP"; then
            print_success "$service_name is ready!"
            return 0
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            print_error "$service_name failed to start after $max_attempts attempts"
            return 1
        fi
        
        sleep 2
        attempt=$((attempt + 1))
    done
}

# Function to stop services
stop_services() {
    print_status "Stopping services..."
    
    for pidfile in *.pid; do
        if [ -f "$pidfile" ]; then
            local pid=$(cat "$pidfile")
            local service_name=$(basename "$pidfile" .pid)
            
            if kill -0 $pid 2>/dev/null; then
                print_status "Stopping $service_name (PID: $pid)..."
                kill $pid
                rm "$pidfile"
            fi
        fi
    done
    
    print_success "All services stopped"
}

# Function to show service status
show_local_status() {
    print_status "Local Service Status:"
    echo ""
    
    services=("eureka-server:8761" "api-gateway:8080" "user-service:8081" "task-service:8082")
    
    for service_port in "${services[@]}"; do
        local service=$(echo $service_port | cut -d: -f1)
        local port=$(echo $service_port | cut -d: -f2)
        
        if [ -f "${service}.pid" ]; then
            local pid=$(cat "${service}.pid")
            if kill -0 $pid 2>/dev/null; then
                if curl -s http://localhost:$port/actuator/health | grep -q "UP"; then
                    print_success "✅ $service is running and healthy (PID: $pid, Port: $port)"
                else
                    print_warning "⚠️  $service is running but not healthy (PID: $pid, Port: $port)"
                fi
            else
                print_error "❌ $service PID file exists but process is dead (Port: $port)"
                rm "${service}.pid"
            fi
        else
            print_error "❌ $service is not running (Port: $port)"
        fi
    done
    
    echo ""
    print_status "Access Points:"
    echo "  📊 Eureka Dashboard: http://localhost:8761"
    echo "  🌐 API Gateway: http://localhost:8080"
    echo "  👤 User Service: http://localhost:8081"
    echo "  � Task Service: http://localhost:8082"
    echo "  �📖 API Documentation: http://localhost:8080/swagger-ui.html"
    echo "  📖 Task Service API: http://localhost:8082/swagger-ui/index.html"
}

# Main execution
case "${1:-start}" in
    "start")
        print_status "Starting services locally..."
        
        # Check PostgreSQL
        if ! check_postgres; then
            exit 1
        fi
        
        # Start services in order
        run_service "eureka-server" 8761 "eureka-server"
        wait_for_service "eureka-server" 8761
        
        run_service "api-gateway" 8080 "api-gateway"
        wait_for_service "api-gateway" 8080
        
        run_service "user-service" 8081 "user-service"
        wait_for_service "user-service" 8081
        
        run_service "task-service" 8082 "task-service"
        wait_for_service "task-service" 8082
        
        show_local_status
        print_success "All services started successfully! 🎉"
        ;;
        
    "stop")
        stop_services
        ;;
        
    "status")
        show_local_status
        ;;
        
    "restart")
        stop_services
        sleep 3
        $0 start
        ;;
        
    *)
        echo "Usage: $0 {start|stop|status|restart}"
        echo ""
        echo "Local Development Commands:"
        echo "  start   - Start all services locally"
        echo "  stop    - Stop all services"
        echo "  status  - Show service status"
        echo "  restart - Restart all services"
        ;;
esac
