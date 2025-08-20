#!/bin/bash

# Build Verification Script for Mshando Microservices
echo "🔨 Building and verifying all services..."

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

services=("eureka-server" "api-gateway" "user-service")
build_failed=false

for service in "${services[@]}"; do
    if [ -d "$service" ]; then
        echo "Building $service..."
        cd "$service"
        
        if mvn clean package -DskipTests -q; then
            echo -e "${GREEN}✅ $service build successful${NC}"
            
            # Check if JAR file exists
            if [ -f "target/$service-1.0.0.jar" ]; then
                echo -e "${GREEN}✅ $service JAR file created${NC}"
            else
                echo -e "${RED}❌ $service JAR file missing${NC}"
                build_failed=true
            fi
        else
            echo -e "${RED}❌ $service build failed${NC}"
            build_failed=true
        fi
        
        cd ..
        echo ""
    else
        echo -e "${RED}❌ $service directory not found${NC}"
        build_failed=true
    fi
done

if [ "$build_failed" = true ]; then
    echo -e "${RED}🚫 Some builds failed. Please check the errors above.${NC}"
    exit 1
else
    echo -e "${GREEN}🎉 All services built successfully!${NC}"
    echo "Ready to start services with: docker-compose up -d"
fi
