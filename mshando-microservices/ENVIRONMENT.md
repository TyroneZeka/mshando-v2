# Environment Configuration

This document explains how to set up environment variables for the Mshando microservices.

## Quick Setup

1. **Copy the appropriate environment template:**
   ```bash
   # For local development
   cp .env.example .env
   
   # For production
   cp .env.production .env
   
   # For staging
   cp .env.staging .env
   ```

2. **Edit the `.env` file with your actual values:**
   ```bash
   # Database Configuration
   POSTGRES_DB=mshando_main
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=your_secure_password_here
   
   # JWT Configuration (use a strong secret in production)
   JWT_SECRET=your_long_secure_jwt_secret_here
   JWT_EXPIRATION=86400
   
   # Spring Profile
   SPRING_PROFILES_ACTIVE=docker
   
   # Eureka Configuration
   EUREKA_SERVER_URL=http://eureka-server:8761/eureka
   ```

## Environment Files

- **`.env`** - Contains actual sensitive values (NOT committed to Git)
- **`.env.example`** - Development/local template (committed to Git)
- **`.env.production`** - Production template (committed to Git)
- **`.env.staging`** - Staging template (committed to Git)
- **`.env.development`** - Development configuration (in project root)

## Environment Profiles

### Local Development (`.env.example`)
- `SPRING_PROFILES_ACTIVE=local`
- Uses local PostgreSQL
- Debug logging enabled
- JPA DDL auto-update
- Health details shown

### Docker Development (`.env`)
- `SPRING_PROFILES_ACTIVE=docker`
- Uses Docker PostgreSQL container
- Moderate logging
- JPA DDL auto-update
- Health details shown

### Staging (`.env.staging`)
- `SPRING_PROFILES_ACTIVE=staging`
- Uses staging database
- Debug logging for troubleshooting
- JPA DDL auto-update
- Health details shown

### Production (`.env.production`)
- `SPRING_PROFILES_ACTIVE=production`
- Uses production database
- Minimal logging for performance
- JPA DDL validation only (no schema changes)
- Health details hidden for security

## Security Notes

⚠️ **IMPORTANT**: Never commit `.env` files containing actual passwords or secrets to version control!

### For Production

1. Use strong, unique passwords (minimum 12 characters)
2. Generate a secure JWT secret (at least 256 bits)
3. Use environment variables or secret management systems
4. Rotate secrets regularly
5. Use HTTPS for all external URLs
6. Disable detailed health information
7. Set logging to WARN or ERROR level

### JWT Secret Generation

Generate a secure JWT secret:
```bash
# Using openssl (recommended)
openssl rand -base64 64

# Using Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"

# Using Python
python -c "import secrets; print(secrets.token_urlsafe(64))"
```

## Database Configuration

### Local Development
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/mshando_users
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_local_password
```

### Docker Environment
```bash
POSTGRES_DB=mshando_main
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_docker_password
```

### Production
```bash
DATABASE_URL=jdbc:postgresql://prod-db-host:5432/mshando_users
DATABASE_USERNAME=mshando_prod_user
DATABASE_PASSWORD=your_super_secure_production_password
```

## Running the Services

After setting up your `.env` file:

```bash
# Start all services with Docker Compose
./start-all.sh

# Or manually with Docker Compose
docker-compose up -d

# Check status
./status.sh
```

## Troubleshooting

- Make sure your `.env` file is in the same directory as `docker-compose.yml`
- Verify all required environment variables are set
- Check that passwords match between services and database
- Ensure JWT_SECRET is at least 256 bits (44+ base64 characters)
- For production, verify all URLs use HTTPS
- Check log levels are appropriate for your environment

## Environment Variable Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring Boot profile | `docker`, `production`, `staging` |
| `POSTGRES_DB` | Database name | `mshando_main` |
| `POSTGRES_USER` | Database username | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `secure_password_123` |
| `JWT_SECRET` | JWT signing secret | Base64 encoded string (256+ bits) |
| `JWT_EXPIRATION` | JWT expiration time | `86400` (24 hours) |
| `EUREKA_SERVER_URL` | Eureka server URL | `http://eureka-server:8761/eureka` |
| `LOG_LEVEL_APP` | Application log level | `DEBUG`, `INFO`, `WARN`, `ERROR` |
