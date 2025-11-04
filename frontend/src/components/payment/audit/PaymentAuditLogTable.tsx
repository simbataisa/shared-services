import React, { useMemo, useState } from "react";
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
import type { PaymentAuditLog } from "@/types/payment";
import type { BaseTableProps, TableFilter } from "@/types/components";

interface PaymentAuditLogTableProps extends BaseTableProps<PaymentAuditLog> {
  data: PaymentAuditLog[];
  showActions?: boolean;
  onViewLog: (log: PaymentAuditLog) => void;
  filters?: TableFilter[];
}

// Utility functions
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  })}`;
};

const getActionBadgeProps = (action: string) => {
  const a = action?.toUpperCase() || "";
  switch (a) {
    case "CREATE":
      return {
        variant: "outline" as const,
        className: "bg-green-50 text-green-700 border-green-200",
      };
    case "UPDATE":
      return {
        variant: "outline" as const,
        className: "bg-blue-50 text-blue-700 border-blue-200",
      };
    case "DELETE":
      return {
        variant: "outline" as const,
        className: "bg-red-50 text-red-700 border-red-200",
      };
    case "APPROVE":
      return {
        variant: "outline" as const,
        className: "bg-emerald-50 text-emerald-700 border-emerald-200",
      };
    case "REJECT":
      return {
        variant: "outline" as const,
        className: "bg-orange-50 text-orange-700 border-orange-200",
      };
    case "CANCEL":
      return {
        variant: "outline" as const,
        className: "bg-gray-50 text-gray-700 border-gray-200",
      };
    case "PROCESS":
      return {
        variant: "outline" as const,
        className: "bg-purple-50 text-purple-700 border-purple-200",
      };
    case "REFUND":
      return {
        variant: "outline" as const,
        className: "bg-yellow-50 text-yellow-700 border-yellow-200",
      };
    case "RETRY":
      return {
        variant: "outline" as const,
        className: "bg-indigo-50 text-indigo-700 border-indigo-200",
      };
    default:
      return {
        variant: "outline" as const,
        className: "bg-slate-50 text-slate-700 border-slate-200",
      };
  }
};

export const PaymentAuditLogTable: React.FC<PaymentAuditLogTableProps> = ({
  data,
  loading = false,
  searchTerm = "",
  onSearchChange = () => {},
  searchPlaceholder = "Search by action, user, IP...",
  filters = [],
  actions,
  showActions = true,
  onViewLog,
}) => {
  const safeData = data || [];

  const [sorting, setSorting] = useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});
  const [rowSelection, _setRowSelection] = useState({});

  const columns: ColumnDef<PaymentAuditLog>[] = useMemo(() => {
    const baseColumns: ColumnDef<PaymentAuditLog>[] = [
      {
        accessorKey: "id",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="ID" />
        ),
        cell: ({ row }) => (
          <span className="font-mono text-xs">{row.getValue("id")}</span>
        ),
      },
      {
        accessorKey: "action",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Action" />
        ),
        cell: ({ row }) => {
          const action = row.getValue("action") as string;
          const badgeProps = getActionBadgeProps(action);
          return (
            <Badge
              variant={badgeProps.variant}
              className={badgeProps.className}
            >
              {action}
            </Badge>
          );
        },
      },
      {
        accessorKey: "username",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Username" />
        ),
        cell: ({ row }) => (
          <div className="text-sm">{row.original.username || "System"}</div>
        ),
      },
      {
        accessorKey: "ipAddress",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="IP Address" />
        ),
        cell: ({ row }) => (
          <div className="text-sm">{row.original.ipAddress || "-"}</div>
        ),
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
      {
        accessorKey: "paymentRequestId",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Request ID" />
        ),
        cell: ({ row }) => (
          <div className="text-xs font-mono">
            {row.original.paymentRequestId || "-"}
          </div>
        ),
      },
      {
        accessorKey: "paymentTransactionId",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Transaction ID" />
        ),
        cell: ({ row }) => (
          <div className="text-xs font-mono">
            {row.original.paymentTransactionId || "-"}
          </div>
        ),
      },
      {
        accessorKey: "paymentRefundId",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Refund ID" />
        ),
        cell: ({ row }) => (
          <div className="text-xs font-mono">
            {row.original.paymentRefundId || "-"}
          </div>
        ),
      },
    ];

    if (showActions) {
      baseColumns.push({
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const log = row.original;
          return (
            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => onViewLog(log)}
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
  }, [showActions, onViewLog]);

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

  const combinedActions = useMemo(
    () => (
      <div className="flex gap-2 items-center">
        <DataTableViewOptions table={table} />
        {actions}
      </div>
    ),
    [table, actions]
  );

  // Provide a guaranteed function to SearchAndFilter
  const handleSearchChange = (term: string) => {
    onSearchChange?.(term);
  };

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
        onSearchChange={handleSearchChange}
        searchPlaceholder={searchPlaceholder}
        filters={filters}
        actions={combinedActions}
      />
      <DataTable columns={columns} data={safeData} table={table} />
    </div>
  );
};

export default PaymentAuditLogTable;
