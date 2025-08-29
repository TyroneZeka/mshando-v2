# Mshando Microservices Deployment Guide

This guide explains how to manage the two different deployment strategies for Mshando microservices.

## Deployment Strategies

### 1. Docker Deployment (Recommended for Production)
- **Use case**: Production, testing, isolated environments
- **Benefits**: Consistent environment, easy scaling, isolated dependencies
- **Command**: `./deploy.sh docker-start`

### 2. Local JAR Deployment (Recommended for Development)
- **Use case**: Local development, debugging, IDE integration
- **Benefits**: Easy debugging, faster restarts, IDE integration
- **Command**: `./deploy.sh local-start`

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL (for local development)

### Choose Your Deployment Method

#### Option 1: Docker Deployment
```bash
# Start all services in Docker containers
./deploy.sh docker-start

# Check status
./deploy.sh status

# View logs
./deploy.sh logs gateway

# Stop all services
./deploy.sh stop
```

#### Option 2: Local JAR Deployment
```bash
# Start all services as local processes
./deploy.sh local-start

# Check status
./deploy.sh status

# View logs
./deploy.sh logs user

# Stop all services
./deploy.sh stop
```

## Port Configuration

| Service | Port | Description |
|---------|------|-------------|
| Eureka Server | 8761 | Service Discovery |
| API Gateway | 8080 | Main Entry Point |
| User Service | 8081 | User Management |
| Task Service | 8082 | Task Management |
| Bidding Service | 8083 | Bid Management |
| Payment Service | 8084 | Payment Processing |
| Notification Service | 8085 | Notifications |
| PostgreSQL | 5432 | Database |
| Frontend (Dev) | 5173 | React Development Server |
| Frontend (Preview) | 4173 | React Preview Server |

## Deployment Scripts

### Main Deployment Manager: `deploy.sh`
- **Purpose**: Unified deployment management
- **Features**: 
  - Automatic port conflict detection
  - Service health checks
  - Log management
  - Clean shutdown procedures

### Port Debugging: `debug-ports.sh` / `debug-ports.bat` / `debug-ports.ps1`
- **Purpose**: Diagnose and resolve port conflicts
- **Features**:
  - Interactive port status checking
  - Process identification and termination
  - System resource monitoring

### Legacy Scripts (Still Available)
- `start-all.sh`: Original local deployment script
- `stop-all.sh`: Stop local services
- `status.sh`: Check service status

## Common Issues & Solutions

### Port Conflicts
**Problem**: Services fail to start due to port conflicts
**Solution**: 
```bash
# Check what's using the ports
./deploy.sh ports

# Or use the dedicated debugging tool
./debug-ports.sh

# Stop conflicting processes
./deploy.sh stop
```

### Mixed Deployments
**Problem**: Both Docker and local services running simultaneously
**Solution**:
```bash
# Stop everything and choose one method
./deploy.sh stop

# Then start with your preferred method
./deploy.sh docker-start    # OR
./deploy.sh local-start
```

### Build Issues
**Problem**: JAR files not found for local deployment
**Solution**:
```bash
# Build all services
./deploy.sh build

# Then start local deployment
./deploy.sh local-start
```

## Environment Configuration

### Docker Deployment
Requires `.env` file with:
```bash
POSTGRES_DB=mshando
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123
SPRING_PROFILES_ACTIVE=docker
EUREKA_SERVER_URL=http://eureka-server:8761/eureka
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
# ... other environment variables
```

### Local Deployment
Services use `application-local.yml` profiles with:
- Database connection to `localhost:5432`
- Eureka client pointing to `localhost:8761`
- Local configuration overrides

## Development Workflow

### For Backend Development (Recommended: Local Deployment)
1. Start services locally for easy debugging
2. Use IDE debugging capabilities
3. Quick service restarts
4. Direct log access

```bash
./deploy.sh local-start
./deploy.sh logs <service-name>
```

### For Integration Testing (Recommended: Docker Deployment)
1. Test in containerized environment
2. Verify Docker configurations
3. Test service interactions

```bash
./deploy.sh docker-start
./deploy.sh status
```

### For Frontend Development
The frontend runs separately and connects to backend services:
```bash
cd ../mshando-v2-frontend
npm run dev    # Starts on port 5173
```

## Monitoring & Logs

### View All Service Status
```bash
./deploy.sh status
```

### View Specific Service Logs
```bash
./deploy.sh logs gateway      # Last 50 lines
./deploy.sh logs user 100     # Last 100 lines
```

### Real-time Log Monitoring
```bash
# Docker deployment
docker-compose logs -f gateway

# Local deployment
tail -f logs/gateway.log
```

## Troubleshooting

### Service Won't Start
1. Check port conflicts: `./deploy.sh ports`
2. Check logs: `./deploy.sh logs <service>`
3. Verify build: `./deploy.sh build`
4. Clean restart: `./deploy.sh clean && ./deploy.sh <method>-start`

### Database Connection Issues
1. Ensure PostgreSQL is running
2. Check database credentials in `.env` or application properties
3. Verify network connectivity

### Service Discovery Issues
1. Ensure Eureka server is running first
2. Check Eureka dashboard: http://localhost:8761
3. Verify service registration logs

## Best Practices

1. **Use Docker for production-like testing**
2. **Use local deployment for active development**
3. **Always stop services cleanly with `./deploy.sh stop`**
4. **Check port status before starting services**
5. **Monitor logs for startup issues**
6. **Keep environment configurations in sync**

## Quick Reference

| Task | Command |
|------|---------|
| Start Docker services | `./deploy.sh docker-start` |
| Start local services | `./deploy.sh local-start` |
| Stop all services | `./deploy.sh stop` |
| Check status | `./deploy.sh status` |
| View logs | `./deploy.sh logs <service>` |
| Check ports | `./deploy.sh ports` |
| Build services | `./deploy.sh build` |
| Clean everything | `./deploy.sh clean` |
| Debug ports | `./debug-ports.sh` |
