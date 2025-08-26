# Payment Service API Documentation

## Overview

The Mshando Payment Service provides comprehensive payment processing capabilities for the platform. This API enables secure payment creation, processing, refunds, and financial analytics.

## Base URL
```
Production: https://api.mshando.com/payment-service
Staging: https://api-staging.mshando.com/payment-service
Development: http://localhost:8083
```

## Authentication

All API endpoints require JWT Bearer token authentication:

```http
Authorization: Bearer {your-jwt-token}
```

## Payment Models

### Payment Entity
```json
{
  "id": 123,
  "customerId": 456,
  "taskerId": 789,
  "taskId": 101,
  "bidId": 555,
  "amount": 150.00,
  "serviceFee": 15.00,
  "netAmount": 135.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentType": "TASK_PAYMENT",
  "status": "COMPLETED",
  "description": "Payment for task completion",
  "externalTransactionId": "pi_1J2K3L4M5N6O7P8Q",
  "failureReason": null,
  "createdAt": "2025-08-21T10:30:00Z",
  "updatedAt": "2025-08-21T10:35:00Z",
  "processedAt": "2025-08-21T10:35:00Z",
  "retryCount": 0,
  "maxRetries": 3,
  "nextRetryAt": null
}
```

### Payment Status
- `PENDING` - Payment created but not processed
- `PROCESSING` - Payment being processed by provider
- `COMPLETED` - Payment successfully processed
- `FAILED` - Payment processing failed
- `CANCELLED` - Payment cancelled before processing
- `REFUNDED` - Payment has been refunded

### Payment Types
- `TASK_PAYMENT` - Payment for completed tasks
- `SERVICE_FEE` - Platform service charges
- `DEPOSIT` - Adding funds to platform balance
- `REFUND` - Refunding previous payments

### Payment Methods
- `CREDIT_CARD` - Credit or debit card
- `BANK_TRANSFER` - Direct bank transfer
- `DIGITAL_WALLET` - PayPal, Apple Pay, etc.
- `PLATFORM_CREDIT` - Internal platform credits

## API Endpoints

### 1. Create Payment

Creates a new payment for processing.

**Endpoint:** `POST /api/v1/payments`

**Request Body:**
```json
{
  "customerId": 456,
  "taskerId": 789,
  "taskId": 101,
  "bidId": 555,
  "amount": 150.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentType": "TASK_PAYMENT",
  "description": "Payment for task completion"
}
```

**Response:** `201 Created`
```json
{
  "id": 123,
  "customerId": 456,
  "taskerId": 789,
  "taskId": 101,
  "bidId": 555,
  "amount": 150.00,
  "serviceFee": 15.00,
  "netAmount": 135.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentType": "TASK_PAYMENT",
  "status": "PENDING",
  "description": "Payment for task completion",
  "createdAt": "2025-08-21T10:30:00Z",
  "retryCount": 0,
  "maxRetries": 3
}
```

### 2. Get Payment by ID

Retrieves payment details by ID.

**Endpoint:** `GET /api/v1/payments/{paymentId}`

**Response:** `200 OK`
```json
{
  "id": 123,
  "customerId": 456,
  "taskerId": 789,
  "amount": 150.00,
  "status": "COMPLETED",
  "createdAt": "2025-08-21T10:30:00Z"
}
```

### 3. Process Payment

Manually triggers payment processing.

**Endpoint:** `PATCH /api/v1/payments/{paymentId}/process`

**Response:** `200 OK`
```json
{
  "id": 123,
  "status": "PROCESSING",
  "processedAt": "2025-08-21T10:35:00Z"
}
```

### 4. Complete Payment

Marks a processing payment as completed.

**Endpoint:** `PATCH /api/v1/payments/{paymentId}/complete`

**Response:** `200 OK`
```json
{
  "id": 123,
  "status": "COMPLETED",
  "processedAt": "2025-08-21T10:35:00Z"
}
```

### 5. Cancel Payment

Cancels a pending payment.

**Endpoint:** `PATCH /api/v1/payments/{paymentId}/cancel?reason={reason}`

**Response:** `200 OK`
```json
{
  "id": 123,
  "status": "CANCELLED",
  "failureReason": "Customer request"
}
```

### 6. Retry Payment

Retries a failed payment.

**Endpoint:** `PATCH /api/v1/payments/{paymentId}/retry`

**Response:** `200 OK`
```json
{
  "id": 123,
  "status": "PENDING",
  "retryCount": 1,
  "nextRetryAt": "2025-08-21T10:40:00Z"
}
```

### 7. Process Refund

Processes a refund for a completed payment.

**Endpoint:** `POST /api/v1/payments/{paymentId}/refund`

**Request Body:**
```json
{
  "amount": 75.00,
  "reason": "Service not satisfactory",
  "refundType": "PARTIAL"
}
```

**Response:** `200 OK`
```json
{
  "id": 124,
  "originalPaymentId": 123,
  "amount": 75.00,
  "paymentType": "REFUND",
  "status": "COMPLETED",
  "description": "Refund: Service not satisfactory"
}
```

### 8. Get Customer Payments

Retrieves paginated list of customer payments.

**Endpoint:** `GET /api/v1/payments/customer/{customerId}?page=0&size=20`

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 123,
      "amount": 150.00,
      "status": "COMPLETED",
      "createdAt": "2025-08-21T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 9. Get Tasker Payments

Retrieves paginated list of tasker payments.

**Endpoint:** `GET /api/v1/payments/tasker/{taskerId}?page=0&size=20`

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 123,
      "amount": 150.00,
      "netAmount": 135.00,
      "status": "COMPLETED",
      "createdAt": "2025-08-21T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 10. Get Task Payments

Retrieves all payments for a specific task.

**Endpoint:** `GET /api/v1/payments/task/{taskId}`

**Response:** `200 OK`
```json
[
  {
    "id": 123,
    "amount": 150.00,
    "status": "COMPLETED",
    "paymentType": "TASK_PAYMENT",
    "createdAt": "2025-08-21T10:30:00Z"
  }
]
```

### 11. Get Payments by Status

Retrieves payments filtered by status.

**Endpoint:** `GET /api/v1/payments/status/{status}?page=0&size=20`

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 123,
      "status": "COMPLETED",
      "amount": 150.00
    }
  ],
  "totalElements": 1
}
```

### 12. Get Payments in Date Range

Retrieves payments within a date range.

**Endpoint:** `GET /api/v1/payments/date-range?startDate=2025-08-01T00:00:00&endDate=2025-08-31T23:59:59&page=0&size=20`

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 123,
      "amount": 150.00,
      "createdAt": "2025-08-21T10:30:00Z"
    }
  ],
  "totalElements": 5
}
```

## Financial Analytics

### Customer Total Payments

**Endpoint:** `GET /api/v1/payments/customer/{customerId}/total`

**Response:** `200 OK`
```json
1250.00
```

### Tasker Total Earnings

**Endpoint:** `GET /api/v1/payments/tasker/{taskerId}/earnings`

**Response:** `200 OK`
```json
2340.00
```

### Service Fees in Period

**Endpoint:** `GET /api/v1/payments/service-fees?startDate=2025-08-01T00:00:00&endDate=2025-08-31T23:59:59`

**Response:** `200 OK`
```json
450.00
```

## Validation Endpoints

### Check Customer Pending Payments

**Endpoint:** `GET /api/v1/payments/customer/{customerId}/has-pending`

**Response:** `200 OK`
```json
true
```

### Check Bid Payments

**Endpoint:** `GET /api/v1/payments/bid/{bidId}/has-payments`

**Response:** `200 OK`
```json
false
```

## Error Responses

### Standard Error Format
```json
{
  "timestamp": "2025-08-21T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid payment amount",
  "path": "/api/v1/payments"
}
```

### Validation Error Format
```json
{
  "timestamp": "2025-08-21T10:30:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request data",
  "path": "/api/v1/payments",
  "fieldErrors": {
    "amount": "Amount must be greater than 0.01",
    "customerId": "Customer ID is required"
  }
}
```

### Common Error Codes

| Status Code | Error | Description |
|-------------|-------|-------------|
| 400 | Bad Request | Invalid request data or business rule violation |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Insufficient permissions for operation |
| 404 | Not Found | Payment or related entity not found |
| 409 | Conflict | Operation conflicts with current payment state |
| 422 | Unprocessable Entity | Valid request but cannot be processed |
| 500 | Internal Server Error | Unexpected server error |

## Rate Limiting

API endpoints are rate limited to prevent abuse:
- **Payment Creation**: 10 requests per minute per user
- **Payment Queries**: 100 requests per minute per user
- **Analytics**: 50 requests per minute per user

Rate limit headers are included in responses:
```http
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 8
X-RateLimit-Reset: 1629550800
```

## Webhooks

The payment service supports webhooks for real-time payment status updates.

### Webhook Events
- `payment.created` - New payment created
- `payment.processing` - Payment processing started
- `payment.completed` - Payment successfully completed
- `payment.failed` - Payment processing failed
- `payment.cancelled` - Payment cancelled
- `payment.refunded` - Payment refunded

### Webhook Payload
```json
{
  "eventType": "payment.completed",
  "timestamp": "2025-08-21T10:35:00Z",
  "paymentId": 123,
  "payment": {
    "id": 123,
    "customerId": 456,
    "amount": 150.00,
    "status": "COMPLETED"
  }
}
```

## SDK Examples

### JavaScript/Node.js
```javascript
const paymentService = new PaymentService({
  baseURL: 'https://api.mshando.com/payment-service',
  authToken: 'your-jwt-token'
});

// Create payment
const payment = await paymentService.createPayment({
  customerId: 456,
  taskerId: 789,
  taskId: 101,
  amount: 150.00,
  paymentMethod: 'CREDIT_CARD',
  paymentType: 'TASK_PAYMENT'
});

// Get payment status
const status = await paymentService.getPayment(payment.id);
```

### Python
```python
from mshando_payment_client import PaymentClient

client = PaymentClient(
    base_url='https://api.mshando.com/payment-service',
    auth_token='your-jwt-token'
)

# Create payment
payment = client.create_payment({
    'customerId': 456,
    'taskerId': 789,
    'taskId': 101,
    'amount': 150.00,
    'paymentMethod': 'CREDIT_CARD',
    'paymentType': 'TASK_PAYMENT'
})

# Process refund
refund = client.refund_payment(payment['id'], {
    'amount': 75.00,
    'reason': 'Customer request',
    'refundType': 'PARTIAL'
})
```

### Java
```java
PaymentServiceClient client = PaymentServiceClient.builder()
    .baseUrl("https://api.mshando.com/payment-service")
    .authToken("your-jwt-token")
    .build();

// Create payment
PaymentCreateDTO createRequest = PaymentCreateDTO.builder()
    .customerId(456L)
    .taskerId(789L)
    .taskId(101L)
    .amount(new BigDecimal("150.00"))
    .paymentMethod(PaymentMethod.CREDIT_CARD)
    .paymentType(PaymentType.TASK_PAYMENT)
    .build();

PaymentResponseDTO payment = client.createPayment(createRequest);

// Get customer total
BigDecimal total = client.getCustomerTotalPayments(456L);
```

## Testing

### Sandbox Environment
Use the sandbox environment for testing:
```
Base URL: https://api-sandbox.mshando.com/payment-service
```

### Test Cards
```
Visa: 4242424242424242
Mastercard: 5555555555554444
Amex: 378282246310005
Declined: 4000000000000002
```

### Test Webhooks
Use webhook testing tools like ngrok to test webhook delivery:
```bash
ngrok http 3000
# Use the ngrok URL as your webhook endpoint
```

---

For more information, visit the [interactive API documentation](http://localhost:8083/swagger-ui.html) or contact the development team at dev@mshando.com.
