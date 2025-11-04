import React, { useState, useEffect, useMemo } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { CreditCard } from "lucide-react";
import { PaymentTransactionTable } from "./PaymentTransactionTable";
import { paymentTransactionApi } from "@/lib/paymentApi";
import type { PaymentTransaction } from "@/types/payment";
import type { TableFilter } from "@/types/components";
import {
  PAYMENT_TRANSACTION_STATUS_MAPPINGS,
  PAYMENT_TRANSACTION_TYPE_MAPPINGS,
} from "@/types/payment";
import { useNavigate } from "react-router-dom";

interface PaymentTransactionListProps {
  className?: string;
}

export const PaymentTransactionList: React.FC<PaymentTransactionListProps> = ({
  className,
}) => {
  const [transactions, setTransactions] = useState<PaymentTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [typeFilter, setTypeFilter] = useState<string>("");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const navigate = useNavigate();

  // Status and type options for filters
  const statusOptions = Object.entries(PAYMENT_TRANSACTION_STATUS_MAPPINGS).map(
    ([value, label]) => ({
      value,
      label,
    })
  );

  const typeOptions = Object.entries(PAYMENT_TRANSACTION_TYPE_MAPPINGS).map(
    ([value, label]) => ({
      value,
      label,
    })
  );

  // Fetch transactions
  useEffect(() => {
    const fetchTransactions = async () => {
      setLoading(true);
      try {
        // Make actual API call to fetch transactions
        const response = await paymentTransactionApi.getAll(currentPage, 10);

        if (response.success && response.data) {
          setTransactions(response.data.content || []);
          setTotalPages(response.data.totalPages || 1);
        } else {
          console.error("Failed to fetch transactions:", response.message);
          setTransactions([]);
        }
      } catch (error) {
        console.error("Error fetching transactions:", error);
        setTransactions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchTransactions();
  }, [currentPage, statusFilter, typeFilter]);

  // Filter data based on search term and filters
  const filteredData = useMemo(() => {
    return transactions.filter((transaction) => {
      const matchesSearch =
        !searchTerm ||
        transaction.transactionCode
          .toLowerCase()
          .includes(searchTerm.toLowerCase()) ||
        (transaction.gatewayName &&
          transaction.gatewayName
            .toLowerCase()
            .includes(searchTerm.toLowerCase())) ||
        transaction.paymentMethod
          .toLowerCase()
          .includes(searchTerm.toLowerCase());

      const matchesStatus =
        !statusFilter || transaction.transactionStatus === statusFilter;
      const matchesType =
        !typeFilter || transaction.paymentMethod === typeFilter;

      return matchesSearch && matchesStatus && matchesType;
    });
  }, [transactions, searchTerm, statusFilter, typeFilter]);

  // Define filters for the table
  const filters: TableFilter[] = [
    {
      label: "Status",
      options: statusOptions,
      value: statusFilter,
      onChange: setStatusFilter,
    },
    {
      label: "Type",
      options: typeOptions,
      value: typeFilter,
      onChange: setTypeFilter,
    },
  ];

  // Handle search change
  const handleSearchChange = (term: string) => {
    setSearchTerm(term);
  };

  // Handle view transaction
  const handleViewTransaction = (transaction: PaymentTransaction) => {
    console.log("View transaction:", transaction);
    navigate(`/payments/transactions/${transaction.id}`);
  };

  // Calculate statistics
  const totalTransactions = transactions.length;
  const completedTransactions = transactions.filter(
    (t) => t.transactionStatus === "COMPLETED"
  ).length;
  const pendingTransactions = transactions.filter(
    (t) => t.transactionStatus === "PENDING"
  ).length;
  const failedTransactions = transactions.filter(
    (t) => t.transactionStatus === "FAILED"
  ).length;

  return (
    <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 space-y-4 sm:space-y-0">
        <div className="space-y-1">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">
            Payment Transaction Management
          </h1>
          <p className="text-sm sm:text-base text-muted-foreground">
            Manage payment transactions and their statuses
          </p>
        </div>
      </div>
      {/* Header with Statistics
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Transactions
            </CardTitle>
            <CreditCard className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-16" />
            ) : (
              <div className="text-2xl font-bold">{totalTransactions}</div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Completed</CardTitle>
            <div className="h-2 w-2 rounded-full bg-green-500" />
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-16" />
            ) : (
              <div className="text-2xl font-bold text-green-600">
                {completedTransactions}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pending</CardTitle>
            <div className="h-2 w-2 rounded-full bg-yellow-500" />
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-16" />
            ) : (
              <div className="text-2xl font-bold text-yellow-600">
                {pendingTransactions}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Failed</CardTitle>
            <div className="h-2 w-2 rounded-full bg-red-500" />
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-16" />
            ) : (
              <div className="text-2xl font-bold text-red-600">
                {failedTransactions}
              </div>
            )}
          </CardContent>
        </Card>
      </div> */}

      {/* Transaction Table */}

      <PaymentTransactionTable
        data={filteredData}
        loading={loading}
        searchTerm={searchTerm}
        onSearchChange={handleSearchChange}
        searchPlaceholder="Search by transaction code, gateway, or payment method..."
        filters={filters}
        onViewTransaction={handleViewTransaction}
      />
    </div>
  );
};

export default PaymentTransactionList;
