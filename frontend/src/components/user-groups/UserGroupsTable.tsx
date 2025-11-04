import React, { useMemo } from "react";
import { Link } from "react-router-dom";
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
import { Skeleton } from "@/components/ui/skeleton";
import { DataTable } from "@/components/ui/data-table";
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header";
import { DataTableViewOptions } from "@/components/ui/data-table-view-options";
import { StatusBadge } from "@/components/common/StatusBadge";
import SearchAndFilter from "@/components/common/SearchAndFilter";
import { Users, Eye } from "lucide-react";
import type { UserGroup } from "@/types";
import { type BaseTableProps } from "@/types/components";

interface UserGroupsTableProps extends Omit<BaseTableProps<UserGroup>, "data"> {
  userGroups: UserGroup[];
  onDeleteGroup?: (groupId: number) => void;
}

const UserGroupsTable: React.FC<UserGroupsTableProps> = ({
  userGroups,
  loading = false,
  searchTerm = "",
  onSearchChange,
  searchPlaceholder = "Search user groups...",
  filters = [],
  actions,
  onDeleteGroup,
}) => {
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = React.useState({});

  // Filter user groups based on search term and other filters
  const filteredUserGroups = useMemo(() => {
    return userGroups.filter((group) => {
      const matchesSearch =
        group.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (group.description?.toLowerCase() || "").includes(
          searchTerm.toLowerCase()
        );

      return matchesSearch;
    });
  }, [userGroups, searchTerm]);

  const columns: ColumnDef<UserGroup>[] = useMemo(() => [
    {
      accessorKey: "name",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Name" />
      ),
      cell: ({ row }) => (
        <div className="flex items-center gap-2 font-medium">
          <Users className="h-4 w-4 text-muted-foreground" />
          {row.getValue("name")}
        </div>
      ),
    },
    {
      accessorKey: "description",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Description" />
      ),
      cell: ({ row }) => {
        const description = row.getValue("description") as string;
        return (
          <div className="max-w-xs truncate">
            {description || (
              <span className="text-muted-foreground italic">
                No description provided
              </span>
            )}
          </div>
        );
      },
    },
    {
      accessorKey: "memberCount",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Members" />
      ),
      cell: ({ row }) => {
        const memberCount = row.getValue("memberCount") as number;
        return (
          <Badge variant="secondary">
            {memberCount} {memberCount === 1 ? "member" : "members"}
          </Badge>
        );
      },
    },
    {
      accessorKey: "roleCount",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Roles" />
      ),
      cell: ({ row }) => {
        const roleCount = row.getValue("roleCount") as number;
        return <Badge variant="secondary">{roleCount} roles</Badge>;
      },
    },
    {
      accessorKey: "userGroupStatus",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Status" />
      ),
      cell: ({ row }) => (
        <StatusBadge 
          status={row.getValue("userGroupStatus")} 
          showIcon={true} 
        />
      ),
    },
    {
      id: "actions",
      header: "Actions",
      cell: ({ row }) => {
        const group = row.original;
        return (
          <div className="flex justify-end gap-1">
            <Button
              variant="ghost"
              size="sm"
              asChild
              className="text-blue-600 hover:text-blue-700"
            >
              <Link to={`/user-groups/${group.userGroupId}`}>
                <Eye className="h-4 w-4" />
              </Link>
            </Button>
          </div>
        );
      },
    },
  ], []);

  const table = useReactTable({
    data: filteredUserGroups,
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

  if (filteredUserGroups.length === 0 && !loading && userGroups.length === 0) {
    return (
      <div className="text-center py-12">
        <Users className="mx-auto h-12 w-12 text-muted-foreground" />
        <h3 className="mt-4 text-lg font-semibold">No groups found</h3>
        <p className="text-muted-foreground">
          Get started by creating your first user group.
        </p>
      </div>
    );
  }

  return (
    <div className="w-full space-y-4">
      <SearchAndFilter
        searchTerm={searchTerm}
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
        <DataTable columns={columns} data={filteredUserGroups} table={table} />
      )}
    </div>
  );
};

export default UserGroupsTable;
