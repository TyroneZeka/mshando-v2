#!/bin/bash

# Swagger/OpenAPI Test Script for User Service
echo "=== Testing Swagger/OpenAPI Documentation ==="
echo ""

USER_SERVICE_URL="http://localhost:8081"

echo "🔍 Testing OpenAPI JSON endpoint..."
curl -s -o /dev/null -w "Status: %{http_code}\n" "$USER_SERVICE_URL/api-docs"
echo ""

echo "🔍 Testing Swagger UI..."
curl -s -o /dev/null -w "Status: %{http_code}\n" "$USER_SERVICE_URL/swagger-ui.html"
echo ""

echo "📖 OpenAPI Documentation URLs:"
echo "  - OpenAPI JSON: $USER_SERVICE_URL/api-docs"
echo "  - Swagger UI: $USER_SERVICE_URL/swagger-ui.html"
echo "  - OpenAPI YAML: $USER_SERVICE_URL/api-docs.yaml"
echo ""

echo "💡 Usage Instructions:"
echo "1. Start the User Service: mvn spring-boot:run"
echo "2. Open Swagger UI in browser: $USER_SERVICE_URL/swagger-ui.html"
echo "3. Use 'Try it out' to test endpoints directly"
echo "4. Authenticate using the 'Authorize' button with Bearer token"
echo ""

echo "🔐 To test authenticated endpoints:"
echo "1. First register/login to get a JWT token"
echo "2. Click 'Authorize' in Swagger UI"
echo "3. Enter: Bearer YOUR_JWT_TOKEN"
echo "4. Now you can test protected endpoints"
