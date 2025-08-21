# 💳 Mshando Payment Service

## 🚀 Overview

The **Mshando Payment Service** is a comprehensive microservice responsible for handling all payment processing operations within the Mshando platform. It provides secure, scalable, and efficient payment processing capabilities with support for multiple payment providers, automated retry logic, and extensive financial reporting.

## 🎯 Features

### Core Payment Operations
- **💰 Payment Processing**: Create, process, and manage payments
- **💸 Refund Management**: Full and partial refund processing
- **🔄 Automated Retries**: Intelligent retry logic for failed payments
- **📊 Financial Analytics**: Revenue tracking and reporting
- **🔒 Security**: JWT authentication and encrypted data handling

### Payment Methods Supported
- Credit/Debit Cards (via Stripe)
- Digital Wallets
- Bank Transfers
- Platform Credits

### Business Features
- **Service Fee Calculation**: Automatic 10% platform fee
- **Multi-Currency Support**: USD and other currencies
- **Payment Validation**: Comprehensive business rule validation
- **Audit Trail**: Complete payment history tracking

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.1.5
- **Database**: PostgreSQL 15+
- **Payment Provider**: Stripe
- **Authentication**: JWT (OAuth 2.0)
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Testing**: JUnit 5, TestContainers

### Service Design
- **Microservice Architecture**: Independent, scalable service
- **Event-Driven**: Asynchronous processing with real-time updates
- **RESTful API**: Standard HTTP methods and status codes
- **Caching**: Optimized performance with Redis-compatible caching
- **Monitoring**: Health checks and metrics via Actuator

## 📋 Business Rules

### Payment Limits
- **Minimum Payment**: $0.01
- **Maximum Payment**: $100,000.00
- **Service Fee**: 10% of payment amount
- **Currency**: USD (default)

### Processing Rules
- **Maximum Retries**: 3 attempts
- **Retry Delay**: 5 minutes between attempts
- **Refund Window**: 90 days from payment date
- **Async Processing**: Enabled for all operations

## 🚦 API Endpoints

### Payment Management

#### Create Payment
```http
POST /api/v1/payments
Content-Type: application/json
Authorization: Bearer {jwt-token}

{
  "customerId": 456,
  "taskerId": 789,
  "taskId": 101,
  "amount": 150.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentType": "TASK_PAYMENT",
  "description": "Payment for task completion"
}
```

#### Get Payment Details
```http
GET /api/v1/payments/{paymentId}
Authorization: Bearer {jwt-token}
```

#### Process Payment
```http
PATCH /api/v1/payments/{paymentId}/process
Authorization: Bearer {jwt-token}
```

#### Process Refund
```http
POST /api/v1/payments/{paymentId}/refund
Content-Type: application/json
Authorization: Bearer {jwt-token}

{
  "amount": 75.00,
  "reason": "Service not satisfactory",
  "refundType": "PARTIAL"
}
```

### Financial Analytics

#### Customer Total Payments
```http
GET /api/v1/payments/customer/{customerId}/total
Authorization: Bearer {jwt-token}
```

#### Tasker Total Earnings
```http
GET /api/v1/payments/tasker/{taskerId}/earnings
Authorization: Bearer {jwt-token}
```

#### Service Fees in Period
```http
GET /api/v1/payments/service-fees?startDate=2025-08-01T00:00:00&endDate=2025-08-31T23:59:59
Authorization: Bearer {jwt-token}
```

## 🔧 Configuration

### Environment Variables

#### Database Configuration
```bash
DB_USERNAME=mshando_user
DB_PASSWORD=mshando_password
DDL_AUTO=validate
```

#### Payment Provider Configuration
```bash
STRIPE_SECRET_KEY=sk_live_your_secret_key
STRIPE_PUBLIC_KEY=pk_live_your_public_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
```

#### Service Configuration
```bash
SERVER_PORT=8083
JWT_JWK_SET_URI=https://auth.mshando.com/realms/mshando/protocol/openid-connect/certs
LOG_LEVEL=INFO
```

### Business Rules Configuration
```bash
SERVICE_FEE_PERCENTAGE=10.0
MIN_PAYMENT=0.01
MAX_PAYMENT=100000.00
MAX_RETRIES=3
REFUND_WINDOW=90
```

## 🧪 Testing

### Running Tests
```bash
# Unit Tests
./mvnw test

# Integration Tests
./mvnw test -Dtest=*IntegrationTest

# All Tests
./mvnw verify
```

### Test Coverage
- **Unit Tests**: Service layer, repository layer, utilities
- **Integration Tests**: Database operations, external service calls
- **API Tests**: Controller endpoints, validation, error handling

### Test Data
The service includes comprehensive test data factories for:
- Payment entities with various statuses
- Customer and tasker information
- Task details and relationships
- Payment provider responses

## 🚀 Deployment

### Local Development
```bash
# Start PostgreSQL database
docker run -d --name payment-db \
  -e POSTGRES_DB=mshando_payments \
  -e POSTGRES_USER=mshando_user \
  -e POSTGRES_PASSWORD=mshando_password \
  -p 5432:5432 postgres:15

# Run the service
./mvnw spring-boot:run
```

### Docker Deployment
```bash
# Build Docker image
docker build -t mshando/payment-service:latest .

# Run container
docker run -d --name payment-service \
  -p 8083:8083 \
  -e DB_USERNAME=mshando_user \
  -e DB_PASSWORD=mshando_password \
  -e STRIPE_SECRET_KEY=your_secret_key \
  mshando/payment-service:latest
```

### Production Deployment
```yaml
# docker-compose.yml
version: '3.8'
services:
  payment-service:
    image: mshando/payment-service:latest
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - STRIPE_SECRET_KEY=${STRIPE_SECRET_KEY}
    depends_on:
      - payment-db
  
  payment-db:
    image: postgres:15
    environment:
      - POSTGRES_DB=mshando_payments
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - payment_data:/var/lib/postgresql/data

volumes:
  payment_data:
```

## 📊 Monitoring

### Health Checks
```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

### Metrics
- Payment processing rate
- Success/failure ratios
- Average processing time
- Retry attempt statistics
- Revenue metrics

### Logging
- Structured JSON logging
- Payment processing events
- Error tracking and alerts
- Performance monitoring

## 🔐 Security

### Authentication
- JWT Bearer token authentication
- OAuth 2.0 Resource Server
- Role-based access control

### Data Protection
- Encrypted payment data at rest
- PCI DSS compliance considerations
- Audit logging for all operations
- Secure communication with payment providers

### Validation
- Input validation on all endpoints
- Business rule enforcement
- Rate limiting and abuse prevention

## 🐛 Troubleshooting

### Common Issues

#### Payment Processing Failures
1. Check Stripe API keys configuration
2. Verify customer and task existence
3. Review payment amount limits
4. Check network connectivity to Stripe

#### Database Connection Issues
1. Verify PostgreSQL is running
2. Check database credentials
3. Ensure database schema is up to date
4. Review connection pool settings

#### Authentication Problems
1. Verify JWT token validity
2. Check OAuth 2.0 configuration
3. Ensure proper audience and issuer settings

### Debugging
```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
export SQL_LOG_LEVEL=DEBUG

# Run with debug profile
./mvnw spring-boot:run -Dspring.profiles.active=dev,debug
```

## 📚 API Documentation

### Swagger UI
Access interactive API documentation at:
```
http://localhost:8083/swagger-ui.html
```

### OpenAPI Specification
Download the OpenAPI specification:
```
http://localhost:8083/v3/api-docs
```

## 🤝 Contributing

### Development Guidelines
1. Follow Spring Boot best practices
2. Write comprehensive tests for all features
3. Use meaningful commit messages
4. Update documentation for API changes

### Code Standards
- Java 17+ features
- Lombok for boilerplate code
- MapStruct for entity mapping
- Comprehensive error handling

## 📞 Support

For technical support or questions:
- **Email**: dev@mshando.com
- **Documentation**: https://docs.mshando.com/payment-service
- **Issue Tracker**: https://github.com/mshando/payment-service/issues

## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

---

**Mshando Payment Service** - Powering secure and efficient payment processing for the Mshando platform.
