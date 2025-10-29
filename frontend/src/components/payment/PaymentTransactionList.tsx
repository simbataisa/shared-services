import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Eye, RefreshCw, DollarSign, CreditCard, Plus } from "lucide-react";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { StatusBadge } from "@/components/common/StatusBadge";
import { paymentApi } from "@/lib/paymentApi";
import type { PaymentTransaction, PaymentTransactionStatus, PaymentTransactionType } from "@/types/payment";

const PaymentTransactionList: React.FC = () => {
  const [transactions, setTransactions] = useState<PaymentTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [typeFilter, setTypeFilter] = useState("");
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

  const typeOptions = [
    { value: "", label: "All Types" },
    { value: "PAYMENT", label: "Payment" },
    { value: "REFUND", label: "Refund" },
    { value: "CHARGEBACK", label: "Chargeback" },
    { value: "ADJUSTMENT", label: "Adjustment" }
  ];

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      let response;
      
      if (statusFilter) {
        response = await paymentApi.transactions.getByStatus(statusFilter as PaymentTransactionStatus, currentPage - 1, 10);
      } else {
        response = await paymentApi.transactions.getAll(currentPage - 1, 10);
      }
      
      setTransactions(response.data.content || []);
      setTotalPages(response.data.totalPages || 1);
    } catch (error) {
      console.error("Error fetching transactions:", error);
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, [currentPage, searchTerm, statusFilter, typeFilter]);

  const handleRetryTransaction = async (transactionId: string) => {
    try {
      await paymentApi.transactions.retry(parseInt(transactionId));
      fetchTransactions(); // Refresh the list
    } catch (error) {
      console.error("Error retrying transaction:", error);
    }
  };

  const getStatusColor = (status: PaymentTransactionStatus): string => {
    switch (status) {
      case "COMPLETED": return "success";
      case "FAILED": return "destructive";
      case "CANCELLED": return "secondary";
      case "PROCESSING": return "default";
      default: return "secondary";
    }
  };

  const getTypeIcon = (type: PaymentTransactionType) => {
    switch (type) {
      case "PAYMENT": return <CreditCard className="h-4 w-4" />;
      case "REFUND": return <RefreshCw className="h-4 w-4" />;
      default: return <DollarSign className="h-4 w-4" />;
    }
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD'
    }).format(amount);
  };

  if (loading && transactions.length === 0) {
    return (
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Payment Transactions</CardTitle>
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
            <CreditCard className="h-5 w-5" />
            Payment Transactions
          </CardTitle>
        </CardHeader>
        <CardContent>
          <SearchAndFilter
            searchTerm={searchTerm}
            onSearchChange={setSearchTerm}
            searchPlaceholder="Search by transaction ID, gateway reference..."
            filters={[
              {
                label: "Status",
                value: statusFilter,
                onChange: setStatusFilter,
                options: statusOptions,
                placeholder: "All Statuses",
                width: "200px"
              },
              {
                label: "Type",
                value: typeFilter,
                onChange: setTypeFilter,
                options: typeOptions,
                placeholder: "All Types",
                width: "200px"
              }
            ]}
            actions={
              <PermissionGuard permission="PAYMENT_MGMT:create">
                <Button asChild>
                  <Link to="/payments/requests/new">
                    <Plus className="mr-2 h-4 w-4" />
                    Create Payment Request
                  </Link>
                </Button>
              </PermissionGuard>
            }
          />
        </CardContent>
      </Card>

      <div className="grid gap-4">
        {transactions.map((transaction) => (
          <Card key={transaction.id} className="hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    {getTypeIcon(transaction.transactionType)}
                    <div>
                      <p className="font-medium">{transaction.transactionCode}</p>
                      <p className="text-sm text-muted-foreground">
                        Gateway: {transaction.gatewayName}
                      </p>
                    </div>
                  </div>
                  
                  <div className="text-center">
                    <p className="font-semibold text-lg">
                      {formatAmount(transaction.amount, transaction.currency)}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {transaction.transactionType}
                    </p>
                  </div>
                </div>

                <div className="flex items-center space-x-4">
                  <div className="text-right">
                    <StatusBadge 
                      status={transaction.status}
                    />
                    <p className="text-sm text-muted-foreground mt-1">
                      {new Date(transaction.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="flex space-x-2">
                    <PermissionGuard permission="PAYMENT_MGMT:read">
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4" />
                      </Button>
                    </PermissionGuard>
                    
                    {transaction.status === "FAILED" && (
                      <PermissionGuard permission="PAYMENT_MGMT:update">
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => handleRetryTransaction(transaction.id.toString())}
                        >
                          <RefreshCw className="h-4 w-4" />
                        </Button>
                      </PermissionGuard>
                    )}
                  </div>
                </div>
              </div>

              {transaction.gatewayTransactionId && (
                <div className="mt-4 pt-4 border-t">
                  <p className="text-sm text-muted-foreground">
                    Gateway Reference: <span className="font-mono">{transaction.gatewayTransactionId}</span>
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        ))}
      </div>

      {transactions.length === 0 && !loading && (
        <Card>
          <CardContent className="text-center py-8">
            <CreditCard className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground">No payment transactions found</p>
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

export default PaymentTransactionList;