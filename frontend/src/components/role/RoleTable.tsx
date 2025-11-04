import React from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { DataTable } from "@/components/ui/data-table";
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { StatusBadge } from "@/components/common/StatusBadge";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { normalizeEntityStatus } from "@/lib/status-utils";
import { Eye } from "lucide-react";
import {
  useReactTable,
  getCoreRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  getFilteredRowModel,
} from "@tanstack/react-table";
import type {
  ColumnDef,
  SortingState,
  ColumnFiltersState,
  VisibilityState,
} from "@tanstack/react-table";
import type { Role, Permission } from "@/types/entities";
import { type BaseTableProps, type TableFilter } from "@/types/components";

interface RoleTableProps extends BaseTableProps<Role> {
  selectedRoleId?: number;
  showActions?: boolean;
  canManageRoles: boolean;
  onViewRole: (role: Role) => void;
}

const RoleTable: React.FC<RoleTableProps> = ({
  data = [],
  selectedRoleId,
  showActions = true,
  canManageRoles,
  onViewRole,
  searchTerm = "",
  onSearchChange,
  searchPlaceholder = "Search roles...",
  filters = [],
  actions,
}) => {
  // Ensure roles is always an array to prevent TypeError
  const safeRoles = data || [];
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
    []
  );
  const [columnVisibility, setColumnVisibility] =
    React.useState<VisibilityState>({});

  // Set up row selection based on selectedRoleId
  const rowSelection = React.useMemo(() => {
    if (selectedRoleId === undefined) return {};
    const selectedIndex = safeRoles.findIndex(
      (role) => role.id === selectedRoleId
    );
    return selectedIndex >= 0 ? { [selectedIndex]: true } : {};
  }, [selectedRoleId, safeRoles]);

  const columns: ColumnDef<Role>[] = React.useMemo(() => {
    const baseColumns: ColumnDef<Role>[] = [
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
        accessorKey: "description",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Description" />
        ),
        cell: ({ row }) => <div>{row.getValue("description")}</div>,
      },
      {
        accessorKey: "status",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="Status" />
        ),
        cell: ({ row }) => (
          <StatusBadge
            status={normalizeEntityStatus(
              "role",
              row.getValue("status") || "ACTIVE"
            )}
          />
        ),
      },
      {
        accessorKey: "permissions",
        header: "Permissions",
        cell: ({ row }) => {
          const permissions = row.getValue("permissions") as Permission[];
          return (
            <div className="flex flex-wrap gap-1">
              {permissions?.slice(0, 3).map((permission: Permission) => (
                <Badge
                  key={permission.id}
                  variant="outline"
                  className="text-xs"
                >
                  {permission.name}
                </Badge>
              ))}
              {permissions && permissions.length > 3 && (
                <Badge variant="outline" className="text-xs">
                  +{permissions.length - 3} more
                </Badge>
              )}
            </div>
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
          return createdAt ? new Date(createdAt).toLocaleDateString() : "";
        },
      },
    ];

    if (showActions) {
      baseColumns.push({
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const role = row.original;
          return canManageRoles ? (
            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => onViewRole(role)}
                className="text-blue-600 hover:text-blue-700"
              >
                <Eye className="h-4 w-4" />
              </Button>
            </div>
          ) : null;
        },
      });
    }

    return baseColumns;
  }, [showActions, canManageRoles, onViewRole]);

  const table = useReactTable({
    data: safeRoles,
    columns,
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onColumnVisibilityChange: setColumnVisibility,
    onRowSelectionChange: () => {}, // Disable row selection changes since we control it
    state: {
      sorting,
      columnFilters,
      columnVisibility,
      rowSelection,
    },
  });

  // Combine actions with DataTableViewOptions for proper alignment
  const combinedActions = (
    <div className="flex gap-2 items-center">
      <DataTableViewOptions table={table} />
      {actions}
    </div>
  );

  return (
    <div className="w-full space-y-4">
      {(onSearchChange || filters.length > 0 || actions) && (
        <SearchAndFilter
          searchTerm={searchTerm}
          onSearchChange={onSearchChange || (() => {})}
          searchPlaceholder={searchPlaceholder}
          filters={filters}
          actions={combinedActions}
        />
      )}
      <DataTable columns={columns} data={safeRoles} table={table} />
    </div>
  );
};

export default RoleTable;
