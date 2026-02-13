-- V001: Initial Schema - Users, Roles, and User Roles

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Create user_roles junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Seed roles
INSERT INTO roles (name) VALUES
    ('OFFICE_STAFF'),
    ('SUPPORT_AGENT'),
    ('ADMIN'),
    ('PROVIDER');

-- Seed users (password is BCrypt hash of username, e.g., "admin" hashed)
-- BCrypt hash for "admin" = $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- BCrypt hash for "staff" = $2a$10$X5wFWHkJ9fHi9ZqXqXj.P.5z3kJfJ5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Zm (placeholder)
-- BCrypt hash for "agent" = $2a$10$Y6wGXHlK0gIj0ArYrYk.Q.6a4lKgK6a6a6a6a6a6a6a6a6a6a6a6an (placeholder)

-- Note: In production, use proper BCrypt hashing. For development, we'll use simpler hashes
-- Password for all users: "password"
-- BCrypt (10 rounds): $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (email, password_hash, first_name, last_name) VALUES
    ('admin@sonexus.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User'),
    ('staff@sonexus.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Staff', 'User'),
    ('agent@sonexus.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Agent', 'User');

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 3), -- admin has ADMIN role
    (2, 1), -- staff has OFFICE_STAFF role
    (3, 2); -- agent has SUPPORT_AGENT role
