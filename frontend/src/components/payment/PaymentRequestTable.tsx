import React, { useMemo } from "react";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  CheckCircle,
  XCircle,
  Clock,
  AlertCircle,
  MoreHorizontal,
  Eye,
  Check,
  X,
  Ban,
  Edit,
} from "lucide-react";
import {
  useReactTable,
  getCoreRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  type ColumnDef,
  type SortingState,
  type ColumnFiltersState,
  type VisibilityState,
} from "@tanstack/react-table";
import { DataTable } from "@/components/ui/data-table";
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import {
  getStatusIcon,
  getPaymentRequestStatusColor,
} from "@/lib/status-utils";
import { formatCurrency, formatDate } from "@/lib/utils";
import type { PaymentRequest, PaymentRequestStatus } from "@/types/payment";
import type { BaseTableProps } from "@/types/components";
import {
  PAYMENT_REQUEST_STATUS_MAPPINGS,
  PAYMENT_METHOD_TYPE_MAPPINGS,
} from "@/types/payment";

interface PaymentRequestTableProps extends BaseTableProps<PaymentRequest> {
  onApproveRequest: (id: string) => void;
  onRejectRequest: (id: string) => void;
  onCancelRequest: (id: string) => void;
}

export function PaymentRequestTable({
  data,
  loading = false,
  searchTerm = "",
  onSearchChange = () => {},
  searchPlaceholder = "Search payment requests...",
  filters = [],
  actions,
  onApproveRequest,
  onRejectRequest,
  onCancelRequest,
}: PaymentRequestTableProps) {
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
    []
  );
  const [columnVisibility, setColumnVisibility] =
    React.useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = React.useState({});

  const columns: ColumnDef<PaymentRequest>[] = useMemo(
    () => [
      {
        accessorKey: "requestCode",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Request Code" />
        ),
        cell: ({ row }) => {
          const request = row.original;
          return (
            <Link
              to={`/payments/requests/${request.id}`}
              className="font-medium text-blue-600 hover:text-blue-800 hover:underline"
            >
              {request.requestCode}
            </Link>
          );
        },
      },
      {
        accessorKey: "payerName",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Payer" />
        ),
        cell: ({ row }) => {
          const request = row.original;
          return (
            <div>
              <div className="font-medium">{request.payerName}</div>
              <div className="text-sm text-muted-foreground">
                {request.payerEmail}
              </div>
            </div>
          );
        },
      },
      {
        accessorKey: "amount",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Amount" />
        ),
        cell: ({ row }) => {
          const request = row.original;
          return (
            <div className="font-medium">
              {formatCurrency(request.amount, request.currency)}
            </div>
          );
        },
      },
      {
        accessorKey: "preSelectedPaymentMethod",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Payment Method" />
        ),
        cell: ({ row }) => {
          const method = row.getValue("preSelectedPaymentMethod") as string;
          return (
            <Badge variant="outline">
              {PAYMENT_METHOD_TYPE_MAPPINGS[
                method as keyof typeof PAYMENT_METHOD_TYPE_MAPPINGS
              ] || method}
            </Badge>
          );
        },
      },
      {
        accessorKey: "status",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Status" />
        ),
        cell: ({ row }) => {
          const status = row.getValue("status") as PaymentRequestStatus;
          return (
            <div className="flex items-center gap-2">
              {getStatusIcon(status)}
              <Badge className={getPaymentRequestStatusColor(status)}>
                {PAYMENT_REQUEST_STATUS_MAPPINGS[status]}
              </Badge>
            </div>
          );
        },
      },
      {
        accessorKey: "expiresAt",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Expires At" />
        ),
        cell: ({ row }) => {
          const expiresAt = row.getValue("expiresAt") as string;
          return expiresAt ? (
            <div className="text-sm">{formatDate(expiresAt.toString())}</div>
          ) : (
            <span className="text-muted-foreground">No expiry</span>
          );
        },
      },
      {
        accessorKey: "createdAt",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Created" />
        ),
        cell: ({ row }) => {
          const createdAt = row.getValue("createdAt") as string;
          return (
            <div className="text-sm">{formatDate(createdAt.toString())}</div>
          );
        },
      },
      {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const request = row.original;

          return (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-8 w-8 p-0">
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem asChild>
                  <Link to={`/payments/requests/${request.id}`}>
                    <Eye className="mr-2 h-4 w-4" />
                    View Details
                  </Link>
                </DropdownMenuItem>

                {request.status === "PENDING" && (
                  <>
                    <PermissionGuard permission="PAYMENT_MGMT:verify">
                      <DropdownMenuItem
                        onClick={() => onApproveRequest(request.id.toString())}
                      >
                        <Check className="mr-2 h-4 w-4" />
                        Approve
                      </DropdownMenuItem>
                    </PermissionGuard>
                    <PermissionGuard permission="PAYMENT_MGMT:verify">
                      <DropdownMenuItem
                        onClick={() => onRejectRequest(request.id.toString())}
                      >
                        <X className="mr-2 h-4 w-4" />
                        Reject
                      </DropdownMenuItem>
                    </PermissionGuard>
                  </>
                )}

                {(request.status === "PENDING" ||
                  request.status === "APPROVED") && (
                  <PermissionGuard permission="PAYMENT_MGMT:void">
                    <DropdownMenuItem
                      onClick={() => onCancelRequest(request.id.toString())}
                    >
                      <Ban className="mr-2 h-4 w-4" />
                      Cancel
                    </DropdownMenuItem>
                  </PermissionGuard>
                )}

                <PermissionGuard permission="PAYMENT_MGMT:update">
                  <DropdownMenuItem asChild>
                    <Link to={`/payments/requests/${request.id}/edit`}>
                      <Edit className="mr-2 h-4 w-4" />
                      Edit
                    </Link>
                  </DropdownMenuItem>
                </PermissionGuard>
              </DropdownMenuContent>
            </DropdownMenu>
          );
        },
      },
    ],
    [onApproveRequest, onRejectRequest, onCancelRequest]
  );

  const table = useReactTable({
    data,
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
      <DataTable columns={columns} data={data} table={table} />
    </div>
  );
}
