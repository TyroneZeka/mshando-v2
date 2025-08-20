#!/bin/bash
# Script to generate secure JWT secret and other secrets for the application

echo "=== Mshando Security Setup ==="
echo ""

# Generate a secure JWT secret (256-bit base64 encoded)
echo "🔐 Generating secure JWT secret..."
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo "JWT_SECRET=$JWT_SECRET"
echo ""

# Generate a secure database password
echo "🔐 Generating secure database password..."
DB_PASSWORD=$(openssl rand -base64 32 | tr -d '\n' | tr '/' '_' | tr '+' '-')
echo "DATABASE_PASSWORD=$DB_PASSWORD"
echo ""

# Generate app-specific password placeholder
echo "📧 Email Configuration:"
echo "EMAIL_USERNAME=your_email@gmail.com"
echo "EMAIL_PASSWORD=your_app_specific_password"
echo ""

echo "💡 Instructions:"
echo "1. Copy the generated values to your .env file"
echo "2. Replace EMAIL_USERNAME and EMAIL_PASSWORD with your actual values"
echo "3. For Gmail, use App Passwords: https://support.google.com/accounts/answer/185833"
echo "4. Keep these secrets secure and never commit them to version control"
echo ""

echo "📝 Example .env file:"
echo "# Copy these to your .env file"
echo "JWT_SECRET=$JWT_SECRET"
echo "DATABASE_PASSWORD=$DB_PASSWORD"
echo "EMAIL_USERNAME=your_email@gmail.com"
echo "EMAIL_PASSWORD=your_16_char_app_password"
