-- V10: Fix orphaned permission resource_type values to match actual module codes
-- Update permissions that have resource_type values not corresponding to any module.code
-- Use more specific names to avoid conflicts

-- Update MODULE_MGMT permissions to use CORE_CONFIG with module-specific names
UPDATE permission 
SET resource_type = 'CORE_CONFIG',
    name = REPLACE(name, 'MODULE_MGMT:', 'CORE_CONFIG:module_')
WHERE resource_type = 'MODULE_MGMT';

-- Update PRODUCT_MGMT permissions to use CORE_DASHBOARD with product-specific names
UPDATE permission 
SET resource_type = 'CORE_DASHBOARD',
    name = REPLACE(name, 'PRODUCT_MGMT:', 'CORE_DASHBOARD:product_')
WHERE resource_type = 'PRODUCT_MGMT';

-- SYSTEM_CONFIG permissions are already using CORE_CONFIG module (module_id 2)
-- Update them to use the proper module code with system-specific names
UPDATE permission 
SET resource_type = 'CORE_CONFIG',
    name = REPLACE(name, 'SYSTEM_CONFIG:', 'CORE_CONFIG:system_')
WHERE resource_type = 'SYSTEM_CONFIG';