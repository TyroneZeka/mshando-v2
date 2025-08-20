#!/bin/bash

# Quick Mshando User Service API Test with Email Verification
# Focus on basic functionality including email verification flow

BASE_URL="http://localhost:8081"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Quick Mshando User Service API Test (with Email Verification)${NC}"
echo -e "${YELLOW}Base URL: $BASE_URL${NC}\n"

# Test 1: Register a new user with unique username
echo -e "${YELLOW}Testing user registration with unique username...${NC}"
TIMESTAMP=$(date +%s)
USERNAME="testuser_$TIMESTAMP"
EMAIL="testuser_$TIMESTAMP@example.com"

echo "Registering user: $USERNAME"

RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "$CONTENT_TYPE" \
  -d "{
    \"username\": \"$USERNAME\",
    \"email\": \"$EMAIL\",
    \"password\": \"password123\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\",
    \"phoneNumber\": \"+1234567890\",
    \"role\": \"CUSTOMER\"
  }")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

echo "Registration Response Status: $HTTP_STATUS"
echo "Registration Response: $RESPONSE_BODY"

if [[ "$HTTP_STATUS" == "200" ]]; then
    echo -e "${GREEN}✅ Registration PASSED${NC}"
    
    # Extract token using more reliable method
    JWT_TOKEN=$(echo "$RESPONSE_BODY" | grep -o '"token":"[^"]*"' | sed 's/"token":"//g' | sed 's/"//g')
    USER_ID=$(echo "$RESPONSE_BODY" | grep -o '"id":[0-9]*' | sed 's/"id"://g' | sed 's/,//g')
    
    echo "Extracted token: ${JWT_TOKEN:0:30}..."
    echo "Extracted user ID: $USER_ID"
else
    echo -e "${RED}❌ Registration FAILED${NC}"
fi

# Test 2: Check email verification status
echo -e "\n${YELLOW}Testing email verification status check...${NC}"

RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/auth/verification-status?email=$EMAIL")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

echo "Verification Status Response: $HTTP_STATUS"
echo "Verification Status: $RESPONSE_BODY"

if [[ "$HTTP_STATUS" == "200" ]]; then
    echo -e "${GREEN}✅ Verification Status Check PASSED${NC}"
else
    echo -e "${RED}❌ Verification Status Check FAILED${NC}"
fi

# Test 3: Login with the registered user
echo -e "\n${YELLOW}Testing login with registered user...${NC}"

RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "$CONTENT_TYPE" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"password123\"
  }")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

echo "Login Response Status: $HTTP_STATUS"
echo "Login Response: $RESPONSE_BODY"

if [[ "$HTTP_STATUS" == "200" ]]; then
    echo -e "${GREEN}✅ Login PASSED${NC}"
    
    # Extract token from login response
    JWT_TOKEN=$(echo "$RESPONSE_BODY" | grep -o '"token":"[^"]*"' | sed 's/"token":"//g' | sed 's/"//g')
    echo "Login token: ${JWT_TOKEN:0:30}..."
else
    echo -e "${RED}❌ Login FAILED${NC}"
fi

# Test 4: Token validation
if [[ -n "$JWT_TOKEN" ]]; then
    echo -e "\n${YELLOW}Testing token validation...${NC}"
    
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/auth/validate" \
      -H "Authorization: Bearer $JWT_TOKEN")
    
    HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')
    
    echo "Token Validation Status: $HTTP_STATUS"
    echo "Token Validation Response: $RESPONSE_BODY"
    
    if [[ "$HTTP_STATUS" == "200" ]]; then
        # Check if token is actually valid (not just HTTP 200)
        IS_VALID=$(echo "$RESPONSE_BODY" | grep -o '"valid":[^,]*' | sed 's/"valid"://g')
        if [[ "$IS_VALID" == "true" ]]; then
            echo -e "${GREEN}✅ Token Validation PASSED${NC}"
        else
            echo -e "${YELLOW}⚠️ Token Validation HTTP 200 but token invalid (expected for unverified email)${NC}"
        fi
    else
        echo -e "${RED}❌ Token Validation FAILED${NC}"
    fi
else
    echo -e "\n${RED}❌ No token available for validation${NC}"
fi

# Test 5: Resend verification email
echo -e "\n${YELLOW}Testing resend verification email...${NC}"

RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/resend-verification" \
  -H "$CONTENT_TYPE" \
  -d "{\"email\": \"$EMAIL\"}")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

echo "Resend Verification Status: $HTTP_STATUS"
echo "Resend Verification Response: $RESPONSE_BODY"

if [[ "$HTTP_STATUS" == "200" ]]; then
    echo -e "${GREEN}✅ Resend Verification Email PASSED${NC}"
else
    echo -e "${RED}❌ Resend Verification Email FAILED${NC}"
fi

# Test 6: User search (should work regardless of verification)
echo -e "\n${YELLOW}Testing user search...${NC}"

RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL/api/users/search?query=test&page=0&size=10")

HTTP_STATUS=$(echo $RESPONSE | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
RESPONSE_BODY=$(echo $RESPONSE | sed -e 's/HTTPSTATUS:.*//g')

echo "Search Status: $HTTP_STATUS"
echo "Search Response: $RESPONSE_BODY"

if [[ "$HTTP_STATUS" == "200" ]]; then
    echo -e "${GREEN}✅ User Search PASSED${NC}"
else
    echo -e "${RED}❌ User Search FAILED${NC}"
fi

echo -e "\n${BLUE}🎯 Quick Test Summary${NC}"
echo -e "${YELLOW}Comprehensive test with email verification flow completed${NC}"
echo -e "${YELLOW}Note: Email verification tokens would be sent via email in production${NC}"
