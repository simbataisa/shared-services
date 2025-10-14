-- Remove module_id column from role table to decouple roles from modules
ALTER TABLE role DROP CONSTRAINT IF EXISTS fk_role_module;
ALTER TABLE role DROP COLUMN IF EXISTS module_id;

-- Add unique constraint on role name since roles are now independent of modules
ALTER TABLE role ADD CONSTRAINT uk_role_name UNIQUE (name);