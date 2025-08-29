#!/bin/bash

# Mshando Deployment Manager
# Handles both Docker and local JAR deployments

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# Configuration
DEPLOYMENT_MODE=""
SERVICES_DIR=$(pwd)
LOGS_DIR="$SERVICES_DIR/logs"

# Ensure logs directory exists
mkdir -p "$LOGS_DIR"

# Service definitions
declare -A SERVICES=(
    ["eureka"]="8761"
    ["gateway"]="8080"
    ["user"]="8081"
    ["task"]="8082"
    ["bidding"]="8083"
    ["payment"]="8084"
    ["notification"]="8085"
    ["postgres"]="5432"
)

declare -A SERVICE_JARS=(
    ["eureka"]="eureka-server/target/eureka-server-1.0.0.jar"
    ["gateway"]="api-gateway/target/api-gateway-1.0.0.jar"
    ["user"]="user-service/target/user-service-1.0.0.jar"
    ["task"]="task-service/target/task-service-1.0.0.jar"
    ["bidding"]="bidding-service/target/bidding-service-1.0.0.jar"
    ["payment"]="payment-service/target/payment-service-1.0.0.jar"
    ["notification"]="notification-service/target/notification-service-1.0.0.jar"
)

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

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_header() {
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}========================================${NC}"
}

# Function to check if port is in use
is_port_in_use() {
    local port=$1
    
    # Windows/Git Bash environment
    if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || command -v netstat.exe >/dev/null 2>&1; then
        netstat -an | grep -q ":$port.*LISTENING"
    elif command -v ss >/dev/null 2>&1; then
        ss -ln | grep -q ":$port "
    elif command -v netstat >/dev/null 2>&1; then
        netstat -ln | grep -q ":$port "
    else
        # Fallback using /proc/net/tcp (Linux)
        if [ -f /proc/net/tcp ]; then
            local hex_port=$(printf "%04X" $port)
            grep -q ":$hex_port " /proc/net/tcp
        else
            return 1
        fi
    fi
}

# Function to get process using port
get_port_process() {
    local port=$1
    
    # Windows/Git Bash environment
    if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || command -v netstat.exe >/dev/null 2>&1; then
        netstat -ano | grep ":$port.*LISTENING" | awk '{print $5}' | head -1
    elif command -v ss >/dev/null 2>&1; then
        ss -lpn | grep ":$port " | awk '{print $7}' | head -1
    elif command -v netstat >/dev/null 2>&1; then
        netstat -lpn 2>/dev/null | grep ":$port " | awk '{print $7}' | head -1
    else
        echo "unknown"
    fi
}

# Function to check port conflicts
check_port_conflicts() {
    local deployment_type=$1
    local conflicts=0
    
    print_info "Checking port conflicts for $deployment_type deployment..."
    
    for service in "${!SERVICES[@]}"; do
        local port=${SERVICES[$service]}
        if is_port_in_use $port; then
            local process=$(get_port_process $port)
            print_warning "Port $port ($service) is in use by: $process"
            conflicts=$((conflicts + 1))
        fi
    done
    
    if [ $conflicts -gt 0 ]; then
        print_error "Found $conflicts port conflicts!"
        return 1
    else
        print_success "No port conflicts detected"
        return 0
    fi
}

# Function to stop all Docker containers
stop_docker_deployment() {
    print_info "Stopping Docker deployment..."
    
    if docker-compose ps -q | grep -q .; then
        docker-compose down
        print_success "Docker containers stopped"
    else
        print_info "No Docker containers running"
    fi
}

# Function to stop all local services
stop_local_deployment() {
    print_info "Stopping local deployment..."
    
    # Kill processes using PID files
    for service in "${!SERVICES[@]}"; do
        if [ "$service" = "postgres" ]; then
            continue  # Skip postgres for local deployment
        fi
        
        local pid_file="$service.pid"
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                print_info "Stopping $service (PID: $pid)..."
                kill "$pid"
                rm -f "$pid_file"
            else
                print_warning "$service PID file exists but process not running"
                rm -f "$pid_file"
            fi
        fi
    done
    
    # Force kill any remaining Java processes on our ports
    for service in "${!SERVICES[@]}"; do
        if [ "$service" = "postgres" ]; then
            continue
        fi
        
        local port=${SERVICES[$service]}
        if is_port_in_use $port; then
            print_warning "Force killing process on port $port..."
            local pid=$(netstat -lpn 2>/dev/null | grep ":$port " | awk '{print $7}' | cut -d'/' -f1 | head -1)
            if [ -n "$pid" ] && [ "$pid" != "-" ]; then
                kill -9 "$pid" 2>/dev/null || true
            fi
        fi
    done
    
    print_success "Local services stopped"
}

# Function to check if build is needed
check_build_needed() {
    local missing_jars=0
    local missing_services=()
    
    for service in "${!SERVICE_JARS[@]}"; do
        local jar_path="${SERVICE_JARS[$service]}"
        if [ ! -f "$jar_path" ]; then
            missing_jars=$((missing_jars + 1))
            missing_services+=("$service")
        fi
    done
    
    if [ $missing_jars -gt 0 ]; then
        print_warning "Missing JAR files for: ${missing_services[*]}"
        return 0  # Build needed
    else
        print_success "All JAR files found - skipping build for faster startup"
        return 1  # Build not needed
    fi
}

# Function to build all services
build_services() {
    local force_build=${1:-false}
    
    # Check if build is needed unless forced
    if [ "$force_build" != "true" ]; then
        if check_build_needed; then
            print_info "Building missing services..."
        else
            print_info "⚡ Fast startup: All JAR files exist, skipping build"
            print_info "💡 Use './deploy.sh build' to force rebuild if needed"
            return 0
        fi
    else
        print_info "🔨 Force building all services..."
    fi
    
    # Define service directories
    local service_dirs=("eureka-server" "api-gateway" "user-service" "task-service" "bidding-service" "payment-service" "notification-service")
    
    # Check if we have individual service directories with pom.xml
    local services_found=0
    for service_dir in "${service_dirs[@]}"; do
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" ]; then
            services_found=$((services_found + 1))
        fi
    done
    
    if [ $services_found -eq 0 ]; then
        print_error "No Maven services found! Expected directories: ${service_dirs[*]}"
        return 1
    fi
    
    print_info "Found $services_found services to build"
    
    # If not force build, only build services with missing JARs
    local services_to_build=()
    if [ "$force_build" = "true" ]; then
        services_to_build=("${service_dirs[@]}")
    else
        # Only build services with missing JARs
        for service_dir in "${service_dirs[@]}"; do
            local service_key=""
            case "$service_dir" in
                "eureka-server") service_key="eureka" ;;
                "api-gateway") service_key="gateway" ;;
                "user-service") service_key="user" ;;
                "task-service") service_key="task" ;;
                "bidding-service") service_key="bidding" ;;
                "payment-service") service_key="payment" ;;
                "notification-service") service_key="notification" ;;
            esac
            
            if [ -n "$service_key" ] && [ ! -f "${SERVICE_JARS[$service_key]}" ]; then
                services_to_build+=("$service_dir")
            fi
        done
    fi
    
    if [ ${#services_to_build[@]} -eq 0 ]; then
        print_success "No services need building"
        return 0
    fi
    
    print_info "Building ${#services_to_build[@]} services: ${services_to_build[*]}"
    
    # Build each required service
    for service_dir in "${services_to_build[@]}"; do
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" ]; then
            print_info "🔨 Building $service_dir..."
            cd "$service_dir"
            
            if mvn clean package -DskipTests -q; then
                print_success "✅ $service_dir built successfully"
            else
                print_error "❌ Failed to build $service_dir"
                cd ..
                return 1
            fi
            
            cd ..
        else
            print_warning "$service_dir not found or missing pom.xml"
        fi
    done
    
    # Verify all JARs exist after build
    local missing_jars=0
    for service in "${!SERVICE_JARS[@]}"; do
        local jar_path="${SERVICE_JARS[$service]}"
        if [ ! -f "$jar_path" ]; then
            print_error "JAR still missing: $jar_path"
            missing_jars=$((missing_jars + 1))
        fi
    done
    
    if [ $missing_jars -gt 0 ]; then
        print_error "$missing_jars JAR files still missing after build"
        return 1
    fi
    
    print_success "✅ All required services built successfully"
}

# Function to start Docker deployment
start_docker_deployment() {
    print_header "Starting Docker Deployment"
    
    # Check if .env file exists
    if [ ! -f ".env" ]; then
        print_error ".env file not found!"
        print_info "Please create .env file with required environment variables"
        return 1
    fi
    
    # Check for port conflicts
    if ! check_port_conflicts "Docker"; then
        print_error "Please resolve port conflicts before starting Docker deployment"
        return 1
    fi
    
    # Start services
    print_info "Starting Docker containers..."
    docker-compose up -d
    
    # Wait for services to start
    print_info "Waiting for services to start..."
    sleep 30
    
    # Check health
    check_docker_health
}

# Function to start local deployment
start_local_deployment() {
    print_header "Starting Local Deployment"
    
    # Check for port conflicts
    if ! check_port_conflicts "Local"; then
        print_error "Please resolve port conflicts before starting local deployment"
        return 1
    fi
    
    # Build services first
    build_services
    
    # Start PostgreSQL via Docker (required for local deployment)
    print_info "Starting PostgreSQL database..."
    docker-compose up -d postgres
    sleep 10
    
    # Start services in order
    local startup_order=("eureka" "gateway" "user" "task" "bidding" "payment" "notification")
    
    for service in "${startup_order[@]}"; do
        start_local_service "$service"
        sleep 15  # Wait between services
    done
    
    print_success "All local services started!"
    print_local_endpoints
}

# Function to start individual local service
start_local_service() {
    local service=$1
    local jar_path="${SERVICE_JARS[$service]}"
    local log_file="$LOGS_DIR/$service.log"
    local pid_file="$service.pid"
    
    print_info "Starting $service service..."
    
    if [ ! -f "$jar_path" ]; then
        print_error "JAR not found: $jar_path"
        return 1
    fi
    
    # Start the service
    nohup java -jar "$jar_path" --spring.profiles.active=local > "$log_file" 2>&1 &
    local pid=$!
    echo $pid > "$pid_file"
    
    print_success "$service started (PID: $pid)"
}

# Function to check Docker health
check_docker_health() {
    print_info "Checking Docker container health..."
    
    local containers=$(docker-compose ps --format "table {{.Service}}\t{{.Status}}")
    echo "$containers"
    
    # Check for unhealthy containers
    local unhealthy=$(docker-compose ps | grep "unhealthy" | wc -l)
    if [ $unhealthy -gt 0 ]; then
        print_warning "$unhealthy containers are unhealthy"
    fi
}

# Function to show local endpoints
print_local_endpoints() {
    print_info "Local service endpoints:"
    echo "  📊 Eureka Dashboard: http://localhost:8761"
    echo "  🌐 API Gateway: http://localhost:8080"
    echo "  👤 User Service: http://localhost:8081"
    echo "  📋 Task Service: http://localhost:8082"
    echo "  💰 Bidding Service: http://localhost:8083"
    echo "  💳 Payment Service: http://localhost:8084"
    echo "  📧 Notification Service: http://localhost:8085"
    echo ""
    print_info "To view logs: tail -f logs/[service].log"
}

# Function to show status
show_status() {
    print_header "Deployment Status"
    
    # Check Docker containers
    print_info "Docker containers:"
    if docker-compose ps -q | grep -q .; then
        docker-compose ps
    else
        echo "  No Docker containers running"
    fi
    
    echo ""
    
    # Check local services
    print_info "Local services:"
    for service in "${!SERVICES[@]}"; do
        if [ "$service" = "postgres" ]; then
            continue
        fi
        
        local port=${SERVICES[$service]}
        local pid_file="$service.pid"
        
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                echo "  ✅ $service (PID: $pid, Port: $port)"
            else
                echo "  ❌ $service (PID file exists but process dead)"
            fi
        else
            if is_port_in_use $port; then
                echo "  ⚠️  $service (Port $port in use, no PID file)"
            else
                echo "  ❌ $service (Not running)"
            fi
        fi
    done
}

# Function to show JAR status
show_jar_status() {
    print_header "JAR Files Status"
    
    local total_jars=0
    local existing_jars=0
    local missing_jars=()
    
    for service in "${!SERVICE_JARS[@]}"; do
        local jar_path="${SERVICE_JARS[$service]}"
        total_jars=$((total_jars + 1))
        
        if [ -f "$jar_path" ]; then
            existing_jars=$((existing_jars + 1))
            local jar_size=$(ls -lh "$jar_path" | awk '{print $5}')
            local jar_date=$(ls -l "$jar_path" | awk '{print $6, $7, $8}')
            echo "  ✅ $service: $jar_size ($jar_date)"
        else
            missing_jars+=("$service")
            echo "  ❌ $service: JAR not found"
        fi
    done
    
    echo ""
    print_info "Summary: $existing_jars/$total_jars JAR files built"
    
    if [ ${#missing_jars[@]} -gt 0 ]; then
        print_warning "Missing JARs for: ${missing_jars[*]}"
        print_info "Run './deploy.sh build' to build missing services"
    else
        print_success "All JAR files are available for fast startup!"
    fi
}

# Function to show logs
show_logs() {
    local service=$1
    local lines=${2:-50}
    
    if [ -z "$service" ]; then
        print_error "Please specify a service name"
        echo "Available services: ${!SERVICES[*]}"
        return 1
    fi
    
    local log_file="$LOGS_DIR/$service.log"
    
    if [ -f "$log_file" ]; then
        print_info "Last $lines lines of $service log:"
        tail -n "$lines" "$log_file"
    else
        print_error "Log file not found: $log_file"
    fi
}

# Function to show help
show_help() {
    print_header "Mshando Deployment Manager"
    echo ""
    echo -e "${CYAN}USAGE:${NC}"
    echo "  $0 <command> [options]"
    echo ""
    echo -e "${CYAN}QUICK START:${NC}"
    echo -e "  ${GREEN}$0 local-start${NC}     # 🚀 Start for development (recommended)"
    echo -e "  ${GREEN}$0 docker-start${NC}    # 🐳 Start for testing (production-like)"
    echo -e "  ${GREEN}$0 status${NC}          # 📊 Check what's running"
    echo -e "  ${GREEN}$0 stop${NC}            # 🛑 Stop everything"
    echo ""
    echo -e "${CYAN}DEPLOYMENT COMMANDS:${NC}"
    echo "  docker-start    🐳 Start all services using Docker Compose"
    echo "  local-start     🚀 Start all services as local JAR processes"
    echo "  stop            🛑 Stop all services (both Docker and local)"
    echo "  restart         🔄 Stop and restart current deployment method"
    echo ""
    echo -e "${CYAN}MONITORING COMMANDS:${NC}"
    echo "  status          📊 Show current deployment status"
    echo "  logs <service>  📋 Show logs for specific service"
    echo "  jar-status      📦 Show JAR files status and build information"
    echo "  ports           🔍 Check port usage with interactive debugger"
    echo ""
    echo -e "${CYAN}BUILD COMMANDS:${NC}"
    echo "  build           🔨 Build all services (force rebuild)"
    echo "  clean           🧹 Clean up all deployments and Docker images"
    echo ""
    echo -e "${CYAN}AVAILABLE SERVICES:${NC}"
    echo "  eureka, gateway, user, task, bidding, payment, notification"
    echo ""
    echo -e "${CYAN}EXAMPLES:${NC}"
    echo "  $0 docker-start        # Start using Docker (recommended for testing)"
    echo "  $0 local-start         # Start using local JARs (recommended for development)"
    echo "  $0 logs gateway        # Show gateway logs"
    echo "  $0 logs user 100       # Show last 100 lines of user service logs"
    echo "  $0 jar-status          # Check which JARs are built"
    echo "  $0 status              # Check what's currently running"
    echo "  $0 build               # Force rebuild all services"
    echo ""
    echo -e "${CYAN}PERFORMANCE FEATURES:${NC}"
    echo "  ⚡ Fast startup: Skips build if JAR files already exist"
    echo "  🔧 Smart build: Only builds services with missing JARs"
    echo "  🛡️  Auto conflict resolution: Stops conflicting deployments automatically"
    echo "  📊 Health monitoring: Checks service status and Docker container health"
    echo ""
    echo -e "${CYAN}DEPLOYMENT METHODS:${NC}"
    echo -e "  ${YELLOW}Docker Deployment:${NC}"
    echo "    • Production-like containerized environment"
    echo "    • Includes PostgreSQL database in containers"
    echo "    • Automatic service discovery and networking"
    echo "    • Health checks and restart policies"
    echo ""
    echo -e "  ${YELLOW}Local Deployment:${NC}"
    echo "    • Development-friendly local processes"
    echo "    • Easy debugging and IDE integration"
    echo "    • Fast service restarts and code changes"
    echo "    • PostgreSQL runs in Docker, services run locally"
    echo ""
    echo -e "${CYAN}TROUBLESHOOTING:${NC}"
    echo "  • Port conflicts: Use '$0 ports' to debug and resolve"
    echo "  • Build issues: Use '$0 build' to force rebuild"
    echo "  • Service issues: Use '$0 logs <service>' to check logs"
    echo "  • Clean restart: Use '$0 clean && $0 <method>-start'"
    echo ""
    echo -e "${CYAN}FILES CREATED:${NC}"
    echo "  • *.pid files: Process IDs for local services"
    echo "  • logs/*.log files: Service logs"
    echo "  • Docker volumes: postgres_data"
    echo ""
    echo -e "${GREEN}For more details, see: DEPLOYMENT_GUIDE.md${NC}"
}

# Main execution
case "$1" in
    "docker-start")
        stop_local_deployment 2>/dev/null || true
        start_docker_deployment
        ;;
    "local-start")
        stop_docker_deployment 2>/dev/null || true
        start_local_deployment
        ;;
    "restart")
        # Detect current deployment method and restart it
        if docker-compose ps -q | grep -q .; then
            print_info "Restarting Docker deployment..."
            stop_docker_deployment
            start_docker_deployment
        elif ls *.pid 2>/dev/null | grep -q .; then
            print_info "Restarting local deployment..."
            stop_local_deployment
            start_local_deployment
        else
            print_error "No active deployment found to restart"
            print_info "Use 'docker-start' or 'local-start' to start a deployment"
        fi
        ;;
    "stop")
        stop_docker_deployment 2>/dev/null || true
        stop_local_deployment 2>/dev/null || true
        print_success "All services stopped"
        ;;
    "status")
        show_status
        ;;
    "logs")
        show_logs "$2" "$3"
        ;;
    "jar-status"|"jars")
        show_jar_status
        ;;
    "build")
        build_services true
        ;;
    "clean")
        stop_docker_deployment 2>/dev/null || true
        stop_local_deployment 2>/dev/null || true
        docker system prune -f
        rm -f *.pid
        print_success "Cleanup completed"
        ;;
    "ports")
        ./debug-ports.sh check
        ;;
    "help"|""|"-h"|"--help")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
