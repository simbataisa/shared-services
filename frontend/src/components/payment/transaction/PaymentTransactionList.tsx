import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Plus } from "lucide-react";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import PaymentTransactionTable from "./PaymentTransactionTable";
import { paymentApi } from "@/lib/paymentApi";
import type { PaymentTransaction, PaymentTransactionStatus } from "@/types/payment";

const PaymentTransactionList: React.FC = () => {
  const [transactions, setTransactions] = useState<PaymentTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [typeFilter, setTypeFilter] = useState("all");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const pageSize = 10;

  const statusOptions = [
    { value: "all", label: "All Statuses" },
    { value: "PENDING", label: "Pending" },
    { value: "SUCCESS", label: "Success" },
    { value: "FAILED", label: "Failed" },
    { value: "CANCELLED", label: "Cancelled" }
  ];

  const typeOptions = [
    { value: "all", label: "All Types" },
    { value: "PAYMENT", label: "Payment" },
    { value: "REFUND", label: "Refund" },
    { value: "CHARGEBACK", label: "Chargeback" },
    { value: "ADJUSTMENT", label: "Adjustment" }
  ];

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      let response;
      
      if (statusFilter && statusFilter !== "all") {
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

  if (loading && transactions.length === 0) {
    return (
      <div className="container mx-auto py-10">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">
              Payment Transactions
            </h1>
            <p className="text-muted-foreground">
              Manage and monitor payment transactions across all gateways
            </p>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Transactions</CardTitle>
            <CardDescription>
              A list of all payment transactions in the system
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-10">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Payment Transactions
          </h1>
          <p className="text-muted-foreground">
            Manage and monitor payment transactions across all gateways
          </p>
        </div>
      </div>

      {/* Search and Filters */}
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
            width: "w-[180px]"
          },
          {
            label: "Type",
            value: typeFilter,
            onChange: setTypeFilter,
            options: typeOptions,
            placeholder: "All Types",
            width: "w-[180px]"
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

      <Card>
        <CardHeader>
          <CardTitle>Transactions</CardTitle>
          <CardDescription>
            A list of all payment transactions in the system
          </CardDescription>
        </CardHeader>
        <CardContent>
          <PaymentTransactionTable
            transactions={transactions}
            loading={loading}
            showActions={true}
            emptyMessage="No payment transactions found"
          />
        </CardContent>
      </Card>

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