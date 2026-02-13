-- PostgreSQL initialization script
-- Database is created by the postgres container environment variables
-- This file is executed on first container startup

-- Ensure database exists (redundant but safe)
SELECT 'CREATE DATABASE hcp_portal'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hcp_portal')\gexec

-- Set timezone
SET timezone = 'UTC';

-- Create extension for UUID generation (may be needed for future features)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- All table creation and seeding is handled by Flyway migrations
