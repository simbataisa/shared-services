import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Eye, RefreshCw, DollarSign, Undo2 } from "lucide-react";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { StatusBadge } from "@/components/common/StatusBadge";
import { paymentApi } from "@/lib/paymentApi";
import type { PaymentRefund, PaymentTransactionStatus } from "@/types/payment";

const PaymentRefundList: React.FC = () => {
  const [refunds, setRefunds] = useState<PaymentRefund[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const pageSize = 10;

  const statusOptions = [
    { value: "", label: "All Statuses" },
    { value: "PENDING", label: "Pending" },
    { value: "PROCESSING", label: "Processing" },
    { value: "COMPLETED", label: "Completed" },
    { value: "FAILED", label: "Failed" },
    { value: "CANCELLED", label: "Cancelled" }
  ];

  const fetchRefunds = async () => {
    try {
      setLoading(true);
      let response;
      
      if (statusFilter) {
        response = await paymentApi.refunds.getByStatus(statusFilter as PaymentTransactionStatus, currentPage - 1, 10);
      } else {
        response = await paymentApi.refunds.getAll(currentPage - 1, 10);
      }
      
      setRefunds(response.data.content || []);
      setTotalPages(response.data.totalPages || 1);
    } catch (error) {
      console.error("Error fetching refunds:", error);
      setRefunds([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRefunds();
  }, [currentPage, searchTerm, statusFilter]);

  const handleRetryRefund = async (refundId: string) => {
    try {
      // Note: Retry functionality might need to be implemented in the backend
      console.log("Retry refund:", refundId);
      fetchRefunds(); // Refresh the list
    } catch (error) {
      console.error("Error retrying refund:", error);
    }
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD'
    }).format(amount);
  };

  if (loading && refunds.length === 0) {
    return (
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Payment Refunds</CardTitle>
          </CardHeader>
          <CardContent>
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
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Undo2 className="h-5 w-5" />
            Payment Refunds
          </CardTitle>
        </CardHeader>
        <CardContent>
          <SearchAndFilter
            searchTerm={searchTerm}
            onSearchChange={setSearchTerm}
            searchPlaceholder="Search by refund code, reason..."
            filters={[
              {
                label: "Status",
                value: statusFilter,
                onChange: setStatusFilter,
                options: statusOptions,
                placeholder: "All Statuses",
                width: "200px"
              }
            ]}
          />
        </CardContent>
      </Card>

      <div className="grid gap-4">
        {refunds.map((refund) => (
          <Card key={refund.id} className="hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <Undo2 className="h-4 w-4" />
                    <div>
                      <p className="font-medium">{refund.refundCode}</p>
                      <p className="text-sm text-muted-foreground">
                        Gateway: {refund.gatewayName}
                      </p>
                    </div>
                  </div>
                  
                  <div className="text-center">
                    <p className="font-semibold text-lg">
                      {formatAmount(refund.refundAmount, refund.currency)}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      Refund
                    </p>
                  </div>
                </div>

                <div className="flex items-center space-x-4">
                  <div className="text-right">
                    <StatusBadge 
                      status={refund.refundStatus}
                    />
                    <p className="text-sm text-muted-foreground mt-1">
                      {new Date(refund.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="flex space-x-2">
                    <PermissionGuard permission="payment:refund:read">
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4" />
                      </Button>
                    </PermissionGuard>
                    
                    {refund.refundStatus === "FAILED" && (
                      <PermissionGuard permission="payment:refund:retry">
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => handleRetryRefund(refund.id.toString())}
                        >
                          <RefreshCw className="h-4 w-4" />
                        </Button>
                      </PermissionGuard>
                    )}
                  </div>
                </div>
              </div>

              <div className="mt-4 pt-4 border-t">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Reason</p>
                    <p className="text-sm">{refund.reason}</p>
                  </div>
                  {refund.externalRefundId && (
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">External ID</p>
                      <p className="text-sm font-mono">{refund.externalRefundId}</p>
                    </div>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {refunds.length === 0 && !loading && (
        <Card>
          <CardContent className="text-center py-8">
            <Undo2 className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground">No payment refunds found</p>
          </CardContent>
        </Card>
      )}

      {totalPages > 1 && (
        <div className="flex justify-center space-x-2">
          <Button
            variant="outline"
            onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
            disabled={currentPage === 1}
          >
            Previous
          </Button>
          <span className="flex items-center px-4">
            Page {currentPage} of {totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
            disabled={currentPage === totalPages}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
};

export default PaymentRefundList;