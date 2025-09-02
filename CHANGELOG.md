# Changelog

All notable changes to the Mshando Backend will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0] - 2025-09-02

### Added
- **JWT Authentication Filter**: Implemented comprehensive JWT authentication filter for task service
- **Internal Task Controller**: Added InternalTaskController for secure microservice communication
- **Enhanced Security Configuration**: Improved SecurityConfig with proper JWT authentication handling
- **Task Validation**: Enhanced TaskCreateRequestDTO with missing validation fields

### Security
- **JWT Token Processing**: Enhanced JwtTokenUtil for consistent token processing across services
- **CORS Configuration**: Improved CORS configuration for bidding service security
- **Authentication Filter**: Added JwtAuthenticationFilter for task service request validation
- **Security Chain**: Updated security filter chain with proper authentication order

### Fixed
- **Task Creation Validation**: Added missing requirementsDescription and estimatedDurationHours fields
- **DTO Validation**: Improved TaskCreateRequestDTO with @NotBlank and @NotNull annotations
- **Authentication Flow**: Fixed JWT authentication flow in task service
- **CORS Issues**: Resolved cross-origin request handling in bidding service

### Changed
- **Security Architecture**: Refactored security configuration for better microservice integration
- **Task Service**: Enhanced task service with improved validation and security
- **Bidding Service**: Updated bidding service with better CORS and security handling
- **JWT Service**: Standardized JWT service implementation across microservices

### Infrastructure
- **Microservice Security**: Improved security integration between microservices
- **Token Validation**: Enhanced token validation and user extraction
- **Error Handling**: Better error handling for authentication failures
- **Service Communication**: Secure internal communication between services

### Technical Improvements
- Consistent JWT handling across all microservices
- Enhanced validation for task creation requests
- Improved security filter configuration
- Better separation of concerns in authentication
- Enhanced microservice architecture

### Database
- **Validation**: Enhanced database validation for task entities
- **Field Mapping**: Improved field mapping for task creation
- **Error Handling**: Better database error handling and validation

## [1.2.0] - Previous Release
- Task service implementation
- Basic authentication system
- Microservice architecture setup

## [1.1.0] - Previous Release
- Initial microservice structure
- Basic user authentication
- Core service implementations
