# Task Service API Documentation

## Overview
The Task Service provides a comprehensive REST API for managing tasks, categories, and task images in the mShando platform. It supports task lifecycle management from creation to completion, with role-based access control for customers, taskers, and administrators.

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication
All endpoints require JWT authentication via Bearer token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## User Roles
- **CUSTOMER**: Can create, manage their own tasks, and assign tasks to taskers
- **TASKER**: Can view published tasks, accept assignments, and update task progress
- **ADMIN**: Has full access to all endpoints and administrative functions

---

## Categories API

### Get All Categories
Retrieve all active categories.

**Endpoint:** `GET /categories`  
**Roles:** All authenticated users  
**Response:** 200 OK

```json
[
  {
    "id": 1,
    "name": "Home Cleaning",
    "description": "Professional home cleaning services",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### Get Category by ID
Retrieve a specific category by ID.

**Endpoint:** `GET /categories/{id}`  
**Roles:** All authenticated users  
**Response:** 200 OK / 404 Not Found

```json
{
  "id": 1,
  "name": "Home Cleaning",
  "description": "Professional home cleaning services",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Create Category
Create a new category.

**Endpoint:** `POST /categories`  
**Roles:** ADMIN only  
**Request Body:**

```json
{
  "name": "Garden Maintenance",
  "description": "Professional garden and lawn care services"
}
```

**Response:** 201 Created

```json
{
  "id": 2,
  "name": "Garden Maintenance",
  "description": "Professional garden and lawn care services",
  "isActive": true,
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

### Update Category
Update an existing category.

**Endpoint:** `PUT /categories/{id}`  
**Roles:** ADMIN only  
**Request Body:**

```json
{
  "name": "Premium Home Cleaning",
  "description": "Premium professional home cleaning services"
}
```

**Response:** 200 OK / 404 Not Found

### Delete Category
Soft delete a category (sets isActive to false).

**Endpoint:** `DELETE /categories/{id}`  
**Roles:** ADMIN only  
**Response:** 204 No Content / 404 Not Found

---

## Tasks API

### Create Task
Create a new task in DRAFT status.

**Endpoint:** `POST /tasks`  
**Roles:** CUSTOMER  
**Request Body:**

```json
{
  "title": "Clean my 3-bedroom house",
  "description": "Need thorough cleaning of all rooms, kitchen, and bathrooms",
  "categoryId": 1,
  "priority": "MEDIUM",
  "budget": 150.00,
  "location": "123 Main St, Springfield",
  "isRemote": false,
  "dueDate": "2024-01-25T14:00:00"
}
```

**Response:** 201 Created

```json
{
  "id": 1,
  "title": "Clean my 3-bedroom house",
  "description": "Need thorough cleaning of all rooms, kitchen, and bathrooms",
  "category": {
    "id": 1,
    "name": "Home Cleaning"
  },
  "customerId": 100,
  "status": "DRAFT",
  "priority": "MEDIUM",
  "budget": 150.00,
  "location": "123 Main St, Springfield",
  "isRemote": false,
  "dueDate": "2024-01-25T14:00:00",
  "createdAt": "2024-01-15T12:00:00",
  "updatedAt": "2024-01-15T12:00:00"
}
```

### Get Task by ID
Retrieve a specific task by ID.

**Endpoint:** `GET /tasks/{id}`  
**Roles:** All authenticated users  
**Response:** 200 OK / 404 Not Found

```json
{
  "id": 1,
  "title": "Clean my 3-bedroom house",
  "description": "Need thorough cleaning of all rooms, kitchen, and bathrooms",
  "category": {
    "id": 1,
    "name": "Home Cleaning"
  },
  "customerId": 100,
  "assignedTaskerId": 200,
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "budget": 150.00,
  "location": "123 Main St, Springfield",
  "isRemote": false,
  "dueDate": "2024-01-25T14:00:00",
  "publishedAt": "2024-01-15T13:00:00",
  "assignedAt": "2024-01-15T14:00:00",
  "startedAt": "2024-01-15T15:00:00",
  "createdAt": "2024-01-15T12:00:00",
  "updatedAt": "2024-01-15T15:00:00"
}
```

### Update Task
Update a task (only allowed for DRAFT tasks).

**Endpoint:** `PUT /tasks/{id}`  
**Roles:** CUSTOMER (task owner only)  
**Request Body:**

```json
{
  "title": "Clean my 4-bedroom house",
  "description": "Updated: Need thorough cleaning of all rooms, kitchen, bathrooms, and study",
  "categoryId": 1,
  "priority": "HIGH",
  "budget": 200.00,
  "location": "123 Main St, Springfield",
  "isRemote": false,
  "dueDate": "2024-01-25T14:00:00"
}
```

**Response:** 200 OK / 400 Bad Request / 403 Forbidden / 404 Not Found

### Get Tasks by Customer
Get paginated list of tasks for a specific customer.

**Endpoint:** `GET /tasks/customer/{customerId}`  
**Roles:** CUSTOMER (own tasks only), ADMIN  
**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)
- `status`: Filter by status (optional)

**Response:** 200 OK

```json
{
  "content": [
    {
      "id": 1,
      "title": "Clean my 3-bedroom house",
      "status": "DRAFT",
      "priority": "MEDIUM",
      "budget": 150.00,
      "createdAt": "2024-01-15T12:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Get Tasks by Tasker
Get paginated list of tasks assigned to a specific tasker.

**Endpoint:** `GET /tasks/tasker/{taskerId}`  
**Roles:** TASKER (own assignments only), ADMIN  
**Query Parameters:** Same as customer tasks

**Response:** 200 OK (same format as customer tasks)

### Publish Task
Publish a task to make it available for taskers.

**Endpoint:** `PATCH /tasks/{id}/publish`  
**Roles:** CUSTOMER (task owner only)  
**Response:** 200 OK / 400 Bad Request / 403 Forbidden / 404 Not Found

```json
{
  "id": 1,
  "status": "PUBLISHED",
  "publishedAt": "2024-01-15T13:00:00",
  "updatedAt": "2024-01-15T13:00:00"
}
```

### Assign Task
Assign a published task to a tasker.

**Endpoint:** `PATCH /tasks/{id}/assign`  
**Roles:** CUSTOMER (task owner only)  
**Query Parameters:**
- `taskerId`: ID of the tasker to assign (required)

**Response:** 200 OK / 400 Bad Request / 403 Forbidden / 404 Not Found

```json
{
  "id": 1,
  "status": "ASSIGNED",
  "assignedTaskerId": 200,
  "assignedAt": "2024-01-15T14:00:00",
  "updatedAt": "2024-01-15T14:00:00"
}
```

### Start Task
Start working on an assigned task.

**Endpoint:** `PATCH /tasks/{id}/start`  
**Roles:** TASKER (assigned tasker only)  
**Response:** 200 OK / 400 Bad Request / 403 Forbidden / 404 Not Found

```json
{
  "id": 1,
  "status": "IN_PROGRESS",
  "startedAt": "2024-01-15T15:00:00",
  "updatedAt": "2024-01-15T15:00:00"
}
```

### Complete Task
Mark a task as completed.

**Endpoint:** `PATCH /tasks/{id}/complete`  
**Roles:** TASKER (assigned tasker only)  
**Response:** 200 OK / 400 Bad Request / 403 Forbidden / 404 Not Found

```json
{
  "id": 1,
  "status": "COMPLETED",
  "completedAt": "2024-01-15T18:00:00",
  "updatedAt": "2024-01-15T18:00:00"
}
```

### Cancel Task
Cancel a task.

**Endpoint:** `PATCH /tasks/{id}/cancel`  
**Roles:** CUSTOMER (task owner only), ADMIN  
**Response:** 200 OK / 400 Bad Request / 403 Forbidden / 404 Not Found

```json
{
  "id": 1,
  "status": "CANCELLED",
  "cancelledAt": "2024-01-15T16:00:00",
  "updatedAt": "2024-01-15T16:00:00"
}
```

### Delete Task
Delete a draft task.

**Endpoint:** `DELETE /tasks/{id}`  
**Roles:** CUSTOMER (task owner only), ADMIN  
**Response:** 204 No Content / 400 Bad Request / 403 Forbidden / 404 Not Found

### Search Published Tasks
Search for available published tasks.

**Endpoint:** `GET /tasks/search`  
**Roles:** TASKER, ADMIN  
**Query Parameters:**
- `categoryId`: Filter by category (optional)
- `minBudget`: Minimum budget (optional)
- `maxBudget`: Maximum budget (optional)
- `location`: Location keyword search (optional)
- `isRemote`: Filter by remote tasks (optional)
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)

**Response:** 200 OK

```json
{
  "content": [
    {
      "id": 1,
      "title": "Clean my 3-bedroom house",
      "description": "Need thorough cleaning of all rooms, kitchen, and bathrooms",
      "category": {
        "id": 1,
        "name": "Home Cleaning"
      },
      "customerId": 100,
      "status": "PUBLISHED",
      "priority": "MEDIUM",
      "budget": 150.00,
      "location": "123 Main St, Springfield",
      "isRemote": false,
      "dueDate": "2024-01-25T14:00:00",
      "publishedAt": "2024-01-15T13:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Get Tasks Due Soon
Get tasks that are due within specified hours (admin only).

**Endpoint:** `GET /tasks/due-soon`  
**Roles:** ADMIN only  
**Query Parameters:**
- `hours`: Hours from now (default: 24)

**Response:** 200 OK

```json
[
  {
    "id": 1,
    "title": "Clean my 3-bedroom house",
    "customerId": 100,
    "assignedTaskerId": 200,
    "status": "IN_PROGRESS",
    "dueDate": "2024-01-15T20:00:00",
    "remainingHours": 2
  }
]
```

---

## Task Images API

### Upload Task Image
Upload an image for a task.

**Endpoint:** `POST /tasks/{taskId}/images`  
**Roles:** CUSTOMER (task owner only)  
**Content-Type:** `multipart/form-data`  
**Form Parameters:**
- `file`: Image file (JPG, PNG, GIF, WebP, max 5MB)

**Response:** 201 Created

```json
{
  "id": 1,
  "taskId": 1,
  "fileName": "task_1_20240115_120000_abc12345.jpg",
  "originalFileName": "house_photo.jpg",
  "fileSize": 2048576,
  "contentType": "image/jpeg",
  "isPrimary": true,
  "createdAt": "2024-01-15T12:00:00"
}
```

### Get Task Images
Get all images for a task.

**Endpoint:** `GET /tasks/{taskId}/images`  
**Roles:** All authenticated users  
**Response:** 200 OK

```json
[
  {
    "id": 1,
    "taskId": 1,
    "fileName": "task_1_20240115_120000_abc12345.jpg",
    "originalFileName": "house_photo.jpg",
    "fileSize": 2048576,
    "contentType": "image/jpeg",
    "isPrimary": true,
    "createdAt": "2024-01-15T12:00:00"
  }
]
```

### Get Primary Image
Get the primary image for a task.

**Endpoint:** `GET /tasks/{taskId}/images/primary`  
**Roles:** All authenticated users  
**Response:** 200 OK / 404 Not Found

```json
{
  "id": 1,
  "taskId": 1,
  "fileName": "task_1_20240115_120000_abc12345.jpg",
  "originalFileName": "house_photo.jpg",
  "fileSize": 2048576,
  "contentType": "image/jpeg",
  "isPrimary": true,
  "createdAt": "2024-01-15T12:00:00"
}
```

### Set Primary Image
Set an image as the primary image for a task.

**Endpoint:** `PATCH /tasks/{taskId}/images/{imageId}/primary`  
**Roles:** CUSTOMER (task owner only)  
**Response:** 200 OK / 403 Forbidden / 404 Not Found

```json
{
  "id": 2,
  "taskId": 1,
  "fileName": "task_1_20240115_130000_def67890.jpg",
  "originalFileName": "kitchen_photo.jpg",
  "fileSize": 1536000,
  "contentType": "image/jpeg",
  "isPrimary": true,
  "createdAt": "2024-01-15T13:00:00"
}
```

### Delete Task Image
Delete an image from a task.

**Endpoint:** `DELETE /tasks/{taskId}/images/{imageId}`  
**Roles:** CUSTOMER (task owner only)  
**Response:** 204 No Content / 403 Forbidden / 404 Not Found

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tasks",
  "errors": [
    {
      "field": "title",
      "message": "Title cannot be empty"
    },
    {
      "field": "budget",
      "message": "Budget must be positive"
    }
  ]
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T12:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid",
  "path": "/api/v1/tasks"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T12:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "User not authorized to access this resource",
  "path": "/api/v1/tasks/1"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with ID: 1",
  "path": "/api/v1/tasks/1"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T12:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/v1/tasks"
}
```

---

## Task Status Flow

Tasks follow this status lifecycle:

1. **DRAFT** → **PUBLISHED** (via publish endpoint)
2. **PUBLISHED** → **ASSIGNED** (via assign endpoint)
3. **ASSIGNED** → **IN_PROGRESS** (via start endpoint)
4. **IN_PROGRESS** → **COMPLETED** (via complete endpoint)

Alternative flows:
- **DRAFT** → **CANCELLED** (via cancel endpoint)
- **PUBLISHED** → **CANCELLED** (via cancel endpoint)
- **ASSIGNED** → **CANCELLED** (via cancel endpoint)
- **DRAFT** → **DELETED** (via delete endpoint)

---

## File Upload Constraints

### Supported Image Types
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

### File Size Limits
- Maximum file size: 5MB per image
- Maximum images per task: 5 images

### File Naming
- Files are automatically renamed with unique identifiers
- Original filename is preserved in metadata
- Format: `task_{taskId}_{timestamp}_{uuid}.{extension}`

---

## Rate Limiting and Pagination

### Default Pagination
- Default page size: 20 items
- Maximum page size: 100 items
- Pages are 0-indexed

### Rate Limiting
- API endpoints are rate-limited to prevent abuse
- Standard rate limit: 100 requests per minute per user
- File upload limit: 10 uploads per minute per user

---

## OpenAPI/Swagger Documentation

Interactive API documentation is available at:
```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:
```
http://localhost:8080/v3/api-docs
```
