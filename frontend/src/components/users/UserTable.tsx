import React, { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
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
import { Badge } from "@/components/ui/badge";
import { DataTable } from "@/components/ui/data-table";
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { StatusBadge } from "@/components/common/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-utils";
import SearchAndFilter from "@/components/common/SearchAndFilter";
import { Eye } from "lucide-react";
import type { User } from "@/types";
import { type BaseTableProps } from "@/types/components";

interface UserTableProps extends Omit<BaseTableProps<User>, "data"> {
  users: User[];
  loading: boolean;
}

/**
 * UserTable Component
 *
 * Displays a table of users with their details including username, email, name,
 * status, roles, user groups, and action buttons using the DataTable system.
 * Includes integrated search and filtering functionality.
 */
export const UserTable: React.FC<UserTableProps> = ({
  users,
  loading,
  searchTerm: externalSearchTerm,
  onSearchChange,
  searchPlaceholder = "Search users...",
  filters = [],
  actions,
}) => {
  const navigate = useNavigate();
  const [internalSearchTerm, setInternalSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [sorting, setSorting] = useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});

  // Use external search term if provided, otherwise use internal state
  const searchTerm =
    externalSearchTerm !== undefined ? externalSearchTerm : internalSearchTerm;
  const handleSearchChange = onSearchChange || setInternalSearchTerm;
  const [rowSelection, setRowSelection] = useState({});

  // Filter users based on search term and status filter
  const filteredUsers = useMemo(() => {
    return users.filter((user) => {
      const matchesSearch =
        user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.lastName.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesStatus =
        statusFilter === "all" || user.userStatus === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [users, searchTerm, statusFilter]);

  const columns: ColumnDef<User>[] = [
    {
      accessorKey: "username",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Username" />
      ),
      cell: ({ row }) => {
        const user = row.original;
        return <div className="font-medium">{user.username}</div>;
      },
    },
    {
      accessorKey: "email",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Email" />
      ),
      cell: ({ row }) => {
        const user = row.original;
        return (
          <div className="text-sm text-muted-foreground">{user.email}</div>
        );
      },
    },
    {
      accessorKey: "name",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Name" />
      ),
      cell: ({ row }) => {
        const user = row.original;
        return (
          <div className="text-sm">
            {user.firstName} {user.lastName}
          </div>
        );
      },
    },
    {
      accessorKey: "userStatus",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Status" />
      ),
      cell: ({ row }) => {
        const user = row.original;
        const normalizedStatus = normalizeEntityStatus("user", user.userStatus);
        return <StatusBadge status={normalizedStatus} />;
      },
    },
    {
      accessorKey: "roles",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Roles" />
      ),
      cell: ({ row }) => {
        const user = row.original;
        return (
          <div className="flex flex-wrap gap-1">
            {user.roles?.map((role) => (
              <Badge key={role.id} variant="secondary" className="text-xs">
                {role.name}
              </Badge>
            )) || <span className="text-muted-foreground">No roles</span>}
          </div>
        );
      },
    },
    {
      accessorKey: "userGroups",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Groups" />
      ),
      cell: ({ row }) => {
        const user = row.original;
        return (
          <div className="flex flex-wrap gap-1">
            {user.userGroups?.map((group) => (
              <Badge key={group.id} variant="outline" className="text-xs">
                {group.name}
              </Badge>
            )) || <span className="text-muted-foreground">No groups</span>}
          </div>
        );
      },
    },
    {
      id: "actions",
      enableHiding: false,
      cell: ({ row }) => {
        const user = row.original;
        return (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(`/users/${user.id}`)}
          >
            <Eye className="h-4 w-4" />
          </Button>
        );
      },
    },
  ];

  const table = useReactTable({
    data: filteredUsers,
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

  return (
    <div className="w-full space-y-4">
      {(onSearchChange || filters.length > 0 || actions) && (
        <SearchAndFilter
          searchTerm={searchTerm}
          onSearchChange={handleSearchChange}
          searchPlaceholder={searchPlaceholder}
          filters={filters}
          actions={combinedActions}
        />
      )}
      <DataTable table={table} columns={columns} data={filteredUsers} />
    </div>
  );
};

export default UserTable;
