# Mshando Microservices Platform

A comprehensive task management and service marketplace platform built with Spring Boot microservices architecture.

## Architecture Overview

This project consists of the following microservices:
- **User Service** (Port: 8081) - User management, authentication, and profiles
- **API Gateway** (Port: 8080) - Request routing and load balancing
- **Eureka Server** (Port: 8761) - Service discovery and registration

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Docker & Docker Compose (optional)
- Git

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd mshando-v2
```

### 2. Environment Setup
```bash
# Copy environment template
cp .env.template .env

# Edit .env file with your actual values
# At minimum, set:
# - DATABASE_PASSWORD
# - JWT_SECRET (generate a secure random string)
# - EMAIL credentials (for email verification)
```

### 3. Database Setup
```bash
# Create PostgreSQL database
createdb taskrabbit_users

# Or using psql
psql -U postgres -c "CREATE DATABASE taskrabbit_users;"
```

### 4. Start Services

#### Option A: Using Local Development
```bash
# Start each service in separate terminals
./run-local.sh
```

#### Option B: Using Docker Compose
```bash
# Start all services with Docker
docker-compose up -d
```

### 5. Verify Installation
```bash
# Check if services are running
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8761/                 # Eureka Server
```

## Development

### Running Tests
```bash
cd mshando-microservices/user-service
./quick-test.sh
```

### API Testing
The project includes comprehensive API test scripts:
- `quick-test.sh` - Basic functionality tests
- `comprehensive-test.sh` - Full API test suite

### Configuration

#### Environment Variables
All sensitive configuration is managed through environment variables. See `.env.template` for required variables.

#### Profiles
- `local` - Local development (default)
- `docker` - Docker container deployment
- `test` - Unit and integration testing
- `production` - Production deployment

#### Security Configuration
- JWT tokens for authentication
- BCrypt password encryption
- CORS configured for frontend integration
- Email verification for user accounts

## API Documentation

### User Service Endpoints

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/validate` - Token validation
- `POST /api/auth/refresh` - Token refresh

#### Email Verification
- `GET /api/auth/verify-email?token=<token>` - Verify email
- `POST /api/auth/resend-verification` - Resend verification email
- `GET /api/auth/verification-status?email=<email>` - Check verification status

#### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/search` - Search users

## Database Schema

### Users Table
- `id` (Primary Key)
- `username` (Unique)
- `email` (Unique)
- `password` (Encrypted)
- `first_name`, `last_name`
- `phone_number`
- `role` (CUSTOMER, SERVICE_PROVIDER, ADMIN)
- `is_verified`, `is_active`
- `verification_token`
- `created_at`, `updated_at`

### Profiles Table
- User profile information
- Address and location data
- Skills and preferences

## Security Best Practices

### Environment Variables
- Never commit `.env` files
- Use strong, unique passwords
- Generate secure JWT secrets (minimum 256 bits)
- Use app-specific passwords for email services

### Database Security
- Use strong database passwords
- Enable SSL connections in production
- Regular security updates

### Application Security
- JWT tokens expire after 24 hours
- Password requirements enforced
- Email verification required
- Rate limiting implemented

## Monitoring and Health Checks

### Actuator Endpoints
- `/actuator/health` - Application health
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Logging
- Configurable log levels
- Structured logging format
- Security event logging

## Deployment

### Docker Deployment
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f user-service

# Stop services
docker-compose down
```

### Production Deployment
1. Set `SPRING_PROFILES_ACTIVE=production`
2. Configure production database
3. Set secure JWT secret
4. Configure email service
5. Set appropriate log levels
6. Enable HTTPS

## Troubleshooting

### Common Issues

#### Database Connection
- Verify PostgreSQL is running
- Check database credentials in `.env`
- Ensure database exists

#### Email Service
- Verify email credentials
- Check firewall settings
- Use app-specific passwords for Gmail

#### Service Discovery
- Ensure Eureka server is running
- Check network connectivity
- Verify service registration

### Logs
```bash
# View service logs
docker-compose logs user-service
docker-compose logs api-gateway
docker-compose logs eureka-server
```

## Contributing

### Development Workflow
1. Create feature branch from `develop`
2. Implement changes with tests
3. Run test suite
4. Update documentation
5. Submit pull request

### Code Standards
- Follow Spring Boot conventions
- Write comprehensive tests
- Document API changes
- Use meaningful commit messages

### Commit Guidelines
- Use conventional commit format
- Include breaking change notes
- Reference issue numbers

## Support

For issues and questions:
1. Check this README
2. Review application logs
3. Check existing issues
4. Create new issue with details

## License

[Add your license information here]

## Version History

- v1.0.0 - Initial release
  - User management system
  - Email verification
  - JWT authentication
  - Microservices architecture
