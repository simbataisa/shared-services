-- =====================================================
-- V14: Add user_group_status column to user_group table
-- =====================================================

-- Add user_group_status column to user_group table (only if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'user_group' AND column_name = 'user_group_status') THEN
        ALTER TABLE user_group ADD COLUMN user_group_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
    END IF;
END $$;

-- Add check constraint to ensure valid status values (only if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'chk_user_group_status' AND table_name = 'user_group') THEN
        ALTER TABLE user_group ADD CONSTRAINT chk_user_group_status 
        CHECK (user_group_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'));
    END IF;
END $$;

-- Create index for better query performance on status
CREATE INDEX IF NOT EXISTS idx_user_group_status ON user_group(user_group_status);

-- Add comment for documentation
COMMENT ON COLUMN user_group.user_group_status IS 'Status of the user group (ACTIVE, INACTIVE, SUSPENDED)';

-- Update existing user groups to have ACTIVE status (if they don't already have a status)
UPDATE user_group 
SET user_group_status = 'ACTIVE', 
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE user_group_status IS NULL;