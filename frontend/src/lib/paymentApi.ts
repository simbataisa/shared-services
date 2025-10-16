import api from "./api";
import type {
  PaymentRequest,
  PaymentTransaction,
  PaymentRefund,
  PaymentAuditLog,
  CreatePaymentRequestDto,
  ProcessPaymentDto,
  CreateRefundDto,
  PaymentRequestFilters,
  PaymentTransactionFilters,
  PaymentRefundFilters,
  PaymentRequestStatus,
  PaymentTransactionStatus,
  PaymentStats
} from "@/types/payment";
import type { ApiResponse } from "@/types/api";

// Payment Requests API
export const paymentRequestApi = {
  // Get all payment requests with pagination
  getAll: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRequest[]; totalElements: number; totalPages: number }>>(`/v1/payments/requests?page=${page}&size=${size}`);
    return response.data;
  },

  // Get payment request by ID
  getById: async (id: number) => {
    const response = await api.get<ApiResponse<PaymentRequest>>(`/v1/payments/requests/${id}`);
    return response.data;
  },

  // Get payment request by code
  getByCode: async (code: string) => {
    const response = await api.get<ApiResponse<PaymentRequest>>(`/v1/payments/requests/code/${code}`);
    return response.data;
  },

  // Get payment requests by tenant
  getByTenant: async (tenantId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRequest[]; totalElements: number; totalPages: number }>>(`/v1/payments/requests/tenant/${tenantId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get payment requests by status
  getByStatus: async (status: PaymentRequestStatus, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRequest[]; totalElements: number; totalPages: number }>>(`/v1/payments/requests/status/${status}?page=${page}&size=${size}`);
    return response.data;
  },

  // Create new payment request
  create: async (data: CreatePaymentRequestDto) => {
    const response = await api.post<ApiResponse<PaymentRequest>>("/v1/payments/requests", data);
    return response.data;
  },

  // Update payment request
  update: async (id: number, data: CreatePaymentRequestDto) => {
    const response = await api.put<ApiResponse<PaymentRequest>>(`/v1/payments/requests/${id}`, data);
    return response.data;
  },

  // Cancel payment request
  cancel: async (id: number) => {
    const response = await api.patch<ApiResponse<void>>(`/v1/payments/requests/${id}/cancel`);
    return response.data;
  },

  // Approve payment request
  approve: async (id: number) => {
    const response = await api.patch<ApiResponse<void>>(`/v1/payments/requests/${id}/approve`);
    return response.data;
  },

  // Reject payment request
  reject: async (id: number) => {
    const response = await api.patch<ApiResponse<void>>(`/v1/payments/requests/${id}/reject`);
    return response.data;
  }
};

// Payment Transactions API
export const paymentTransactionApi = {
  // Get all transactions with pagination
  getAll: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentTransaction[]; totalElements: number; totalPages: number }>>(`/v1/payments/transactions?page=${page}&size=${size}`);
    return response.data;
  },

  // Get transaction by ID
  getById: async (id: number) => {
    const response = await api.get<ApiResponse<PaymentTransaction>>(`/v1/payments/transactions/${id}`);
    return response.data;
  },

  // Get transaction by code
  getByCode: async (code: string) => {
    const response = await api.get<ApiResponse<PaymentTransaction>>(`/v1/payments/transactions/code/${code}`);
    return response.data;
  },

  // Get transactions by request
  getByRequest: async (requestId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentTransaction[]; totalElements: number; totalPages: number }>>(`/v1/payments/transactions/request/${requestId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get transactions by status
  getByStatus: async (status: PaymentTransactionStatus, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentTransaction[]; totalElements: number; totalPages: number }>>(`/v1/payments/transactions/status/${status}?page=${page}&size=${size}`);
    return response.data;
  },

  // Process payment
  process: async (data: ProcessPaymentDto) => {
    const response = await api.post<ApiResponse<PaymentTransaction>>("/v1/payments/transactions/process", data);
    return response.data;
  },

  // Retry transaction
  retry: async (id: number) => {
    const response = await api.patch<ApiResponse<void>>(`/v1/payments/transactions/${id}/retry`);
    return response.data;
  },

  // Cancel transaction
  cancel: async (id: number) => {
    const response = await api.patch<ApiResponse<void>>(`/v1/payments/transactions/${id}/cancel`);
    return response.data;
  }
};

// Payment Refunds API
export const paymentRefundApi = {
  // Get all refunds with pagination
  getAll: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRefund[]; totalElements: number; totalPages: number }>>(`/v1/payments/refunds?page=${page}&size=${size}`);
    return response.data;
  },

  // Get refund by ID
  getById: async (id: number) => {
    const response = await api.get<ApiResponse<PaymentRefund>>(`/v1/payments/refunds/${id}`);
    return response.data;
  },

  // Get refund by code
  getByCode: async (code: string) => {
    const response = await api.get<ApiResponse<PaymentRefund>>(`/v1/payments/refunds/code/${code}`);
    return response.data;
  },

  // Get refunds by transaction
  getByTransaction: async (transactionId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRefund[]; totalElements: number; totalPages: number }>>(`/v1/payments/refunds/transaction/${transactionId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get refunds by status
  getByStatus: async (status: PaymentTransactionStatus, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRefund[]; totalElements: number; totalPages: number }>>(`/v1/payments/refunds/status/${status}?page=${page}&size=${size}`);
    return response.data;
  },

  // Create refund
  create: async (data: CreateRefundDto) => {
    const response = await api.post<ApiResponse<PaymentRefund>>("/v1/payments/refunds", data);
    return response.data;
  },

  // Cancel refund
  cancel: async (id: number) => {
    const response = await api.patch<ApiResponse<void>>(`/v1/payments/refunds/${id}/cancel`);
    return response.data;
  }
};

// Payment Audit Logs API
export const paymentAuditLogApi = {
  // Get all audit logs with pagination
  getAll: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/v1/payments/audit-logs?page=${page}&size=${size}`);
    return response.data;
  },

  // Get audit log by ID
  getById: async (id: number) => {
    const response = await api.get<ApiResponse<PaymentAuditLog>>(`/v1/payments/audit-logs/${id}`);
    return response.data;
  },

  // Get audit logs by payment request
  getByPaymentRequest: async (requestId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/v1/payments/audit-logs/request/${requestId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get audit logs by transaction
  getByTransaction: async (transactionId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/v1/payments/audit-logs/transaction/${transactionId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get audit logs by refund
  getByRefund: async (refundId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/v1/payments/audit-logs/refund/${refundId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get audit logs by user
  getByUser: async (userId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/v1/payments/audit-logs/user/${userId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Search audit logs
  search: async (searchTerm: string, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/v1/payments/audit-logs/search?searchTerm=${encodeURIComponent(searchTerm)}&page=${page}&size=${size}`);
    return response.data;
  }
};

// Payment Statistics API
export const paymentStatsApi = {
  // Get payment request statistics
  getRequestStats: async () => {
    const response = await api.get<ApiResponse<Record<string, number>>>("/v1/payments/stats/requests");
    return response.data;
  },

  // Get transaction statistics
  getTransactionStats: async () => {
    const response = await api.get<ApiResponse<Record<string, number>>>("/v1/payments/stats/transactions");
    return response.data;
  },

  // Get refund statistics
  getRefundStats: async () => {
    const response = await api.get<ApiResponse<Record<string, number>>>("/v1/payments/stats/refunds");
    return response.data;
  },

  // Get audit log statistics
  getAuditLogStats: async () => {
    const response = await api.get<ApiResponse<Record<string, any>>>("/v1/payments/stats/audit-logs");
    return response.data;
  }
};

// Combined payment API object
export const paymentApi = {
  requests: paymentRequestApi,
  transactions: paymentTransactionApi,
  refunds: paymentRefundApi,
  auditLogs: paymentAuditLogApi,
  stats: paymentStatsApi
};

export default paymentApi;