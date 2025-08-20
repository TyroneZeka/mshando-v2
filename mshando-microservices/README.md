# Mshando - The place to get things done

A comprehensive microservices-based platform that connects customers who need tasks done with taskers who can complete them. Built with Spring Boot, PostgreSQL, Docker, and JWT Authentication.

## Architecture Overview

```
[React Frontend] → [API Gateway (8080)] → [Eureka Server (8761)]
                                      ├── [User Service (8081)]
                                      ├── [Task Service (8082)]
                                      ├── [Bidding Service (8083)]
                                      ├── [Payment Service (8084)]
                                      ├── [Notification Service (8085)]
                                      └── [Review Service (8086)]
```

## 🛠️ Technology Stack

- **Backend Framework:** Spring Boot 3.1+ with Java 17
- **Microservices:** Spring Cloud, Eureka Discovery, Gateway
- **Database:** PostgreSQL with JPA/Hibernate
- **Security:** JWT Authentication + OAuth2
- **Documentation:** OpenAPI/Swagger
- **Containerization:** Docker & Docker Compose
- **Testing:** JUnit 5, MockMvc, TestContainers

## Prerequisites

Before running this project, ensure you have:

- **Java 17** or higher installed
- **Maven 3.8+** for dependency management
- **Docker & Docker Compose** for containerization
- **PostgreSQL Client** (optional, for database management)
- **Git** for version control

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd mshando-v2/mshando-microservices
```

### 2. Start Infrastructure Services

Start PostgreSQL and Eureka Server:

```bash
# Start PostgreSQL database
docker-compose up -d postgres

# Wait for PostgreSQL to be ready (check logs)
docker-compose logs postgres

# Start Eureka Server
docker-compose up -d eureka-server
```

### 3. Verify Eureka Server

Navigate to [http://localhost:8761](http://localhost:8761) to access the Eureka Dashboard.

### 4. Build and Start Services

#### Option A: Using Docker Compose (Recommended)

```bash
# Build all services
./mvnw clean package -DskipTests

# Start all services
docker-compose up -d
```

#### Option B: Local Development

Build each service individually:

```bash
# Navigate to each service directory and build
cd eureka-server && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..
cd user-service && mvn clean package -DskipTests && cd ..
# ... repeat for other services
```

Run services locally:

```bash
# Terminal 1: Eureka Server
cd eureka-server && mvn spring-boot:run

# Terminal 2: API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 3: User Service
cd user-service && mvn spring-boot:run

# Continue with other services...
```

### 5. Verify Services

Check that all services are registered in Eureka:
- Visit [http://localhost:8761](http://localhost:8761)
- Confirm all services appear in the registry

## 📚 API Documentation

Once services are running, access API documentation:

- **API Gateway:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **User Service:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **Task Service:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **Other Services:** `http://localhost:{port}/swagger-ui.html`

## 🔑 Authentication Flow

### 1. User Registration

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER"
  }'
```

### 2. User Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john@example.com",
    "password": "password123"
  }'
```

### 3. Access Protected Endpoints

```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer <your-jwt-token>"
```

## 🗄️ Database Schema

### User Service Database (taskrabbit_users)

**Users Table:**
- id, username, email, password
- first_name, last_name, phone_number
- role (CUSTOMER, TASKER, ADMIN)
- is_verified, is_active
- verification_token, reset_password_token
- created_at, updated_at

**Profiles Table:**
- id, user_id (FK), bio, profile_picture_url
- address, city, state, postal_code, country
- latitude, longitude, hourly_rate
- skills, availability, languages
- average_rating, total_reviews, total_tasks_completed
- verification_documents, is_background_checked
- emergency contact details

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# Database
POSTGRES_DB=taskrabbit_main
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123

# JWT
JWT_SECRET=mySecretKey
JWT_EXPIRATION=86400

# Email (optional)
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password
```

### Service Ports

| Service | Port | Description |
|---------|------|-------------|
| Eureka Server | 8761 | Service Discovery |
| API Gateway | 8080 | Main Entry Point |
| User Service | 8081 | Authentication & Users |
| Task Service | 8082 | Task Management |
| Bidding Service | 8083 | Bid Management |
| Payment Service | 8084 | Payment Processing |
| Notification Service | 8085 | Email/SMS Notifications |
| Review Service | 8086 | Reviews & Ratings |

## 🧪 Testing

### Run Unit Tests

```bash
# Test all services
./mvnw test

# Test specific service
cd user-service && mvn test
```

### Run Integration Tests

```bash
# Run integration tests with test profile
./mvnw test -Dspring.profiles.active=test
```

### Test Coverage

Generate test coverage reports:

```bash
./mvnw jacoco:report
```

View reports in `target/site/jacoco/index.html`

## 📦 Service Details

### 🔐 User Service (Port: 8081)

**Key Features:**
- JWT-based authentication
- Role-based access control (CUSTOMER, TASKER, ADMIN)
- Profile management with file upload
- User verification system
- Password encryption with BCrypt

**Main Endpoints:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update profile
- `POST /api/users/profile/picture` - Upload profile picture
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/{id}/rating` - Get user rating

### 🌐 API Gateway (Port: 8080)

**Key Features:**
- Single entry point for all microservices
- JWT authentication filter
- Load balancing with Eureka
- CORS configuration
- Request routing

**Route Configuration:**
- `/api/users/**` → User Service
- `/api/tasks/**` → Task Service
- `/api/bids/**` → Bidding Service
- `/api/payments/**` → Payment Service
- `/api/notifications/**` → Notification Service
- `/api/reviews/**` → Review Service

### 🔍 Eureka Server (Port: 8761)

**Key Features:**
- Service discovery and registration
- Health monitoring
- Load balancing support
- Self-preservation mode

## 🚧 Development Phase Status

### ✅ Phase 1: Foundation Services (COMPLETED)
- [x] Eureka Server Service
- [x] API Gateway Service  
- [x] User Service (Core Implementation)

### 🔄 Phase 2: Core Business Services (IN PROGRESS)
- [ ] Task Service
- [ ] Bidding Service

### ⏳ Phase 3: Supporting Services (PENDING)
- [ ] Payment Service
- [ ] Notification Service
- [ ] Review Service

## 🔍 Monitoring & Health Checks

### Health Endpoints

Check service health:

```bash
# Overall health through gateway
curl http://localhost:8080/actuator/health

# Individual service health
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Task Service
```

### Service Registry

Monitor registered services:
- Eureka Dashboard: [http://localhost:8761](http://localhost:8761)
- Gateway Routes: [http://localhost:8080/actuator/gateway/routes](http://localhost:8080/actuator/gateway/routes)

## 🐛 Troubleshooting

### Common Issues

1. **Services not registering with Eureka:**
   - Check Eureka server is running
   - Verify network connectivity
   - Check application.yml configuration

2. **Database connection issues:**
   - Ensure PostgreSQL is running
   - Verify credentials in application.yml
   - Check database exists

3. **JWT authentication failures:**
   - Verify JWT secret consistency
   - Check token expiration
   - Validate request headers

4. **File upload issues:**
   - Check file size limits
   - Verify upload directory permissions
   - Confirm multipart configuration

### Logs

View service logs:

```bash
# Docker logs
docker-compose logs -f user-service
docker-compose logs -f api-gateway

# Local development logs
tail -f logs/user-service.log
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Lead Developer:** Tyrone X
- **Project:** Mshando
- **Version:** 1.0.0

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

---

**Note:** This implementation follows the phased approach outlined in the requirements. Phase 1 (Foundation Services) is complete and functional. Continue with Phase 2 and Phase 3 services based on business priorities.
