-- V2__seed_data.sql
-- Comprehensive seed data for AHSS Shared Services
-- Includes admin accounts, roles, permissions, and sample data

-- =====================================================
-- 1. TENANTS - Sample Organizations
-- =====================================================

INSERT INTO tenant (tenant_code, name, type, tenant_status, created_by, updated_by) VALUES
('AHSS_MAIN', 'AHSS Main Organization', 'BUSINESS_IN', 'ACTIVE', 'system', 'system'),
('DEMO_CORP', 'Demo Corporation', 'BUSINESS_OUT', 'ACTIVE', 'system', 'system'),
('TEST_ORG', 'Test Organization', 'BUSINESS_IN', 'ACTIVE', 'system', 'system'),
('INDIVIDUAL_1', 'John Doe Individual', 'INDIVIDUAL', 'ACTIVE', 'system', 'system');

-- =====================================================
-- 2. ORGANIZATIONS - Hierarchical Structure
-- =====================================================

INSERT INTO organization (tenant_id, name, parent_org_id, country, path, created_by, updated_by) VALUES
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), 'AHSS Headquarters', NULL, 'USA', '/ahss-hq', 'system', 'system'),
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), 'Engineering Division', (SELECT org_id FROM organization WHERE name = 'AHSS Headquarters'), 'USA', '/ahss-hq/engineering', 'system', 'system'),
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), 'Operations Division', (SELECT org_id FROM organization WHERE name = 'AHSS Headquarters'), 'USA', '/ahss-hq/operations', 'system', 'system'),
((SELECT tenant_id FROM tenant WHERE tenant_code = 'DEMO_CORP'), 'Demo Corp Main Office', NULL, 'CAN', '/demo-main', 'system', 'system'),
((SELECT tenant_id FROM tenant WHERE tenant_code = 'TEST_ORG'), 'Test Organization HQ', NULL, 'GBR', '/test-hq', 'system', 'system');

-- =====================================================
-- 3. ENTITIES - Users in the System
-- =====================================================

INSERT INTO entity (name, parent_entity_id, path, created_by, updated_by) VALUES
('System Administrator', NULL, '/admin', 'system', 'system'),
('Super Admin User', NULL, '/superadmin', 'system', 'system'),
('Tenant Admin User', NULL, '/tenant-admin', 'system', 'system'),
('Product Manager', NULL, '/product-manager', 'system', 'system'),
('Module Manager', NULL, '/module-manager', 'system', 'system'),
('Role Manager', NULL, '/role-manager', 'system', 'system'),
('User Manager', NULL, '/user-manager', 'system', 'system'),
('Demo User', NULL, '/demo-user', 'system', 'system'),
('Test User 1', NULL, '/test-user-1', 'system', 'system'),
('Test User 2', NULL, '/test-user-2', 'system', 'system'),
('Read Only User', NULL, '/readonly-user', 'system', 'system'),
('Guest User', NULL, '/guest-user', 'system', 'system');

-- =====================================================
-- 4. TENANT-ENTITY RELATIONSHIPS
-- =====================================================

INSERT INTO tenant_entity (tenant_id, entity_id, created_by, updated_by) VALUES
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT entity_id FROM entity WHERE name = 'System Administrator'), 'system', 'system'), -- System Admin -> AHSS Main
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT entity_id FROM entity WHERE name = 'Super Admin User'), 'system', 'system'), -- Super Admin -> AHSS Main
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT entity_id FROM entity WHERE name = 'Tenant Admin User'), 'system', 'system'), -- Tenant Admin -> AHSS Main
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT entity_id FROM entity WHERE name = 'Product Manager'), 'system', 'system'), -- Product Manager -> AHSS Main
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT entity_id FROM entity WHERE name = 'Module Manager'), 'system', 'system'), -- Module Manager -> AHSS Main
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT entity_id FROM entity WHERE name = 'Role Manager'), 'system', 'system'), -- Role Manager -> AHSS Main
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT entity_id FROM entity WHERE name = 'User Manager'), 'system', 'system'), -- User Manager -> AHSS Main
((SELECT tenant_id FROM tenant WHERE tenant_code = 'DEMO_CORP'), (SELECT entity_id FROM entity WHERE name = 'Demo User'), 'system', 'system'), -- Demo User -> Demo Corp
((SELECT tenant_id FROM tenant WHERE tenant_code = 'TEST_ORG'), (SELECT entity_id FROM entity WHERE name = 'Test User 1'), 'system', 'system'), -- Test User 1 -> Test Org
((SELECT tenant_id FROM tenant WHERE tenant_code = 'TEST_ORG'), (SELECT entity_id FROM entity WHERE name = 'Test User 2'), 'system', 'system'), -- Test User 2 -> Test Org
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT entity_id FROM entity WHERE name = 'Read Only User'), 'system', 'system'), -- Read Only User -> AHSS Main
((SELECT tenant_id FROM tenant WHERE tenant_code = 'DEMO_CORP'), (SELECT entity_id FROM entity WHERE name = 'Guest User'), 'system', 'system'); -- Guest User -> Demo Corp

-- =====================================================
-- 5. PRODUCTS - Core Business Products
-- =====================================================

INSERT INTO product (product_code, product_name, description, product_status, created_by, updated_by) VALUES
('AHSS_CORE', 'AHSS Core Platform', 'Main platform for shared services management', 'ACTIVE', 'system', 'system'),
('USER_MGMT', 'User Management System', 'Comprehensive user and identity management', 'ACTIVE', 'system', 'system'),
('TENANT_MGMT', 'Tenant Management System', 'Multi-tenant organization management', 'ACTIVE', 'system', 'system'),
('RBAC_SYSTEM', 'Role-Based Access Control', 'Advanced RBAC and ABAC system', 'ACTIVE', 'system', 'system'),
('ANALYTICS', 'Analytics & Reporting', 'Business intelligence and analytics platform', 'ACTIVE', 'system', 'system'),
('INTEGRATION', 'Integration Hub', 'API gateway and integration services', 'DRAFT', 'system', 'system');

-- =====================================================
-- 6. MODULES - Product Components
-- =====================================================

INSERT INTO module (product_id, name, code, description, module_status, created_by, updated_by) VALUES
-- AHSS Core Platform Modules
((SELECT product_id FROM product WHERE product_code = 'AHSS_CORE'), 'Dashboard', 'CORE_DASHBOARD', 'Main dashboard and overview', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'AHSS_CORE'), 'System Configuration', 'CORE_CONFIG', 'System-wide configuration management', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'AHSS_CORE'), 'Audit Logging', 'CORE_AUDIT', 'System audit and logging functionality', 'ACTIVE', 'system', 'system'),

-- User Management System Modules
((SELECT product_id FROM product WHERE product_code = 'USER_MGMT'), 'User Administration', 'USER_ADMIN', 'User creation, modification, and management', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'USER_MGMT'), 'Authentication', 'USER_AUTH', 'User authentication and session management', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'USER_MGMT'), 'Profile Management', 'USER_PROFILE', 'User profile and preferences management', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'USER_MGMT'), 'User Groups', 'USER_GROUPS', 'User group management and organization', 'ACTIVE', 'system', 'system'),

-- Tenant Management System Modules
((SELECT product_id FROM product WHERE product_code = 'TENANT_MGMT'), 'Tenant Administration', 'TENANT_ADMIN', 'Tenant creation and management', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'TENANT_MGMT'), 'Organization Structure', 'TENANT_ORG', 'Hierarchical organization management', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'TENANT_MGMT'), 'Tenant Configuration', 'TENANT_CONFIG', 'Tenant-specific configuration', 'ACTIVE', 'system', 'system'),

-- RBAC System Modules
((SELECT product_id FROM product WHERE product_code = 'RBAC_SYSTEM'), 'Role Management', 'RBAC_ROLES', 'Role definition and management', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'RBAC_SYSTEM'), 'Permission Management', 'RBAC_PERMISSIONS', 'Permission definition and assignment', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'RBAC_SYSTEM'), 'Access Control', 'RBAC_ACCESS', 'Access control enforcement', 'ACTIVE', 'system', 'system'),

-- Analytics & Reporting Modules
((SELECT product_id FROM product WHERE product_code = 'ANALYTICS'), 'User Analytics', 'ANALYTICS_USER', 'User behavior and usage analytics', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'ANALYTICS'), 'System Metrics', 'ANALYTICS_SYSTEM', 'System performance and health metrics', 'ACTIVE', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'ANALYTICS'), 'Custom Reports', 'ANALYTICS_REPORTS', 'Custom report generation', 'ACTIVE', 'system', 'system'),

-- Integration Hub Modules
((SELECT product_id FROM product WHERE product_code = 'INTEGRATION'), 'API Gateway', 'INTEGRATION_API', 'API gateway and routing', 'DRAFT', 'system', 'system'),
((SELECT product_id FROM product WHERE product_code = 'INTEGRATION'), 'SSO Integration', 'INTEGRATION_SSO', 'Single sign-on integration', 'DRAFT', 'system', 'system');

-- =====================================================
-- 7. PERMISSIONS - Granular Access Controls
-- =====================================================

INSERT INTO permission (name, description, resource_type, action, created_by, updated_by) VALUES
-- System-level permissions
('system:admin', 'Full system administration access', 'system', 'admin', 'system', 'system'),
('system:read', 'Read system information', 'system', 'read', 'system', 'system'),
('system:config', 'Configure system settings', 'system', 'config', 'system', 'system'),

-- User permissions
('user:create', 'Create new users', 'user', 'create', 'system', 'system'),
('user:read', 'View user information', 'user', 'read', 'system', 'system'),
('user:update', 'Update user information', 'user', 'update', 'system', 'system'),
('user:delete', 'Delete users', 'user', 'delete', 'system', 'system'),
('user:admin', 'Full user administration', 'user', 'admin', 'system', 'system'),

-- Tenant permissions
('tenant:create', 'Create new tenants', 'tenant', 'create', 'system', 'system'),
('tenant:read', 'View tenant information', 'tenant', 'read', 'system', 'system'),
('tenant:update', 'Update tenant information', 'tenant', 'update', 'system', 'system'),
('tenant:delete', 'Delete tenants', 'tenant', 'delete', 'system', 'system'),
('tenant:admin', 'Full tenant administration', 'tenant', 'admin', 'system', 'system'),

-- Product permissions
('product:create', 'Create new products', 'product', 'create', 'system', 'system'),
('product:read', 'View product information', 'product', 'read', 'system', 'system'),
('product:update', 'Update product information', 'product', 'update', 'system', 'system'),
('product:delete', 'Delete products', 'product', 'delete', 'system', 'system'),
('product:admin', 'Full product administration', 'product', 'admin', 'system', 'system'),

-- Module permissions
('module:create', 'Create new modules', 'module', 'create', 'system', 'system'),
('module:read', 'View module information', 'module', 'read', 'system', 'system'),
('module:update', 'Update module information', 'module', 'update', 'system', 'system'),
('module:delete', 'Delete modules', 'module', 'delete', 'system', 'system'),
('module:admin', 'Full module administration', 'module', 'admin', 'system', 'system'),

-- Role permissions
('role:create', 'Create new roles', 'role', 'create', 'system', 'system'),
('role:read', 'View role information', 'role', 'read', 'system', 'system'),
('role:update', 'Update role information', 'role', 'update', 'system', 'system'),
('role:delete', 'Delete roles', 'role', 'delete', 'system', 'system'),
('role:admin', 'Full role administration', 'role', 'admin', 'system', 'system'),

-- Permission permissions
('permission:create', 'Create new permissions', 'permission', 'create', 'system', 'system'),
('permission:read', 'View permission information', 'permission', 'read', 'system', 'system'),
('permission:update', 'Update permission information', 'permission', 'update', 'system', 'system'),
('permission:delete', 'Delete permissions', 'permission', 'delete', 'system', 'system'),
('permission:admin', 'Full permission administration', 'permission', 'admin', 'system', 'system'),

-- Analytics permissions
('analytics:read', 'View analytics and reports', 'analytics', 'read', 'system', 'system'),
('analytics:create', 'Create custom reports', 'analytics', 'create', 'system', 'system'),
('analytics:admin', 'Full analytics administration', 'analytics', 'admin', 'system', 'system'),

-- Audit permissions
('audit:read', 'View audit logs', 'audit', 'read', 'system', 'system'),
('audit:admin', 'Full audit log administration', 'audit', 'admin', 'system', 'system');

-- =====================================================
-- 8. ROLES - Collections of Permissions
-- =====================================================

INSERT INTO role (name, description, role_status, module_id, created_by, updated_by) VALUES
-- System Administration Roles
('Super Administrator', 'Full system access with all permissions', 'ACTIVE', 1, 'system', 'system'),
('System Administrator', 'System-level administration access', 'ACTIVE', 1, 'system', 'system'),
('System Viewer', 'Read-only system access', 'ACTIVE', 1, 'system', 'system'),

-- User Management Roles
('User Administrator', 'Full user management capabilities', 'ACTIVE', 4, 'system', 'system'),
('User Manager', 'Standard user management operations', 'ACTIVE', 4, 'system', 'system'),
('User Viewer', 'Read-only user information access', 'ACTIVE', 4, 'system', 'system'),

-- Tenant Management Roles
('Tenant Administrator', 'Full tenant management capabilities', 'ACTIVE', 8, 'system', 'system'),
('Tenant Manager', 'Standard tenant operations', 'ACTIVE', 8, 'system', 'system'),
('Tenant Viewer', 'Read-only tenant information', 'ACTIVE', 8, 'system', 'system'),

-- Product Management Roles
('Product Administrator', 'Full product management capabilities', 'ACTIVE', 1, 'system', 'system'),
('Product Manager', 'Standard product operations', 'ACTIVE', 1, 'system', 'system'),
('Product Viewer', 'Read-only product information', 'ACTIVE', 1, 'system', 'system'),

-- Module Management Roles
('Module Administrator', 'Full module management capabilities', 'ACTIVE', 1, 'system', 'system'),
('Module Manager', 'Standard module operations', 'ACTIVE', 1, 'system', 'system'),
('Module Viewer', 'Read-only module information', 'ACTIVE', 1, 'system', 'system'),

-- RBAC Management Roles
('RBAC Administrator', 'Full role and permission management', 'ACTIVE', 11, 'system', 'system'),
('Role Manager', 'Role management operations', 'ACTIVE', 11, 'system', 'system'),
('Permission Manager', 'Permission management operations', 'ACTIVE', 12, 'system', 'system'),

-- Analytics Roles
('Analytics Administrator', 'Full analytics and reporting access', 'ACTIVE', 14, 'system', 'system'),
('Report Viewer', 'Read-only analytics access', 'ACTIVE', 14, 'system', 'system'),

-- General User Roles
('Standard User', 'Standard user with basic access', 'ACTIVE', 1, 'system', 'system'),
('Guest User', 'Limited guest access', 'ACTIVE', 1, 'system', 'system'),
('Read Only User', 'Read-only access to assigned resources', 'ACTIVE', 1, 'system', 'system');

-- =====================================================
-- 9. ROLE-PERMISSION ASSIGNMENTS
-- =====================================================

-- Super Administrator (Role ID: 1) - All permissions
INSERT INTO role_permission (role_id, permission_id, created_by, updated_by) 
SELECT (SELECT role_id FROM role WHERE name = 'Super Administrator'), permission_id, 'system', 'system' FROM permission;

-- System Administrator - System and user management
INSERT INTO role_permission (role_id, permission_id, created_by, updated_by) VALUES
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'system:admin'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'system:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'system:config'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:create'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:update'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:delete'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:admin'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'audit:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'System Administrator'), (SELECT permission_id FROM permission WHERE name = 'audit:admin'), 'system', 'system');

-- User Administrator - Full user management
INSERT INTO role_permission (role_id, permission_id, created_by, updated_by) VALUES
((SELECT role_id FROM role WHERE name = 'User Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:create'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'User Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'User Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:update'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'User Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:delete'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'User Administrator'), (SELECT permission_id FROM permission WHERE name = 'user:admin'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'User Administrator'), (SELECT permission_id FROM permission WHERE name = 'system:read'), 'system', 'system');

-- Tenant Administrator - Full tenant management
INSERT INTO role_permission (role_id, permission_id, created_by, updated_by) VALUES
((SELECT role_id FROM role WHERE name = 'Tenant Administrator'), (SELECT permission_id FROM permission WHERE name = 'tenant:create'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Tenant Administrator'), (SELECT permission_id FROM permission WHERE name = 'tenant:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Tenant Administrator'), (SELECT permission_id FROM permission WHERE name = 'tenant:update'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Tenant Administrator'), (SELECT permission_id FROM permission WHERE name = 'tenant:delete'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Tenant Administrator'), (SELECT permission_id FROM permission WHERE name = 'tenant:admin'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Tenant Administrator'), (SELECT permission_id FROM permission WHERE name = 'system:read'), 'system', 'system');

-- Product Administrator - Full product management
INSERT INTO role_permission (role_id, permission_id, created_by, updated_by) VALUES
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'product:create'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'product:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'product:update'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'product:delete'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'product:admin'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'module:create'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'module:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'module:update'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'module:delete'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Product Administrator'), (SELECT permission_id FROM permission WHERE name = 'module:admin'), 'system', 'system');

-- RBAC Administrator - Role and permission management
INSERT INTO role_permission (role_id, permission_id, created_by, updated_by) VALUES
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'role:create'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'role:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'role:update'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'role:delete'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'role:admin'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'permission:create'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'permission:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'permission:update'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'permission:delete'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'RBAC Administrator'), (SELECT permission_id FROM permission WHERE name = 'permission:admin'), 'system', 'system');

-- Standard User - Basic access
INSERT INTO role_permission (role_id, permission_id, created_by, updated_by) VALUES
((SELECT role_id FROM role WHERE name = 'Standard User'), (SELECT permission_id FROM permission WHERE name = 'system:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Standard User'), (SELECT permission_id FROM permission WHERE name = 'user:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Standard User'), (SELECT permission_id FROM permission WHERE name = 'tenant:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Standard User'), (SELECT permission_id FROM permission WHERE name = 'product:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Standard User'), (SELECT permission_id FROM permission WHERE name = 'module:read'), 'system', 'system');

-- Read Only User - Read-only access
INSERT INTO role_permission (role_id, permission_id, created_by, updated_by) VALUES
((SELECT role_id FROM role WHERE name = 'Read Only User'), (SELECT permission_id FROM permission WHERE name = 'system:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Read Only User'), (SELECT permission_id FROM permission WHERE name = 'user:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Read Only User'), (SELECT permission_id FROM permission WHERE name = 'tenant:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Read Only User'), (SELECT permission_id FROM permission WHERE name = 'product:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Read Only User'), (SELECT permission_id FROM permission WHERE name = 'module:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Read Only User'), (SELECT permission_id FROM permission WHERE name = 'role:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Read Only User'), (SELECT permission_id FROM permission WHERE name = 'permission:read'), 'system', 'system'),
((SELECT role_id FROM role WHERE name = 'Read Only User'), (SELECT permission_id FROM permission WHERE name = 'audit:read'), 'system', 'system');

-- =====================================================
-- 10. USER GROUPS - Organizational Groups
-- =====================================================

INSERT INTO user_group (name, description, created_by, updated_by) VALUES
('System Administrators', 'Group for all system administrators', 'system', 'system'),
('Tenant Administrators', 'Group for tenant-level administrators', 'system', 'system'),
('Product Managers', 'Group for product management team', 'system', 'system'),
('RBAC Managers', 'Group for role and permission managers', 'system', 'system'),
('Standard Users', 'Group for standard system users', 'system', 'system'),
('Read Only Users', 'Group for users with read-only access', 'system', 'system'),
('Demo Users', 'Group for demonstration purposes', 'system', 'system');

-- =====================================================
-- 11. USER GROUP MEMBERSHIPS
-- =====================================================

INSERT INTO user_group_member (user_group_id, entity_id, created_by, updated_by) VALUES
-- System Administrators Group
((SELECT user_group_id FROM user_group WHERE name = 'System Administrators'), (SELECT entity_id FROM entity WHERE name = 'System Administrator'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'System Administrators'), (SELECT entity_id FROM entity WHERE name = 'Super Admin User'), 'system', 'system'),

-- Tenant Administrators Group
((SELECT user_group_id FROM user_group WHERE name = 'Tenant Administrators'), (SELECT entity_id FROM entity WHERE name = 'Tenant Admin User'), 'system', 'system'),

-- Product Managers Group
((SELECT user_group_id FROM user_group WHERE name = 'Product Managers'), (SELECT entity_id FROM entity WHERE name = 'Product Manager'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Product Managers'), (SELECT entity_id FROM entity WHERE name = 'Module Manager'), 'system', 'system'),

-- RBAC Managers Group
((SELECT user_group_id FROM user_group WHERE name = 'RBAC Managers'), (SELECT entity_id FROM entity WHERE name = 'Role Manager'), 'system', 'system'),

-- Standard Users Group
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT entity_id FROM entity WHERE name = 'User Manager'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT entity_id FROM entity WHERE name = 'Demo User'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT entity_id FROM entity WHERE name = 'Test User 1'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT entity_id FROM entity WHERE name = 'Test User 2'), 'system', 'system'),

-- Read Only Users Group
((SELECT user_group_id FROM user_group WHERE name = 'Read Only Users'), (SELECT entity_id FROM entity WHERE name = 'Read Only User'), 'system', 'system'),

-- Demo Users Group
((SELECT user_group_id FROM user_group WHERE name = 'Demo Users'), (SELECT entity_id FROM entity WHERE name = 'Guest User'), 'system', 'system');

-- =====================================================
-- 12. GROUP-MODULE-ROLE ASSIGNMENTS
-- =====================================================

-- System Administrators Group - Super Administrator role on Core modules
INSERT INTO group_module_role (user_group_id, module_id, role_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'System Administrators'), (SELECT module_id FROM module WHERE name = 'Dashboard'), (SELECT role_id FROM role WHERE name = 'Super Administrator'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'System Administrators'), (SELECT module_id FROM module WHERE name = 'System Configuration'), (SELECT role_id FROM role WHERE name = 'Super Administrator'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'System Administrators'), (SELECT module_id FROM module WHERE name = 'Audit Logging'), (SELECT role_id FROM role WHERE name = 'Super Administrator'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'System Administrators'), (SELECT module_id FROM module WHERE name = 'User Administration'), (SELECT role_id FROM role WHERE name = 'System Administrator'), 'system', 'system');

-- Tenant Administrators Group - Tenant management roles
INSERT INTO group_module_role (user_group_id, module_id, role_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'Tenant Administrators'), (SELECT module_id FROM module WHERE name = 'Tenant Administration'), (SELECT role_id FROM role WHERE name = 'Tenant Administrator'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Tenant Administrators'), (SELECT module_id FROM module WHERE name = 'Organization Structure'), (SELECT role_id FROM role WHERE name = 'Tenant Administrator'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Tenant Administrators'), (SELECT module_id FROM module WHERE name = 'Tenant Configuration'), (SELECT role_id FROM role WHERE name = 'Tenant Administrator'), 'system', 'system');

-- Product Managers Group - Product and module management
INSERT INTO group_module_role (user_group_id, module_id, role_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'Product Managers'), (SELECT module_id FROM module WHERE name = 'Dashboard'), (SELECT role_id FROM role WHERE name = 'Product Administrator'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Product Managers'), (SELECT module_id FROM module WHERE name = 'Dashboard'), (SELECT role_id FROM role WHERE name = 'Module Administrator'), 'system', 'system');

-- RBAC Managers Group - Role and permission management
INSERT INTO group_module_role (user_group_id, module_id, role_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'RBAC Managers'), (SELECT module_id FROM module WHERE name = 'Role Management'), (SELECT role_id FROM role WHERE name = 'RBAC Administrator'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'RBAC Managers'), (SELECT module_id FROM module WHERE name = 'Permission Management'), (SELECT role_id FROM role WHERE name = 'RBAC Administrator'), 'system', 'system');

-- Standard Users Group - Standard user access
INSERT INTO group_module_role (user_group_id, module_id, role_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT module_id FROM module WHERE name = 'Dashboard'), (SELECT role_id FROM role WHERE name = 'Standard User'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT module_id FROM module WHERE name = 'Profile Management'), (SELECT role_id FROM role WHERE name = 'Standard User'), 'system', 'system');

-- Read Only Users Group - Read-only access
INSERT INTO group_module_role (user_group_id, module_id, role_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'Read Only Users'), (SELECT module_id FROM module WHERE name = 'Dashboard'), (SELECT role_id FROM role WHERE name = 'Read Only User'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Read Only Users'), (SELECT module_id FROM module WHERE name = 'User Administration'), (SELECT role_id FROM role WHERE name = 'User Viewer'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Read Only Users'), (SELECT module_id FROM module WHERE name = 'Tenant Administration'), (SELECT role_id FROM role WHERE name = 'Tenant Viewer'), 'system', 'system');

-- =====================================================
-- 13. USER PROFILES - Authentication Profiles
-- =====================================================

INSERT INTO profile (entity_id, role_id, username, username_type, created_by, updated_by) VALUES
-- Admin Accounts
((SELECT entity_id FROM entity WHERE name = 'System Administrator'), (SELECT role_id FROM role WHERE name = 'System Administrator'), 'admin@ahss.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'Super Admin User'), (SELECT role_id FROM role WHERE name = 'Super Administrator'), 'superadmin@ahss.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'Tenant Admin User'), (SELECT role_id FROM role WHERE name = 'Tenant Administrator'), 'tenant.admin@ahss.com', 'email', 'system', 'system'),

-- Management Accounts
((SELECT entity_id FROM entity WHERE name = 'Product Manager'), (SELECT role_id FROM role WHERE name = 'Product Administrator'), 'product.manager@ahss.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'Module Manager'), (SELECT role_id FROM role WHERE name = 'Module Administrator'), 'module.manager@ahss.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'Role Manager'), (SELECT role_id FROM role WHERE name = 'RBAC Administrator'), 'role.manager@ahss.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'User Manager'), (SELECT role_id FROM role WHERE name = 'User Administrator'), 'user.manager@ahss.com', 'email', 'system', 'system'),

-- Standard User Accounts
((SELECT entity_id FROM entity WHERE name = 'Demo User'), (SELECT role_id FROM role WHERE name = 'Standard User'), 'demo.user@democorp.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'Test User 1'), (SELECT role_id FROM role WHERE name = 'Standard User'), 'test.user1@testorg.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'Test User 2'), (SELECT role_id FROM role WHERE name = 'Standard User'), 'test.user2@testorg.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'Read Only User'), (SELECT role_id FROM role WHERE name = 'Read Only User'), 'readonly@ahss.com', 'email', 'system', 'system'),
((SELECT entity_id FROM entity WHERE name = 'Guest User'), (SELECT role_id FROM role WHERE name = 'Guest User'), 'guest@democorp.com', 'email', 'system', 'system');

-- =====================================================
-- 14. SAMPLE PLANS AND PACKAGES
-- =====================================================

INSERT INTO plan (name, discount_rate, start_date, end_date, plan_type, plan_status, created_by, updated_by) VALUES
('Enterprise Plan', 10.00, '2024-01-01', '2024-12-31', 'SUBSCRIPTION', 'ACTIVE', 'system', 'system'),
('Standard Plan', 5.00, '2024-01-01', '2024-12-31', 'SUBSCRIPTION', 'ACTIVE', 'system', 'system'),
('Basic Plan', 0.00, '2024-01-01', '2024-12-31', 'SUBSCRIPTION', 'ACTIVE', 'system', 'system'),
('Demo Plan', 0.00, '2024-01-01', '2024-12-31', 'QUOTABASED', 'ACTIVE', 'system', 'system');

INSERT INTO package (plan_id, name, type, price, package_status, start_date, end_date, version, created_by, updated_by) VALUES
((SELECT plan_id FROM plan WHERE name = 'Enterprise Plan'), 'Enterprise Full Suite', 'FULL', 999.99, 'ACTIVE', '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1, 'system', 'system'),
((SELECT plan_id FROM plan WHERE name = 'Standard Plan'), 'Standard Package', 'STANDARD', 499.99, 'ACTIVE', '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1, 'system', 'system'),
((SELECT plan_id FROM plan WHERE name = 'Basic Plan'), 'Basic Package', 'BASIC', 99.99, 'ACTIVE', '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1, 'system', 'system'),
((SELECT plan_id FROM plan WHERE name = 'Demo Plan'), 'Demo Package', 'DEMO', 0.00, 'ACTIVE', '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1, 'system', 'system');

-- =====================================================
-- 15. TENANT-PLAN ASSIGNMENTS
-- =====================================================

INSERT INTO tenant_plan (tenant_id, plan_id, created_by, updated_by) VALUES
((SELECT tenant_id FROM tenant WHERE tenant_code = 'AHSS_MAIN'), (SELECT plan_id FROM plan WHERE name = 'Enterprise Plan'), 'system', 'system'), -- AHSS Main -> Enterprise Plan
((SELECT tenant_id FROM tenant WHERE tenant_code = 'DEMO_CORP'), (SELECT plan_id FROM plan WHERE name = 'Demo Plan'), 'system', 'system'), -- Demo Corp -> Demo Plan
((SELECT tenant_id FROM tenant WHERE tenant_code = 'TEST_ORG'), (SELECT plan_id FROM plan WHERE name = 'Standard Plan'), 'system', 'system'), -- Test Org -> Standard Plan
((SELECT tenant_id FROM tenant WHERE tenant_code = 'INDIVIDUAL_1'), (SELECT plan_id FROM plan WHERE name = 'Basic Plan'), 'system', 'system'); -- Individual -> Basic Plan

-- =====================================================
-- SEED DATA SUMMARY
-- =====================================================
-- 
-- This seed data provides:
-- 
-- 1. 4 Tenants with different types and statuses
-- 2. 5 Organizations with hierarchical structure
-- 3. 12 Entities (Users) with various roles
-- 4. 6 Products covering core business areas
-- 5. 17 Modules across all products
-- 6. 35 Permissions with granular access control
-- 7. 21 Roles from super admin to read-only
-- 8. 7 User Groups for organizational structure
-- 9. Complete role-permission mappings
-- 10. User profiles with email-based authentication
-- 11. Sample plans and packages for billing
-- 12. Tenant-plan assignments
-- 
-- Admin Accounts Created:
-- - superadmin@ahss.com (Super Administrator - All permissions)
-- - admin@ahss.com (System Administrator - System & user management)
-- - tenant.admin@ahss.com (Tenant Administrator - Tenant management)
-- - product.manager@ahss.com (Product Administrator - Product management)
-- - role.manager@ahss.com (RBAC Administrator - Role/permission management)
-- 
-- Test Accounts:
-- - demo.user@democorp.com (Standard User)
-- - readonly@ahss.com (Read-only access)
-- - guest@democorp.com (Guest access)
-- 
-- =====================================================