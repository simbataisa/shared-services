import React, { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import type { ColumnDef } from "@tanstack/react-table";
import {
  useReactTable,
  getCoreRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  type SortingState,
  type ColumnFiltersState,
  type VisibilityState,
} from "@tanstack/react-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Receipt, Eye, MoreHorizontal, ArrowUpDown } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { DataTable } from "@/components/ui/data-table";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import type { PaymentTransaction } from "@/types/payment";
import type { BaseTableProps } from "@/types/components";
import {
  PAYMENT_TRANSACTION_STATUS_MAPPINGS,
  PAYMENT_TRANSACTION_TYPE_MAPPINGS,
  PAYMENT_METHOD_TYPE_MAPPINGS,
} from "@/types/payment";
import { getTransactionStatusBadgeProps } from "@/lib/status-utils";

interface PaymentTransactionTableProps
  extends BaseTableProps<PaymentTransaction> {
  data: PaymentTransaction[];
  selectedPaymentTrxId?: string;
  showActions?: boolean;
  onViewTransaction: (transaction: PaymentTransaction) => void;
}

// Utility functions
const formatCurrency = (amount: number, currency: string) => {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: currency || "USD",
  }).format(amount);
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const getTransactionTypeLabel = (type: string) => {
  return (
    PAYMENT_TRANSACTION_TYPE_MAPPINGS[
      type as keyof typeof PAYMENT_TRANSACTION_TYPE_MAPPINGS
    ] || type
  );
};

const getPaymentMethodLabel = (method: string) => {
  return (
    PAYMENT_METHOD_TYPE_MAPPINGS[
      method as keyof typeof PAYMENT_METHOD_TYPE_MAPPINGS
    ] || method
  );
};

export const PaymentTransactionTable: React.FC<
  PaymentTransactionTableProps
> = ({
  data = [],
  loading = false,
  searchTerm = "",
  onSearchChange = () => {},
  searchPlaceholder = "Search transactions...",
  filters = [],
  actions,
  showActions = true,
  onViewTransaction,
}) => {
  const [sorting, setSorting] = useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = useState({});

  // Ensure data is always an array to prevent undefined errors
  const safeData = data || [];

  const columns: ColumnDef<PaymentTransaction>[] = useMemo(() => {
    const baseColumns: ColumnDef<PaymentTransaction>[] = [
      {
        accessorKey: "transactionCode",
        header: ({ column }) => (
          <Button
            variant="ghost"
            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
            className="h-8 px-2 lg:px-3"
          >
            Transaction Code
            <ArrowUpDown className="ml-2 h-4 w-4" />
          </Button>
        ),
        cell: ({ row }) => (
          <div className="font-mono text-sm">
            {row.getValue("transactionCode")}
          </div>
        ),
      },
      {
        accessorKey: "amount",
        header: ({ column }) => (
          <Button
            variant="ghost"
            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
            className="h-8 px-2 lg:px-3"
          >
            Amount
            <ArrowUpDown className="ml-2 h-4 w-4" />
          </Button>
        ),
        cell: ({ row }) => {
          const transaction = row.original;
          return (
            <div className="font-medium">
              {formatCurrency(transaction.amount, transaction.currency)}
            </div>
          );
        },
      },
      {
        accessorKey: "paymentMethod",
        header: "Payment Method",
        cell: ({ row }) => {
          const method = row.original.paymentMethod;
          return <div className="text-sm">{getPaymentMethodLabel(method)}</div>;
        },
      },
      {
        accessorKey: "gateway",
        header: "Gateway",
        cell: ({ row }) => (
          <div className="text-sm capitalize">
            {row.original.gatewayName || "-"}
          </div>
        ),
      },
      {
        accessorKey: "status",
        header: "Status",
        cell: ({ row }) => {
          const status = row.original.transactionStatus;
          const badgeProps = getTransactionStatusBadgeProps(status);
          return (
            <Badge
              variant={badgeProps.variant}
              className={badgeProps.className}
            >
              {PAYMENT_TRANSACTION_STATUS_MAPPINGS[
                status as keyof typeof PAYMENT_TRANSACTION_STATUS_MAPPINGS
              ] || status}
            </Badge>
          );
        },
      },
      {
        accessorKey: "processedAt",
        header: ({ column }) => (
          <Button
            variant="ghost"
            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
            className="h-8 px-2 lg:px-3"
          >
            Processed At
            <ArrowUpDown className="ml-2 h-4 w-4" />
          </Button>
        ),
        cell: ({ row }) => {
          const processedAt = row.getValue("processedAt") as string;
          return processedAt ? (
            <div className="text-sm">{formatDate(processedAt)}</div>
          ) : (
            <div className="text-sm text-muted-foreground">-</div>
          );
        },
      },
      {
        accessorKey: "retryCount",
        header: "Retry Count",
        cell: ({ row }) => (
          <div className="text-sm text-center">
            {row.getValue("retryCount") || 0}
          </div>
        ),
      },
    ];

    if (showActions) {
      baseColumns.push({
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const transaction = row.original;

          return (
            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => onViewTransaction(transaction)}
                className="text-blue-600 hover:text-blue-700"
              >
                <Eye className="h-4 w-4" />
              </Button>
            </div>
          );
        },
      });
    }
    return baseColumns;
  }, [onViewTransaction]);

  const table = useReactTable({
    data: safeData,
    columns,
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onColumnVisibilityChange: setColumnVisibility,
    onRowSelectionChange: setRowSelection,
    state: {
      sorting,
      columnFilters,
      columnVisibility,
      rowSelection,
    },
  });

  // Combine actions with DataTableViewOptions for proper alignment
  const combinedActions = useMemo(
    () => (
      <div className="flex gap-2 items-center">
        <DataTableViewOptions table={table} />
        {actions}
      </div>
    ),
    [table, actions]
  );

  if (loading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <Skeleton className="h-10 w-[250px]" />
          <Skeleton className="h-10 w-[100px]" />
        </div>
        <div className="rounded-md border">
          <div className="p-4">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="flex items-center space-x-4 py-2">
                <Skeleton className="h-4 w-[100px]" />
                <Skeleton className="h-4 w-[150px]" />
                <Skeleton className="h-4 w-[100px]" />
                <Skeleton className="h-4 w-[120px]" />
                <Skeleton className="h-4 w-[80px]" />
                <Skeleton className="h-4 w-[100px]" />
                <Skeleton className="h-4 w-[100px]" />
                <Skeleton className="h-4 w-[150px]" />
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full space-y-4">
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={onSearchChange}
        searchPlaceholder={searchPlaceholder}
        filters={filters}
        actions={combinedActions}
      />
      <DataTable columns={columns} data={safeData} table={table} />
    </div>
  );
};

export default PaymentTransactionTable;
