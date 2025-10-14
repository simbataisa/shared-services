-- Enable extensions required by indexes
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Enums
DO $$ BEGIN
    CREATE TYPE tenant_type AS ENUM ('BUSINESS_IN', 'BUSINESS_OUT', 'INDIVIDUAL');
EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN
    CREATE TYPE tenant_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN
    CREATE TYPE product_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'DISCONTINUED');
EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN
    CREATE TYPE module_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'EXPIRED', 'DISCONTINUED');
EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN
    CREATE TYPE role_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'DEPRECATED');
EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN
    CREATE TYPE plan_type AS ENUM ('SUBSCRIPTION', 'QUOTABASED', 'AFFILIATE');
EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN
    CREATE TYPE plan_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'EXPIRED', 'DISCONTINUED', 'PENDING_PAYMENT', 'PENDING_RENEW', 'OVERDUE');
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- Tenant Management
CREATE TABLE IF NOT EXISTS tenant (
    tenant_id BIGSERIAL PRIMARY KEY,
    tenant_code VARCHAR(50) UNIQUE NOT NULL,
    name TEXT NOT NULL,
    type tenant_type NOT NULL,
    organization_id BIGINT,
    tenant_status tenant_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

CREATE TABLE IF NOT EXISTS organization (
    org_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenant(tenant_id),
    name TEXT NOT NULL,
    parent_org_id BIGINT REFERENCES organization(org_id),
    country VARCHAR(3),
    path TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

-- Entity (User) Management
CREATE TABLE IF NOT EXISTS entity (
    entity_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    parent_entity_id BIGINT REFERENCES entity(entity_id),
    path TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

CREATE TABLE IF NOT EXISTS tenant_entity (
    tenant_id BIGINT REFERENCES tenant(tenant_id),
    entity_id BIGINT REFERENCES entity(entity_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    PRIMARY KEY (tenant_id, entity_id)
);

-- Product & Module Management
CREATE TABLE IF NOT EXISTS product (
    product_id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    product_name TEXT NOT NULL,
    description TEXT,
    product_status product_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

CREATE TABLE IF NOT EXISTS module (
    module_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(product_id),
    name TEXT NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    module_status module_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

-- Role & Permission Management
CREATE TABLE IF NOT EXISTS role (
    role_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    role_status role_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    module_id BIGINT NOT NULL REFERENCES module(module_id),
    UNIQUE(module_id, name)
);

CREATE TABLE IF NOT EXISTS permission (
    permission_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    resource_type TEXT,
    action TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id BIGINT REFERENCES role(role_id),
    permission_id BIGINT REFERENCES permission(permission_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    PRIMARY KEY (role_id, permission_id)
);

-- User Group (Permission Group) Management
CREATE TABLE IF NOT EXISTS user_group (
    user_group_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_group_member (
    user_group_id BIGINT REFERENCES user_group(user_group_id),
    entity_id BIGINT REFERENCES entity(entity_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    PRIMARY KEY (user_group_id, entity_id)
);

CREATE TABLE IF NOT EXISTS group_module_role (
    group_module_role_id BIGSERIAL PRIMARY KEY,
    user_group_id BIGINT NOT NULL REFERENCES user_group(user_group_id),
    module_id BIGINT NOT NULL REFERENCES module(module_id),
    role_id BIGINT NOT NULL REFERENCES role(role_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    UNIQUE(user_group_id, module_id, role_id)
);

-- Profile (User-Role Assignment)
CREATE TABLE IF NOT EXISTS profile (
    profile_id BIGSERIAL PRIMARY KEY,
    entity_id BIGINT NOT NULL REFERENCES entity(entity_id),
    role_id BIGINT NOT NULL REFERENCES role(role_id),
    username TEXT NOT NULL,
    username_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

-- SSO Provider
CREATE TABLE IF NOT EXISTS sso_provider (
    sso_provider_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    client_id TEXT NOT NULL,
    client_secret TEXT NOT NULL,
    discovery_url TEXT NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenant(tenant_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

-- Plan & Package Management
CREATE TABLE IF NOT EXISTS plan (
    plan_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    discount_rate DECIMAL(5,2),
    start_date DATE NOT NULL,
    end_date DATE,
    plan_type plan_type NOT NULL,
    plan_status plan_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

CREATE TABLE IF NOT EXISTS package (
    package_id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plan(plan_id),
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    package_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

CREATE TABLE IF NOT EXISTS package_module (
    package_module_id BIGSERIAL PRIMARY KEY,
    package_id BIGINT NOT NULL REFERENCES package(package_id),
    module_id BIGINT NOT NULL REFERENCES module(module_id),
    price DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

CREATE TABLE IF NOT EXISTS tenant_plan (
    tenant_id BIGINT REFERENCES tenant(tenant_id),
    plan_id BIGINT REFERENCES plan(plan_id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    PRIMARY KEY (tenant_id, plan_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_tenant_code ON tenant(tenant_code);
CREATE INDEX IF NOT EXISTS idx_tenant_status ON tenant(tenant_status);
CREATE INDEX IF NOT EXISTS idx_organization_tenant ON organization(tenant_id);
CREATE INDEX IF NOT EXISTS idx_organization_parent ON organization(parent_org_id);
CREATE INDEX IF NOT EXISTS idx_entity_path ON entity USING GIST (path gist_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_module_product ON module(product_id);
CREATE INDEX IF NOT EXISTS idx_role_module ON role(module_id);
CREATE INDEX IF NOT EXISTS idx_user_group_deleted ON user_group(deleted_at);
CREATE INDEX IF NOT EXISTS idx_group_module_role_group ON group_module_role(user_group_id);
CREATE INDEX IF NOT EXISTS idx_group_module_role_module ON group_module_role(module_id);