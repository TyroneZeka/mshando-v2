#!/bin/bash

# Mshando User Service API Test Suite
# This script tests all available endpoints in the User Service

BASE_URL="http://localhost:8081"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test results
print_test_result() {
    local test_name="$1"
    local status_code="$2"
    local expected_code="$3"
    local response="$4"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "\n${BLUE}=== Test: $test_name ===${NC}"
    echo -e "Expected: HTTP $expected_code"
    echo -e "Actual: HTTP $status_code"
    
    if [[ "$status_code" == "$expected_code" ]]; then
        echo -e "${GREEN}✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    echo -e "Response: $response"
    echo -e "${YELLOW}---${NC}"
}

# Function to extract token from response
extract_token() {
    echo "$1" | grep -o '"token":"[^"]*"' | cut -d'"' -f4
}

# Function to extract user ID from response
extract_user_id() {
    echo "$1" | grep -o '"id":[0-9]*' | cut -d':' -f2
}

echo -e "${BLUE}🚀 Starting Mshando User Service API Tests${NC}"
echo -e "${YELLOW}Base URL: $BASE_URL${NC}\n"

# Global variables for storing tokens and IDs
JWT_TOKEN=""
USER_ID=""

# ================================
# AUTH CONTROLLER TESTS
# ================================

echo -e "${BLUE}📝 Testing Authentication Endpoints${NC}"

# Test 1: Register a new user
echo -e "\n${YELLOW}Testing user registration...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "testuser123",
    "email": "testuser123@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER"
  }')

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "User Registration" "$HTTP_STATUS" "200" "$RESPONSE_BODY"

# Extract token and user ID for subsequent tests
if [[ "$HTTP_STATUS" == "200" ]]; then
    JWT_TOKEN=$(extract_token "$RESPONSE_BODY")
    USER_ID=$(extract_user_id "$RESPONSE_BODY")
    echo -e "${GREEN}Token extracted: ${JWT_TOKEN:0:20}...${NC}"
    echo -e "${GREEN}User ID extracted: $USER_ID${NC}"
fi

# Test 2: Login with valid credentials
echo -e "\n${YELLOW}Testing user login...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "testuser123",
    "password": "password123"
  }')

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "User Login - Valid Credentials" "$HTTP_STATUS" "200" "$RESPONSE_BODY"

# Test 3: Login with invalid credentials
echo -e "\n${YELLOW}Testing login with invalid credentials...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "nonexistent",
    "password": "wrongpassword"
  }')

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "User Login - Invalid Credentials" "$HTTP_STATUS" "400" "$RESPONSE_BODY"

# Test 4: Validate token
echo -e "\n${YELLOW}Testing token validation...${NC}"
if [[ -n "$JWT_TOKEN" ]]; then
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/auth/validate" \
      -H "Authorization: Bearer $JWT_TOKEN")

    HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

    print_test_result "Token Validation - Valid Token" "$HTTP_STATUS" "200" "$RESPONSE_BODY"
else
    print_test_result "Token Validation - Valid Token" "SKIP" "200" "No token available"
fi

# Test 5: Validate invalid token
echo -e "\n${YELLOW}Testing invalid token validation...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/auth/validate" \
  -H "Authorization: Bearer invalid.token.here")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Token Validation - Invalid Token" "$HTTP_STATUS" "400" "$RESPONSE_BODY"

# Test 6: Refresh token
echo -e "\n${YELLOW}Testing token refresh...${NC}"
if [[ -n "$JWT_TOKEN" ]]; then
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/refresh" \
      -H "Authorization: Bearer $JWT_TOKEN")

    HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

    print_test_result "Token Refresh" "$HTTP_STATUS" "200" "$RESPONSE_BODY"
else
    print_test_result "Token Refresh" "SKIP" "200" "No token available"
fi

# ================================
# USER CONTROLLER TESTS
# ================================

echo -e "\n${BLUE}👤 Testing User Management Endpoints${NC}"

# Test 7: Get current user profile
echo -e "\n${YELLOW}Testing get current user profile...${NC}"
if [[ -n "$JWT_TOKEN" ]]; then
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/users/me" \
      -H "Authorization: Bearer $JWT_TOKEN")

    HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

    print_test_result "Get Current User Profile" "$HTTP_STATUS" "200" "$RESPONSE_BODY"
else
    print_test_result "Get Current User Profile" "SKIP" "200" "No token available"
fi

# Test 8: Get current user profile without token
echo -e "\n${YELLOW}Testing get current user profile without authorization...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/users/me")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Get Current User Profile - No Auth" "$HTTP_STATUS" "401" "$RESPONSE_BODY"

# Test 9: Update user profile
echo -e "\n${YELLOW}Testing update user profile...${NC}"
if [[ -n "$JWT_TOKEN" ]]; then
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X PUT "$BASE_URL/api/users/me" \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -H "$CONTENT_TYPE" \
      -d '{
        "firstName": "Updated",
        "lastName": "Name",
        "phoneNumber": "+0987654321"
      }')

    HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

    print_test_result "Update User Profile" "$HTTP_STATUS" "200" "$RESPONSE_BODY"
else
    print_test_result "Update User Profile" "SKIP" "200" "No token available"
fi

# Test 10: Get user by ID
echo -e "\n${YELLOW}Testing get user by ID...${NC}"
if [[ -n "$USER_ID" ]]; then
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/users/$USER_ID")

    HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

    print_test_result "Get User by ID" "$HTTP_STATUS" "200" "$RESPONSE_BODY"
else
    print_test_result "Get User by ID" "SKIP" "200" "No user ID available"
fi

# Test 11: Get non-existent user by ID
echo -e "\n${YELLOW}Testing get non-existent user by ID...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/users/99999")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Get Non-existent User by ID" "$HTTP_STATUS" "404" "$RESPONSE_BODY"

# Test 12: Search users
echo -e "\n${YELLOW}Testing search users...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/users/search?query=test&page=0&size=10")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Search Users" "$HTTP_STATUS" "200" "$RESPONSE_BODY"

# Test 13: Search users without query
echo -e "\n${YELLOW}Testing search users without query...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/users/search?page=0&size=10")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Search Users - No Query" "$HTTP_STATUS" "200" "$RESPONSE_BODY"

# Test 14: Delete user by ID
echo -e "\n${YELLOW}Testing delete user by ID...${NC}"
if [[ -n "$USER_ID" ]]; then
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X DELETE "$BASE_URL/api/users/$USER_ID")

    HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

    print_test_result "Delete User by ID" "$HTTP_STATUS" "204" "$RESPONSE_BODY"
else
    print_test_result "Delete User by ID" "SKIP" "204" "No user ID available"
fi

# Test 15: Delete non-existent user
echo -e "\n${YELLOW}Testing delete non-existent user...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X DELETE "$BASE_URL/api/users/99999")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Delete Non-existent User" "$HTTP_STATUS" "404" "$RESPONSE_BODY"

# ================================
# VALIDATION TESTS
# ================================

echo -e "\n${BLUE}🔍 Testing Input Validation${NC}"

# Test 16: Register with invalid email
echo -e "\n${YELLOW}Testing registration with invalid email...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "testuser2",
    "email": "invalid-email",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER"
  }')

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Registration - Invalid Email" "$HTTP_STATUS" "400" "$RESPONSE_BODY"

# Test 17: Register with missing fields
echo -e "\n${YELLOW}Testing registration with missing fields...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "testuser3"
  }')

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Registration - Missing Fields" "$HTTP_STATUS" "400" "$RESPONSE_BODY"

# Test 18: Login with missing password
echo -e "\n${YELLOW}Testing login with missing password...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "testuser"
  }')

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Login - Missing Password" "$HTTP_STATUS" "400" "$RESPONSE_BODY"

# Test 19: Malformed JSON
echo -e "\n${YELLOW}Testing malformed JSON...${NC}"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "$CONTENT_TYPE" \
  -d '{invalid json}')

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

print_test_result "Malformed JSON" "$HTTP_STATUS" "400" "$RESPONSE_BODY"

# ================================
# TEST SUMMARY
# ================================

echo -e "\n${BLUE}📊 Test Summary${NC}"
echo -e "${YELLOW}===================${NC}"
echo -e "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"

if [[ $FAILED_TESTS -eq 0 ]]; then
    echo -e "\n${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    PASS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "\n${YELLOW}📈 Pass Rate: $PASS_RATE%${NC}"
    
    if [[ $PASS_RATE -ge 80 ]]; then
        echo -e "${YELLOW}⚠️  Most tests passed, but some issues need attention.${NC}"
        exit 1
    else
        echo -e "${RED}❌ Many tests failed. Please check the service implementation.${NC}"
        exit 2
    fi
fi
