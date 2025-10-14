import React, { useState, useEffect, useMemo } from "react";
import { Activity, ChevronDown, ChevronUp, Edit, Save, X, Plus } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import httpClient from "@/lib/httpClient";
import type { Permission } from "@/types";

export interface RolePermissionCardProps {
  roleId: number;
  permissions?: Permission[];
  title?: string;
  emptyMessage?: string;
  className?: string;
  showIcon?: boolean;
  badgeVariant?: "default" | "secondary" | "destructive" | "outline";
  gridCols?: {
    base?: number;
    md?: number;
    lg?: number;
  };
  defaultExpanded?: boolean;
  onPermissionsUpdate?: (updatedPermissions: Permission[]) => void;
  updating?: boolean;
  mode?: "create" | "edit";
}

export const RolePermissionCard: React.FC<RolePermissionCardProps> = ({
  roleId,
  permissions = [],
  title = "Permissions",
  emptyMessage = "No permissions assigned.",
  className = "",
  showIcon = true,
  badgeVariant = "outline",
  gridCols = { base: 1, md: 2, lg: 3 },
  defaultExpanded = true,
  onPermissionsUpdate,
  updating = false,
  mode = "edit",
}) => {
  const [isExpanded, setIsExpanded] = useState(defaultExpanded);
  const [isEditing, setIsEditing] = useState(false);
  const [availablePermissions, setAvailablePermissions] = useState<Permission[]>([]);
  const [selectedPermissionIds, setSelectedPermissionIds] = useState<number[]>([]);
  const [loadingPermissions, setLoadingPermissions] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterType, setFilterType] = useState<"all" | "assigned" | "unassigned">("all");
  
  const gridClasses = `grid grid-cols-${gridCols.base} ${
    gridCols.md ? `md:grid-cols-${gridCols.md}` : ""
  } ${gridCols.lg ? `lg:grid-cols-${gridCols.lg}` : ""} gap-2`;

  // Initialize selected permissions when editing starts
  useEffect(() => {
    if (isEditing) {
      setSelectedPermissionIds(permissions.map(p => p.id));
      fetchAvailablePermissions();
    }
  }, [isEditing, permissions]);

  const fetchAvailablePermissions = async () => {
    setLoadingPermissions(true);
    try {
      const allPermissions = await httpClient.getPermissions();
      setAvailablePermissions(allPermissions);
    } catch (error) {
      console.error("Failed to fetch available permissions:", error);
    } finally {
      setLoadingPermissions(false);
    }
  };

  const toggleExpanded = () => {
    setIsExpanded(!isExpanded);
  };

  const handleEditToggle = () => {
    if (isEditing) {
      // Cancel editing - reset selected permissions
      setSelectedPermissionIds(permissions.map(p => p.id));
    }
    setIsEditing(!isEditing);
  };

  const handlePermissionToggle = (permissionId: number, checked: boolean) => {
    setSelectedPermissionIds(prev => 
      checked 
        ? [...prev, permissionId]
        : prev.filter(id => id !== permissionId)
    );
  };

  const handleClearAll = () => {
    setSelectedPermissionIds([]);
  };

  const handleSelectAll = () => {
    const allPermissionIds = [...permissions.map(p => p.id), ...getUnassignedPermissions().map(p => p.id)];
    setSelectedPermissionIds(allPermissionIds);
  };

  const handleSaveChanges = async () => {
    try {
      // Get current permission IDs
      const currentPermissionIds = permissions.map(p => p.id);
      
      // Find permissions to add and remove
      const permissionsToAdd = selectedPermissionIds.filter(id => !currentPermissionIds.includes(id));
      const permissionsToRemove = currentPermissionIds.filter(id => !selectedPermissionIds.includes(id));

      // Update permissions via API
      if (permissionsToAdd.length > 0) {
        await httpClient.assignPermissionsToRole(roleId, permissionsToAdd);
      }
      
      if (permissionsToRemove.length > 0) {
        await httpClient.removePermissionsFromRole(roleId, permissionsToRemove);
      }

      // Get updated permissions
      const updatedPermissions = availablePermissions.filter(p => selectedPermissionIds.includes(p.id));
      
      // Notify parent component
      if (onPermissionsUpdate) {
        onPermissionsUpdate(updatedPermissions);
      }

      setIsEditing(false);
    } catch (error) {
      console.error("Failed to update role permissions:", error);
    }
  };

  const getUnassignedPermissions = () => {
    const assignedIds = permissions.map(p => p.id);
    return availablePermissions.filter(p => !assignedIds.includes(p.id));
  };

  // Filter and search functionality
  const filteredPermissions = useMemo(() => {
    let filtered: Permission[] = [];
    
    switch (filterType) {
      case "assigned":
        filtered = permissions;
        break;
      case "unassigned":
        filtered = getUnassignedPermissions();
        break;
      case "all":
      default:
        filtered = [...permissions, ...getUnassignedPermissions()];
        break;
    }

    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(permission => 
        permission.name.toLowerCase().includes(query) ||
        (permission.description && permission.description.toLowerCase().includes(query))
      );
    }

    return filtered;
  }, [permissions, availablePermissions, searchQuery, filterType]);

  const filteredCurrentPermissions = useMemo(() => {
    if (!searchQuery.trim()) return permissions;
    
    const query = searchQuery.toLowerCase();
    return permissions.filter(permission => 
      permission.name.toLowerCase().includes(query) ||
      (permission.description && permission.description.toLowerCase().includes(query))
    );
  }, [permissions, searchQuery]);

  const filteredAvailablePermissions = useMemo(() => {
    const unassigned = getUnassignedPermissions();
    if (!searchQuery.trim()) return unassigned;
    
    const query = searchQuery.toLowerCase();
    return unassigned.filter(permission => 
      permission.name.toLowerCase().includes(query) ||
      (permission.description && permission.description.toLowerCase().includes(query))
    );
  }, [permissions, availablePermissions, searchQuery]);

  // Get selected permissions for display
  const selectedPermissions = useMemo(() => {
    return availablePermissions.filter(p => selectedPermissionIds.includes(p.id));
  }, [availablePermissions, selectedPermissionIds]);

  return (
    <Card className={className}>
      <CardHeader 
        className="cursor-pointer hover:bg-gray-50 transition-colors"
        onClick={!isEditing ? toggleExpanded : undefined}
      >
        <CardTitle className="flex items-center justify-between">
          <div className="flex items-center">
            {showIcon && <Activity className="mr-2 h-5 w-5" />}
            {title} ({permissions.length})
          </div>
          <div className="flex items-center gap-2">
            {mode === "create" ? null : (
              <PermissionGuard permission="ROLE_MGMT:update">
                {isEditing ? (
                  <div className="flex gap-2" onClick={(e) => e.stopPropagation()}>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={handleSaveChanges}
                      disabled={updating || loadingPermissions}
                    >
                      <Save className="h-4 w-4 mr-2" />
                      Save
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={handleEditToggle}
                      disabled={updating}
                    >
                      <X className="h-4 w-4 mr-2" />
                      Cancel
                    </Button>
                  </div>
                ) : (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleEditToggle();
                    }}
                  >
                    <Edit className="h-4 w-4 mr-2" />
                    Edit
                  </Button>
                )}
              </PermissionGuard>
            )}
            {!isEditing && (
              isExpanded ? (
                <ChevronUp className="h-4 w-4 text-gray-500" />
              ) : (
                <ChevronDown className="h-4 w-4 text-gray-500" />
              )
            )}
          </div>
        </CardTitle>
      </CardHeader>
      {(isExpanded || isEditing) && (
        <CardContent>
          {isEditing ? (
            <div className="space-y-6">
              {/* Search and Filter Controls */}
              <SearchAndFilter
                searchTerm={searchQuery}
                onSearchChange={setSearchQuery}
                searchPlaceholder="Search permissions..."
                filters={[
                  {
                    label: "Type",
                    value: filterType,
                    onChange: (value: string) => setFilterType(value as "all" | "assigned" | "unassigned"),
                    options: [
                      { value: "all", label: "All Permissions" },
                      { value: "assigned", label: "Currently Assigned" },
                      { value: "unassigned", label: "Available to Add" }
                    ],
                    placeholder: "Filter by...",
                    width: "w-48"
                  }
                ]}
                actions={
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={handleSelectAll}
                      disabled={selectedPermissions.length === (permissions.length + getUnassignedPermissions().length)}
                    >
                      Select All
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={handleClearAll}
                      disabled={selectedPermissions.length === 0}
                    >
                      Clear All
                    </Button>
                  </div>
                }
                className="mb-4"
              />

              {/* Selected Permissions Summary */}
              {selectedPermissions.length > 0 && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
                  <div className="flex items-center justify-between mb-2">
                    <Label className="text-sm font-medium text-blue-900">
                      Selected Permissions ({selectedPermissions.length})
                    </Label>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={handleClearAll}
                      className="text-blue-700 hover:text-blue-900 hover:bg-blue-100"
                    >
                      <X className="h-4 w-4 mr-1" />
                      Clear All
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-1">
                    {selectedPermissions.map((permission) => (
                      <Badge
                        key={permission.id}
                        variant="secondary"
                        className="bg-blue-100 text-blue-800 hover:bg-blue-200"
                      >
                        {permission.name}
                        <button
                          onClick={() => handlePermissionToggle(permission.id, false)}
                          className="ml-1 hover:bg-blue-300 rounded-full p-0.5"
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </Badge>
                    ))}
                  </div>
                </div>
              )}

              {/* Current Permissions */}
              <div>
                <Label className="text-sm font-medium">Current Permissions</Label>
                {filteredCurrentPermissions.length > 0 ? (
                  <div className="mt-2 space-y-2 max-h-40 overflow-y-auto">
                    {filteredCurrentPermissions.map((permission) => (
                      <div key={permission.id} className="flex items-center space-x-2">
                        <Checkbox
                          id={`current-${permission.id}`}
                          checked={selectedPermissionIds.includes(permission.id)}
                          onCheckedChange={(checked) => 
                            handlePermissionToggle(permission.id, checked as boolean)
                          }
                        />
                        <Label 
                          htmlFor={`current-${permission.id}`}
                          className="text-sm cursor-pointer flex-1"
                        >
                          <div>
                            <div className="font-medium">{permission.name}</div>
                            {permission.description && (
                              <div className="text-xs text-gray-500">{permission.description}</div>
                            )}
                          </div>
                        </Label>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-gray-500 mt-2">
                    {searchQuery ? "No matching assigned permissions found" : "No permissions currently assigned"}
                  </p>
                )}
              </div>

              {/* Available Permissions */}
              {filteredAvailablePermissions.length > 0 && (
                <div>
                  <Label className="text-sm font-medium">Available Permissions</Label>
                  <div className="mt-2 space-y-2 max-h-40 overflow-y-auto">
                    {filteredAvailablePermissions.map((permission) => (
                      <div key={permission.id} className="flex items-center space-x-2">
                        <Checkbox
                          id={`available-${permission.id}`}
                          checked={selectedPermissionIds.includes(permission.id)}
                          onCheckedChange={(checked) => 
                            handlePermissionToggle(permission.id, checked as boolean)
                          }
                        />
                        <Label 
                          htmlFor={`available-${permission.id}`}
                          className="text-sm cursor-pointer flex-1"
                        >
                          <div>
                            <div className="font-medium">{permission.name}</div>
                            {permission.description && (
                              <div className="text-xs text-gray-500">{permission.description}</div>
                            )}
                          </div>
                        </Label>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {loadingPermissions && (
                <p className="text-sm text-gray-500">Loading available permissions...</p>
              )}

              {searchQuery && filteredCurrentPermissions.length === 0 && filteredAvailablePermissions.length === 0 && (
                <p className="text-sm text-gray-500 text-center py-4">
                  No permissions found matching "{searchQuery}"
                </p>
              )}
            </div>
          ) : (
            // Display mode
            permissions.length > 0 ? (
              <div className={gridClasses}>
                {permissions.map((permission) => (
                  <Badge
                    key={permission.id}
                    variant={badgeVariant}
                    className="justify-start"
                    title={permission.description}
                  >
                    {permission.name}
                  </Badge>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Activity className="mx-auto h-12 w-12 text-gray-400" />
                <p className="mt-2 text-sm text-gray-500">{emptyMessage}</p>
                <PermissionGuard permission="ROLE_MGMT:update">
                  <Button
                    variant="outline"
                    className="mt-4"
                    onClick={handleEditToggle}
                  >
                    <Plus className="h-4 w-4 mr-2" />
                    Add Permissions
                  </Button>
                </PermissionGuard>
              </div>
            )
          )}
        </CardContent>
      )}
    </Card>
  );
};

export default RolePermissionCard;