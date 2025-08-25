#!/bin/bash

# Build script for all microservices
echo "🏗️  Building Mshando Microservices..."

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

# Function to build a service
build_service() {
    local service_name=$1
    local service_dir=$2
    
    if [ ! -d "$service_dir" ]; then
        print_error "$service_dir directory not found"
        return 1
    fi
    
    print_status "Building $service_name..."
    
    cd "$service_dir"
    
    # Clean and build with retry mechanism
    for attempt in 1 2 3; do
        print_status "Build attempt $attempt for $service_name..."
        mvn clean package -DskipTests -U
        
        if [ $? -eq 0 ]; then
            print_success "$service_name built successfully"
            cd ..
            return 0
        elif [ $attempt -eq 3 ]; then
            print_error "Failed to build $service_name after 3 attempts"
            cd ..
            return 1
        else
            print_warning "Build attempt $attempt failed for $service_name, retrying..."
            sleep 5
        fi
    done
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

# Clean previous builds
clean_previous_builds() {
    print_status "Cleaning previous builds..."
    
    # Stop and remove containers
    docker-compose down -v --remove-orphans 2>/dev/null || true
    
    # Remove old images
    docker-compose down --rmi all 2>/dev/null || true
    
    # Clean Docker system (optional)
    # docker system prune -f
    
    print_success "Previous builds cleaned"
}

# Build all services
build_all_services() {
    print_status "Building all services..."
    
    # List of services to build
    services=(
        "eureka-server:eureka-server"
        "api-gateway:api-gateway" 
        "user-service:user-service"
        "task-service:task-service"
        "bidding-service:bidding-service"
        "payment-service:payment-service"
        "notification-service:notification-service"
    )
    
    local failed_services=()
    
    for service_info in "${services[@]}"; do
        local service_name=$(echo $service_info | cut -d: -f1)
        local service_dir=$(echo $service_info | cut -d: -f2)
        
        if ! build_service "$service_name" "$service_dir"; then
            failed_services+=("$service_name")
        fi
    done
    
    if [ ${#failed_services[@]} -eq 0 ]; then
        print_success "All services built successfully!"
        return 0
    else
        print_error "Failed to build services: ${failed_services[*]}"
        return 1
    fi
}

# Build Docker images
build_docker_images() {
    print_status "Building Docker images..."
    
    # Build images without starting containers
    if docker-compose build --no-cache; then
        print_success "Docker images built successfully"
        return 0
    else
        print_error "Failed to build Docker images"
        return 1
    fi
}

# Verify JAR files exist
verify_jar_files() {
    print_status "Verifying JAR files..."
    
    services=("eureka-server" "api-gateway" "user-service" "task-service" "bidding-service" "payment-service")
    
    for service in "${services[@]}"; do
        jar_file="$service/target/$service-1.0.0.jar"
        if [ -f "$jar_file" ]; then
            print_success "✅ $jar_file exists"
        else
            print_error "❌ $jar_file not found"
            return 1
        fi
    done
    
    print_success "All JAR files verified"
    return 0
}

# Main build process
main() {
    case "${1:-all}" in
        "clean")
            clean_previous_builds
            ;;
        "check")
            check_prerequisites
            ;;
        "build")
            check_prerequisites
            build_all_services
            ;;
        "docker")
            verify_jar_files
            build_docker_images
            ;;
        "verify")
            verify_jar_files
            ;;
        "all")
            check_prerequisites
            clean_previous_builds
            build_all_services
            verify_jar_files
            build_docker_images
            print_success "🎉 Build process completed successfully!"
            print_status "Ready to run: docker-compose up -d"
            ;;
        *)
            echo "Usage: $0 {clean|check|build|docker|verify|all}"
            echo ""
            echo "Commands:"
            echo "  clean  - Clean previous builds and containers"
            echo "  check  - Check prerequisites"
            echo "  build  - Build all services (Maven)"
            echo "  docker - Build Docker images"
            echo "  verify - Verify JAR files exist"
            echo "  all    - Run complete build process (default)"
            exit 1
            ;;
    esac
}

# Run with all arguments
main "$@"
