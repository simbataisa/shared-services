// Tenant-related types that match the backend structure

export type TenantType = "BUSINESS_IN" | "BUSINESS_OUT" | "INDIVIDUAL";
export type TenantStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";

// Extended Tenant interface that includes all properties needed for detail view
export interface TenantDetail {
  id: number;
  code: string;
  name: string;
  type: TenantType;
  organizationId?: number;
  status: TenantStatus;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
  description?: string;
}

export interface TenantUpdateData {
  code?: string;
  name?: string;
  type?: TenantType;
  organizationId?: number;
  status?: TenantStatus;
}

export interface TenantFormData {
  code: string;
  name: string;
  type: TenantType;
  organizationId?: number;
  status: TenantStatus;
}