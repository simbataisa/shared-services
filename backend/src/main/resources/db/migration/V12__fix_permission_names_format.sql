-- V12: Fix permission names to follow module.code:action format
-- Remove redundant prefixes from permission names to match the template module.code:action
-- Use a different approach: rename conflicting permissions to avoid duplicates

-- Fix PRODUCT_MGMT permissions - remove "product_" prefix
UPDATE permission 
SET name = REPLACE(name, 'PRODUCT_MGMT:product_', 'PRODUCT_MGMT:')
WHERE resource_type = 'PRODUCT_MGMT' AND name LIKE 'PRODUCT_MGMT:product_%';

-- Fix MODULE_MGMT permissions - remove "module_" prefix  
UPDATE permission 
SET name = REPLACE(name, 'MODULE_MGMT:module_', 'MODULE_MGMT:')
WHERE resource_type = 'MODULE_MGMT' AND name LIKE 'MODULE_MGMT:module_%';

-- Fix MODULE_MGMT system permissions - rename to avoid conflicts
UPDATE permission 
SET name = 'MODULE_MGMT:config'
WHERE resource_type = 'MODULE_MGMT' AND name = 'MODULE_MGMT:system_config';

-- Remove duplicate system permissions that now conflict with module ones
DELETE FROM role_permission 
WHERE permission_id IN (
    SELECT permission_id FROM permission 
    WHERE resource_type = 'MODULE_MGMT' 
    AND name IN ('MODULE_MGMT:system_admin', 'MODULE_MGMT:system_read')
);

DELETE FROM permission 
WHERE resource_type = 'MODULE_MGMT' 
AND name IN ('MODULE_MGMT:system_admin', 'MODULE_MGMT:system_read');