#!/bin/bash

# Simple script to stop all services
echo "🛑 Stopping Mshando Microservices..."

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

# Stop services
for service in eureka gateway user task bidding payment; do
    if [ -f "${service}.pid" ]; then
        pid=$(cat "${service}.pid")
        if kill -0 $pid 2>/dev/null; then
            print_info "Stopping $service (PID: $pid)..."
            kill $pid
            rm "${service}.pid"
        else
            print_info "$service was not running"
            rm "${service}.pid"
        fi
    else
        print_info "$service PID file not found"
    fi
done

print_success "All services stopped!"
