-- This script runs automatically on first container startup.
-- It creates all databases needed by the microservices.

CREATE DATABASE auth_db;
CREATE DATABASE user_db;
CREATE DATABASE expense_db;