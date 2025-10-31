# Data Table Components Documentation

## Overview

The data table system is a comprehensive set of React components built on top of TanStack Table (formerly React Table) that provides a powerful, flexible, and reusable table solution for the frontend application. The system consists of four main components that work together to create feature-rich data tables with sorting, filtering, pagination, and column visibility controls.

## Architecture

The data table system follows a modular architecture where each component has a specific responsibility:

- **DataTable**: Main container component that orchestrates the table functionality
- **DataTableColumnHeader**: Provides sortable column headers with dropdown controls
- **DataTablePagination**: Handles pagination controls and row selection information
- **DataTableViewOptions**: Manages column visibility with a dropdown interface

## TypeScript Interfaces

### BaseTableProps Interface

The system includes a standardized `BaseTableProps` interface defined in `/src/types/components.ts` that provides a consistent foundation for all table components:

```typescript
// Common table interfaces
export interface TableFilterOption {
  value: string;
  label: string;
}

export interface TableFilter {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: TableFilterOption[];
  placeholder?: string;
  width?: string;
}

// Base table props interface that can be extended by specific table components
export interface BaseTableProps<T = any> {
  data: T[];
  loading?: boolean;
  searchTerm?: string;
  onSearchChange?: (value: string) => void;
  searchPlaceholder?: string;
  filters?: TableFilter[];
  actions?: React.ReactNode;
}
```

#### Usage in Table Components

Table components should extend `BaseTableProps` to ensure consistency:

```typescript
interface UserTableProps extends BaseTableProps<User> {
  // Additional user-specific props
  onUserSelect?: (user: User) => void;
  selectedUsers?: User[];
}

interface RoleTableProps extends BaseTableProps<Role> {
  // Additional role-specific props
  onRoleEdit?: (role: Role) => void;
  canManageRoles?: boolean;
}
```

#### Benefits of BaseTableProps

1. **Consistency**: Ensures all table components have the same basic interface
2. **Type Safety**: Provides proper TypeScript typing for common table props
3. **Maintainability**: Centralizes common prop definitions
4. **Extensibility**: Easy to extend for specific table requirements
5. **Documentation**: Self-documenting interface for developers

## Components

### 1. DataTable (`data-table.tsx`)

The main component that renders the complete table with all features. It supports both standalone usage with internal state management and integration with external table instances for advanced scenarios.

#### Props Interface

```typescript
interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[]     // Column definitions from TanStack Table
  data: TData[]                           // Array of data objects
  searchKey?: string                      // Column key for search functionality (only used with internal table)
  searchPlaceholder?: string              // Placeholder text for search input (default: "Search...")
  table?: any                            // External table instance (optional)
}
```

#### Key Features

- **Dual Mode Operation**: Can work with internal table state or accept external table instance
- **Conditional Search Controls**: Only renders SearchAndFilter when using internal table state
- **Built-in Search**: Optional search functionality when `searchKey` is provided
- **State Management**: Manages sorting, filtering, column visibility, and row selection internally
- **Responsive Design**: Adapts to different screen sizes
- **Integration Ready**: Works seamlessly with SearchAndFilter component and DataTableViewOptions

#### Internal State (when not using external table)

```typescript
const [sorting, setSorting] = React.useState<SortingState>([])
const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([])
const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})
const [rowSelection, setRowSelection] = React.useState({})
```

#### Rendering Logic

The component conditionally renders search controls based on whether an external table is provided:

```typescript
// Only render search controls if no external table is provided
{!externalTable && (
  <div className="flex items-center py-4">
    {searchKey && (
      <SearchAndFilter
        searchTerm={(table.getColumn(searchKey)?.getFilterValue() as string) ?? ""}
        onSearchChange={(value) => table.getColumn(searchKey)?.setFilterValue(value)}
        searchPlaceholder={searchPlaceholder}
        className="flex-1 mr-4"
        actions={<DataTableViewOptions table={table} />}
      />
    )}
    {!searchKey && <DataTableViewOptions table={table} />}
  </div>
)}
```

#### Usage Examples

**Basic Usage (Internal State):**
```typescript
<DataTable
  columns={columns}
  data={users}
  searchKey="name"
  searchPlaceholder="Search users..."
/>
```

**Simple Table without Search:**
```typescript
<DataTable
  columns={columns}
  data={users}
/>
```

**With External Table Instance (Advanced):**
```typescript
const table = useReactTable({
  data: permissions,
  columns: permissionColumns,
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

<DataTable
  columns={columns}
  data={permissions}
  table={table}
/>
```

### 2. DataTableColumnHeader (`data-table-column-header.tsx`)

A specialized header component that provides sorting and column visibility controls.

#### Props Interface

```typescript
interface DataTableColumnHeaderProps<TData, TValue>
  extends React.HTMLAttributes<HTMLDivElement> {
  column: Column<TData, TValue>  // TanStack Table column instance
  title: string                  // Display title for the column
}
```

#### Features

- **Smart Rendering**: Only shows sorting controls for sortable columns
- **Visual Indicators**: Shows current sort direction with icons
- **Dropdown Menu**: Provides ascending/descending sort and hide column options
- **Accessibility**: Proper ARIA labels and keyboard navigation

#### Usage in Column Definitions

```typescript
const columns: ColumnDef<Permission>[] = [
  {
    accessorKey: "name",
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="Permission Name" />
    ),
  },
  // ... other columns
]
```

#### Visual States

- **Unsorted**: Shows `ChevronsUpDown` icon
- **Ascending**: Shows `ArrowUp` icon
- **Descending**: Shows `ArrowDown` icon

### 3. DataTablePagination (`data-table-pagination.tsx`)

Handles pagination controls and displays row selection information.

#### Props Interface

```typescript
interface DataTablePaginationProps<TData> {
  table: Table<TData>  // TanStack Table instance
}
```

#### Features

- **Row Selection Display**: Shows selected vs total rows
- **Page Size Control**: Dropdown to change rows per page (10, 20, 30, 40, 50)
- **Navigation Controls**: First, previous, next, last page buttons
- **Page Information**: Current page and total pages display
- **Responsive Design**: Adapts button visibility based on screen size

#### Layout Structure

```
[Row Selection Info] [Page Size] [Page Info] [Navigation Buttons]
```

#### Styling Classes

- **Container**: `flex items-center justify-between px-2 pt-4`
- **Spacing**: Uses `space-x-6 lg:space-x-8` for responsive spacing
- **Button Sizes**: Consistent `h-8 w-8` for navigation buttons

### 4. DataTableViewOptions (`data-table-view-options.tsx`)

Provides column visibility controls through a dropdown interface.

#### Props Interface

```typescript
interface DataTableViewOptionsProps<TData> {
  table: Table<TData>  // TanStack Table instance
}
```

#### Features

- **Column Toggle**: Checkbox interface for showing/hiding columns
- **Smart Filtering**: Only shows columns that can be hidden and have accessor functions
- **Responsive Visibility**: Hidden on small screens (`hidden lg:flex`)
- **Consistent Styling**: Matches other action buttons in the interface

#### Button Configuration

```typescript
<Button
  variant="outline"
  className="ml-auto hidden h-10 px-4 py-2 lg:flex"
>
  <Settings2 className="mr-2 h-4 w-4" />
  View
</Button>
```

## Integration Patterns

### 1. Basic Integration (Internal State Management)

For simple tables without external controls, use the DataTable component with internal state management:

```typescript
function SimpleTable() {
  const columns: ColumnDef<User>[] = [
    {
      accessorKey: "name",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Name" />
      ),
    },
    {
      accessorKey: "email",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Email" />
      ),
    },
  ]

  return (
    <DataTable
      columns={columns}
      data={users}
      searchKey="name"
      searchPlaceholder="Search users..."
    />
  )
}
```

**Features included:**
- Built-in search functionality
- Automatic SearchAndFilter integration
- DataTableViewOptions for column visibility
- Internal state management for sorting, filtering, pagination

### 2. Advanced Integration with External Table Instance

For complex tables that need custom search and filter controls, use external table state management. This pattern is used in components like `PermissionTable` and `RoleTable`:

```typescript
function AdvancedTable() {
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([])
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})
  const [rowSelection, setRowSelection] = React.useState({})
  
  // External search and filter state
  const [searchTerm, setSearchTerm] = React.useState("")
  const [statusFilter, setStatusFilter] = React.useState<string>("all")

  const table = useReactTable({
    data: filteredData, // Pre-filtered data based on external state
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

  const filters = [
    {
      key: "status",
      label: "Status",
      options: [
        { label: "All Status", value: "all" },
        { label: "Active", value: "active" },
        { label: "Inactive", value: "inactive" },
      ],
      value: statusFilter,
      onChange: setStatusFilter,
    },
  ]

  const combinedActions = (
    <div className="flex gap-2 items-center">
      <DataTableViewOptions table={table} />
      <Button onClick={handleCreate}>
        <Plus className="mr-2 h-4 w-4" />
        Add New
      </Button>
    </div>
  )

  return (
    <div className="w-full space-y-4">
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search..."
        filters={filters}
        actions={combinedActions}
      />
      <DataTable
        columns={columns}
        data={filteredData}
        table={table}
      />
    </div>
  )
}
```

**Key differences from basic integration:**
- External SearchAndFilter component with custom filters
- Pre-filtered data passed to DataTable
- Custom actions integrated into SearchAndFilter
- Full control over search and filter state
- No duplicate search controls (DataTable doesn't render its own)

### 3. Integration with Permission Guards

For tables that include action buttons with permission controls:

```typescript
const combinedActions = (
  <div className="flex gap-2 items-center">
    <DataTableViewOptions table={table} />
    <PermissionGuard permissions={["CREATE_ROLE"]}>
      <Button onClick={handleCreateRole}>
        <Plus className="mr-2 h-4 w-4" />
        Add Role
      </Button>
    </PermissionGuard>
  </div>
)
```

### 4. Real-world Implementation Examples

**PermissionTable Integration:**
```typescript
// In PermissionTable.tsx
<SearchAndFilter
  searchTerm={searchTerm}
  onSearchChange={onSearchChange}
  searchPlaceholder={searchPlaceholder}
  filters={filters}
  actions={
    <div className="flex gap-2 items-center">
      <DataTableViewOptions table={table} />
      {actions}
    </div>
  }
/>
<DataTable columns={columns} data={data} table={table} />
```

**RoleTable Integration:**
```typescript
// In RoleTable.tsx - similar pattern with role-specific filters
const filters = [
  {
    key: "status",
    label: "Status",
    options: [
      { label: "All Status", value: "all" },
      { label: "Active", value: "active" },
      { label: "Inactive", value: "inactive" },
      { label: "Deprecated", value: "deprecated" },
    ],
    value: statusFilter,
    onChange: onStatusFilterChange,
  },
 ]
 ```

## CombinedActions Pattern

### Overview

The `combinedActions` pattern is a standardized approach for integrating table view options with custom action buttons passed down from parent components. This pattern ensures consistent UI layout and proper separation of concerns between table components and their parent containers.

### Pattern Implementation

#### 1. Basic CombinedActions Structure

The `combinedActions` should always combine `DataTableViewOptions` with actions passed from the parent component:

```typescript
// In table component (e.g., UserTable.tsx)
const combinedActions = useMemo(() => (
  <div className="flex gap-2 items-center">
    <DataTableViewOptions table={table} />
    {actions}
  </div>
), [table, actions]);
```

#### 2. Parent Component Actions

Parent components (e.g., `UserList.tsx`) should pass actions through the `actions` prop following the `BaseTableProps` interface:

```typescript
// In parent component (e.g., UserList.tsx)
const actions = canManageUsers ? (
  <Button onClick={() => navigate('/users/create')}>
    <Plus className="mr-2 h-4 w-4" />
    Add User
  </Button>
) : null;

return (
  <UserTable
    data={users}
    loading={loading}
    searchTerm={searchTerm}
    onSearchChange={setSearchTerm}
    actions={actions}
  />
);
```

#### 3. Complete Implementation Example

**Parent Component (UserList.tsx):**
```typescript
function UserList() {
  const { canManageUsers } = usePermissions();
  const navigate = useNavigate();
  
  // ... other state and logic
  
  const actions = canManageUsers ? (
    <Button onClick={() => navigate('/users/create')}>
      <Plus className="mr-2 h-4 w-4" />
      Add User
    </Button>
  ) : null;

  return (
    <div className="container mx-auto py-6">
      <UserTable
        data={users}
        loading={loading}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search users..."
        actions={actions}
      />
    </div>
  );
}
```

**Table Component (UserTable.tsx):**
```typescript
interface UserTableProps extends BaseTableProps<User> {
  // Additional user-specific props if needed
}

function UserTable({ 
  data, 
  loading, 
  searchTerm, 
  onSearchChange, 
  searchPlaceholder, 
  actions 
}: UserTableProps) {
  // ... table setup and state management
  
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
```

### Key Benefits

1. **Consistent Layout**: All tables have the same action button placement and styling
2. **Separation of Concerns**: Parent components handle business logic, table components handle presentation
3. **Permission Integration**: Actions can be conditionally rendered based on user permissions
4. **Reusability**: Table components remain generic and reusable across different contexts
5. **Type Safety**: Using `BaseTableProps` ensures proper typing for actions

### Best Practices

#### 1. Always Use useMemo for CombinedActions

```typescript
// ✅ Good - prevents unnecessary re-renders
const combinedActions = useMemo(() => (
  <div className="flex gap-2 items-center">
    <DataTableViewOptions table={table} />
    {actions}
  </div>
), [table, actions]);

// ❌ Bad - creates new object on every render
const combinedActions = (
  <div className="flex gap-2 items-center">
    <DataTableViewOptions table={table} />
    {actions}
  </div>
);
```

#### 2. Handle Null Actions Gracefully

```typescript
// ✅ Good - handles cases where no actions are provided
const combinedActions = useMemo(() => (
  <div className="flex gap-2 items-center">
    <DataTableViewOptions table={table} />
    {actions}
  </div>
), [table, actions]);

// Actions will be null/undefined if no permissions, which is fine
```

#### 3. Consistent Button Styling

```typescript
// ✅ Good - consistent with other action buttons
<Button onClick={handleAction}>
  <Plus className="mr-2 h-4 w-4" />
  Add Item
</Button>

// ✅ Good - with permission guard
<PermissionGuard permissions={["CREATE_USER"]}>
  <Button onClick={handleCreateUser}>
    <Plus className="mr-2 h-4 w-4" />
    Add User
  </Button>
</PermissionGuard>
```

#### 4. Multiple Actions Support

```typescript
// Parent component can pass multiple actions
const actions = (
  <>
    {canImportUsers && (
      <Button variant="outline" onClick={handleImport}>
        <Upload className="mr-2 h-4 w-4" />
        Import
      </Button>
    )}
    {canManageUsers && (
      <Button onClick={handleCreate}>
        <Plus className="mr-2 h-4 w-4" />
        Add User
      </Button>
    )}
  </>
);
```

### Common Patterns

#### 1. Permission-Based Actions
```typescript
const actions = canManageUsers ? (
  <Button onClick={() => navigate('/users/create')}>
    <Plus className="mr-2 h-4 w-4" />
    Add User
  </Button>
) : null;
```

#### 2. Multiple Conditional Actions
```typescript
const actions = (
  <>
    {canImportData && (
      <Button variant="outline" onClick={handleImport}>
        <Upload className="mr-2 h-4 w-4" />
        Import
      </Button>
    )}
    {canExportData && (
      <Button variant="outline" onClick={handleExport}>
        <Download className="mr-2 h-4 w-4" />
        Export
      </Button>
    )}
    {canCreateItem && (
      <Button onClick={handleCreate}>
        <Plus className="mr-2 h-4 w-4" />
        Add New
      </Button>
    )}
  </>
);
```

#### 3. Dialog Integration
```typescript
const actions = canManageRoles ? (
  <>
    <Button onClick={() => setShowDialog(true)}>
      <Plus className="mr-2 h-4 w-4" />
      Add Role
    </Button>
    <RoleDialog 
      open={showDialog} 
      onOpenChange={setShowDialog}
      onSuccess={handleRoleCreated}
    />
  </>
) : null;
```

## External Table Instance Usage

### When to Use External Table Instances

Use external table instances when you need:

1. **Custom Search and Filter Controls**: When the built-in search isn't sufficient
2. **Pre-filtered Data**: When you need to filter data before it reaches the table
3. **Complex State Management**: When table state needs to be shared with other components
4. **Custom Actions Integration**: When you need to integrate action buttons with SearchAndFilter
5. **Advanced Filtering**: When you need multiple filter types (status, date ranges, etc.)

### External Table Setup

#### 1. State Management

```typescript
// Required state for external table management
const [sorting, setSorting] = React.useState<SortingState>([])
const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([])
const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})
const [rowSelection, setRowSelection] = React.useState({})

// Optional: External search and filter state
const [searchTerm, setSearchTerm] = React.useState("")
const [statusFilter, setStatusFilter] = React.useState<string>("all")
```

#### 2. Data Filtering Logic

When using external table instances, implement your own filtering logic:

```typescript
// Example filtering logic (similar to RoleList.tsx)
const filteredData = React.useMemo(() => {
  return data.filter((item) => {
    // Search term filtering
    const matchesSearch = searchTerm === "" || 
      item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.description?.toLowerCase().includes(searchTerm.toLowerCase())
    
    // Status filtering
    const matchesStatus = statusFilter === "all" || item.status === statusFilter
    
    return matchesSearch && matchesStatus
  })
}, [data, searchTerm, statusFilter])
```

#### 3. Table Instance Creation

```typescript
const table = useReactTable({
  data: filteredData, // Use pre-filtered data
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
```

### SearchAndFilter Integration

#### Filter Configuration

```typescript
interface FilterOption {
  label: string
  value: string
}

interface Filter {
  key: string
  label: string
  options: FilterOption[]
  value: string
  onChange: (value: string) => void
}

const filters: Filter[] = [
  {
    key: "status",
    label: "Status",
    options: [
      { label: "All Status", value: "all" },
      { label: "Active", value: "active" },
      { label: "Inactive", value: "inactive" },
      { label: "Deprecated", value: "deprecated" },
    ],
    value: statusFilter,
    onChange: setStatusFilter,
  },
  // Add more filters as needed
]
```

#### Actions Integration

```typescript
const combinedActions = (
  <div className="flex gap-2 items-center">
    <DataTableViewOptions table={table} />
    <PermissionGuard permissions={["CREATE_PERMISSION"]}>
      <Button onClick={handleCreate}>
        <Plus className="mr-2 h-4 w-4" />
        Add New
      </Button>
    </PermissionGuard>
  </div>
)
```

### Complete External Table Example

```typescript
function ExternalTableExample() {
  // Table state
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([])
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})
  const [rowSelection, setRowSelection] = React.useState({})
  
  // External filter state
  const [searchTerm, setSearchTerm] = React.useState("")
  const [statusFilter, setStatusFilter] = React.useState<string>("all")
  
  // Data filtering
  const filteredData = React.useMemo(() => {
    return data.filter((item) => {
      const matchesSearch = searchTerm === "" || 
        item.name.toLowerCase().includes(searchTerm.toLowerCase())
      const matchesStatus = statusFilter === "all" || item.status === statusFilter
      return matchesSearch && matchesStatus
    })
  }, [data, searchTerm, statusFilter])
  
  // Table instance
  const table = useReactTable({
    data: filteredData,
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
  
  // Filter configuration
  const filters = [
    {
      key: "status",
      label: "Status",
      options: [
        { label: "All Status", value: "all" },
        { label: "Active", value: "active" },
        { label: "Inactive", value: "inactive" },
      ],
      value: statusFilter,
      onChange: setStatusFilter,
    },
  ]
  
  // Actions
  const actions = (
    <div className="flex gap-2 items-center">
      <DataTableViewOptions table={table} />
      <Button onClick={handleCreate}>
        <Plus className="mr-2 h-4 w-4" />
        Add New
      </Button>
    </div>
  )
  
  return (
    <div className="w-full space-y-4">
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search items..."
        filters={filters}
        actions={actions}
      />
      <DataTable
        columns={columns}
        data={filteredData}
        table={table}
      />
    </div>
  )
}
```

### Benefits of External Table Usage

1. **No Duplicate Controls**: DataTable won't render its own SearchAndFilter when external table is provided
2. **Flexible Filtering**: Implement custom filtering logic beyond simple column filtering
3. **Integrated Actions**: Combine DataTableViewOptions with custom action buttons
4. **Shared State**: Table state can be accessed by other components
5. **Performance**: Pre-filter data to reduce table processing overhead

### Common Patterns

#### Pattern 1: Status-based Filtering
```typescript
// Used in RoleTable, PermissionTable
const matchesStatus = statusFilter === "all" || item.status === statusFilter
```

#### Pattern 2: Multi-field Search
```typescript
// Search across multiple fields
const matchesSearch = searchTerm === "" || 
  item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
  item.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
  item.email?.toLowerCase().includes(searchTerm.toLowerCase())
```

#### Pattern 3: Permission-gated Actions
```typescript
<PermissionGuard permissions={["CREATE_ROLE"]}>
  <Button onClick={handleCreateRole}>
    <Plus className="mr-2 h-4 w-4" />
    Add Role
  </Button>
</PermissionGuard>
```


### Core Dependencies

- **@tanstack/react-table**: Table state management and utilities
- **React**: Component framework
- **lucide-react**: Icon library for UI elements

### Internal Dependencies

- **@/components/ui/table**: Base table components (Table, TableHeader, etc.)
- **@/components/ui/button**: Button component
- **@/components/ui/dropdown-menu**: Dropdown menu components
- **@/components/ui/select**: Select input components
- **@/components/common/SearchAndFilter**: Search and filter controls
- **@/lib/utils**: Utility functions (cn for className merging)

## Styling System

The components use Tailwind CSS with a consistent design system:

### Color Scheme
- **Borders**: `border` class for table borders
- **Backgrounds**: `bg-muted/50` for hover states
- **Text**: `text-muted-foreground` for secondary text

### Spacing
- **Padding**: Consistent `p-4` for table cells, `px-2 pt-4` for pagination
- **Margins**: `ml-auto` for right alignment, `mr-2` for icon spacing
- **Gaps**: `gap-2` for button groups, `space-x-2` for inline elements

### Responsive Design
- **Breakpoints**: `lg:` prefix for large screen adaptations
- **Visibility**: `hidden lg:flex` for responsive element visibility
- **Spacing**: Different spacing values for different screen sizes

## Best Practices

### 1. Column Definition

Always use proper TypeScript types and consistent patterns for column definitions:

```typescript
const columns: ColumnDef<YourDataType>[] = [
  {
    accessorKey: "id",
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="ID" />
    ),
    cell: ({ row }) => <div className="font-medium">{row.getValue("id")}</div>,
  },
  {
    accessorKey: "name",
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="Name" />
    ),
    cell: ({ row }) => {
      const name = row.getValue("name") as string
      return <div className="font-medium">{name}</div>
    },
  },
]
```

**Key points:**
- Always use `DataTableColumnHeader` for sortable columns
- Include proper TypeScript type assertions in cell renderers
- Use consistent styling classes (`font-medium`, etc.)

### 2. State Management Strategy

Choose the appropriate state management approach based on your needs:

#### Use Internal State When:
- Simple tables with basic search functionality
- No custom filters needed
- No integration with external components
- Minimal customization required

```typescript
// ✅ Good: Simple internal state usage
<DataTable
  columns={columns}
  data={users}
  searchKey="name"
  searchPlaceholder="Search users..."
/>
```

#### Use External State When:
- Custom search and filter controls needed
- Multiple filter types (status, date ranges, etc.)
- Integration with SearchAndFilter component
- Action buttons need to be included
- Table state needs to be shared with other components

```typescript
// ✅ Good: External state for complex scenarios
const table = useReactTable({
  data: filteredData,
  columns,
  // ... full configuration
})

return (
  <div className="w-full space-y-4">
    <SearchAndFilter {...searchAndFilterProps} />
    <DataTable columns={columns} data={filteredData} table={table} />
  </div>
)
```

### 3. Performance Optimization

#### Memoize Column Definitions
```typescript
const columns = React.useMemo<ColumnDef<DataType>[]>(
  () => [
    {
      accessorKey: "name",
      header: ({ column }) => (
        <DataTableColumnHeader column={column} title="Name" />
      ),
    },
    // ... other columns
  ],
  [] // Empty dependency array if columns don't change
)
```

#### Memoize Filtered Data
```typescript
const filteredData = React.useMemo(() => {
  return data.filter((item) => {
    const matchesSearch = searchTerm === "" || 
      item.name.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === "all" || item.status === statusFilter
    return matchesSearch && matchesStatus
  })
}, [data, searchTerm, statusFilter])
```

#### Use Proper Dependencies
```typescript
// ✅ Good: Include all dependencies
const filteredData = React.useMemo(() => {
  // filtering logic
}, [data, searchTerm, statusFilter, otherFilter])

// ❌ Bad: Missing dependencies
const filteredData = React.useMemo(() => {
  // filtering logic that uses searchTerm
}, [data]) // Missing searchTerm dependency
```

### 4. Accessibility Best Practices

#### Proper ARIA Labels
```typescript
<Button
  variant="ghost"
  size="sm"
  className="-ml-3 h-8 data-[state=open]:bg-accent"
  aria-label={`Sort by ${title}`}
>
  <span>{title}</span>
  {/* Sort icons */}
</Button>
```

#### Screen Reader Support
```typescript
<span className="sr-only">Go to first page</span>
<ChevronsLeft className="h-4 w-4" />
```

#### Keyboard Navigation
Ensure all interactive elements are keyboard accessible and follow proper tab order.

### 5. Error Handling and Edge Cases

#### Handle Empty Data
```typescript
// The DataTable component already handles this with:
{table.getRowModel().rows?.length ? (
  // Render rows
) : (
  <TableRow>
    <TableCell colSpan={columns.length} className="h-24 text-center">
      No results.
    </TableCell>
  </TableRow>
)}
```

#### Handle Loading States
```typescript
function TableWithLoading() {
  if (loading) {
    return <div>Loading...</div>
  }
  
  if (error) {
    return <div>Error: {error.message}</div>
  }
  
  return <DataTable columns={columns} data={data} />
}
```

#### Validate Props
```typescript
// Ensure searchKey matches actual column accessorKey
const searchColumn = columns.find(col => 
  'accessorKey' in col && col.accessorKey === searchKey
)

if (searchKey && !searchColumn) {
  console.warn(`Search key "${searchKey}" not found in columns`)
}
```

### 6. Styling Consistency

#### Use Design System Classes
```typescript
// ✅ Good: Consistent with design system
<Button variant="outline" size="sm" className="h-8 w-8 p-0">

// ❌ Bad: Custom styles that break consistency
<Button style={{ width: '32px', height: '32px' }}>
```

#### Responsive Design
```typescript
// ✅ Good: Responsive visibility
<Button className="hidden h-8 w-8 p-0 lg:flex">

// ✅ Good: Responsive spacing
<div className="flex items-center space-x-6 lg:space-x-8">
```

### 7. Integration Patterns

#### SearchAndFilter Integration
```typescript
// ✅ Good: Proper actions integration
const actions = (
  <div className="flex gap-2 items-center">
    <DataTableViewOptions table={table} />
    <PermissionGuard permissions={["CREATE_ROLE"]}>
      <Button onClick={handleCreate}>
        <Plus className="mr-2 h-4 w-4" />
        Add New
      </Button>
    </PermissionGuard>
  </div>
)

<SearchAndFilter
  searchTerm={searchTerm}
  onSearchChange={setSearchTerm}
  searchPlaceholder="Search..."
  filters={filters}
  actions={actions}
/>
```

#### Permission Guards
```typescript
// ✅ Good: Wrap action buttons with permission guards
<PermissionGuard permissions={["CREATE_ROLE"]}>
  <Button onClick={handleCreateRole}>Add Role</Button>
</PermissionGuard>

// ❌ Bad: No permission checking
<Button onClick={handleCreateRole}>Add Role</Button>
```

### 8. Common Anti-patterns to Avoid

#### Don't Mix Internal and External State
```typescript
// ❌ Bad: Using both internal search and external table
<DataTable
  columns={columns}
  data={data}
  searchKey="name" // This won't work with external table
  table={externalTable}
/>

// ✅ Good: Use one or the other
<DataTable columns={columns} data={data} table={externalTable} />
```

#### Don't Duplicate Search Controls
```typescript
// ❌ Bad: Duplicate search controls
<SearchAndFilter {...props} />
<DataTable columns={columns} data={data} searchKey="name" />

// ✅ Good: Use external table to avoid duplication
<SearchAndFilter {...props} />
<DataTable columns={columns} data={data} table={table} />
```

#### Don't Forget Column Visibility Support
```typescript
// ❌ Bad: Column without proper accessorKey
{
  id: "name",
  header: "Name",
  cell: ({ row }) => row.original.name // Won't work with visibility controls
}

// ✅ Good: Proper accessorKey for visibility support
{
  accessorKey: "name",
  header: ({ column }) => (
    <DataTableColumnHeader column={column} title="Name" />
  ),
}
```

### 9. Testing Considerations

#### Test Column Sorting
```typescript
// Ensure columns are sortable when expected
expect(column.getCanSort()).toBe(true)
```

#### Test Filter Functionality
```typescript
// Test that filters work correctly
fireEvent.change(searchInput, { target: { value: 'test' } })
expect(filteredResults).toHaveLength(expectedCount)
```

#### Test Pagination
```typescript
// Test pagination controls
fireEvent.click(nextPageButton)
expect(currentPage).toBe(2)
```

### 10. Migration Guidelines

#### From Basic HTML Tables
1. Convert table structure to column definitions
2. Replace manual pagination with DataTablePagination
3. Add sorting with DataTableColumnHeader
4. Implement search with SearchAndFilter integration

#### From Other Table Libraries
1. Map existing column configurations to TanStack Table format
2. Update state management to use TanStack Table hooks
3. Replace custom controls with DataTable components
4. Ensure TypeScript types are properly configured

## Common Issues and Solutions

### 1. TypeScript Errors with Table Instance

**Issue**: `Parameter 'headerGroup' implicitly has an 'any' type`

**Solution**: This is a known limitation when using external table instances. The errors are cosmetic and don't affect functionality. Consider using type assertions if needed:

```typescript
{table.getHeaderGroups().map((headerGroup: any) => (
  // ... component code
))}
```

### 2. Column Visibility Not Working

**Issue**: Columns don't hide/show when using DataTableViewOptions

**Solution**: Ensure the column definition includes proper `accessorKey` or `accessorFn`:

```typescript
// ✅ Good
{
  accessorKey: "name",
  header: "Name",
}

// ❌ Bad - won't work with visibility controls
{
  id: "name",
  header: "Name",
  cell: ({ row }) => row.original.name
}
```

### 3. Search Not Working

**Issue**: Search functionality doesn't filter results

**Solution**: Ensure the `searchKey` matches an actual column `accessorKey`:

```typescript
// Column definition
{
  accessorKey: "name", // This key
  header: "Name",
}

// DataTable usage
<DataTable
  searchKey="name" // Must match the accessorKey
  // ... other props
/>
```

## Migration Guide

### From Basic Table to DataTable

1. **Replace table markup** with DataTable component
2. **Convert data structure** to column definitions
3. **Add necessary imports** for TanStack Table types
4. **Update styling** to use Tailwind classes

### Example Migration

**Before:**
```typescript
<table>
  <thead>
    <tr>
      <th>Name</th>
      <th>Status</th>
    </tr>
  </thead>
  <tbody>
    {data.map(item => (
      <tr key={item.id}>
        <td>{item.name}</td>
        <td>{item.status}</td>
      </tr>
    ))}
  </tbody>
</table>
```

**After:**
```typescript
const columns: ColumnDef<DataType>[] = [
  {
    accessorKey: "name",
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="Name" />
    ),
  },
  {
    accessorKey: "status",
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="Status" />
    ),
  },
]

<DataTable columns={columns} data={data} />
```

## Future Enhancements

### Planned Features

1. **Export Functionality**: CSV/Excel export capabilities
2. **Advanced Filtering**: Date ranges, multi-select filters
3. **Column Resizing**: Draggable column width adjustment
4. **Row Actions**: Bulk operations and row-level actions
5. **Virtual Scrolling**: Performance optimization for large datasets

### Extension Points

The current architecture supports easy extension through:

- **Custom Cell Renderers**: Via column definition `cell` property
- **Custom Filters**: Through TanStack Table's filter functions
- **Custom Actions**: Via the `actions` prop in SearchAndFilter integration
- **Theming**: Through Tailwind CSS class overrides

## Conclusion

The data table component system provides a robust foundation for building feature-rich tables in the application. Its modular design, TypeScript support, and integration with TanStack Table make it suitable for both simple and complex data presentation needs. 

### Key Features

- **Standardized Interface**: The `BaseTableProps` interface ensures consistency across all table components
- **CombinedActions Pattern**: Provides a standardized approach for integrating table controls with custom actions
- **Type Safety**: Full TypeScript support with proper interfaces and type definitions
- **Flexible Architecture**: Supports both simple internal state management and complex external table instances
- **Permission Integration**: Seamless integration with permission-based action rendering
- **Consistent UI**: Unified styling and layout patterns across all table implementations

The system's flexibility allows for easy customization while maintaining consistency across the application. The introduction of `BaseTableProps` and the `combinedActions` pattern ensures that all table components follow the same architectural principles, making the codebase more maintainable and developer-friendly.