#!/bin/bash

# Mshando Port Debugger and Manager
# This script helps debug and manage port conflicts for microservices

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Port definitions
declare -A MSHANDO_PORTS=(
    ["eureka"]="8761"
    ["gateway"]="8080"
    ["user"]="8081"
    ["task"]="8082"
    ["bidding"]="8083"
    ["payment"]="8084"
    ["notification"]="8085"
    ["postgres"]="5432"
    ["frontend"]="5173"
    ["frontend-preview"]="4173"
)

echo -e "${CYAN}🔍 Mshando Port Debugger${NC}"
echo "================================"

# Function to get process using a port (Windows)
get_port_process_windows() {
    local port=$1
    netstat -ano | grep ":$port " | head -1 | awk '{print $5}' | sed 's/.*://'
}

# Function to get process using a port (Unix-like)
get_port_process_unix() {
    local port=$1
    lsof -ti:$port 2>/dev/null | head -1
}

# Function to get process info
get_process_info() {
    local pid=$1
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        # Windows
        tasklist | grep "$pid" | head -1
    else
        # Unix-like
        ps -p $pid -o pid,ppid,cmd --no-headers 2>/dev/null
    fi
}

# Function to check a single port
check_port() {
    local service=$1
    local port=$2
    
    echo -e "\n${BLUE}📋 Checking $service (Port $port)${NC}"
    echo "----------------------------------------"
    
    # Check if port is listening
    if netstat -an | grep -q ":$port.*LISTENING"; then
        echo -e "${YELLOW}⚠️  Port $port is in use${NC}"
        
        # Get process ID
        if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
            local pid=$(netstat -ano | grep ":$port " | grep "LISTENING" | awk '{print $5}' | head -1)
        else
            local pid=$(lsof -ti:$port 2>/dev/null | head -1)
        fi
        
        if [ ! -z "$pid" ]; then
            echo -e "${PURPLE}🔍 Process ID: $pid${NC}"
            
            # Get process details
            local process_info=$(get_process_info $pid)
            if [ ! -z "$process_info" ]; then
                echo -e "${CYAN}📝 Process Info:${NC}"
                echo "$process_info"
            fi
            
            # Check if it's one of our services
            if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
                if echo "$process_info" | grep -q "java"; then
                    echo -e "${GREEN}✅ Likely Mshando microservice${NC}"
                elif echo "$process_info" | grep -q "node"; then
                    echo -e "${GREEN}✅ Likely Node.js application${NC}"
                else
                    echo -e "${RED}❓ Unknown process type${NC}"
                fi
            fi
            
            echo -e "${YELLOW}💡 To kill this process: kill $pid (Unix) or taskkill /PID $pid /F (Windows)${NC}"
        else
            echo -e "${RED}❌ Could not determine process ID${NC}"
        fi
    else
        echo -e "${GREEN}✅ Port $port is available${NC}"
    fi
}

# Function to kill process on port
kill_port_process() {
    local port=$1
    echo -e "${YELLOW}🔪 Attempting to kill process on port $port...${NC}"
    
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        # Windows
        local pids=$(netstat -ano | grep ":$port " | grep "LISTENING" | awk '{print $5}')
        for pid in $pids; do
            if [ ! -z "$pid" ]; then
                echo "Killing Windows process $pid..."
                taskkill //PID $pid //F 2>/dev/null && echo -e "${GREEN}✅ Killed process $pid${NC}" || echo -e "${RED}❌ Failed to kill process $pid${NC}"
            fi
        done
    else
        # Unix-like
        local pids=$(lsof -ti:$port 2>/dev/null)
        for pid in $pids; do
            if [ ! -z "$pid" ]; then
                echo "Killing Unix process $pid..."
                kill -9 $pid 2>/dev/null && echo -e "${GREEN}✅ Killed process $pid${NC}" || echo -e "${RED}❌ Failed to kill process $pid${NC}"
            fi
        done
    fi
    
    sleep 2
    
    # Verify port is now free
    if netstat -an | grep -q ":$port.*LISTENING"; then
        echo -e "${RED}❌ Port $port is still in use${NC}"
    else
        echo -e "${GREEN}✅ Port $port is now available${NC}"
    fi
}

# Function to show all port status
show_all_ports() {
    echo -e "\n${CYAN}📊 All Mshando Ports Status${NC}"
    echo "==============================="
    
    for service in "${!MSHANDO_PORTS[@]}"; do
        local port=${MSHANDO_PORTS[$service]}
        printf "%-12s (%-4s): " "$service" "$port"
        
        if netstat -an | grep -q ":$port.*LISTENING"; then
            echo -e "${RED}❌ IN USE${NC}"
        else
            echo -e "${GREEN}✅ FREE${NC}"
        fi
    done
}

# Function to kill all Mshando processes
kill_all_mshando() {
    echo -e "\n${RED}🚨 KILLING ALL MSHANDO PROCESSES${NC}"
    echo "================================="
    echo -e "${YELLOW}⚠️  This will stop all Mshando services!${NC}"
    read -p "Are you sure? (y/N): " confirm
    
    if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
        for service in "${!MSHANDO_PORTS[@]}"; do
            local port=${MSHANDO_PORTS[$service]}
            if [ "$service" != "postgres" ]; then  # Don't kill postgres
                kill_port_process $port
            fi
        done
        echo -e "${GREEN}✅ All Mshando processes terminated${NC}"
    else
        echo -e "${YELLOW}❌ Operation cancelled${NC}"
    fi
}

# Function to show system resource usage
show_resources() {
    echo -e "\n${CYAN}💻 System Resources${NC}"
    echo "==================="
    
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        echo -e "${BLUE}Memory Usage:${NC}"
        wmic OS get TotalVisibleMemorySize,FreePhysicalMemory /format:list 2>/dev/null | grep -E "TotalVisibleMemorySize|FreePhysicalMemory"
        
        echo -e "\n${BLUE}Java Processes:${NC}"
        tasklist | grep "java.exe" | head -5
    else
        echo -e "${BLUE}Memory Usage:${NC}"
        free -h
        
        echo -e "\n${BLUE}Java Processes:${NC}"
        ps aux | grep java | grep -v grep | head -5
    fi
}

# Main menu
show_menu() {
    echo -e "\n${GREEN}🛠️  What would you like to do?${NC}"
    echo "1. Check all ports"
    echo "2. Check specific service"
    echo "3. Kill process on specific port"
    echo "4. Kill all Mshando processes"
    echo "5. Show system resources"
    echo "6. Show service logs"
    echo "0. Exit"
    echo
}

# Function to show service logs
show_service_logs() {
    echo -e "\n${CYAN}📋 Available Service Logs:${NC}"
    echo "=========================="
    
    local log_dir="logs"
    if [ -d "$log_dir" ]; then
        ls -la $log_dir/*.log 2>/dev/null | awk '{print $9}' | while read logfile; do
            if [ ! -z "$logfile" ]; then
                local service=$(basename "$logfile" .log)
                echo "- $service"
            fi
        done
        
        echo
        read -p "Enter service name to view logs (or 'all' for overview): " service_choice
        
        if [ "$service_choice" = "all" ]; then
            for logfile in $log_dir/*.log; do
                if [ -f "$logfile" ]; then
                    echo -e "\n${BLUE}=== $(basename "$logfile") (last 5 lines) ===${NC}"
                    tail -5 "$logfile"
                fi
            done
        elif [ -f "$log_dir/${service_choice}.log" ]; then
            echo -e "\n${BLUE}=== $service_choice.log (last 20 lines) ===${NC}"
            tail -20 "$log_dir/${service_choice}.log"
        else
            echo -e "${RED}❌ Log file not found: $log_dir/${service_choice}.log${NC}"
        fi
    else
        echo -e "${RED}❌ Logs directory not found${NC}"
    fi
}

# Main execution
if [ $# -eq 0 ]; then
    # Interactive mode
    while true; do
        show_all_ports
        show_menu
        read -p "Choose an option: " choice
        
        case $choice in
            1)
                for service in "${!MSHANDO_PORTS[@]}"; do
                    check_port "$service" "${MSHANDO_PORTS[$service]}"
                done
                ;;
            2)
                echo "Available services:"
                for service in "${!MSHANDO_PORTS[@]}"; do
                    echo "- $service (${MSHANDO_PORTS[$service]})"
                done
                read -p "Enter service name: " service_name
                if [ ! -z "${MSHANDO_PORTS[$service_name]}" ]; then
                    check_port "$service_name" "${MSHANDO_PORTS[$service_name]}"
                else
                    echo -e "${RED}❌ Unknown service: $service_name${NC}"
                fi
                ;;
            3)
                read -p "Enter port number: " port_num
                kill_port_process "$port_num"
                ;;
            4)
                kill_all_mshando
                ;;
            5)
                show_resources
                ;;
            6)
                show_service_logs
                ;;
            0)
                echo -e "${GREEN}👋 Goodbye!${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}❌ Invalid option${NC}"
                ;;
        esac
        
        echo
        read -p "Press Enter to continue..."
    done
else
    # Command line mode
    case $1 in
        "check")
            show_all_ports
            ;;
        "kill")
            if [ ! -z "$2" ]; then
                kill_port_process "$2"
            else
                echo "Usage: $0 kill <port>"
            fi
            ;;
        "killall")
            kill_all_mshando
            ;;
        "resources")
            show_resources
            ;;
        *)
            echo "Usage: $0 [check|kill <port>|killall|resources]"
            echo "   or: $0 (for interactive mode)"
            ;;
    esac
fi
