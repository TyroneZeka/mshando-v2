-- Initialize multiple databases for microservices
CREATE DATABASE taskrabbit_users;
CREATE DATABASE taskrabbit_tasks;
CREATE DATABASE taskrabbit_bidding;
CREATE DATABASE taskrabbit_payments;
CREATE DATABASE taskrabbit_reviews;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE taskrabbit_users TO postgres;
GRANT ALL PRIVILEGES ON DATABASE taskrabbit_tasks TO postgres;
GRANT ALL PRIVILEGES ON DATABASE taskrabbit_bidding TO postgres;
GRANT ALL PRIVILEGES ON DATABASE taskrabbit_payments TO postgres;
GRANT ALL PRIVILEGES ON DATABASE taskrabbit_reviews TO postgres;
