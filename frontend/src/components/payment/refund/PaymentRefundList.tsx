import React, { useState, useEffect, useMemo } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Undo2, Plus } from "lucide-react";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { paymentApi } from "@/lib/paymentApi";
import { PaymentRefundTable } from "./PaymentRefundTable";
import type { PaymentRefund, PaymentTransactionStatus } from "@/types/payment";
import type { RefundListProps, TableFilter } from "@/types/components";
import { useNavigate } from "react-router-dom";

const PaymentRefundList: React.FC<RefundListProps> = ({
  onRefundSelect,
  selectedRefundId,
  showActions = true,
}) => {
  const [refunds, setRefunds] = useState<PaymentRefund[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const navigate = useNavigate();

  const statusOptions = [
    { value: "all", label: "All Statuses" },
    { value: "PENDING", label: "Pending" },
    { value: "SUCCESS", label: "Success" },
    { value: "FAILED", label: "Failed" },
    { value: "CANCELLED", label: "Cancelled" },
  ];

  const fetchRefunds = async () => {
    try {
      setLoading(true);
      let response;

      if (statusFilter && statusFilter !== "all") {
        response = await paymentApi.refunds.getByStatus(
          statusFilter as PaymentTransactionStatus,
          0,
          100
        );
      } else {
        response = await paymentApi.refunds.getAll(0, 100);
      }

      setRefunds(response.data.content || []);
    } catch (error) {
      console.error("Error fetching refunds:", error);
      setRefunds([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRefunds();
  }, [statusFilter]);

  const handleViewRefund = (refund: PaymentRefund) => {
    console.log("Viewing refund:", refund);
    navigate(`/payments/refunds/${refund.id}`);
    onRefundSelect?.(refund);
  };

  // Filter data based on search term and status filter
  const filteredData = useMemo(() => {
    return refunds.filter((refund) => {
      const matchesSearch =
        !searchTerm ||
        refund.refundCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
        refund.reason.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (refund.gatewayName &&
          refund.gatewayName.toLowerCase().includes(searchTerm.toLowerCase()));

      const matchesStatus =
        statusFilter === "all" || refund.refundStatus === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [refunds, searchTerm, statusFilter]);

  // Define filters for the table
  const filters: TableFilter[] = [
    {
      label: "Status",
      options: statusOptions,
      value: statusFilter,
      onChange: setStatusFilter,
    },
  ];

  // Define actions for the table
  const actions = (
    <PermissionGuard permission="payment:refund:create">
      <Button>
        <Plus className="mr-2 h-4 w-4" />
        Create Refund
      </Button>
    </PermissionGuard>
  );

  return (
    <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 space-y-4 sm:space-y-0">
        <div className="space-y-1">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight flex items-center gap-2">
            <Undo2 className="h-8 w-8" />
            Payment Refunds
          </h1>
          <p className="text-sm sm:text-base text-muted-foreground">
            Manage payment refunds and their statuses
          </p>
        </div>
      </div>

      <PaymentRefundTable
        data={filteredData}
        loading={loading}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search by refund code, reason, gateway..."
        filters={filters}
        actions={actions}
        onViewRefund={handleViewRefund}
      />
    </div>
  );
};

export default PaymentRefundList;
