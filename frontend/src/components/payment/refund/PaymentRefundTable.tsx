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
import { Eye } from "lucide-react";
import { DataTable } from "@/components/ui/data-table";
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import type { PaymentRefund } from "@/types/payment";
import type { BaseTableProps } from "@/types/components";
import { PAYMENT_TRANSACTION_STATUS_MAPPINGS } from "@/types/payment";
import { getTransactionStatusBadgeProps } from "@/lib/status-utils";

interface PaymentRefundTableProps extends BaseTableProps<PaymentRefund> {
  data: PaymentRefund[];
  selectedRefundId?: string;
  showActions?: boolean;
  onViewRefund: (refund: PaymentRefund) => void;
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

export const PaymentRefundTable: React.FC<PaymentRefundTableProps> = ({
  data = [],
  loading = false,
  searchTerm = "",
  onSearchChange = () => {},
  searchPlaceholder = "Search refunds...",
  filters = [],
  actions,
  showActions = true,
  selectedRefundId,
  onViewRefund,
}) => {
  const [sorting, setSorting] = useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});

  // Ensure data is always an array to prevent undefined errors
  const safeData = data || [];

  // Set up row selection based on selectedRefundId
  const rowSelection = React.useMemo(() => {
    if (selectedRefundId === undefined) return {};
    const selectedIndex = safeData.findIndex(
      (refund) => refund.id === selectedRefundId
    );
    return selectedIndex >= 0 ? { [selectedIndex]: true } : {};
  }, [selectedRefundId, safeData]);

  const columns: ColumnDef<PaymentRefund>[] = React.useMemo(() => {
    const baseColumns: ColumnDef<PaymentRefund>[] = [
      {
        accessorKey: "refundCode",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Refund Code" />
        ),
        cell: ({ row }) => (
          <div className="font-mono text-sm">{row.getValue("refundCode")}</div>
        ),
      },
      {
        accessorKey: "refundAmount",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Amount" />
        ),
        cell: ({ row }) => {
          const refund = row.original;
          return (
            <div className="font-medium">
              {formatCurrency(refund.refundAmount, refund.currency)}
            </div>
          );
        },
      },
      {
        accessorKey: "gatewayName",
        header: "Gateway",
        cell: ({ row }) => (
          <div className="text-sm capitalize">
            {row.original.gatewayName || "-"}
          </div>
        ),
      },
      {
        accessorKey: "refundStatus",
        header: "Status",
        cell: ({ row }) => {
          const status = row.original.refundStatus;
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
          <DataTableColumnHeader column={column} title="Processed At" />
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
        accessorKey: "createdAt",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Created At" />
        ),
        cell: ({ row }) => {
          const createdAt = row.getValue("createdAt") as string;
          return <div className="text-sm">{formatDate(createdAt)}</div>;
        },
      },
    ];

    if (showActions) {
      baseColumns.push({
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const refund = row.original;
          return (
            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => onViewRefund(refund)}
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
  }, [showActions, onViewRefund]);

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
    onRowSelectionChange: () => {},
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
                <Skeleton className="h-4 w-[120px]" />
                <Skeleton className="h-4 w-[100px]" />
                <Skeleton className="h-4 w-[150px]" />
                <Skeleton className="h-4 w-[100px]" />
                <Skeleton className="h-4 w-[80px]" />
                <Skeleton className="h-4 w-[120px]" />
                <Skeleton className="h-4 w-[120px]" />
                <Skeleton className="h-4 w-[100px]" />
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

export default PaymentRefundTable;
