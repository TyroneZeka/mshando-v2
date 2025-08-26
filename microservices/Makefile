# Mshando Microservices Makefile
# Provides convenient commands for development workflow

.PHONY: help check build clean test start stop restart status logs

# Default target
help:
	@echo "🎯 Mshando TaskRabbit Clone - Available Commands:"
	@echo ""
	@echo "Setup & Build:"
	@echo "  make check     - Check prerequisites"
	@echo "  make build     - Build all services"
	@echo "  make clean     - Clean build artifacts"
	@echo "  make test      - Run tests"
	@echo ""
	@echo "Development:"
	@echo "  make start     - Start all services"
	@echo "  make stop      - Stop all services"
	@echo "  make restart   - Restart all services"
	@echo "  make status    - Show service status"
	@echo "  make logs      - Show logs for all services"
	@echo ""
	@echo "Individual Services:"
	@echo "  make start-infra      - Start infrastructure only"
	@echo "  make logs-user        - Show user service logs"
	@echo "  make logs-gateway     - Show gateway logs"
	@echo "  make logs-eureka      - Show eureka logs"
	@echo ""
	@echo "Database:"
	@echo "  make db-start         - Start PostgreSQL only"
	@echo "  make db-stop          - Stop PostgreSQL"
	@echo "  make db-logs          - Show database logs"
	@echo ""
	@echo "Cleanup:"
	@echo "  make clean-docker     - Remove Docker containers and images"
	@echo "  make clean-volumes    - Remove Docker volumes (⚠️  Data loss!)"

# Check prerequisites
check:
	@echo "🔍 Checking prerequisites..."
	@command -v java >/dev/null 2>&1 || { echo "❌ Java is required but not installed."; exit 1; }
	@command -v mvn >/dev/null 2>&1 || { echo "❌ Maven is required but not installed."; exit 1; }
	@command -v docker >/dev/null 2>&1 || { echo "❌ Docker is required but not installed."; exit 1; }
	@command -v docker-compose >/dev/null 2>&1 || { echo "❌ Docker Compose is required but not installed."; exit 1; }
	@echo "✅ All prerequisites are installed"

# Build all services
build: check
	@echo "🔨 Building all services..."
	@for service in eureka-server api-gateway user-service; do \
		if [ -d "$$service" ]; then \
			echo "Building $$service..."; \
			cd $$service && mvn clean package -DskipTests && cd ..; \
		fi; \
	done
	@echo "✅ All services built successfully"

# Clean build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	@for service in eureka-server api-gateway user-service; do \
		if [ -d "$$service" ]; then \
			echo "Cleaning $$service..."; \
			cd $$service && mvn clean && cd ..; \
		fi; \
	done
	@echo "✅ Cleanup completed"

# Run tests
test: check
	@echo "🧪 Running tests..."
	@for service in eureka-server api-gateway user-service; do \
		if [ -d "$$service" ]; then \
			echo "Testing $$service..."; \
			cd $$service && mvn test && cd ..; \
		fi; \
	done
	@echo "✅ All tests completed"

# Database operations
db-start:
	@echo "🗄️ Starting PostgreSQL..."
	@docker-compose up -d postgres
	@echo "✅ PostgreSQL started"

db-stop:
	@echo "🛑 Stopping PostgreSQL..."
	@docker-compose stop postgres
	@echo "✅ PostgreSQL stopped"

db-logs:
	@echo "📋 PostgreSQL logs:"
	@docker-compose logs -f postgres

# Infrastructure services
start-infra: db-start
	@echo "🏗️ Starting infrastructure services..."
	@docker-compose up -d postgres eureka-server
	@echo "⏳ Waiting for services to be ready..."
	@sleep 15
	@echo "✅ Infrastructure services started"

# Start all services
start: build start-infra
	@echo "🚀 Starting all services..."
	@docker-compose up -d
	@echo "⏳ Waiting for services to be ready..."
	@sleep 30
	@make status
	@echo "✅ All services started"

# Stop all services
stop:
	@echo "🛑 Stopping all services..."
	@docker-compose down
	@echo "✅ All services stopped"

# Restart all services
restart: stop start

# Show service status
status:
	@echo "📊 Service Status:"
	@docker-compose ps
	@echo ""
	@echo "🌐 Access Points:"
	@echo "  Eureka Dashboard: http://localhost:8761"
	@echo "  API Gateway: http://localhost:8080"
	@echo "  User Service: http://localhost:8081"
	@echo "  API Documentation: http://localhost:8080/swagger-ui.html"

# Show logs for all services
logs:
	@echo "📋 Service Logs:"
	@docker-compose logs -f

# Individual service logs
logs-user:
	@docker-compose logs -f user-service

logs-gateway:
	@docker-compose logs -f api-gateway

logs-eureka:
	@docker-compose logs -f eureka-server

# Docker cleanup
clean-docker:
	@echo "🧹 Cleaning Docker containers and images..."
	@docker-compose down --rmi all --remove-orphans
	@echo "✅ Docker cleanup completed"

clean-volumes:
	@echo "⚠️  This will delete all data in PostgreSQL!"
	@read -p "Are you sure? (y/N): " confirm && [ "$$confirm" = "y" ] || exit 1
	@docker-compose down -v
	@echo "✅ Volumes cleaned"

# Development helpers
dev-setup: check build start-infra
	@echo "👨‍💻 Development environment ready!"
	@echo "You can now start individual services locally for development"

quick-start: build start
	@echo "⚡ Quick start completed!"
	@echo "All services are running in Docker"

# Health checks
health-check:
	@echo "🏥 Health Check:"
	@curl -s http://localhost:8761/actuator/health | grep -q UP && echo "✅ Eureka Server: UP" || echo "❌ Eureka Server: DOWN"
	@curl -s http://localhost:8080/actuator/health | grep -q UP && echo "✅ API Gateway: UP" || echo "❌ API Gateway: DOWN"
	@curl -s http://localhost:8081/actuator/health | grep -q UP && echo "✅ User Service: UP" || echo "❌ User Service: DOWN"

# Environment info
env-info:
	@echo "🔧 Environment Information:"
	@echo "Java Version: $$(java -version 2>&1 | head -n 1)"
	@echo "Maven Version: $$(mvn -version 2>&1 | head -n 1)"
	@echo "Docker Version: $$(docker --version)"
	@echo "Docker Compose Version: $$(docker-compose --version)"
