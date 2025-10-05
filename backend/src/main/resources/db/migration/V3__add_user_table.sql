-- =====================================================
-- V3: Add User Management Tables
-- =====================================================

-- Create user_status enum
DO $$ BEGIN
    CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'LOCKED');
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    user_status user_status NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TIMESTAMP,
    password_changed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

-- Create user_roles junction table (many-to-many between users and roles)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES role(role_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    PRIMARY KEY (user_id, role_id)
);

-- Create user_group_users junction table (many-to-many between users and user_groups)
CREATE TABLE IF NOT EXISTS user_group_users (
    user_group_id BIGINT REFERENCES user_group(user_group_id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    PRIMARY KEY (user_group_id, user_id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(user_status);
CREATE INDEX IF NOT EXISTS idx_users_email_verified ON users(email_verified);
CREATE INDEX IF NOT EXISTS idx_users_last_login ON users(last_login);
CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_user_group_users_group ON user_group_users(user_group_id);
CREATE INDEX IF NOT EXISTS idx_user_group_users_user ON user_group_users(user_id);

-- Add comments for documentation
COMMENT ON TABLE users IS 'User accounts with authentication and profile information';
COMMENT ON TABLE user_roles IS 'Many-to-many relationship between users and roles';
COMMENT ON TABLE user_group_users IS 'Many-to-many relationship between users and user groups';

COMMENT ON COLUMN users.username IS 'Unique username for login';
COMMENT ON COLUMN users.email IS 'Unique email address for login and communication';
COMMENT ON COLUMN users.password IS 'Hashed password for authentication';
COMMENT ON COLUMN users.user_status IS 'Current status of the user account';
COMMENT ON COLUMN users.email_verified IS 'Whether the user has verified their email address';
COMMENT ON COLUMN users.failed_login_attempts IS 'Number of consecutive failed login attempts';
COMMENT ON COLUMN users.account_locked_until IS 'Timestamp until which the account is locked';
COMMENT ON COLUMN users.password_changed_at IS 'Timestamp of last password change';