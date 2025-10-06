--
-- PostgreSQL database dump
--

\restrict ESHVcKYRxedu2VRMf9i2C0l3IsO3MGuCwHxXWzetdZfWoU20glXwolSvcqaLosE

-- Dumped from database version 16.10
-- Dumped by pg_dump version 16.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- Name: module_status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.module_status AS ENUM (
    'DRAFT',
    'ACTIVE',
    'INACTIVE',
    'EXPIRED',
    'DISCONTINUED'
);


ALTER TYPE public.module_status OWNER TO postgres;

--
-- Name: plan_status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.plan_status AS ENUM (
    'DRAFT',
    'ACTIVE',
    'INACTIVE',
    'EXPIRED',
    'DISCONTINUED',
    'PENDING_PAYMENT',
    'PENDING_RENEW',
    'OVERDUE'
);


ALTER TYPE public.plan_status OWNER TO postgres;

--
-- Name: plan_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.plan_type AS ENUM (
    'SUBSCRIPTION',
    'QUOTABASED',
    'AFFILIATE'
);


ALTER TYPE public.plan_type OWNER TO postgres;

--
-- Name: product_status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.product_status AS ENUM (
    'DRAFT',
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'DISCONTINUED'
);


ALTER TYPE public.product_status OWNER TO postgres;

--
-- Name: role_status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.role_status AS ENUM (
    'DRAFT',
    'ACTIVE',
    'INACTIVE',
    'DEPRECATED'
);


ALTER TYPE public.role_status OWNER TO postgres;

--
-- Name: tenant_status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.tenant_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED'
);


ALTER TYPE public.tenant_status OWNER TO postgres;

--
-- Name: tenant_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.tenant_type AS ENUM (
    'BUSINESS_IN',
    'BUSINESS_OUT',
    'INDIVIDUAL'
);


ALTER TYPE public.tenant_type OWNER TO postgres;

--
-- Name: user_status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.user_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'PENDING_VERIFICATION',
    'LOCKED'
);


ALTER TYPE public.user_status OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: entity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.entity (
    entity_id bigint NOT NULL,
    name text NOT NULL,
    parent_entity_id bigint,
    path text NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.entity OWNER TO postgres;

--
-- Name: entity_entity_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.entity_entity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.entity_entity_id_seq OWNER TO postgres;

--
-- Name: entity_entity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.entity_entity_id_seq OWNED BY public.entity.entity_id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- Name: group_module_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.group_module_role (
    group_module_role_id bigint NOT NULL,
    user_group_id bigint NOT NULL,
    module_id bigint NOT NULL,
    role_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.group_module_role OWNER TO postgres;

--
-- Name: group_module_role_group_module_role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.group_module_role_group_module_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.group_module_role_group_module_role_id_seq OWNER TO postgres;

--
-- Name: group_module_role_group_module_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.group_module_role_group_module_role_id_seq OWNED BY public.group_module_role.group_module_role_id;


--
-- Name: module; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.module (
    module_id bigint NOT NULL,
    product_id bigint NOT NULL,
    name text NOT NULL,
    code character varying(50) NOT NULL,
    description text,
    module_status public.module_status DEFAULT 'DRAFT'::public.module_status NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.module OWNER TO postgres;

--
-- Name: module_module_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.module_module_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.module_module_id_seq OWNER TO postgres;

--
-- Name: module_module_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.module_module_id_seq OWNED BY public.module.module_id;


--
-- Name: organization; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.organization (
    org_id bigint NOT NULL,
    tenant_id bigint,
    name text NOT NULL,
    parent_org_id bigint,
    country character varying(3),
    path text NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.organization OWNER TO postgres;

--
-- Name: organization_org_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.organization_org_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.organization_org_id_seq OWNER TO postgres;

--
-- Name: organization_org_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.organization_org_id_seq OWNED BY public.organization.org_id;


--
-- Name: package; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.package (
    package_id bigint NOT NULL,
    plan_id bigint NOT NULL,
    name text NOT NULL,
    type text NOT NULL,
    price numeric(10,2) NOT NULL,
    package_status character varying(50) DEFAULT 'DRAFT'::character varying NOT NULL,
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone,
    version integer DEFAULT 1 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.package OWNER TO postgres;

--
-- Name: package_module; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.package_module (
    package_module_id bigint NOT NULL,
    package_id bigint NOT NULL,
    module_id bigint NOT NULL,
    price numeric(10,2),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.package_module OWNER TO postgres;

--
-- Name: package_module_package_module_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.package_module_package_module_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.package_module_package_module_id_seq OWNER TO postgres;

--
-- Name: package_module_package_module_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.package_module_package_module_id_seq OWNED BY public.package_module.package_module_id;


--
-- Name: package_package_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.package_package_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.package_package_id_seq OWNER TO postgres;

--
-- Name: package_package_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.package_package_id_seq OWNED BY public.package.package_id;


--
-- Name: permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission (
    permission_id bigint NOT NULL,
    name text NOT NULL,
    description text,
    resource_type text,
    action text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.permission OWNER TO postgres;

--
-- Name: permission_permission_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.permission_permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.permission_permission_id_seq OWNER TO postgres;

--
-- Name: permission_permission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.permission_permission_id_seq OWNED BY public.permission.permission_id;


--
-- Name: plan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.plan (
    plan_id bigint NOT NULL,
    name text NOT NULL,
    discount_rate numeric(5,2),
    start_date date NOT NULL,
    end_date date,
    plan_type public.plan_type NOT NULL,
    plan_status public.plan_status DEFAULT 'DRAFT'::public.plan_status NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.plan OWNER TO postgres;

--
-- Name: plan_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.plan_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.plan_plan_id_seq OWNER TO postgres;

--
-- Name: plan_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.plan_plan_id_seq OWNED BY public.plan.plan_id;


--
-- Name: product; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product (
    product_id bigint NOT NULL,
    product_code character varying(50) NOT NULL,
    product_name text NOT NULL,
    description text,
    product_status public.product_status DEFAULT 'DRAFT'::public.product_status NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.product OWNER TO postgres;

--
-- Name: product_product_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.product_product_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.product_product_id_seq OWNER TO postgres;

--
-- Name: product_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.product_product_id_seq OWNED BY public.product.product_id;


--
-- Name: profile; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.profile (
    profile_id bigint NOT NULL,
    entity_id bigint NOT NULL,
    role_id bigint NOT NULL,
    username text NOT NULL,
    username_type character varying(50) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.profile OWNER TO postgres;

--
-- Name: profile_profile_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.profile_profile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.profile_profile_id_seq OWNER TO postgres;

--
-- Name: profile_profile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.profile_profile_id_seq OWNED BY public.profile.profile_id;


--
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role (
    role_id bigint NOT NULL,
    module_id bigint NOT NULL,
    name text NOT NULL,
    description text,
    role_status character varying(20) DEFAULT 'DRAFT'::public.role_status NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.role OWNER TO postgres;

--
-- Name: role_permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role_permission (
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.role_permission OWNER TO postgres;

--
-- Name: role_role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.role_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.role_role_id_seq OWNER TO postgres;

--
-- Name: role_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.role_role_id_seq OWNED BY public.role.role_id;


--
-- Name: sso_provider; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sso_provider (
    sso_provider_id bigint NOT NULL,
    name text NOT NULL,
    client_id text NOT NULL,
    client_secret text NOT NULL,
    discovery_url text NOT NULL,
    tenant_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.sso_provider OWNER TO postgres;

--
-- Name: sso_provider_sso_provider_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sso_provider_sso_provider_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sso_provider_sso_provider_id_seq OWNER TO postgres;

--
-- Name: sso_provider_sso_provider_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sso_provider_sso_provider_id_seq OWNED BY public.sso_provider.sso_provider_id;


--
-- Name: tenant; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tenant (
    tenant_id bigint NOT NULL,
    tenant_code character varying(50) NOT NULL,
    name text NOT NULL,
    type public.tenant_type NOT NULL,
    organization_id bigint,
    tenant_status public.tenant_status DEFAULT 'ACTIVE'::public.tenant_status NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.tenant OWNER TO postgres;

--
-- Name: tenant_entity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tenant_entity (
    tenant_id bigint NOT NULL,
    entity_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.tenant_entity OWNER TO postgres;

--
-- Name: tenant_plan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tenant_plan (
    tenant_id bigint NOT NULL,
    plan_id bigint NOT NULL,
    assigned_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.tenant_plan OWNER TO postgres;

--
-- Name: tenant_tenant_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tenant_tenant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tenant_tenant_id_seq OWNER TO postgres;

--
-- Name: tenant_tenant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tenant_tenant_id_seq OWNED BY public.tenant.tenant_id;


--
-- Name: user_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_group (
    user_group_id bigint NOT NULL,
    name text NOT NULL,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    deleted_at timestamp without time zone
);


ALTER TABLE public.user_group OWNER TO postgres;

--
-- Name: user_group_member; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_group_member (
    user_group_id bigint NOT NULL,
    entity_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.user_group_member OWNER TO postgres;

--
-- Name: user_group_user_group_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_group_user_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_group_user_group_id_seq OWNER TO postgres;

--
-- Name: user_group_user_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_group_user_group_id_seq OWNED BY public.user_group.user_group_id;


--
-- Name: user_group_users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_group_users (
    user_group_id bigint NOT NULL,
    user_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.user_group_users OWNER TO postgres;

--
-- Name: TABLE user_group_users; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_group_users IS 'Many-to-many relationship between users and user groups';


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: TABLE user_roles; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_roles IS 'Many-to-many relationship between users and roles';


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    first_name character varying(100),
    last_name character varying(100),
    phone_number character varying(20),
    user_status public.user_status DEFAULT 'ACTIVE'::public.user_status NOT NULL,
    email_verified boolean DEFAULT false NOT NULL,
    last_login timestamp without time zone,
    failed_login_attempts integer DEFAULT 0,
    account_locked_until timestamp without time zone,
    password_changed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(50) DEFAULT 'system'::character varying NOT NULL,
    updated_by character varying(50) DEFAULT 'system'::character varying NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: TABLE users; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.users IS 'User accounts with authentication and profile information';


--
-- Name: COLUMN users.username; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.username IS 'Unique username for login';


--
-- Name: COLUMN users.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.email IS 'Unique email address for login and communication';


--
-- Name: COLUMN users.password; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.password IS 'Hashed password for authentication';


--
-- Name: COLUMN users.user_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.user_status IS 'Current status of the user account';


--
-- Name: COLUMN users.email_verified; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.email_verified IS 'Whether the user has verified their email address';


--
-- Name: COLUMN users.failed_login_attempts; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.failed_login_attempts IS 'Number of consecutive failed login attempts';


--
-- Name: COLUMN users.account_locked_until; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.account_locked_until IS 'Timestamp until which the account is locked';


--
-- Name: COLUMN users.password_changed_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.password_changed_at IS 'Timestamp of last password change';


--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- Name: entity entity_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entity ALTER COLUMN entity_id SET DEFAULT nextval('public.entity_entity_id_seq'::regclass);


--
-- Name: group_module_role group_module_role_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_module_role ALTER COLUMN group_module_role_id SET DEFAULT nextval('public.group_module_role_group_module_role_id_seq'::regclass);


--
-- Name: module module_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.module ALTER COLUMN module_id SET DEFAULT nextval('public.module_module_id_seq'::regclass);


--
-- Name: organization org_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organization ALTER COLUMN org_id SET DEFAULT nextval('public.organization_org_id_seq'::regclass);


--
-- Name: package package_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package ALTER COLUMN package_id SET DEFAULT nextval('public.package_package_id_seq'::regclass);


--
-- Name: package_module package_module_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_module ALTER COLUMN package_module_id SET DEFAULT nextval('public.package_module_package_module_id_seq'::regclass);


--
-- Name: permission permission_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission ALTER COLUMN permission_id SET DEFAULT nextval('public.permission_permission_id_seq'::regclass);


--
-- Name: plan plan_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan ALTER COLUMN plan_id SET DEFAULT nextval('public.plan_plan_id_seq'::regclass);


--
-- Name: product product_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product ALTER COLUMN product_id SET DEFAULT nextval('public.product_product_id_seq'::regclass);


--
-- Name: profile profile_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.profile ALTER COLUMN profile_id SET DEFAULT nextval('public.profile_profile_id_seq'::regclass);


--
-- Name: role role_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role ALTER COLUMN role_id SET DEFAULT nextval('public.role_role_id_seq'::regclass);


--
-- Name: sso_provider sso_provider_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sso_provider ALTER COLUMN sso_provider_id SET DEFAULT nextval('public.sso_provider_sso_provider_id_seq'::regclass);


--
-- Name: tenant tenant_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant ALTER COLUMN tenant_id SET DEFAULT nextval('public.tenant_tenant_id_seq'::regclass);


--
-- Name: user_group user_group_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group ALTER COLUMN user_group_id SET DEFAULT nextval('public.user_group_user_group_id_seq'::regclass);


--
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- Data for Name: entity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.entity (entity_id, name, parent_entity_id, path, created_at, updated_at, created_by, updated_by) FROM stdin;
1	System Administrator	\N	/admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	Super Admin User	\N	/superadmin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	Tenant Admin User	\N	/tenant-admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	Product Manager	\N	/product-manager	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	Module Manager	\N	/module-manager	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
6	Role Manager	\N	/role-manager	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	User Manager	\N	/user-manager	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
8	Demo User	\N	/demo-user	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
9	Test User 1	\N	/test-user-1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	Test User 2	\N	/test-user-2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
11	Read Only User	\N	/readonly-user	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
12	Guest User	\N	/guest-user	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	init	SQL	V1__init.sql	1027859043	postgres	2025-10-05 12:25:46.329163	186	t
2	2	seed data	SQL	V2__seed_data.sql	2002366034	postgres	2025-10-05 12:25:46.546584	63	t
3	3	add user table	SQL	V3__add_user_table.sql	1972131854	postgres	2025-10-05 13:36:56.871288	67	t
4	4	seed user data	SQL	V4__seed_user_data.sql	1575078922	postgres	2025-10-05 13:36:56.969545	20	t
\.


--
-- Data for Name: group_module_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.group_module_role (group_module_role_id, user_group_id, module_id, role_id, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	1	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	1	2	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	1	3	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	1	4	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	2	8	7	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
6	2	9	7	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	2	10	7	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
8	3	1	10	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
9	3	1	13	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	4	11	16	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
11	4	12	16	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
12	5	1	21	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
13	5	6	21	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
14	6	1	23	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
15	6	4	6	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	6	8	9	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: module; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.module (module_id, product_id, name, code, description, module_status, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	Dashboard	CORE_DASHBOARD	Main dashboard and overview	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	1	System Configuration	CORE_CONFIG	System-wide configuration management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	1	Audit Logging	CORE_AUDIT	System audit and logging functionality	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	2	User Administration	USER_ADMIN	User creation, modification, and management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	2	Authentication	USER_AUTH	User authentication and session management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
6	2	Profile Management	USER_PROFILE	User profile and preferences management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	2	User Groups	USER_GROUPS	User group management and organization	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
8	3	Tenant Administration	TENANT_ADMIN	Tenant creation and management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
9	3	Organization Structure	TENANT_ORG	Hierarchical organization management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	3	Tenant Configuration	TENANT_CONFIG	Tenant-specific configuration	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
11	4	Role Management	RBAC_ROLES	Role definition and management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
12	4	Permission Management	RBAC_PERMISSIONS	Permission definition and assignment	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
13	4	Access Control	RBAC_ACCESS	Access control enforcement	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
14	5	User Analytics	ANALYTICS_USER	User behavior and usage analytics	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
15	5	System Metrics	ANALYTICS_SYSTEM	System performance and health metrics	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	5	Custom Reports	ANALYTICS_REPORTS	Custom report generation	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
17	6	API Gateway	INTEGRATION_API	API gateway and routing	DRAFT	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
18	6	SSO Integration	INTEGRATION_SSO	Single sign-on integration	DRAFT	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: organization; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.organization (org_id, tenant_id, name, parent_org_id, country, path, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	AHSS Headquarters	\N	USA	/ahss-hq	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	1	Engineering Division	\N	USA	/ahss-hq/engineering	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	1	Operations Division	\N	USA	/ahss-hq/operations	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	2	Demo Corp Main Office	\N	CAN	/demo-main	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	3	Test Organization HQ	\N	GBR	/test-hq	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package (package_id, plan_id, name, type, price, package_status, start_date, end_date, version, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	Enterprise Full Suite	FULL	999.99	ACTIVE	2024-01-01 00:00:00	2024-12-31 23:59:59	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	2	Standard Package	STANDARD	499.99	ACTIVE	2024-01-01 00:00:00	2024-12-31 23:59:59	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	3	Basic Package	BASIC	99.99	ACTIVE	2024-01-01 00:00:00	2024-12-31 23:59:59	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	4	Demo Package	DEMO	0.00	ACTIVE	2024-01-01 00:00:00	2024-12-31 23:59:59	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: package_module; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package_module (package_module_id, package_id, module_id, price, created_at, updated_at, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.permission (permission_id, name, description, resource_type, action, created_at, updated_at, created_by, updated_by) FROM stdin;
1	system:admin	Full system administration access	system	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	system:read	Read system information	system	read	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	system:config	Configure system settings	system	config	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	user:create	Create new users	user	create	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	user:read	View user information	user	read	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
6	user:update	Update user information	user	update	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	user:delete	Delete users	user	delete	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
8	user:admin	Full user administration	user	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
9	tenant:create	Create new tenants	tenant	create	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
11	tenant:update	Update tenant information	tenant	update	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
12	tenant:delete	Delete tenants	tenant	delete	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
13	tenant:admin	Full tenant administration	tenant	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
14	product:create	Create new products	product	create	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
15	product:read	View product information	product	read	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	product:update	Update product information	product	update	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
17	product:delete	Delete products	product	delete	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
18	product:admin	Full product administration	product	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
19	module:create	Create new modules	module	create	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
20	module:read	View module information	module	read	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
21	module:update	Update module information	module	update	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
22	module:delete	Delete modules	module	delete	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	module:admin	Full module administration	module	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
24	role:create	Create new roles	role	create	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
25	role:read	View role information	role	read	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
26	role:update	Update role information	role	update	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
27	role:delete	Delete roles	role	delete	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
28	role:admin	Full role administration	role	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
29	permission:create	Create new permissions	permission	create	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
30	permission:read	View permission information	permission	read	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
31	permission:update	Update permission information	permission	update	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
32	permission:delete	Delete permissions	permission	delete	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
33	permission:admin	Full permission administration	permission	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
34	analytics:read	View analytics and reports	analytics	read	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
35	analytics:create	Create custom reports	analytics	create	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
36	analytics:admin	Full analytics administration	analytics	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
37	audit:read	View audit logs	audit	read	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
38	audit:admin	Full audit log administration	audit	admin	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
39	user-groups:read	View user groups	user_group	read	2025-10-06 01:19:12.68843	2025-10-06 01:19:12.68843	system	system
10	tenants:read	View tenants information	tenant	read	2025-10-05 12:25:46.576211	2025-10-06 01:19:39.163099	system	system
\.


--
-- Data for Name: plan; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.plan (plan_id, name, discount_rate, start_date, end_date, plan_type, plan_status, created_at, updated_at, created_by, updated_by) FROM stdin;
1	Enterprise Plan	10.00	2024-01-01	2024-12-31	SUBSCRIPTION	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	Standard Plan	5.00	2024-01-01	2024-12-31	SUBSCRIPTION	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	Basic Plan	0.00	2024-01-01	2024-12-31	SUBSCRIPTION	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	Demo Plan	0.00	2024-01-01	2024-12-31	QUOTABASED	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: product; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.product (product_id, product_code, product_name, description, product_status, created_at, updated_at, created_by, updated_by) FROM stdin;
1	AHSS_CORE	AHSS Core Platform	Main platform for shared services management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	USER_MGMT	User Management System	Comprehensive user and identity management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	TENANT_MGMT	Tenant Management System	Multi-tenant organization management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	RBAC_SYSTEM	Role-Based Access Control	Advanced RBAC and ABAC system	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	ANALYTICS	Analytics & Reporting	Business intelligence and analytics platform	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
6	INTEGRATION	Integration Hub	API gateway and integration services	DRAFT	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: profile; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.profile (profile_id, entity_id, role_id, username, username_type, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	2	admin@ahss.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	2	1	superadmin@ahss.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	3	7	tenant.admin@ahss.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	4	10	product.manager@ahss.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	5	13	module.manager@ahss.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
6	6	16	role.manager@ahss.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	7	4	user.manager@ahss.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
8	8	21	demo.user@democorp.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
9	9	21	test.user1@testorg.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	10	21	test.user2@testorg.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
11	11	23	readonly@ahss.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
12	12	22	guest@democorp.com	email	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role (role_id, module_id, name, description, role_status, created_at, updated_at, created_by, updated_by) FROM stdin;
1	2	Super Administrator	Full system access with all permissions	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	2	System Administrator	System-level administration access	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	2	System Viewer	Read-only system access	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	4	User Administrator	Full user management capabilities	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	4	User Manager	Standard user management operations	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
6	4	User Viewer	Read-only user information access	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	8	Tenant Administrator	Full tenant management capabilities	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
8	8	Tenant Manager	Standard tenant operations	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
9	8	Tenant Viewer	Read-only tenant information	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	1	Product Administrator	Full product management capabilities	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
11	1	Product Manager	Standard product operations	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
12	1	Product Viewer	Read-only product information	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
13	1	Module Administrator	Full module management capabilities	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
14	1	Module Manager	Standard module operations	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
15	1	Module Viewer	Read-only module information	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	11	RBAC Administrator	Full role and permission management	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
17	11	Role Manager	Role management operations	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
18	12	Permission Manager	Permission management operations	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
19	14	Analytics Administrator	Full analytics and reporting access	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
20	14	Report Viewer	Read-only analytics access	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
21	7	Standard User	Standard user with basic access	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
22	7	Guest User	Limited guest access	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	7	Read Only User	Read-only access to assigned resources	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
25	1	Draft Role	Draft Description	DRAFT	2025-10-05 13:16:17.012526	2025-10-05 13:16:17.012536	system	system
27	1	Deprecated Role	Deprecated Description	DEPRECATED	2025-10-05 13:16:38.206649	2025-10-05 13:16:38.20666	system	system
28	1	Default Role	Default Description	ACTIVE	2025-10-05 13:16:48.774305	2025-10-05 13:16:48.77432	system	system
24	1	Updated Test Role	Updated Description	ACTIVE	2025-10-05 13:16:06.241024	2025-10-05 13:17:18.993414	system	system
26	1	Inactive Role	Inactive Description	INACTIVE	2025-10-05 13:16:27.496281	2025-10-05 13:18:59.42295	system	system
29	11	JPA Enum Test Role 2025	Testing with standard JPA enum mapping	ACTIVE	2025-10-05 14:02:58.074814	2025-10-05 14:02:58.074825	system	system
30	11	Final Enum Fix Test Role 2025	Testing with all entities using consistent JPA enum mapping	ACTIVE	2025-10-05 14:04:51.236419	2025-10-05 14:04:51.236431	system	system
\.


--
-- Data for Name: role_permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role_permission (role_id, permission_id, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	3	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	4	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	5	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	6	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	7	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	8	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	9	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	10	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	11	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	12	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	13	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	14	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	15	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	16	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	17	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	18	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	19	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	20	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	21	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	22	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	23	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	24	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	25	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	26	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	27	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	28	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	29	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	30	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	31	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	32	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	33	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	34	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	35	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	36	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	37	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	38	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	3	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	4	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	5	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	6	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	7	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	8	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	37	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	38	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	4	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	5	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	6	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	7	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	8	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	9	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	10	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	11	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	12	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	13	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	14	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	15	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	16	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	17	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	18	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	19	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	20	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	21	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	22	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
10	23	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	24	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	25	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	26	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	27	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	28	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	29	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	30	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	31	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	32	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
16	33	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
21	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
21	5	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
21	10	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
21	15	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
21	20	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	5	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	10	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	15	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	20	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	25	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	30	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
23	37	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	39	2025-10-06 01:19:26.51312	2025-10-06 01:19:26.51312	system	system
\.


--
-- Data for Name: sso_provider; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sso_provider (sso_provider_id, name, client_id, client_secret, discovery_url, tenant_id, created_at, updated_at, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: tenant; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tenant (tenant_id, tenant_code, name, type, organization_id, tenant_status, created_at, updated_at, created_by, updated_by) FROM stdin;
1	AHSS_MAIN	AHSS Main Organization	BUSINESS_IN	\N	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	DEMO_CORP	Demo Corporation	BUSINESS_OUT	\N	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	TEST_ORG	Test Organization	BUSINESS_IN	\N	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	INDIVIDUAL_1	John Doe Individual	INDIVIDUAL	\N	ACTIVE	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: tenant_entity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tenant_entity (tenant_id, entity_id, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	3	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	4	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	5	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	6	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	7	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	8	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	9	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	10	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	11	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	12	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: tenant_plan; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tenant_plan (tenant_id, plan_id, assigned_at, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	4	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	3	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: user_group; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_group (user_group_id, name, description, created_at, updated_at, created_by, updated_by, deleted_at) FROM stdin;
1	System Administrators	Group for all system administrators	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system	\N
2	Tenant Administrators	Group for tenant-level administrators	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system	\N
3	Product Managers	Group for product management team	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system	\N
4	RBAC Managers	Group for role and permission managers	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system	\N
5	Standard Users	Group for standard system users	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system	\N
6	Read Only Users	Group for users with read-only access	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system	\N
7	Demo Users	Group for demonstration purposes	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system	\N
\.


--
-- Data for Name: user_group_member; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_group_member (user_group_id, entity_id, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
1	2	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
2	3	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	4	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
3	5	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
4	6	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	7	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	8	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	9	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
5	10	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
6	11	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
7	12	2025-10-05 12:25:46.576211	2025-10-05 12:25:46.576211	system	system
\.


--
-- Data for Name: user_group_users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_group_users (user_group_id, user_id, created_at, updated_at, created_by, updated_by) FROM stdin;
1	1	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
1	2	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
2	3	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
3	4	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
4	5	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
5	6	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
5	7	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
5	9	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
5	10	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
7	8	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (user_id, role_id, created_at, updated_at, created_by, updated_by) FROM stdin;
2	1	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
1	2	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
3	7	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
4	10	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
5	16	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
6	4	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
7	21	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
8	21	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
9	21	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
10	21	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, username, email, password, first_name, last_name, phone_number, user_status, email_verified, last_login, failed_login_attempts, account_locked_until, password_changed_at, created_at, updated_at, created_by, updated_by) FROM stdin;
3	tenant.admin	tenant.admin@ahss.com	$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPdkzjKWw0/0i	Tenant	Administrator	\N	ACTIVE	t	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
4	product.manager	product.manager@ahss.com	$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPdkzjKWw0/0i	Product	Manager	\N	ACTIVE	t	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
5	role.manager	role.manager@ahss.com	$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPdkzjKWw0/0i	Role	Manager	\N	ACTIVE	t	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
6	user.manager	user.manager@ahss.com	$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPdkzjKWw0/0i	User	Manager	\N	ACTIVE	t	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
7	testuser	testuser@test.com	$2a$12$4RWLuXXvfxe24piLxVlsH.VmllEqjWi/VpJd/E8RZt7wUhALn0aJy	Test	User	\N	ACTIVE	t	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
8	demo	demo@demo.com	$2a$12$ZeUwHfuS8yhs.rjid/31OOEkEGDh.nu/PiB13bLlyidQBhow4Es9O	Demo	User	\N	ACTIVE	t	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
9	john.doe	john.doe@example.com	$2a$12$4RWLuXXvfxe24piLxVlsH.VmllEqjWi/VpJd/E8RZt7wUhALn0aJy	John	Doe	\N	ACTIVE	t	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
10	jane.smith	jane.smith@example.com	$2a$12$4RWLuXXvfxe24piLxVlsH.VmllEqjWi/VpJd/E8RZt7wUhALn0aJy	Jane	Smith	\N	ACTIVE	t	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
11	inactive.user	inactive@test.com	$2a$12$4RWLuXXvfxe24piLxVlsH.VmllEqjWi/VpJd/E8RZt7wUhALn0aJy	Inactive	User	\N	INACTIVE	f	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
12	pending.user	pending@test.com	$2a$12$4RWLuXXvfxe24piLxVlsH.VmllEqjWi/VpJd/E8RZt7wUhALn0aJy	Pending	User	\N	PENDING_VERIFICATION	f	\N	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
13	testuser2025	testuser2025@test.com	$2a$10$Q0Nok8r/9XK/QGWifdOavuh6wvHdURzCAdTMvXYzeI6BxkRI1MX0e	Test	User	\N	ACTIVE	f	\N	0	\N	2025-10-05 13:46:10.824322	2025-10-05 13:46:10.824311	2025-10-05 13:46:10.824319	system	system
1	admin	admin@ahss.com	$2a$12$fe11/7dbJGWP.XSY6e7ISei4cF2hGtvC9bL35Is.oYiGdTCxmfhHa	System	Administrator	\N	ACTIVE	t	2025-10-06 08:09:50.039957	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
2	superadmin	superadmin@ahss.com	$2a$10$9qgUn7rhqgA4cF1zriweJ.zTciXnaEevRpS3kZ10JeShby4Yy8vwe	Super	Administrator	\N	ACTIVE	t	2025-10-06 09:20:35.904108	0	\N	\N	2025-10-05 13:36:56.978954	2025-10-05 13:36:56.978954	system	system
\.


--
-- Name: entity_entity_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.entity_entity_id_seq', 12, true);


--
-- Name: group_module_role_group_module_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.group_module_role_group_module_role_id_seq', 16, true);


--
-- Name: module_module_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.module_module_id_seq', 18, true);


--
-- Name: organization_org_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.organization_org_id_seq', 5, true);


--
-- Name: package_module_package_module_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.package_module_package_module_id_seq', 1, false);


--
-- Name: package_package_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.package_package_id_seq', 4, true);


--
-- Name: permission_permission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.permission_permission_id_seq', 39, true);


--
-- Name: plan_plan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.plan_plan_id_seq', 4, true);


--
-- Name: product_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.product_product_id_seq', 6, true);


--
-- Name: profile_profile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.profile_profile_id_seq', 12, true);


--
-- Name: role_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.role_role_id_seq', 30, true);


--
-- Name: sso_provider_sso_provider_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sso_provider_sso_provider_id_seq', 1, false);


--
-- Name: tenant_tenant_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.tenant_tenant_id_seq', 4, true);


--
-- Name: user_group_user_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_group_user_group_id_seq', 7, true);


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 13, true);


--
-- Name: entity entity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entity
    ADD CONSTRAINT entity_pkey PRIMARY KEY (entity_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: group_module_role group_module_role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_module_role
    ADD CONSTRAINT group_module_role_pkey PRIMARY KEY (group_module_role_id);


--
-- Name: group_module_role group_module_role_user_group_id_module_id_role_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_module_role
    ADD CONSTRAINT group_module_role_user_group_id_module_id_role_id_key UNIQUE (user_group_id, module_id, role_id);


--
-- Name: module module_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.module
    ADD CONSTRAINT module_code_key UNIQUE (code);


--
-- Name: module module_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.module
    ADD CONSTRAINT module_pkey PRIMARY KEY (module_id);


--
-- Name: organization organization_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (org_id);


--
-- Name: package_module package_module_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_module
    ADD CONSTRAINT package_module_pkey PRIMARY KEY (package_module_id);


--
-- Name: package package_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_pkey PRIMARY KEY (package_id);


--
-- Name: permission permission_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_name_key UNIQUE (name);


--
-- Name: permission permission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (permission_id);


--
-- Name: plan plan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan
    ADD CONSTRAINT plan_pkey PRIMARY KEY (plan_id);


--
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (product_id);


--
-- Name: product product_product_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_product_code_key UNIQUE (product_code);


--
-- Name: profile profile_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (profile_id);


--
-- Name: role role_module_id_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_module_id_name_key UNIQUE (module_id, name);


--
-- Name: role_permission role_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT role_permission_pkey PRIMARY KEY (role_id, permission_id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_id);


--
-- Name: sso_provider sso_provider_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sso_provider
    ADD CONSTRAINT sso_provider_pkey PRIMARY KEY (sso_provider_id);


--
-- Name: tenant_entity tenant_entity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant_entity
    ADD CONSTRAINT tenant_entity_pkey PRIMARY KEY (tenant_id, entity_id);


--
-- Name: tenant tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant
    ADD CONSTRAINT tenant_pkey PRIMARY KEY (tenant_id);


--
-- Name: tenant_plan tenant_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant_plan
    ADD CONSTRAINT tenant_plan_pkey PRIMARY KEY (tenant_id, plan_id);


--
-- Name: tenant tenant_tenant_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant
    ADD CONSTRAINT tenant_tenant_code_key UNIQUE (tenant_code);


--
-- Name: user_group_member user_group_member_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_member
    ADD CONSTRAINT user_group_member_pkey PRIMARY KEY (user_group_id, entity_id);


--
-- Name: user_group user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group
    ADD CONSTRAINT user_group_pkey PRIMARY KEY (user_group_id);


--
-- Name: user_group_users user_group_users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_users
    ADD CONSTRAINT user_group_users_pkey PRIMARY KEY (user_group_id, user_id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_entity_path; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_entity_path ON public.entity USING gist (path public.gist_trgm_ops);


--
-- Name: idx_group_module_role_group; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_group_module_role_group ON public.group_module_role USING btree (user_group_id);


--
-- Name: idx_group_module_role_module; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_group_module_role_module ON public.group_module_role USING btree (module_id);


--
-- Name: idx_module_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_module_product ON public.module USING btree (product_id);


--
-- Name: idx_organization_parent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_organization_parent ON public.organization USING btree (parent_org_id);


--
-- Name: idx_organization_tenant; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_organization_tenant ON public.organization USING btree (tenant_id);


--
-- Name: idx_role_module; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_role_module ON public.role USING btree (module_id);


--
-- Name: idx_tenant_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tenant_code ON public.tenant USING btree (tenant_code);


--
-- Name: idx_tenant_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tenant_status ON public.tenant USING btree (tenant_status);


--
-- Name: idx_user_group_deleted; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_group_deleted ON public.user_group USING btree (deleted_at);


--
-- Name: idx_user_group_users_group; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_group_users_group ON public.user_group_users USING btree (user_group_id);


--
-- Name: idx_user_group_users_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_group_users_user ON public.user_group_users USING btree (user_id);


--
-- Name: idx_user_roles_role; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_roles_role ON public.user_roles USING btree (role_id);


--
-- Name: idx_user_roles_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_roles_user ON public.user_roles USING btree (user_id);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: idx_users_email_verified; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_email_verified ON public.users USING btree (email_verified);


--
-- Name: idx_users_last_login; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_last_login ON public.users USING btree (last_login);


--
-- Name: idx_users_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_status ON public.users USING btree (user_status);


--
-- Name: idx_users_username; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_username ON public.users USING btree (username);


--
-- Name: entity entity_parent_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entity
    ADD CONSTRAINT entity_parent_entity_id_fkey FOREIGN KEY (parent_entity_id) REFERENCES public.entity(entity_id);


--
-- Name: group_module_role group_module_role_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_module_role
    ADD CONSTRAINT group_module_role_module_id_fkey FOREIGN KEY (module_id) REFERENCES public.module(module_id);


--
-- Name: group_module_role group_module_role_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_module_role
    ADD CONSTRAINT group_module_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(role_id);


--
-- Name: group_module_role group_module_role_user_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_module_role
    ADD CONSTRAINT group_module_role_user_group_id_fkey FOREIGN KEY (user_group_id) REFERENCES public.user_group(user_group_id);


--
-- Name: module module_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.module
    ADD CONSTRAINT module_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.product(product_id);


--
-- Name: organization organization_parent_org_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_parent_org_id_fkey FOREIGN KEY (parent_org_id) REFERENCES public.organization(org_id);


--
-- Name: organization organization_tenant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenant(tenant_id);


--
-- Name: package_module package_module_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_module
    ADD CONSTRAINT package_module_module_id_fkey FOREIGN KEY (module_id) REFERENCES public.module(module_id);


--
-- Name: package_module package_module_package_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_module
    ADD CONSTRAINT package_module_package_id_fkey FOREIGN KEY (package_id) REFERENCES public.package(package_id);


--
-- Name: package package_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES public.plan(plan_id);


--
-- Name: profile profile_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES public.entity(entity_id);


--
-- Name: profile profile_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(role_id);


--
-- Name: role role_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_module_id_fkey FOREIGN KEY (module_id) REFERENCES public.module(module_id);


--
-- Name: role_permission role_permission_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT role_permission_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permission(permission_id);


--
-- Name: role_permission role_permission_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT role_permission_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(role_id);


--
-- Name: sso_provider sso_provider_tenant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sso_provider
    ADD CONSTRAINT sso_provider_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenant(tenant_id);


--
-- Name: tenant_entity tenant_entity_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant_entity
    ADD CONSTRAINT tenant_entity_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES public.entity(entity_id);


--
-- Name: tenant_entity tenant_entity_tenant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant_entity
    ADD CONSTRAINT tenant_entity_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenant(tenant_id);


--
-- Name: tenant_plan tenant_plan_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant_plan
    ADD CONSTRAINT tenant_plan_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES public.plan(plan_id);


--
-- Name: tenant_plan tenant_plan_tenant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant_plan
    ADD CONSTRAINT tenant_plan_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenant(tenant_id);


--
-- Name: user_group_member user_group_member_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_member
    ADD CONSTRAINT user_group_member_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES public.entity(entity_id);


--
-- Name: user_group_member user_group_member_user_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_member
    ADD CONSTRAINT user_group_member_user_group_id_fkey FOREIGN KEY (user_group_id) REFERENCES public.user_group(user_group_id);


--
-- Name: user_group_users user_group_users_user_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_users
    ADD CONSTRAINT user_group_users_user_group_id_fkey FOREIGN KEY (user_group_id) REFERENCES public.user_group(user_group_id) ON DELETE CASCADE;


--
-- Name: user_group_users user_group_users_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_users
    ADD CONSTRAINT user_group_users_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(role_id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict ESHVcKYRxedu2VRMf9i2C0l3IsO3MGuCwHxXWzetdZfWoU20glXwolSvcqaLosE

