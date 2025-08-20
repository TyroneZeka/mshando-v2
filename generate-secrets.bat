@echo off
REM Script to generate secure configuration for the application

echo === Mshando Security Setup ===
echo.

echo 🔐 Generating secure configuration...
echo.

REM Generate random string for JWT secret (pseudo-random for Windows)
set "chars=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
set "jwt_secret="
for /L %%i in (1,1,64) do (
    set /a "rand=!random! %% 64"
    for %%j in (!rand!) do set "jwt_secret=!jwt_secret!!chars:~%%j,1!"
)

echo JWT_SECRET=%jwt_secret%
echo.

REM Generate database password
set "db_chars=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
set "db_password="
for /L %%i in (1,1,32) do (
    set /a "rand=!random! %% 62"
    for %%j in (!rand!) do set "db_password=!db_password!!db_chars:~%%j,1!"
)

echo DATABASE_PASSWORD=%db_password%
echo.

echo 📧 Email Configuration:
echo EMAIL_USERNAME=your_email@gmail.com
echo EMAIL_PASSWORD=your_app_specific_password
echo.

echo 💡 Instructions:
echo 1. Copy the generated values to your .env file
echo 2. Replace EMAIL_USERNAME and EMAIL_PASSWORD with your actual values
echo 3. For Gmail, use App Passwords: https://support.google.com/accounts/answer/185833
echo 4. Keep these secrets secure and never commit them to version control
echo.

echo 📝 Example .env file:
echo # Copy these to your .env file
echo JWT_SECRET=%jwt_secret%
echo DATABASE_PASSWORD=%db_password%
echo EMAIL_USERNAME=your_email@gmail.com
echo EMAIL_PASSWORD=your_16_char_app_password

pause
