import { useState, useEffect, useMemo } from "react";
import { Link } from "react-router-dom";
import { CreditCard, Plus } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { paymentApi } from "@/lib/paymentApi";
import type { PaymentRequest, PaymentRequestStatus } from "@/types/payment";
import type { TableFilter } from "@/types/components";
import { PAYMENT_REQUEST_STATUS_OPTIONS } from "@/types/payment";
import { PaymentRequestTable } from "./PaymentRequestTable";

export default function PaymentRequestList() {
  const [paymentRequests, setPaymentRequests] = useState<PaymentRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");

  useEffect(() => {
    fetchPaymentRequests();
  }, [statusFilter]);

  const fetchPaymentRequests = async () => {
    try {
      setLoading(true);
      let response;

      if (statusFilter && statusFilter !== "all") {
        response = await paymentApi.requests.getByStatus(
          statusFilter as PaymentRequestStatus,
          0,
          1000
        );
      } else {
        response = await paymentApi.requests.getAll(0, 1000);
      }

      setPaymentRequests(response.data.content || []);
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

  // Filter data based on search term and status
  const filteredData = useMemo(() => {
    return paymentRequests.filter((request) => {
      const matchesSearch =
        searchTerm === "" ||
        request.requestCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
        request.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        request.payerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        request.payerEmail.toLowerCase().includes(searchTerm.toLowerCase());

      return matchesSearch;
    });
  }, [paymentRequests, searchTerm]);

  // Define filters for the table
  const filters: TableFilter[] = [
    {
      label: "Status",
      value: statusFilter,
      onChange: setStatusFilter,
      options: [...PAYMENT_REQUEST_STATUS_OPTIONS],
      placeholder: "All Statuses",
      width: "200px",
    },
  ];

  // Define actions for the table
  const actions = (
    <PermissionGuard permission="PAYMENT_MGMT:create">
      <Button asChild>
        <Link to="/payments/requests/new">
          <Plus className="mr-2 h-4 w-4" />
          New Request
        </Link>
      </Button>
    </PermissionGuard>
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
    <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 space-y-4 sm:space-y-0">
        <div className="space-y-1">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">
            Payment Request Management
          </h1>
          <p className="text-sm sm:text-base text-muted-foreground">
            Manage payment requests and their statuses
          </p>
        </div>
      </div>

      <PaymentRequestTable
        data={filteredData}
        loading={loading}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search by code, title, payer..."
        filters={filters}
        actions={actions}
        onApproveRequest={handleApproveRequest}
        onRejectRequest={handleRejectRequest}
        onCancelRequest={handleCancelRequest}
      />
    </div>
  );
}
