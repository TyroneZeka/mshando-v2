# Bidding Service

A comprehensive Spring Boot microservice for managing bids in the Mshando marketplace platform.

## Overview

The Bidding Service handles all bid-related operations in the Mshando ecosystem, enabling taskers to submit bids for tasks and customers to manage those bids through their complete lifecycle.

## Features

### Core Functionality
- ✅ **Bid Management**: Create, update, accept, reject, withdraw, complete, and cancel bids
- ✅ **Status Lifecycle**: Complete bid status management from creation to completion
- ✅ **Validation**: Comprehensive business rule validation and constraints
- ✅ **Inter-Service Communication**: Seamless integration with User and Task services
- ✅ **Statistics**: Real-time bid analytics and reporting

### Security
- ✅ **JWT Authentication**: Secure token-based authentication
- ✅ **Role-Based Access**: Customer and Tasker role-based permissions
- ✅ **Input Validation**: Jakarta Bean Validation for all inputs
- ✅ **Business Rules**: Comprehensive business logic enforcement

### Technical Features
- ✅ **Spring Boot 3.1.5**: Latest Spring Boot framework
- ✅ **PostgreSQL**: Robust database with optimized indexes
- ✅ **Eureka Integration**: Service discovery and registration
- ✅ **OpenAPI Documentation**: Complete API documentation with Swagger UI
- ✅ **Reactive Communication**: WebClient for inter-service calls

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User Service  │    │   Task Service  │    │ Bidding Service │
│    (Port 8081)  │    │    (Port 8082)  │    │    (Port 8083)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │ Eureka Discovery│
                    │    (Port 8761)  │
                    └─────────────────┘
```

## Bid Lifecycle

```
┌─────────┐   Accept   ┌──────────┐   Complete   ┌───────────┐
│ PENDING ├───────────→│ ACCEPTED ├─────────────→│ COMPLETED │
└────┬────┘            └────┬─────┘              └───────────┘
     │                      │
     │ Reject               │ Cancel
     ↓                      ↓
┌─────────┐              ┌───────────┐
│REJECTED │              │ CANCELLED │
└─────────┘              └───────────┘
     │
     │ Withdraw
     ↓
┌───────────┐
│ WITHDRAWN │
└───────────┘
```

## API Endpoints

### Core Operations
- `POST /api/v1/bids` - Create new bid
- `GET /api/v1/bids/{id}` - Get bid details
- `PUT /api/v1/bids/{id}` - Update bid
- `GET /api/v1/bids` - List bids with filtering
- `DELETE /api/v1/bids/{id}` - Delete bid (admin only)

### Lifecycle Management
- `PUT /api/v1/bids/{id}/accept` - Accept bid
- `PUT /api/v1/bids/{id}/reject` - Reject bid
- `PUT /api/v1/bids/{id}/withdraw` - Withdraw bid
- `PUT /api/v1/bids/{id}/complete` - Complete bid
- `PUT /api/v1/bids/{id}/cancel` - Cancel bid

### Analytics
- `GET /api/v1/bids/statistics` - Overall statistics
- `GET /api/v1/bids/statistics/task/{taskId}` - Task-specific stats

## Configuration

### Application Properties
```yaml
server:
  port: 8083

spring:
  application:
    name: bidding-service
  datasource:
    url: jdbc:postgresql://localhost:5432/mshando_bidding
    username: ${DB_USERNAME:mshando_user}
    password: ${DB_PASSWORD:mshando_pass}

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Environment Variables
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret
- `USER_SERVICE_URL` - User service base URL
- `TASK_SERVICE_URL` - Task service base URL

## Business Rules

### Bid Creation
- Maximum 5 bids per tasker per task
- Taskers cannot bid on their own tasks
- Task must be in OPEN status
- Bid amount must be positive

### Status Transitions
- Only PENDING bids can be accepted/rejected
- Only bid owners can withdraw their bids
- Only ACCEPTED bids can be completed/cancelled
- Status changes trigger task status updates

### Authorization
- **Customers**: Can accept, reject, complete, cancel bids
- **Taskers**: Can create, update, withdraw their own bids
- **Admin**: Full access to all operations

## Database Schema

### Bid Table
```sql
CREATE TABLE bids (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    tasker_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    message TEXT,
    status VARCHAR(50) NOT NULL,
    rejection_reason TEXT,
    withdrawal_reason TEXT,
    cancellation_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_bids_task_id ON bids(task_id);
CREATE INDEX idx_bids_tasker_id ON bids(tasker_id);
CREATE INDEX idx_bids_status ON bids(status);
CREATE INDEX idx_bids_created_at ON bids(created_at);
```

## Development

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Docker (optional)

### Running Locally
1. Start PostgreSQL database
2. Create `mshando_bidding` database
3. Start Eureka Server (port 8761)
4. Start User Service (port 8081)
5. Start Task Service (port 8082)
6. Run Bidding Service:
   ```bash
   mvn spring-boot:run
   ```

### Testing
```bash
# Run unit tests
mvn test

# Run integration tests
mvn test -Dspring.profiles.active=integration

# Generate coverage report
mvn jacoco:report
```

### Docker
```bash
# Build image
docker build -t mshando/bidding-service .

# Run container
docker run -p 8083:8083 mshando/bidding-service
```

## API Documentation

Once the service is running, access the Swagger UI at:
- **Local**: http://localhost:8083/swagger-ui.html
- **API Docs**: http://localhost:8083/v3/api-docs

## Monitoring

### Health Checks
- **Health Endpoint**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

### Logging
The service uses structured logging with correlation IDs for request tracing across microservices.

## Contributing

1. Create feature branch from `main`
2. Implement changes with tests
3. Ensure all tests pass
4. Submit pull request

## Version

**Current Version**: 1.0.0

## License

© 2024 Mshando Team. All rights reserved.
