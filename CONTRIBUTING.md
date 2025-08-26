# Contributing to Mshando

Thank you for your interest in contributing to the Mshando task marketplace platform! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Documentation](#documentation)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## Code of Conduct

By participating in this project, you agree to abide by our code of conduct:

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Keep discussions professional
- Report any inappropriate behavior

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git
- Docker (optional)

### Setup

1. **Fork the repository**
   ```bash
   # Fork on GitHub, then clone your fork
   git clone https://github.com/your-username/mshando-v2.git
   cd mshando-v2
   ```

2. **Set up development environment**
   ```bash
   # Copy environment template
   cp .env.template .env
   
   # Generate secure secrets
   ./generate-secrets.sh  # Linux/Mac
   # or
   generate-secrets.bat   # Windows
   
   # Edit .env with your values
   ```

3. **Set up database**
   ```bash
   # Create database
   createdb taskrabbit_users
   ```

4. **Start services**
   ```bash
   # Start all services
   ./mshando-microservices/run-local.sh
   
   # Or individual services
   cd mshando-microservices/user-service
   mvn spring-boot:run
   ```

5. **Verify setup**
   ```bash
   # Run tests
   cd mshando-microservices/user-service
   ./quick-test.sh
   ```

## Development Workflow

### Branch Strategy

We use Git Flow for development:

- `master`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: Feature development
- `bugfix/*`: Bug fixes
- `hotfix/*`: Emergency fixes

### Working on Features

1. **Create feature branch**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Make changes**
   - Follow coding standards
   - Write tests for new functionality
   - Update documentation as needed
   - Commit regularly with meaningful messages

3. **Test your changes**
   ```bash
   # Run unit tests
   mvn test
   
   # Run integration tests
   ./quick-test.sh
   
   # Test all endpoints
   ./test-all-endpoints.sh
   ```

4. **Submit pull request**
   - Push to your fork
   - Create PR from your branch to `develop`
   - Fill out PR template
   - Address review feedback

## Coding Standards

### Java/Spring Boot

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Keep methods focused and small
- Use Spring conventions and best practices

### Example Code Style

```java
/**
 * Service for managing user authentication and authorization.
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Authenticate user with username/email and password.
     * 
     * @param loginRequest login credentials
     * @return authentication response with JWT token
     * @throws AuthenticationException if credentials are invalid
     */
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        try {
            // Implementation here
            log.info("User {} logged in successfully", loginRequest.getUsername());
            return response;
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            throw new AuthenticationException("Invalid credentials");
        }
    }
}
```

### Configuration and Properties

- Use environment variables for sensitive data
- Provide default values for non-sensitive configuration
- Document all configuration options
- Use Spring profiles for environment-specific settings

### Database

- Use meaningful table and column names
- Add proper indexes for performance
- Include database migration scripts
- Follow JPA/Hibernate best practices

## Testing

### Testing Strategy

- **Unit Tests**: Test individual components
- **Integration Tests**: Test service interactions
- **API Tests**: Test HTTP endpoints
- **Contract Tests**: Test service contracts

### Writing Tests

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "jwt.secret=test-secret-key-for-testing-only"
})
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void shouldAuthenticateValidUser() {
        // Given
        LoginRequestDTO request = new LoginRequestDTO("testuser", "password");
        User user = createTestUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        AuthResponseDTO response = authService.login(request);

        // Then
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }
}
```

### Test Coverage

- Aim for >80% code coverage
- Focus on critical paths and edge cases
- Test error conditions and exception handling
- Include performance tests for critical operations

## Documentation

### Code Documentation

- Add JavaDoc for all public APIs
- Include usage examples in documentation
- Document complex algorithms or business logic
- Keep documentation up to date with code changes

### API Documentation

- Use OpenAPI/Swagger annotations
- Provide clear request/response examples
- Document error codes and messages
- Include authentication requirements

### README Updates

- Update setup instructions for new features
- Add configuration options
- Include troubleshooting information
- Provide examples and usage scenarios

## Pull Request Process

### Before Submitting

1. **Run the full test suite**
   ```bash
   mvn test
   ./quick-test.sh
   ```

2. **Check code quality**
   ```bash
   # Ensure no compilation warnings
   mvn compile
   
   # Check for common issues
   mvn spotbugs:check
   ```

3. **Update documentation**
   - Update README if needed
   - Add/update API documentation
   - Include migration notes for breaking changes

### PR Template

```markdown
## Description
Brief description of changes and motivation.

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## How Has This Been Tested?
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed
- [ ] Test configuration: 

## Checklist:
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
```

### Review Process

1. **Automated checks** must pass
2. **Code review** by at least one maintainer
3. **Manual testing** if applicable
4. **Documentation review** for user-facing changes

## Issue Reporting

### Bug Reports

Include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Environment details (OS, Java version, etc.)
- Relevant logs or error messages
- Screenshots if applicable

### Feature Requests

Include:
- Clear description of the feature
- Use case and motivation
- Proposed implementation approach
- Alternatives considered
- Additional context

### Security Issues

**Do not** create public issues for security vulnerabilities.
Instead:
1. Email security concerns to [security email]
2. Provide detailed description
3. Include proof of concept if applicable
4. Allow time for fix before disclosure

## Development Environment

### IDE Setup

#### IntelliJ IDEA
1. Import as Maven project
2. Set up code style from Google Java Style Guide
3. Enable annotation processing for Lombok
4. Configure run configurations for services

#### VS Code
1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Configure formatting and linting
4. Set up debug configurations

### Database Setup

#### PostgreSQL
```bash
# Create development database
createdb taskrabbit_users

# Create test database
createdb taskrabbit_users_test
```

#### Docker Alternative
```bash
# Start PostgreSQL in Docker
docker run --name postgres-dev -e POSTGRES_PASSWORD=postgres123 -p 5432:5432 -d postgres:13
```

### Email Testing

For development, you can:
1. Leave email credentials empty (development mode)
2. Use a test email service like MailHog
3. Configure with real SMTP for full testing

## Performance Guidelines

### Database
- Use appropriate indexes
- Optimize query performance
- Use pagination for large result sets
- Monitor slow queries

### API
- Implement proper caching
- Use async processing for heavy operations
- Implement rate limiting
- Monitor response times

### Memory
- Avoid memory leaks
- Use streaming for large data
- Monitor heap usage
- Implement proper resource cleanup

## Security Guidelines

### Authentication
- Use strong password policies
- Implement proper session management
- Add rate limiting for auth endpoints
- Log security events

### Data Protection
- Encrypt sensitive data
- Use HTTPS in production
- Implement proper access controls
- Follow OWASP guidelines

### Dependencies
- Keep dependencies updated
- Scan for vulnerabilities
- Use dependency management tools
- Review security advisories

## Release Process

### Version Management
- Follow semantic versioning (SemVer)
- Tag releases properly
- Maintain changelog
- Document breaking changes

### Deployment
- Test in staging environment
- Create deployment scripts
- Document rollback procedures
- Monitor post-deployment

## Getting Help

- **Documentation**: Check README and docs/
- **Issues**: Search existing issues on GitHub
- **Discussions**: Use GitHub Discussions for questions
- **Chat**: [Add chat platform if available]

## Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md file
- Release notes
- Documentation
- Project README

Thank you for contributing to Mshando! 🚀
