import React from "react";
import { Link } from "react-router-dom";
import { Edit, Trash2, Eye } from "lucide-react";
import {
  type ColumnDef,
  type SortingState,
  type ColumnFiltersState,
  type VisibilityState,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
} from "@tanstack/react-table";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { DataTable } from "@/components/ui/data-table";
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { Skeleton } from "@/components/ui/skeleton";
import { type Module, type BaseTableProps } from "@/types";

interface ModuleTableProps extends BaseTableProps<Module> {
  onDelete: (moduleId: number) => void;
}

export default function ModuleTable({ 
  data = [], 
  loading, 
  searchTerm, 
  onSearchChange, 
  searchPlaceholder = "Search modules...", 
  filters = [], 
  actions,
  onDelete 
}: ModuleTableProps) {
  // Ensure data is always an array to prevent TypeError
  const safeData = data || [];
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = React.useState({});

  const columns: ColumnDef<Module>[] = React.useMemo(
    () => [
      {
        accessorKey: "name",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Module" />
        ),
        cell: ({ row }) => {
          const module = row.original;
          return (
            <div>
              <div className="font-medium">{module.name}</div>
              <div className="text-sm text-muted-foreground">
                {module.description}
              </div>
              {module.code && (
                <div className="text-xs text-muted-foreground mt-1">
                  Code: {module.code}
                </div>
              )}
            </div>
          );
        },
      },
      {
        accessorKey: "productName",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Product" />
        ),
        cell: ({ row }) => (
          <div className="text-sm">{row.getValue("productName")}</div>
        ),
      },
      {
        accessorKey: "moduleStatus",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Status" />
        ),
        cell: ({ row }) => (
          <StatusBadge status={row.getValue("moduleStatus")} />
        ),
      },
      {
        accessorKey: "updatedAt",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Last Updated" />
        ),
        cell: ({ row }) => {
          const updatedAt = row.getValue("updatedAt") as string;
          return (
            <div className="text-sm text-muted-foreground">
              {updatedAt ? new Date(updatedAt).toLocaleDateString() : "N/A"}
            </div>
          );
        },
      },
      {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const module = row.original;
          return (
            <div className="flex justify-end space-x-2">
              <Button
                variant="ghost"
                size="sm"
                asChild
                className="text-blue-600 hover:text-blue-700"
              >
                <Link to={`/modules/${module.id}`}>
                  <Eye className="h-4 w-4" />
                </Link>
              </Button>
              <PermissionGuard permission="MODULE_MGMT:update">
                <Button
                  variant="ghost"
                  size="sm"
                  asChild
                  className="text-yellow-600 hover:text-yellow-700"
                >
                  <Link to={`/modules/${module.id}/edit`}>
                    <Edit className="h-4 w-4" />
                  </Link>
                </Button>
              </PermissionGuard>
              <PermissionGuard permission="MODULE_MGMT:delete">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => onDelete(module.id)}
                  className="text-red-600 hover:text-red-700"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </PermissionGuard>
            </div>
          );
        },
      },
    ],
    [onDelete]
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

  const combinedActions = React.useMemo(() => (
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
        <DataTable columns={columns} data={safeData} table={table} />
      )}
    </div>
  );
}
