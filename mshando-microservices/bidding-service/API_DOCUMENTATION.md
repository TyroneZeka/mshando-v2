# 🎯 Mshando Bidding Service API Documentation

## 📋 Table of Contents

- [Overview](#-overview)
- [Quick Start](#-quick-start)
- [Authentication](#-authentication)
- [API Endpoints](#-api-endpoints)
- [Data Models](#-data-models)
- [Error Handling](#-error-handling)
- [Examples](#-examples)
- [Business Rules](#-business-rules)

## 🌟 Overview

The **Mshando Bidding Service** is a comprehensive RESTful API for managing task bids in the Mshando marketplace platform. It provides complete lifecycle management for bids, from creation to completion.

### Key Features

- 📝 **Bid Management**: Create, update, accept, reject, and withdraw bids
- 📊 **Status Tracking**: Complete lifecycle management of bid statuses
- 📄 **Pagination**: Efficient retrieval of large bid datasets
- ✅ **Validation**: Business rule enforcement and data validation
- 🔗 **Integration**: Seamless integration with task and user services
- 📈 **Statistics**: Comprehensive bidding analytics and metrics

### Service Information

- **Base URL**: `http://localhost:8083` (Development)
- **Production URL**: `https://api.mshando.com/bidding`
- **API Version**: v1
- **Documentation**: Available at `/swagger-ui.html`

## 🚀 Quick Start

### 1. Authentication

All API endpoints require JWT authentication:

```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
     https://api.mshando.com/bidding/api/v1/bids
```

### 2. Create Your First Bid

```bash
curl -X POST \
  http://localhost:8083/api/v1/bids \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": 123,
    "amount": 75.50,
    "message": "I have extensive experience with this type of work.",
    "estimatedCompletionHours": 24
  }'
```

### 3. Check Bid Status

```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
     http://localhost:8083/api/v1/bids/456
```

## 🔐 Authentication

The API uses **JWT (JSON Web Token)** authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### How to get a JWT token:

1. Register/Login through the user service
2. Receive JWT token in response
3. Include token in all API requests

## 🛠 API Endpoints

### 📝 Bid Creation & Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/api/v1/bids` | Create a new bid | ✅ Required |
| `PUT` | `/api/v1/bids/{bidId}` | Update existing bid | ✅ Required |
| `GET` | `/api/v1/bids/{bidId}` | Get bid details | ✅ Required |

### 📋 Bid Retrieval

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/api/v1/bids/task/{taskId}` | Get all bids for a task | ✅ Required |
| `GET` | `/api/v1/bids/my-bids` | Get tasker's own bids | ✅ Required |
| `GET` | `/api/v1/bids/my-tasks-bids` | Get bids for tasker's tasks | ✅ Required |

### ⚡ Bid Actions

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `PATCH` | `/api/v1/bids/{bidId}/accept` | Accept a bid | ✅ Required |
| `PATCH` | `/api/v1/bids/{bidId}/reject` | Reject a bid | ✅ Required |
| `PATCH` | `/api/v1/bids/{bidId}/withdraw` | Withdraw a bid | ✅ Required |
| `PATCH` | `/api/v1/bids/{bidId}/complete` | Mark bid as completed | ✅ Required |
| `PATCH` | `/api/v1/bids/{bidId}/cancel` | Cancel an accepted bid | ✅ Required |

### 📊 Analytics & Statistics

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/api/v1/bids/statistics` | Get bidding statistics | ✅ Required |
| `GET` | `/api/v1/bids/task/{taskId}/count` | Get bid count for task | ✅ Required |

## 📊 Data Models

### BidCreateDTO

```json
{
  "taskId": 123,
  "amount": 75.50,
  "message": "I have extensive experience with this type of work.",
  "estimatedCompletionHours": 24
}
```

**Validation Rules:**
- `taskId`: Required, must be valid task ID
- `amount`: Required, $5.00 - $10,000.00
- `message`: Optional, max 1000 characters
- `estimatedCompletionHours`: Required, 1-720 hours

### BidResponseDTO

```json
{
  "id": 456,
  "taskId": 123,
  "taskerId": 789,
  "customerId": 101,
  "amount": 75.50,
  "message": "I have extensive experience with this type of work.",
  "status": "PENDING",
  "estimatedCompletionHours": 24,
  "createdAt": "2025-08-21T10:30:00",
  "updatedAt": "2025-08-21T10:30:00",
  "version": 1
}
```

### Bid Status Lifecycle

```
PENDING → ACCEPTED → COMPLETED
    ↓         ↓         ↓
REJECTED  CANCELLED  WITHDRAWN
```

**Status Descriptions:**
- 🕐 **PENDING**: Awaiting customer decision
- ✅ **ACCEPTED**: Approved by customer
- ❌ **REJECTED**: Declined by customer
- 🔙 **WITHDRAWN**: Cancelled by tasker
- 🎉 **COMPLETED**: Work finished and verified
- ⚠️ **CANCELLED**: Accepted bid was cancelled

## ❌ Error Handling

### Standard Error Response Format

```json
{
  "timestamp": "2025-08-21T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/v1/bids",
  "details": {
    "amount": "Bid amount is required",
    "taskId": "Task ID is required"
  }
}
```

### HTTP Status Codes

| Code | Description | Example |
|------|-------------|---------|
| `200` | ✅ Success | Request completed successfully |
| `201` | ✅ Created | Bid created successfully |
| `400` | ❌ Bad Request | Validation failed |
| `401` | 🔒 Unauthorized | JWT token required |
| `403` | 🚫 Forbidden | Not authorized for this action |
| `404` | ❓ Not Found | Bid/resource not found |
| `409` | ⚠️ Conflict | Business rule violation |
| `500` | 🔥 Server Error | Internal server error |

## 📝 Examples

### Create a Bid

**Request:**
```bash
POST /api/v1/bids
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "taskId": 123,
  "amount": 85.00,
  "message": "I can complete this task efficiently with my experience.",
  "estimatedCompletionHours": 18
}
```

**Response:**
```json
{
  "id": 456,
  "taskId": 123,
  "taskerId": 789,
  "customerId": 101,
  "amount": 85.00,
  "message": "I can complete this task efficiently with my experience.",
  "status": "PENDING",
  "estimatedCompletionHours": 18,
  "createdAt": "2025-08-21T10:30:00",
  "updatedAt": "2025-08-21T10:30:00",
  "version": 1
}
```

### Accept a Bid

**Request:**
```bash
PATCH /api/v1/bids/456/accept
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "id": 456,
  "status": "ACCEPTED",
  "acceptedAt": "2025-08-21T14:30:00",
  "updatedAt": "2025-08-21T14:30:00",
  "version": 2
}
```

### Get Task Bids (Paginated)

**Request:**
```bash
GET /api/v1/bids/task/123?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "content": [
    {
      "id": 456,
      "taskId": 123,
      "amount": 85.00,
      "status": "PENDING",
      "taskerInfo": {
        "id": 789,
        "name": "John Doe",
        "rating": 4.8
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 5,
  "totalPages": 1
}
```

## 📋 Business Rules

### ✅ Bid Creation Rules

- **One Bid Per Task**: Each tasker can only place one bid per task
- **Amount Limits**: $5.00 minimum, $10,000.00 maximum
- **Time Limits**: 1-720 hours (1 hour to 30 days)
- **Message Length**: Maximum 1000 characters

### ⚡ Status Transition Rules

| Current Status | Allowed Transitions | Who Can Perform |
|----------------|-------------------|-----------------|
| `PENDING` | `ACCEPTED`, `REJECTED`, `WITHDRAWN` | Customer (accept/reject), Tasker (withdraw) |
| `ACCEPTED` | `COMPLETED`, `CANCELLED` | Tasker (complete), Both (cancel) |
| `REJECTED` | None | - |
| `WITHDRAWN` | None | - |
| `COMPLETED` | None | - |
| `CANCELLED` | None | - |

### 🔒 Permission Rules

- **Bid Creation**: Only authenticated taskers
- **Bid Updates**: Only bid owner, only for PENDING bids
- **Accept/Reject**: Only task owner, only for PENDING bids
- **Withdraw**: Only bid owner, for PENDING or ACCEPTED bids
- **Complete**: Only bid owner, only for ACCEPTED bids
- **Cancel**: Task owner or bid owner, only for ACCEPTED bids

### 🚀 Advanced Features

- **Auto-Accept**: Configurable auto-acceptance of old pending bids
- **Notifications**: Real-time updates for status changes
- **Analytics**: Comprehensive bidding statistics
- **Caching**: Performance optimization for frequently accessed data

## 🛡️ Security

- **Authentication**: JWT tokens required for all endpoints
- **Authorization**: Role-based access control
- **Validation**: Comprehensive input validation
- **Rate Limiting**: Protection against abuse
- **CORS**: Configured for cross-origin requests

## 📚 Additional Resources

- **Swagger UI**: `/swagger-ui.html` - Interactive API documentation
- **OpenAPI Spec**: `/v3/api-docs` - Machine-readable API specification
- **Health Check**: `/actuator/health` - Service health status
- **Metrics**: `/actuator/metrics` - Performance metrics

## 🆘 Support

For API support or questions:

- **Email**: dev@mshando.com
- **GitHub**: https://github.com/mshando/bidding-service
- **Documentation**: This guide and Swagger UI

---

*Happy bidding! 🎯*
