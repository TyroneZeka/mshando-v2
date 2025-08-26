# Mshando Microservices API Documentation for Frontend Developers

**Version:** 1.0.0  
**Date:** August 26, 2025  
**Platform:** Spring Boot 3.1.5 Microservices  

## Table of Contents
1. [Authentication & Authorization](#authentication--authorization)
2. [User Service](#user-service)
3. [Task Service](#task-service)
4. [Bidding Service](#bidding-service)
5. [Payment Service](#payment-service)
6. [Notification Service](#notification-service)
7. [Common Response Formats](#common-response-formats)
8. [Error Handling](#error-handling)
9. [Authentication Flow](#authentication-flow)

---

## Authentication & Authorization

### Base URL Pattern
- **User Service:** `http://localhost:8081/api/v1`
- **Task Service:** `http://localhost:8082/api/v1`
- **Bidding Service:** `http://localhost:8083/api/v1`
- **Payment Service:** `http://localhost:8084/api/v1`
- **Notification Service:** `http://localhost:8085/api/v1`

### Authentication Header
```
Authorization: Bearer {jwt_token}
```

### User Roles
- `ADMIN` - Administrative access
- `CUSTOMER` - Task creators
- `TASKER` - Service providers

---

## User Service

### Authentication Endpoints

#### 1. User Registration
- **POST** `/api/v1/auth/register`
- **Description:** Register a new user account
- **Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "CUSTOMER|TASKER",
  "phoneNumber": "string"
}
```
- **Response:** `201 Created`
```json
{
  "success": true,
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER",
    "emailVerified": false
  }
}
```

#### 2. User Login
- **POST** `/api/v1/auth/login`
- **Description:** Authenticate user and get JWT token
- **Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```
- **Response:** `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "role": "CUSTOMER"
  }
}
```

#### 3. Validate Token
- **GET** `/api/v1/auth/validate`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`
```json
{
  "valid": true,
  "userId": 1,
  "username": "user123",
  "role": "CUSTOMER",
  "expiresAt": "2025-08-27T10:30:00"
}
```

#### 4. Refresh Token
- **POST** `/api/v1/auth/refresh`
- **Request Body:**
```json
{
  "refreshToken": "refresh_token_here"
}
```
- **Response:** `200 OK`
```json
{
  "success": true,
  "token": "new_jwt_token",
  "refreshToken": "new_refresh_token",
  "expiresIn": 3600
}
```

### User Management Endpoints

#### 5. Get Current User Profile
- **GET** `/api/v1/users/me`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`
```json
{
  "id": 1,
  "username": "user123",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER",
  "phoneNumber": "+1234567890",
  "emailVerified": true,
  "createdAt": "2025-08-20T10:00:00",
  "updatedAt": "2025-08-21T15:30:00"
}
```

#### 6. Update User Profile
- **PUT** `/api/v1/users/me`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:**
```json
{
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "bio": "string"
}
```
- **Response:** `200 OK` (Same as Get Current User)

#### 7. Get User by ID
- **GET** `/api/v1/users/{userId}`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK` (Same format as Get Current User)

#### 8. Search Users
- **GET** `/api/v1/users/search?query={searchTerm}&page=0&size=20`
- **Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "username": "user123",
      "firstName": "John",
      "lastName": "Doe",
      "role": "TASKER"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### 9. Delete User (Admin Only)
- **DELETE** `/api/v1/users/{userId}`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `204 No Content`

### Email Verification Endpoints

#### 10. Verify Email
- **GET** `/api/v1/auth/verify-email?token={verificationToken}`
- **Response:** `200 OK`
```json
{
  "success": true,
  "message": "Email verified successfully",
  "status": "SUCCESS"
}
```

#### 11. Resend Verification Email
- **POST** `/api/v1/auth/resend-verification`
- **Request Body:**
```json
{
  "email": "user@example.com"
}
```
- **Response:** `200 OK`
```json
{
  "success": true,
  "message": "Verification email sent successfully",
  "status": "SUCCESS"
}
```

#### 12. Check Verification Status
- **GET** `/api/v1/auth/verification-status?email={email}`
- **Response:** `200 OK`
```json
{
  "email": "user@example.com",
  "verified": true
}
```

---

## Task Service

### Task Management Endpoints

#### 1. Create Task (Customer Only)
- **POST** `/api/v1/tasks`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:**
```json
{
  "title": "string",
  "description": "string",
  "categoryId": 1,
  "budget": 100.00,
  "location": "string",
  "isRemote": false,
  "dueDate": "2025-08-30T10:00:00",
  "requirementsDescription": "string"
}
```
- **Response:** `201 Created`
```json
{
  "id": 1,
  "title": "Website Development",
  "description": "Build a responsive website",
  "categoryId": 1,
  "customerId": 1,
  "budget": 100.00,
  "location": "New York",
  "isRemote": false,
  "status": "DRAFT",
  "dueDate": "2025-08-30T10:00:00",
  "createdAt": "2025-08-26T10:00:00"
}
```

#### 2. Get Task by ID
- **GET** `/api/v1/tasks/{id}`
- **Response:** `200 OK` (Same format as Create Task)

#### 3. Get My Tasks (Customer Only)
- **GET** `/api/v1/tasks/my-tasks?page=0&size=20`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "Website Development",
      "status": "PUBLISHED",
      "budget": 100.00,
      "createdAt": "2025-08-26T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

#### 4. Get My Assignments (Tasker Only)
- **GET** `/api/v1/tasks/my-assignments?page=0&size=20`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK` (Same pagination format)

#### 5. Search Published Tasks
- **GET** `/api/v1/tasks/search?categoryId=1&minBudget=50&maxBudget=500&location=NewYork&isRemote=false&page=0&size=20`
- **Response:** `200 OK` (Same pagination format)

#### 6. Text Search Tasks
- **GET** `/api/v1/tasks/search/text?q=website&page=0&size=20`
- **Response:** `200 OK` (Same pagination format)

#### 7. Update Task (Customer Only)
- **PUT** `/api/v1/tasks/{id}`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:** Same as Create Task
- **Response:** `200 OK`

#### 8. Publish Task (Customer Only)
- **PATCH** `/api/v1/tasks/{id}/publish`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 9. Assign Task (Customer Only)
- **PATCH** `/api/v1/tasks/{id}/assign?taskerId={taskerId}`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 10. Start Task (Tasker Only)
- **PATCH** `/api/v1/tasks/{id}/start`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 11. Complete Task (Tasker Only)
- **PATCH** `/api/v1/tasks/{id}/complete`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 12. Cancel Task (Customer Only)
- **PATCH** `/api/v1/tasks/{id}/cancel`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 13. Delete Task (Customer Only)
- **DELETE** `/api/v1/tasks/{id}`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `204 No Content`

#### 14. Get Tasks Due Soon (Admin Only)
- **GET** `/api/v1/tasks/due-soon?hours=24`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

### Category Management Endpoints

#### 15. Create Category (Admin Only)
- **POST** `/api/v1/categories`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:**
```json
{
  "name": "Web Development",
  "description": "Website and web application development",
  "iconName": "web-icon"
}
```
- **Response:** `201 Created`

#### 16. Get Category by ID
- **GET** `/api/v1/categories/{id}`
- **Response:** `200 OK`

#### 17. Get All Active Categories
- **GET** `/api/v1/categories/active`
- **Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Web Development",
    "description": "Website development services",
    "iconName": "web-icon",
    "active": true
  }
]
```

#### 18. Get Categories with Pagination (Admin Only)
- **GET** `/api/v1/categories?page=0&size=20`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 19. Update Category (Admin Only)
- **PUT** `/api/v1/categories/{id}`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:** Same as Create Category
- **Response:** `200 OK`

#### 20. Deactivate Category (Admin Only)
- **PATCH** `/api/v1/categories/{id}/deactivate`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `204 No Content`

#### 21. Activate Category (Admin Only)
- **PATCH** `/api/v1/categories/{id}/activate`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `204 No Content`

#### 22. Delete Category (Admin Only)
- **DELETE** `/api/v1/categories/{id}`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `204 No Content`

#### 23. Search Categories
- **GET** `/api/v1/categories/search?q=web`
- **Response:** `200 OK`

### Task Image Management Endpoints

#### 24. Upload Task Image (Customer Only)
- **POST** `/api/v1/tasks/{taskId}/images`
- **Headers:** `Authorization: Bearer {token}`, `Content-Type: multipart/form-data`
- **Request Body:** `FormData` with `file` field
- **Response:** `201 Created`
```json
{
  "id": 1,
  "taskId": 1,
  "fileName": "image.jpg",
  "fileUrl": "https://storage.example.com/images/image.jpg",
  "fileSize": 1024000,
  "contentType": "image/jpeg",
  "isPrimary": false,
  "uploadedAt": "2025-08-26T10:00:00"
}
```

#### 25. Get Task Images
- **GET** `/api/v1/tasks/{taskId}/images`
- **Response:** `200 OK`
```json
[
  {
    "id": 1,
    "taskId": 1,
    "fileName": "image.jpg",
    "fileUrl": "https://storage.example.com/images/image.jpg",
    "isPrimary": true
  }
]
```

#### 26. Get Primary Image
- **GET** `/api/v1/tasks/{taskId}/images/primary`
- **Response:** `200 OK` (Same format as single image)

#### 27. Set Primary Image (Customer Only)
- **PATCH** `/api/v1/tasks/{taskId}/images/{imageId}/set-primary`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 28. Delete Task Image (Customer Only)
- **DELETE** `/api/v1/tasks/{taskId}/images/{imageId}`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `204 No Content`

#### 29. Upload Multiple Images (Customer Only)
- **POST** `/api/v1/tasks/{taskId}/images/batch`
- **Headers:** `Authorization: Bearer {token}`, `Content-Type: multipart/form-data`
- **Request Body:** `FormData` with `files[]` field
- **Response:** `201 Created` (Array of image objects)

---

## Bidding Service

### Bid Management Endpoints

#### 1. Create Bid
- **POST** `/api/v1/bids`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:**
```json
{
  "taskId": 1,
  "amount": 75.50,
  "message": "I have extensive experience with this type of work",
  "estimatedCompletionHours": 24
}
```
- **Response:** `201 Created`
```json
{
  "id": 1,
  "taskId": 1,
  "taskerId": 2,
  "customerId": 1,
  "amount": 75.50,
  "message": "I have extensive experience with this type of work",
  "status": "PENDING",
  "estimatedCompletionHours": 24,
  "createdAt": "2025-08-26T10:30:00",
  "updatedAt": "2025-08-26T10:30:00"
}
```

#### 2. Update Bid
- **PUT** `/api/v1/bids/{bidId}`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:**
```json
{
  "amount": 80.00,
  "message": "Updated proposal with better terms",
  "estimatedCompletionHours": 20
}
```
- **Response:** `200 OK`

#### 3. Get Bid by ID
- **GET** `/api/v1/bids/{bidId}`
- **Response:** `200 OK`

#### 4. Get Bids for Task
- **GET** `/api/v1/bids/task/{taskId}?page=0&size=10`
- **Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "taskId": 1,
      "taskerId": 2,
      "amount": 75.50,
      "status": "PENDING",
      "createdAt": "2025-08-26T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

#### 5. Get My Bids
- **GET** `/api/v1/bids/my-bids?page=0&size=10&status=PENDING`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK` (Same pagination format)

#### 6. Get Bids for My Tasks
- **GET** `/api/v1/bids/my-tasks-bids?page=0&size=10&status=PENDING`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK` (Same pagination format)

#### 7. Accept Bid (Customer Only)
- **PATCH** `/api/v1/bids/{bidId}/accept`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 8. Reject Bid (Customer Only)
- **PATCH** `/api/v1/bids/{bidId}/reject`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 9. Withdraw Bid (Tasker Only)
- **PATCH** `/api/v1/bids/{bidId}/withdraw`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 10. Complete Bid (Tasker Only)
- **PATCH** `/api/v1/bids/{bidId}/complete`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`

#### 11. Cancel Bid
- **PATCH** `/api/v1/bids/{bidId}/cancel`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:**
```json
{
  "reason": "Customer requested cancellation"
}
```
- **Response:** `200 OK`

#### 12. Get Bid Statistics
- **GET** `/api/v1/bids/statistics`
- **Headers:** `Authorization: Bearer {token}`
- **Response:** `200 OK`
```json
{
  "totalBids": 10,
  "acceptedBids": 3,
  "pendingBids": 2,
  "rejectedBids": 5,
  "averageBidAmount": 125.50,
  "successRate": 30.0
}
```

#### 13. Get Bid Count for Task
- **GET** `/api/v1/bids/task/{taskId}/count`
- **Response:** `200 OK`
```json
{
  "taskId": 1,
  "totalBids": 5,
  "pendingBids": 2
}
```

---

## Payment Service

### Payment Management Endpoints

#### 1. Create Payment
- **POST** `/api/v1/payments`
- **Headers:** `Authorization: Bearer {token}`
- **Request Body:**
```json
{
  "customerId": 1,
  "taskerId": 2,
  "taskId": 1,
  "amount": 150.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentType": "TASK_PAYMENT",
  "description": "Payment for task completion"
}
```
- **Response:** `201 Created`
```json
{
  "id": 1,
  "customerId": 1,
  "taskerId": 2,
  "taskId": 1,
  "amount": 150.00,
  "serviceFee": 15.00,
  "netAmount": 135.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentType": "TASK_PAYMENT",
  "status": "PENDING",
  "description": "Payment for task completion",
  "createdAt": "2025-08-26T10:30:00",
  "retryCount": 0,
  "maxRetries": 3
}
```

#### 2. Get Payment by ID
- **GET** `/api/v1/payments/{paymentId}`
- **Response:** `200 OK`

#### 3. Get Payment by External Transaction ID
- **GET** `/api/v1/payments/external/{externalTransactionId}`
- **Response:** `200 OK`

#### 4. Process Payment
- **PATCH** `/api/v1/payments/{paymentId}/process`
- **Response:** `200 OK`

#### 5. Complete Payment
- **PATCH** `/api/v1/payments/{paymentId}/complete`
- **Response:** `200 OK`

#### 6. Retry Failed Payment
- **PATCH** `/api/v1/payments/{paymentId}/retry`
- **Response:** `200 OK`

#### 7. Cancel Payment
- **PATCH** `/api/v1/payments/{paymentId}/cancel?reason=Customer request`
- **Response:** `200 OK`

#### 8. Refund Payment
- **POST** `/api/v1/payments/{paymentId}/refund`
- **Request Body:**
```json
{
  "amount": 150.00,
  "reason": "Task not completed",
  "description": "Full refund requested by customer"
}
```
- **Response:** `200 OK`

#### 9. Get Customer Payments
- **GET** `/api/v1/payments/customer/{customerId}?page=0&size=20`
- **Response:** `200 OK` (Paginated payments)

#### 10. Get Tasker Payments
- **GET** `/api/v1/payments/tasker/{taskerId}?page=0&size=20`
- **Response:** `200 OK` (Paginated payments)

#### 11. Get Task Payments
- **GET** `/api/v1/payments/task/{taskId}`
- **Response:** `200 OK`
```json
[
  {
    "id": 1,
    "amount": 150.00,
    "status": "COMPLETED",
    "paymentType": "TASK_PAYMENT",
    "createdAt": "2025-08-26T10:30:00"
  }
]
```

#### 12. Get Payments by Status
- **GET** `/api/v1/payments/status/{status}?page=0&size=20`
- **Response:** `200 OK` (Paginated payments)

#### 13. Get Payments by Status and Type
- **GET** `/api/v1/payments/filter?status=COMPLETED&paymentType=TASK_PAYMENT&page=0&size=20`
- **Response:** `200 OK` (Paginated payments)

#### 14. Get Payments in Date Range
- **GET** `/api/v1/payments/date-range?startDate=2025-08-01T00:00:00&endDate=2025-08-31T23:59:59&page=0&size=20`
- **Response:** `200 OK` (Paginated payments)

#### 15. Get Customer Total Payments
- **GET** `/api/v1/payments/customer/{customerId}/total`
- **Response:** `200 OK`
```json
1250.00
```

#### 16. Get Tasker Total Earnings
- **GET** `/api/v1/payments/tasker/{taskerId}/earnings`
- **Response:** `200 OK`
```json
850.00
```

#### 17. Get Service Fees in Period
- **GET** `/api/v1/payments/service-fees?startDate=2025-08-01T00:00:00&endDate=2025-08-31T23:59:59`
- **Response:** `200 OK`
```json
125.00
```

#### 18. Check Customer Pending Payments
- **GET** `/api/v1/payments/customer/{customerId}/has-pending`
- **Response:** `200 OK`
```json
true
```

#### 19. Check Bid Payments
- **GET** `/api/v1/payments/bid/{bidId}/has-payments`
- **Response:** `200 OK`
```json
false
```

---

## Notification Service

### Notification Endpoints

#### 1. Send Email Notification
- **POST** `/api/v1/notifications/email`
- **Request Body:**
```json
{
  "recipientEmail": "user@example.com",
  "subject": "Task Update",
  "content": "Your task has been updated",
  "templateName": "task_update",
  "variables": {
    "taskTitle": "Website Development",
    "status": "In Progress"
  }
}
```
- **Response:** `202 Accepted`
```json
{
  "id": 1,
  "type": "EMAIL",
  "recipientEmail": "user@example.com",
  "subject": "Task Update",
  "status": "PENDING",
  "createdAt": "2025-08-26T10:30:00"
}
```

#### 2. Send SMS Notification
- **POST** `/api/v1/notifications/sms`
- **Request Body:**
```json
{
  "recipientPhoneNumber": "+1234567890",
  "message": "Your task has been updated",
  "templateName": "task_update_sms",
  "variables": {
    "taskTitle": "Website Development"
  }
}
```
- **Response:** `202 Accepted`

#### 3. Get Notification by ID
- **GET** `/api/v1/notifications/{id}`
- **Response:** `200 OK`

#### 4. Get Notifications by Recipient
- **GET** `/api/v1/notifications/recipient/{recipientId}?page=0&size=20`
- **Response:** `200 OK` (Paginated notifications)

#### 5. Get Notifications by Status
- **GET** `/api/v1/notifications/status/{status}?page=0&size=20`
- **Response:** `200 OK` (Paginated notifications)

#### 6. Get Notifications by Type
- **GET** `/api/v1/notifications/type/{type}?page=0&size=20`
- **Response:** `200 OK` (Paginated notifications)

#### 7. Retry Failed Notification
- **POST** `/api/v1/notifications/{id}/retry`
- **Response:** `202 Accepted`

#### 8. Cancel Notification
- **DELETE** `/api/v1/notifications/{id}`
- **Response:** `204 No Content`

### Template Management Endpoints

#### 9. Create/Update Template
- **POST** `/api/v1/notifications/templates`
- **Request Body:**
```json
{
  "name": "task_update",
  "type": "EMAIL",
  "subject": "Task Update: {{taskTitle}}",
  "content": "Your task {{taskTitle}} status has been updated to {{status}}",
  "description": "Template for task status updates",
  "active": true
}
```
- **Response:** `201 Created`

#### 10. Get Template by Name and Type
- **GET** `/api/v1/notifications/templates/{name}/type/{type}`
- **Response:** `200 OK`

#### 11. Activate Template
- **PUT** `/api/v1/notifications/templates/{id}/activate`
- **Response:** `204 No Content`

#### 12. Deactivate Template
- **PUT** `/api/v1/notifications/templates/{id}/deactivate`
- **Response:** `204 No Content`

---

## Common Response Formats

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {...}
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "details": "Additional error details",
  "timestamp": "2025-08-26T10:30:00"
}
```

### Paginated Response
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## Error Handling

### HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `204 No Content` - Request successful, no content to return
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Access denied
- `404 Not Found` - Resource not found
- `409 Conflict` - Business rule violation
- `413 Payload Too Large` - File size exceeds limit
- `500 Internal Server Error` - Server error

### Common Error Codes
- `INVALID_CREDENTIALS` - Wrong username/password
- `TOKEN_EXPIRED` - JWT token has expired
- `INSUFFICIENT_PERMISSIONS` - User lacks required role
- `RESOURCE_NOT_FOUND` - Requested resource doesn't exist
- `BUSINESS_RULE_VIOLATION` - Action violates business rules
- `VALIDATION_ERROR` - Request data validation failed

---

## Authentication Flow

### 1. User Registration Flow
```
POST /api/auth/register
→ User created (emailVerified: false)
→ Verification email sent
→ GET /api/auth/verify-email?token={token}
→ Email verified
```

### 2. Login Flow
```
POST /api/auth/login
→ Returns JWT token + refresh token
→ Use JWT token in Authorization header for subsequent requests
→ When token expires, use POST /api/auth/refresh
```

### 3. Request Authentication
```javascript
// Headers for authenticated requests
{
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "Content-Type": "application/json"
}
```

### 4. Role-Based Access
- **Public endpoints:** No authentication required
- **User endpoints:** Valid JWT token required
- **Role-specific endpoints:** JWT token + specific role required
- **Admin endpoints:** JWT token + ADMIN role required

---

## Additional Notes

### File Upload Guidelines
- **Supported formats:** JPEG, PNG, GIF, PDF
- **Maximum file size:** 10MB per file
- **Multiple uploads:** Up to 5 files per request
- **File storage:** Cloud storage with CDN URLs returned

### Pagination
- Default page size: 20 items
- Maximum page size: 100 items
- Page numbers are 0-based
- Sort parameters: `sort=field,direction` (e.g., `sort=createdAt,desc`)

### Rate Limiting
- Authentication endpoints: 5 requests per minute
- File upload endpoints: 10 requests per minute
- Other endpoints: 100 requests per minute

### Environment URLs
- **Development:** `http://localhost:808{x}`
- **Staging:** `https://staging-api.mshando.com`
- **Production:** `https://api.mshando.com`

---

**End of Documentation**

For questions or clarifications, contact the backend development team.
