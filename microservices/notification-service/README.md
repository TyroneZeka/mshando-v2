# Notification Service

The Notification Service is a microservice responsible for handling email and SMS notifications in the Mshando platform. It provides comprehensive notification management with template support, scheduling, retry mechanisms, and delivery tracking.

## Features

### Core Functionality
- **Email Notifications**: Send HTML/text emails using Spring Mail
- **SMS Notifications**: Send SMS messages using Twilio
- **Template Processing**: Thymeleaf-based template engine for dynamic content
- **Scheduled Notifications**: Support for delayed/scheduled notifications
- **Retry Mechanism**: Automatic retry for failed notifications with configurable limits
- **Delivery Tracking**: Track notification status and delivery confirmation

### Advanced Features
- **Asynchronous Processing**: Non-blocking notification sending
- **Priority Levels**: LOW, MEDIUM, HIGH, URGENT priority support
- **Batch Processing**: Efficient handling of multiple notifications
- **Template Management**: CRUD operations for notification templates
- **Audit Trail**: Complete audit log for all notifications

## Architecture

### Technology Stack
- **Spring Boot 3.1.5**: Core framework
- **Spring Mail**: Email sending capabilities
- **Twilio SDK**: SMS sending integration
- **Thymeleaf**: Template processing engine
- **PostgreSQL**: Database for notifications and templates
- **Spring Cloud Eureka**: Service discovery
- **Spring Async**: Asynchronous processing

### Database Schema
- **notifications**: Main notification records
- **notification_templates**: Reusable message templates

## API Endpoints

### Notification Management
```http
POST /api/notifications/email
POST /api/notifications/sms
GET /api/notifications/{id}
GET /api/notifications/recipient/{recipientId}
GET /api/notifications/status/{status}
GET /api/notifications/type/{type}
POST /api/notifications/{id}/retry
DELETE /api/notifications/{id}
```

### Template Management
```http
POST /api/notifications/templates
GET /api/notifications/templates/{name}/type/{type}
PUT /api/notifications/templates/{id}/activate
PUT /api/notifications/templates/{id}/deactivate
```

## Configuration

### Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mshando_notifications
SPRING_DATASOURCE_USERNAME=mshando_user
SPRING_DATASOURCE_PASSWORD=mshando_password

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# SMS Configuration
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_PHONE_NUMBER=+1234567890

# Feature Toggles
EMAIL_ENABLED=true
SMS_ENABLED=true

# Service Discovery
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
```

### Profiles
- **local**: Development environment
- **test**: Testing environment with disabled notifications
- **production**: Production environment with optimized settings
- **docker**: Docker container environment

## Request Examples

### Send Email Notification
```json
POST /api/notifications/email
{
  "recipientId": 123,
  "recipientEmail": "user@example.com",
  "subject": "Welcome to Mshando",
  "content": "<h1>Welcome!</h1><p>Thank you for joining Mshando.</p>",
  "priority": "HIGH",
  "templateId": 1,
  "templateParameters": {
    "userName": "John Doe",
    "loginUrl": "https://mshando.com/login"
  },
  "referenceType": "USER_REGISTRATION",
  "referenceId": "123"
}
```

### Send SMS Notification
```json
POST /api/notifications/sms
{
  "recipientId": 123,
  "recipientPhoneNumber": "+1234567890",
  "content": "Your Mshando verification code is: {{verificationCode}}",
  "priority": "URGENT",
  "templateId": 2,
  "templateParameters": {
    "verificationCode": "123456"
  },
  "referenceType": "PHONE_VERIFICATION",
  "referenceId": "123"
}
```

### Create Notification Template
```json
POST /api/notifications/templates
{
  "name": "user_welcome",
  "type": "EMAIL",
  "subject": "Welcome to Mshando, {{userName}}!",
  "content": "<html><body><h1>Welcome {{userName}}!</h1><p>Click <a href='{{loginUrl}}'>here</a> to login.</p></body></html>",
  "description": "Welcome email template for new users",
  "active": true
}
```

## Deployment

### Local Development
```bash
# Build the service
mvn clean package

# Run locally
java -jar target/notification-service-1.0.0.jar --spring.profiles.active=local
```

### Docker
```bash
# Build Docker image
docker build -t mshando/notification-service .

# Run with Docker Compose
docker-compose up notification-service
```

### Service Startup Order
1. PostgreSQL Database
2. Eureka Server
3. API Gateway
4. User Service
5. Task Service
6. Bidding Service
7. Payment Service
8. **Notification Service** ← This service
9. Review Service

## Monitoring and Health

### Health Check
```http
GET /actuator/health
```

### Metrics
```http
GET /actuator/metrics
GET /actuator/prometheus
```

### Logging
- Application logs: `DEBUG` level for development
- Email sending logs: Detailed SMTP transaction logs
- SMS sending logs: Twilio API interaction logs
- Audit logs: All notification lifecycle events

## Error Handling

### Retry Logic
- **Email**: Automatic retry for SMTP failures
- **SMS**: Automatic retry for Twilio API failures
- **Max Retries**: Configurable (default: 3)
- **Retry Delay**: Exponential backoff

### Error Scenarios
- Invalid recipient addresses
- SMTP server unavailable
- Twilio API errors
- Template processing failures
- Database connectivity issues

## Security

### Authentication
- JWT token validation for API access
- Service-to-service authentication via Eureka

### Data Protection
- Sensitive data encryption in database
- Secure SMTP connections (STARTTLS)
- Twilio HTTPS API communication

## Performance

### Async Processing
- All notifications sent asynchronously
- Configurable thread pool for concurrent processing
- Non-blocking API responses

### Scalability
- Horizontal scaling support
- Database connection pooling
- Template caching for performance

## Integration

### Service Dependencies
- **User Service**: Recipient information
- **Task Service**: Task-related notifications
- **Bidding Service**: Bid notifications
- **Payment Service**: Payment confirmations

### External Dependencies
- **SMTP Provider**: Email delivery
- **Twilio**: SMS delivery
- **PostgreSQL**: Data persistence
- **Eureka**: Service discovery

## Development

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 15+
- SMTP server access (Gmail recommended)
- Twilio account (for SMS)

### Testing
```bash
# Run unit tests
mvn test

# Run integration tests
mvn integration-test

# Test coverage
mvn jacoco:report
```

### Code Quality
- Checkstyle configuration
- PMD static analysis
- SpotBugs security analysis
- SonarQube integration

## Support

For support and documentation:
- **API Documentation**: Available at `/swagger-ui.html` when running
- **Logs**: Check application logs for detailed error information
- **Monitoring**: Use Actuator endpoints for health and metrics

## Version History

- **1.0.0**: Initial release with email and SMS support
- **1.1.0**: Template management system
- **1.2.0**: Advanced scheduling and retry mechanisms
