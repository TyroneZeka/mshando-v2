-- Initialize multiple databases for microservices
CREATE DATABASE mshando_users;
CREATE DATABASE mshando_tasks;
CREATE DATABASE mshando_bidding;
CREATE DATABASE mshando_payments;
CREATE DATABASE mshando_notifications;
CREATE DATABASE mshando_reviews;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE mshando_users TO postgres;
GRANT ALL PRIVILEGES ON DATABASE mshando_tasks TO postgres;
GRANT ALL PRIVILEGES ON DATABASE mshando_bidding TO postgres;
GRANT ALL PRIVILEGES ON DATABASE mshando_payments TO postgres;
GRANT ALL PRIVILEGES ON DATABASE mshando_notifications TO postgres;
GRANT ALL PRIVILEGES ON DATABASE mshando_reviews TO postgres;
