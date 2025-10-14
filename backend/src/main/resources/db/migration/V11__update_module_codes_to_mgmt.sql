-- V11: Update module codes from CORE_CONFIG to MODULE_MGMT and CORE_DASHBOARD to PRODUCT_MGMT
-- Also update related permissions to use the new module codes

-- Update CORE_CONFIG module to MODULE_MGMT
UPDATE module 
SET code = 'MODULE_MGMT',
    name = 'Module Management'
WHERE code = 'CORE_CONFIG';

-- Update CORE_DASHBOARD module to PRODUCT_MGMT  
UPDATE module 
SET code = 'PRODUCT_MGMT',
    name = 'Product Management'
WHERE code = 'CORE_DASHBOARD';

-- Update permissions that use CORE_CONFIG resource_type to MODULE_MGMT
UPDATE permission 
SET resource_type = 'MODULE_MGMT',
    name = REPLACE(name, 'CORE_CONFIG:', 'MODULE_MGMT:')
WHERE resource_type = 'CORE_CONFIG';

-- Update permissions that use CORE_DASHBOARD resource_type to PRODUCT_MGMT
UPDATE permission 
SET resource_type = 'PRODUCT_MGMT',
    name = REPLACE(name, 'CORE_DASHBOARD:', 'PRODUCT_MGMT:')
WHERE resource_type = 'CORE_DASHBOARD';