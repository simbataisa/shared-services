# Data Table Components Documentation

## Overview

The data table system is a comprehensive set of React components built on top of TanStack Table (formerly React Table) that provides a powerful, flexible, and reusable table solution for the frontend application. The system consists of four main components that work together to create feature-rich data tables with sorting, filtering, pagination, and column visibility controls.

## Architecture

The data table system follows a modular architecture where each component has a specific responsibility:

- **DataTable**: Main container component that orchestrates the table functionality
- **DataTableColumnHeader**: Provides sortable column headers with dropdown controls
- **DataTablePagination**: Handles pagination controls and row selection information
- **DataTableViewOptions**: Manages column visibility with a dropdown interface

## Components

### 1. DataTable (`data-table.tsx`)

The main component that renders the complete table with all features.

#### Props Interface

```typescript
interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[]     // Column definitions from TanStack Table
  data: TData[]                           // Array of data objects
  searchKey?: string                      // Column key for search functionality
  searchPlaceholder?: string              // Placeholder text for search input
  table?: any                            // External table instance (optional)
}
```

#### Key Features

- **Dual Mode Operation**: Can work with internal table state or accept external table instance
- **Built-in Search**: Optional search functionality when `searchKey` is provided
- **State Management**: Manages sorting, filtering, column visibility, and row selection
- **Responsive Design**: Adapts to different screen sizes
- **Integration Ready**: Works seamlessly with SearchAndFilter component

#### Internal State

```typescript
const [sorting, setSorting] = React.useState<SortingState>([])
const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([])
const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})
const [rowSelection, setRowSelection] = React.useState({})
```

#### Usage Examples

**Basic Usage:**
```typescript
<DataTable
  columns={columns}
  data={users}
  searchKey="name"
  searchPlaceholder="Search users..."
/>
```

**With External Table Instance:**
```typescript
const table = useReactTable({
  data: permissions,
  columns: permissionColumns,
  // ... other table configuration
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

### 1. Basic Integration

For simple tables without external controls:

```typescript
function SimpleTable() {
  return (
    <DataTable
      columns={columns}
      data={data}
      searchKey="name"
      searchPlaceholder="Search..."
    />
  )
}
```

### 2. Advanced Integration with External Controls

For complex tables with custom search and filter controls:

```typescript
function AdvancedTable() {
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([])
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})
  const [rowSelection, setRowSelection] = React.useState({})

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
  })

  const combinedActions = (
    <div className="flex gap-2 items-center">
      <DataTableViewOptions table={table} />
      <Button>Add New</Button>
    </div>
  )

  return (
    <div className="w-full space-y-4">
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={onSearchChange}
        searchPlaceholder="Search..."
        filters={filters}
        actions={combinedActions}
      />
      <DataTable
        columns={columns}
        data={data}
        table={table}
      />
    </div>
  )
}
```

## Dependencies

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

Always use proper TypeScript types for column definitions:

```typescript
const columns: ColumnDef<YourDataType>[] = [
  {
    accessorKey: "id",
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="ID" />
    ),
    cell: ({ row }) => <div>{row.getValue("id")}</div>,
  },
]
```

### 2. State Management

For complex tables, manage state externally:

```typescript
// ✅ Good: External state management
const [sorting, setSorting] = React.useState<SortingState>([])
const table = useReactTable({
  // ... configuration
  onSortingChange: setSorting,
  state: { sorting }
})

// ❌ Avoid: Relying only on internal state for complex scenarios
```

### 3. Performance Optimization

Use React.memo for column definitions to prevent unnecessary re-renders:

```typescript
const columns = React.useMemo<ColumnDef<DataType>[]>(
  () => [
    // ... column definitions
  ],
  []
)
```

### 4. Accessibility

Ensure proper accessibility attributes:

```typescript
<Button
  variant="ghost"
  size="sm"
  className="-ml-3 h-8 data-[state=open]:bg-accent"
  aria-label={`Sort by ${title}`}
>
```

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

The data table component system provides a robust foundation for building feature-rich tables in the application. Its modular design, TypeScript support, and integration with TanStack Table make it suitable for both simple and complex data presentation needs. The system's flexibility allows for easy customization while maintaining consistency across the application.