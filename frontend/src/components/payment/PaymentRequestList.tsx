import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  CreditCard,
  Plus
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { getStatusIcon, getPaymentRequestStatusColor } from "@/lib/status-utils";
import { formatCurrency, formatDate } from "@/lib/utils";
import { paymentApi } from "@/lib/paymentApi";
import type { PaymentRequest, PaymentRequestStatus } from "@/types/payment";
import { PAYMENT_REQUEST_STATUS_MAPPINGS, PAYMENT_METHOD_TYPE_MAPPINGS, PAYMENT_REQUEST_STATUS_OPTIONS } from "@/types/payment";
import SearchAndFilter from "@/components/common/SearchAndFilter";
import { PaymentRequestTable } from "./PaymentRequestTable";

export default function PaymentRequestList() {
  const [paymentRequests, setPaymentRequests] = useState<PaymentRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchPaymentRequests();
  }, [currentPage, statusFilter]);

  const fetchPaymentRequests = async () => {
    try {
      setLoading(true);
      let response;
      
      if (statusFilter && statusFilter !== "all") {
        response = await paymentApi.requests.getByStatus(statusFilter as PaymentRequestStatus, currentPage - 1, 10);
      } else {
        response = await paymentApi.requests.getAll(currentPage - 1, 10);
      }
      
      setPaymentRequests(response.data.content || []);
      setTotalPages(response.data.totalPages || 1);
      setTotalElements(response.data.totalElements || 0);
    } catch (error) {
      console.error("Error fetching payment requests:", error);
      setPaymentRequests([]);
    } finally {
      setLoading(false);
    }
  };

  const handleApproveRequest = async (id: string) => {
    try {
      await paymentApi.requests.approve(id);
      fetchPaymentRequests(); // Refresh the list
    } catch (error) {
      console.error("Error approving request:", error);
    }
  };

  const handleRejectRequest = async (id: string) => {
    try {
      await paymentApi.requests.reject(id);
      fetchPaymentRequests(); // Refresh the list
    } catch (error) {
      console.error("Error rejecting request:", error);
    }
  };

  const handleCancelRequest = async (id: string) => {
    try {
      await paymentApi.requests.cancel(id);
      fetchPaymentRequests(); // Refresh the list
    } catch (error) {
      console.error("Error cancelling request:", error);
    }
  };

  const filteredRequests = paymentRequests.filter((request) =>
    searchTerm === "" ||
    request.requestCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
    request.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    request.payerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    request.payerEmail.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-10 w-32" />
        </div>
        <Card>
          <CardContent className="p-6">
            <div className="space-y-4">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Payment Requests</h1>
          <p className="text-muted-foreground">
            Manage and track payment requests ({totalElements} total)
          </p>
        </div>
      </div>

      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search by code, title, payer..."
        filters={[
          {
            label: "Status",
            value: statusFilter,
            onChange: setStatusFilter,
            options: [...PAYMENT_REQUEST_STATUS_OPTIONS],
            placeholder: "All Statuses",
            width: "200px"
          }
        ]}
        actions={
          <PermissionGuard permission="PAYMENT_MGMT:create">
            <Button asChild>
              <Link to="/payments/requests/new">
                <Plus className="mr-2 h-4 w-4" />
                New Request
              </Link>
            </Button>
          </PermissionGuard>
        }
      />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="h-5 w-5" />
            Payment Requests
          </CardTitle>
        </CardHeader>
        <CardContent>
          <PaymentRequestTable
          requests={filteredRequests}
          getPaymentRequestStatusColor={getPaymentRequestStatusColor}
          onApproveRequest={handleApproveRequest}
          onRejectRequest={handleRejectRequest}
          onCancelRequest={handleCancelRequest}
          currentPage={currentPage}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={setCurrentPage}
        />
        </CardContent>
      </Card>
    </div>
  );
}