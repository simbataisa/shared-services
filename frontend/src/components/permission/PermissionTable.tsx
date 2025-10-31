"use client"

import * as React from "react"
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
} from "@tanstack/react-table"
import { Eye } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { DataTable } from "@/components/ui/data-table"
import { DataTableColumnHeader } from "@/components/ui/data-table-column-header"
import { DataTableViewOptions } from "@/components/ui/data-table-view-options"
import { SearchAndFilter } from "@/components/common/SearchAndFilter"
import { type Permission } from "@/types/entities"
import { type BaseTableProps, type TableFilter } from "@/types/components"

interface PermissionTableProps extends Omit<BaseTableProps<Permission>, 'data'> {
  permissions: Permission[]
  onViewPermission?: (permission: Permission) => void
}

// Helper function to parse permission name
const parsePermissionName = (name: string): { resource: string; action: string } => {
  const parts = name.split(':')
  if (parts.length >= 2) {
    return {
      resource: parts[0],
      action: parts[1]
    }
  }
  return {
    resource: 'unknown',
    action: 'unknown'
  }
}

// Helper function to get badge variant for actions
const getActionBadgeVariant = (action: string): "default" | "secondary" | "destructive" | "outline" => {
  switch (action.toLowerCase()) {
    case 'create':
      return 'default'
    case 'read':
      return 'secondary'
    case 'update':
      return 'outline'
    case 'delete':
      return 'destructive'
    default:
      return 'secondary'
  }
}

export function PermissionTable({ 
  permissions = [],
  onViewPermission,
  searchTerm = "",
  onSearchChange,
  searchPlaceholder = "Search permissions...",
  filters = [],
  actions
}: PermissionTableProps) {
  // Ensure permissions is always an array to prevent TypeError
  const safePermissions = permissions || [];
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([])
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})
  const [rowSelection, setRowSelection] = React.useState({})

  const columns: ColumnDef<Permission>[] = [
    {
      accessorKey: "name",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Name" />
      ),
      cell: ({ row }) => {
        const permission = row.original
        return (
          <div className="font-medium">
            {permission.name}
          </div>
        )
      },
    },
    {
      accessorKey: "description",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Description" />
      ),
      cell: ({ row }) => {
        const permission = row.original
        return (
          <div className="text-sm text-muted-foreground">
            {permission.description || '-'}
          </div>
        )
      },
    },
    {
      accessorKey: "resource",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Resource" />
      ),
      cell: ({ row }) => {
        const permission = row.original
        const { resource } = parsePermissionName(permission.name)
        return (
          <Badge variant="outline">
            {permission.resource || resource}
          </Badge>
        )
      },
    },
    {
      accessorKey: "action",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Action" />
      ),
      cell: ({ row }) => {
        const permission = row.original
        const { action } = parsePermissionName(permission.name)
        const actionValue = permission.action || action
        return (
          <Badge variant={getActionBadgeVariant(actionValue)}>
            {actionValue}
          </Badge>
        )
      },
    },
    {
      accessorKey: "createdAt",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Created" />
      ),
      cell: ({ row }) => {
        const permission = row.original
        return (
          <div className="text-sm text-muted-foreground">
            {permission.createdAt ? new Date(permission.createdAt).toLocaleDateString() : '-'}
          </div>
        )
      },
    },
    {
      id: "actions",
      enableHiding: false,
      cell: ({ row }) => {
        const permission = row.original
        return (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onViewPermission?.(permission)}
          >
            <Eye className="h-4 w-4" />
          </Button>
        )
      },
    },
  ]

  const table = useReactTable({
    data: safePermissions,
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
  })

  // Combine actions with DataTableViewOptions for proper alignment
  const combinedActions = (
    <div className="flex gap-2 items-center">
      <DataTableViewOptions table={table} />
      {actions}
    </div>
  )

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
      <DataTable
        columns={columns}
        data={safePermissions}
        table={table}
      />
    </div>
  )
}