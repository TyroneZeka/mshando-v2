#!/bin/bash

# Mshando Microservices Setup Script
# This script helps set up the development environment

echo "🚀 Setting up Mshando Microservices..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check Java
    if java -version 2>&1 | grep -q "17\|18\|19\|20\|21"; then
        print_success "Java 17+ is installed"
    else
        print_error "Java 17+ is required but not found"
        exit 1
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        print_success "Maven is installed"
    else
        print_error "Maven is required but not found"
        exit 1
    fi
    
    # Check Docker
    if command -v docker &> /dev/null; then
        print_success "Docker is installed"
    else
        print_error "Docker is required but not found"
        exit 1
    fi
    
    # Check Docker Compose
    if command -v docker-compose &> /dev/null; then
        print_success "Docker Compose is installed"
    else
        print_error "Docker Compose is required but not found"
        exit 1
    fi
}

# Build services
build_services() {
    print_status "Building services..."
    
    services=("eureka-server" "api-gateway" "user-service")
    
    for service in "${services[@]}"; do
        if [ -d "$service" ]; then
            print_status "Building $service..."
            cd "$service"
            
            # Try building with retry mechanism for network issues
            for attempt in 1 2 3; do
                print_status "Build attempt $attempt for $service..."
                mvn clean package -DskipTests -U
                if [ $? -eq 0 ]; then
                    print_success "$service built successfully"
                    break
                elif [ $attempt -eq 3 ]; then
                    print_error "Failed to build $service after 3 attempts"
                    exit 1
                else
                    print_warning "Build attempt $attempt failed, retrying..."
                    sleep 5
                fi
            done
            cd ..
        else
            print_warning "$service directory not found, skipping..."
        fi
    done
}

# Start infrastructure
start_infrastructure() {
    print_status "Starting infrastructure services..."
    
    # Start PostgreSQL
    print_status "Starting PostgreSQL database..."
    docker-compose up -d postgres
    
    # Wait for PostgreSQL to be ready
    print_status "Waiting for PostgreSQL to be ready..."
    sleep 10
    
    # Check if PostgreSQL is ready
    until docker-compose exec postgres pg_isready -h localhost -p 5432 -U postgres; do
        print_status "Waiting for PostgreSQL..."
        sleep 2
    done
    print_success "PostgreSQL is ready"
    
    # Start Eureka Server
    print_status "Starting Eureka Server..."
    docker-compose up -d eureka-server
    
    # Wait for Eureka to be ready
    print_status "Waiting for Eureka Server to be ready..."
    sleep 15
    
    # Check Eureka health
    until curl -s http://localhost:8761/actuator/health | grep -q "UP"; do
        print_status "Waiting for Eureka Server..."
        sleep 3
    done
    print_success "Eureka Server is ready"
}

# Start all services
start_all_services() {
    print_status "Starting all services..."
    docker-compose up -d
    
    print_status "Waiting for services to start..."
    sleep 30
    
    print_status "Checking service health..."
    
    # Check services
    services_ports=("8761:eureka-server" "8080:api-gateway" "8081:user-service")
    
    for service_port in "${services_ports[@]}"; do
        port=$(echo $service_port | cut -d: -f1)
        service=$(echo $service_port | cut -d: -f2)
        
        if curl -s http://localhost:$port/actuator/health | grep -q "UP"; then
            print_success "$service is healthy on port $port"
        else
            print_warning "$service might not be ready yet on port $port"
        fi
    done
}

# Show status
show_status() {
    print_status "Current service status:"
    docker-compose ps
    
    echo ""
    print_status "Access points:"
    echo "  📊 Eureka Dashboard: http://localhost:8761"
    echo "  🌐 API Gateway: http://localhost:8080"
    echo "  👤 User Service: http://localhost:8081"
    echo "  📖 API Documentation: http://localhost:8080/swagger-ui.html"
    echo ""
    print_status "To view logs: docker-compose logs -f [service-name]"
    print_status "To stop services: docker-compose down"
}

# Test API endpoints
test_endpoints() {
    print_status "Testing API endpoints..."
    
    # Test Eureka
    if curl -s http://localhost:8761/actuator/health | grep -q "UP"; then
        print_success "✅ Eureka Server is accessible"
    else
        print_error "❌ Eureka Server is not accessible"
    fi
    
    # Test API Gateway
    if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
        print_success "✅ API Gateway is accessible"
    else
        print_error "❌ API Gateway is not accessible"
    fi
    
    # Test User Service
    if curl -s http://localhost:8081/actuator/health | grep -q "UP"; then
        print_success "✅ User Service is accessible"
    else
        print_error "❌ User Service is not accessible"
    fi
}

# Main script execution
main() {
    echo "============================================"
    echo "🎯 Mshando TaskRabbit Clone Setup"
    echo "============================================"
    
    case "${1:-all}" in
        "check")
            check_prerequisites
            ;;
        "build")
            check_prerequisites
            build_services
            ;;
        "infra")
            check_prerequisites
            start_infrastructure
            ;;
        "start")
            check_prerequisites
            start_all_services
            ;;
        "status")
            show_status
            ;;
        "test")
            test_endpoints
            ;;
        "all")
            check_prerequisites
            build_services
            start_infrastructure
            start_all_services
            show_status
            test_endpoints
            ;;
        *)
            echo "Usage: $0 {check|build|infra|start|status|test|all}"
            echo ""
            echo "Commands:"
            echo "  check  - Check prerequisites"
            echo "  build  - Build all services"
            echo "  infra  - Start infrastructure services only"
            echo "  start  - Start all services"
            echo "  status - Show current status"
            echo "  test   - Test API endpoints"
            echo "  all    - Run complete setup (default)"
            exit 1
            ;;
    esac
    
    print_success "Setup completed! 🎉"
}

# Run main function with all arguments
main "$@"
