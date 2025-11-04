import React, { useMemo } from "react";
import { Link } from "react-router-dom";
import type { ColumnDef } from "@tanstack/react-table";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { DataTable } from "@/components/ui/data-table";
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { Skeleton } from "@/components/ui/skeleton";
import type { ProductWithModules } from "@/types";
import type { BaseTableProps } from "@/types/components";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Edit, Trash2, Eye } from "lucide-react";
import { StatusBadge } from "@/components/common/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-utils";
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

interface ProductTableProps extends BaseTableProps<ProductWithModules> {
  onDeleteProduct: (productId: string) => void;
}

export function ProductTable({
  data = [],
  loading = false,
  searchTerm = "",
  onSearchChange = () => {},
  searchPlaceholder = "Search products...",
  filters = [],
  actions,
  onDeleteProduct,
}: ProductTableProps) {
  // Ensure data is always an array to prevent TypeError
  const safeData = data || [];
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = React.useState({});

  const columns: ColumnDef<ProductWithModules>[] = useMemo(
    () => [
      {
        accessorKey: "name",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Name" />
        ),
        cell: ({ row }) => (
          <div className="font-medium">{row.getValue("name")}</div>
        ),
      },
      {
        accessorKey: "code",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Code" />
        ),
        cell: ({ row }) => (
          <div className="font-mono text-sm">{row.getValue("code")}</div>
        ),
      },
      {
        accessorKey: "version",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Version" />
        ),
        cell: ({ row }) => <div>{row.getValue("version")}</div>,
      },
      {
        accessorKey: "productStatus",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Status" />
        ),
        cell: ({ row }) => (
          <StatusBadge
            status={normalizeEntityStatus("product", row.getValue("productStatus"))}
          />
        ),
      },
      {
        accessorKey: "description",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Description" />
        ),
        cell: ({ row }) => (
          <div className="max-w-xs truncate">
            {row.getValue("description") || "-"}
          </div>
        ),
      },
      {
        accessorKey: "modules",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Modules" />
        ),
        cell: ({ row }) => {
          const modules = row.getValue("modules") as any[];
          return (
            <Badge variant="outline">
              {modules?.length || 0}
            </Badge>
          );
        },
      },
      {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const product = row.original;
          return (
            <div className="flex items-center gap-1">
              <Button
                variant="ghost"
                size="icon"
                asChild
                className="text-blue-600 hover:text-blue-700"
              >
                <Link to={`/products/${product.id}`} title="View Details">
                  <Eye className="h-4 w-4" />
                </Link>
              </Button>
              <PermissionGuard permission="PRODUCT_MGMT:update">
                <Button
                  variant="ghost"
                  size="icon"
                  asChild
                  className="text-yellow-600 hover:text-yellow-700"
                >
                  <Link to={`/products/${product.id}/edit`} title="Edit Product">
                    <Edit className="h-4 w-4" />
                  </Link>
                </Button>
              </PermissionGuard>
              <PermissionGuard permission="PRODUCT_MGMT:delete">
                <Button
                  variant="ghost"
                  size="icon"
                  className="text-red-600 hover:text-red-700"
                  onClick={() => onDeleteProduct(product.id.toString())}
                  title="Delete Product"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </PermissionGuard>
            </div>
          );
        },
      },
    ],
    [onDeleteProduct]
  );

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

  const combinedActions = useMemo(
    () => (
      <div className="flex gap-2 items-center">
        <DataTableViewOptions table={table} />
        {actions}
      </div>
    ),
    [table, actions]
  );

  return (
    <div className="w-full space-y-4">
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={onSearchChange}
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
        <DataTable columns={columns} data={safeData} table={table} />
      )}
    </div>
  );
}