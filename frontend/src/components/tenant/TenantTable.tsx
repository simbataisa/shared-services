import React, { useMemo } from "react";
import { Link } from "react-router-dom";
import { Building2, Eye } from "lucide-react";
import {
  type ColumnDef,
  getCoreRowModel,
  useReactTable,
  getPaginationRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  type ColumnFiltersState,
  type SortingState,
  type VisibilityState,
} from "@tanstack/react-table";
import { Button } from "@/components/ui/button";
import { DataTable } from "@/components/ui/data-table";
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { Skeleton } from "@/components/ui/skeleton";
import { getTenantTypeLabel } from "@/lib/status-utils";
import { type Tenant } from "@/types/entities";
import { type BaseTableProps } from "@/types/components";

interface TenantTableProps extends BaseTableProps<Tenant> {
  // Additional tenant-specific props if needed
}

export default function TenantTable({
  data,
  loading,
  searchTerm,
  onSearchChange,
  searchPlaceholder = "Search tenants...",
  filters = [],
  actions,
}: TenantTableProps) {
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = React.useState({});

  const columns: ColumnDef<Tenant>[] = useMemo(() => [
    {
      accessorKey: "name",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Tenant" />
      ),
      cell: ({ row }) => {
        const tenant = row.original;
        return (
          <div className="flex items-center gap-3">
            <div className="bg-primary/10 p-2 rounded-full">
              <Building2 className="h-4 w-4 text-primary" />
            </div>
            <div>
              <div className="font-medium">{tenant.name}</div>
              <div className="text-sm text-muted-foreground font-mono">
                {tenant.code}
              </div>
            </div>
          </div>
        );
      },
    },
    {
      accessorKey: "type",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Type" />
      ),
      cell: ({ row }) => (
        <span className="text-sm">{getTenantTypeLabel(row.getValue("type"))}</span>
      ),
    },
    {
      accessorKey: "status",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Status" />
      ),
      cell: ({ row }) => (
        <div className="flex items-center gap-2">
          <StatusBadge status={row.getValue("status")} showIcon={true} />
        </div>
      ),
    },
    {
      id: "actions",
      header: () => <div className="text-right">Actions</div>,
      cell: ({ row }) => {
        const tenant = row.original;
        return (
          <div className="flex items-center justify-end gap-2">
            <PermissionGuard permission="TENANT_MGMT:read">
              <Button
                variant="ghost"
                size="icon"
                asChild
                className="text-blue-600 hover:text-blue-700"
              >
                <Link to={`/tenants/${tenant.id}`} title="View Details">
                  <Eye className="h-4 w-4" />
                </Link>
              </Button>
            </PermissionGuard>
          </div>
        );
      },
    },
  ], []);

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

  const combinedActions = useMemo(() => (
    <div className="flex gap-2 items-center">
      <DataTableViewOptions table={table} />
      {actions}
    </div>
  ), [table, actions]);

  return (
    <div className="w-full space-y-4">
      <SearchAndFilter
        searchTerm={searchTerm || ""}
        onSearchChange={onSearchChange || (() => {})}
        searchPlaceholder={searchPlaceholder}
        filters={filters}
        actions={combinedActions}
      />
      {loading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : (
        <DataTable columns={columns} data={data} table={table} />
      )}
    </div>
  );
}
