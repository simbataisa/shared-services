import React, { useState, useMemo } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import {
  Shield,
  ChevronDown,
  ChevronRight,
  Search,
  Filter,
  X,
} from "lucide-react";
import type { RoleAssignment, Role, Module } from "@/types";

interface UserGroup {
  userGroupId: number;
  name: string;
  description: string;
  memberCount: number;
  roleAssignments?: RoleAssignment[];
}

interface UserGroupRolesManagerProps {
  group: UserGroup;
  modules: Module[];
  roles: Role[];
  onAssignRoles: (moduleId: number, roleIds: number[]) => void;
  onRemoveRoleAssignment: (groupId: number, assignmentId: number) => void;
  isLoading?: boolean;
}

const UserGroupRolesManager: React.FC<UserGroupRolesManagerProps> = ({
  group,
  modules,
  roles,
  onAssignRoles,
  onRemoveRoleAssignment,
  isLoading = false,
}) => {
  const [isExpanded, setIsExpanded] = useState(true);
  const [expandedModules, setExpandedModules] = useState<Set<number>>(
    new Set()
  );

  // Search and filter states
  const [searchTerm, setSearchTerm] = useState("");
  const [filterType, setFilterType] = useState<
    "all" | "assigned" | "unassigned"
  >("all");
  const [roleSearchTerms, setRoleSearchTerms] = useState<
    Record<number, string>
  >({});

  // Toggle individual module expansion
  const toggleModuleExpansion = (moduleId: number) => {
    const newExpanded = new Set(expandedModules);
    if (newExpanded.has(moduleId)) {
      newExpanded.delete(moduleId);
    } else {
      newExpanded.add(moduleId);
    }
    setExpandedModules(newExpanded);
  };

  // Clear all filters
  const clearFilters = () => {
    setSearchTerm("");
    setFilterType("all");
    setRoleSearchTerms({});
  };

  // Update role search term for specific module
  const updateRoleSearchTerm = (moduleId: number, term: string) => {
    setRoleSearchTerms((prev) => ({
      ...prev,
      [moduleId]: term,
    }));
  };
  // Get assigned role IDs for quick lookup
  const getAssignedRoleIds = () => {
    if (!group.roleAssignments) return new Set<number>();
    return new Set(
      group.roleAssignments.map((assignment) => assignment.roleId)
    );
  };

  const assignedRoleIds = getAssignedRoleIds();

  // Get assignment ID for a specific role
  const getAssignmentIdForRole = (roleId: number) => {
    if (!group.roleAssignments) return null;
    const assignment = group.roleAssignments.find((a) => a.roleId === roleId);
    return assignment ? assignment.id : null;
  };

  // Filter modules based on search term and filter type
  const filteredModules = useMemo(() => {
    if (!modules) return [];

    return modules.filter((module) => {
      // Search term filter
      const matchesSearch =
        searchTerm === "" ||
        module.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (module.description?.toLowerCase() || "").includes(
          searchTerm.toLowerCase()
        );

      // Assignment filter
      if (filterType === "all") return matchesSearch;

      const moduleRoles =
        roles?.filter((role) => role.moduleId === module.id) || [];
      const hasAssignedRoles = moduleRoles.some((role) =>
        assignedRoleIds.has(role.id)
      );

      if (filterType === "assigned") return matchesSearch && hasAssignedRoles;
      if (filterType === "unassigned")
        return matchesSearch && !hasAssignedRoles;

      return matchesSearch;
    });
  }, [modules, roles, searchTerm, filterType, assignedRoleIds]);

  // Filter roles within a module based on role search term
  const getFilteredRolesForModule = (moduleId: number) => {
    const moduleRoles = rolesByModule[moduleId] || [];
    const roleSearchTerm = roleSearchTerms[moduleId] || "";

    if (roleSearchTerm === "") return moduleRoles;

    return moduleRoles.filter(
      (role) =>
        role.name.toLowerCase().includes(roleSearchTerm.toLowerCase()) ||
        role.description.toLowerCase().includes(roleSearchTerm.toLowerCase())
    );
  };

  // Group roles by module
  const getRolesByModule = () => {
    const rolesByModule: { [moduleId: number]: Role[] } = {};

    // Add safety checks for undefined arrays
    if (!modules || !roles) return rolesByModule;

    modules.forEach((module) => {
      rolesByModule[module.id] = roles.filter(
        (role) => role.moduleId === module.id
      );
    });

    return rolesByModule;
  };

  const rolesByModule = getRolesByModule();

  // Handle role checkbox toggle
  const handleRoleToggle = (role: Role, isAssigned: boolean) => {
    if (isAssigned) {
      // Remove role assignment
      const assignmentId = getAssignmentIdForRole(role.id);
      if (assignmentId) {
        onRemoveRoleAssignment(group.userGroupId, assignmentId);
      }
    } else {
      // Assign role
      if (role.moduleId) {
        onAssignRoles(role.moduleId, [role.id]);
      }
    }
  };

  return (
    <Card>
      <CardHeader>
        <div
          className="flex items-center justify-between cursor-pointer"
          onClick={() => setIsExpanded(!isExpanded)}
        >
          <CardTitle className="flex items-center gap-2">
            <Shield className="h-5 w-5" />
            Role Management
          </CardTitle>
          {isExpanded ? (
            <ChevronDown className="h-4 w-4" />
          ) : (
            <ChevronRight className="h-4 w-4" />
          )}
        </div>
      </CardHeader>
      {isExpanded && (
        <CardContent className="space-y-6">
          {/* Search and Filter Controls */}
          <div className="flex flex-col sm:flex-row gap-4 p-4 bg-muted/30 rounded-lg">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search modules by name or description..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <div className="flex gap-2">
              <Select
                value={filterType}
                onValueChange={(value: "all" | "assigned" | "unassigned") =>
                  setFilterType(value)
                }
              >
                <SelectTrigger className="w-40">
                  <Filter className="h-4 w-4 mr-2" />
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Modules</SelectItem>
                  <SelectItem value="assigned">With Roles</SelectItem>
                  <SelectItem value="unassigned">Without Roles</SelectItem>
                </SelectContent>
              </Select>
              {(searchTerm ||
                filterType !== "all" ||
                Object.keys(roleSearchTerms).length > 0) && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={clearFilters}
                  className="px-3"
                >
                  <X className="h-4 w-4" />
                </Button>
              )}
            </div>
          </div>

          {!filteredModules || filteredModules.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Shield className="h-12 w-12 mx-auto mb-2 opacity-50" />
              <p>
                {searchTerm || filterType !== "all"
                  ? "No modules match your search criteria"
                  : "No modules available"}
              </p>
            </div>
          ) : (
            <div className="space-y-6">
              {filteredModules.map((module) => {
                const moduleRoles = rolesByModule[module.id] || [];
                const assignedCount = moduleRoles.filter((role) =>
                  assignedRoleIds.has(role.id)
                ).length;

                return (
                  <div key={module.id} className="space-y-3">
                    <div
                      className="flex items-center justify-between cursor-pointer hover:bg-muted/30 p-2 rounded"
                      onClick={() => toggleModuleExpansion(module.id)}
                    >
                      <div className="flex items-center gap-2">
                        <div className="flex items-center gap-2">
                          {expandedModules.has(module.id) ? (
                            <ChevronDown className="h-4 w-4" />
                          ) : (
                            <ChevronRight className="h-4 w-4" />
                          )}
                          <h3 className="text-lg font-semibold">
                            {module.name}
                          </h3>
                        </div>
                        <Badge variant="outline">
                          {assignedCount}/{moduleRoles.length} assigned
                        </Badge>
                      </div>
                    </div>

                    {module.description && expandedModules.has(module.id) && (
                      <p className="text-sm text-muted-foreground ml-6">
                        {module.description}
                      </p>
                    )}

                    {expandedModules.has(module.id) && (
                      <div className="border rounded-lg p-4 ml-6">
                        {/* Role search within module */}
                        {moduleRoles.length > 3 && (
                          <div className="mb-4">
                            <div className="relative">
                              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                              <Input
                                placeholder={`Search roles in ${module.name}...`}
                                value={roleSearchTerms[module.id] || ""}
                                onChange={(e) =>
                                  updateRoleSearchTerm(
                                    module.id,
                                    e.target.value
                                  )
                                }
                                className="pl-10 h-8"
                              />
                            </div>
                          </div>
                        )}

                        {(() => {
                          const filteredRoles = getFilteredRolesForModule(
                            module.id
                          );
                          return filteredRoles.length > 0 ? (
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                              {filteredRoles.map((role) => {
                                const isAssigned = assignedRoleIds.has(role.id);

                                return (
                                  <div
                                    key={role.id}
                                    className="flex items-start space-x-3 p-3 hover:bg-muted/50 rounded cursor-pointer"
                                    onClick={() =>
                                      handleRoleToggle(role, isAssigned)
                                    }
                                  >
                                    <Checkbox
                                      checked={isAssigned}
                                      onChange={() =>
                                        handleRoleToggle(role, isAssigned)
                                      }
                                      disabled={isLoading}
                                    />
                                    <div className="flex-1 min-w-0">
                                      <div className="text-sm font-medium">
                                        {role.name}
                                      </div>
                                      <div className="text-xs text-muted-foreground">
                                        {role.description}
                                      </div>
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          ) : (
                            <div className="text-center py-4 text-muted-foreground">
                              <p>
                                {roleSearchTerms[module.id]
                                  ? "No roles match your search"
                                  : "No roles available for this module"}
                              </p>
                            </div>
                          );
                        })()}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      )}
    </Card>
  );
};

export default UserGroupRolesManager;
