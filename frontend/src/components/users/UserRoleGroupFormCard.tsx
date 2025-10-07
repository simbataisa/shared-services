import React, { useState, useMemo } from "react";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Users, Shield, Search } from "lucide-react";
interface Role {
  id: number;
  name: string;
  description?: string;
}

interface UserGroup {
  id: number;
  userGroupId: number;
  name: string;
  description?: string;
}

interface UserRoleGroupFormCardProps {
  availableRoles: Role[];
  availableGroups: UserGroup[];
  selectedRoleIds: string[];
  selectedGroupIds: string[];
  onRoleToggle: (roleId: number, checked: boolean) => void;
  onGroupToggle: (groupId: number, checked: boolean) => void;
  loading?: boolean;
  className?: string;
}

const UserRoleGroupFormCard: React.FC<UserRoleGroupFormCardProps> = ({
  availableRoles,
  availableGroups,
  selectedRoleIds,
  selectedGroupIds,
  onRoleToggle,
  onGroupToggle,
  loading = false,
  className = "",
}) => {
  // Search states
  const [roleSearchTerm, setRoleSearchTerm] = useState("");
  const [groupSearchTerm, setGroupSearchTerm] = useState("");

  // Filtered roles based on search term
  const filteredRoles = useMemo(() => {
    if (!roleSearchTerm.trim()) return availableRoles;
    return availableRoles.filter(role =>
      role.name.toLowerCase().includes(roleSearchTerm.toLowerCase()) ||
      (role.description && role.description.toLowerCase().includes(roleSearchTerm.toLowerCase()))
    );
  }, [availableRoles, roleSearchTerm]);

  // Filtered groups based on search term
  const filteredGroups = useMemo(() => {
    if (!groupSearchTerm.trim()) return availableGroups;
    return availableGroups.filter(group =>
      group.name.toLowerCase().includes(groupSearchTerm.toLowerCase()) ||
      (group.description && group.description.toLowerCase().includes(groupSearchTerm.toLowerCase()))
    );
  }, [availableGroups, groupSearchTerm]);

  return (
    <div className={`grid grid-cols-1 xl:grid-cols-2 gap-4 sm:gap-6 ${className}`}>
      {/* Roles Card */}
      <Card>
        <CardHeader className="pb-4 sm:pb-6">
          <CardTitle className="flex items-center gap-2 text-lg sm:text-xl">
            <Shield className="h-4 w-4 sm:h-5 sm:w-5" />
            Roles
          </CardTitle>
          <CardDescription className="text-sm">
            Assign roles to the user
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {/* Search Input for Roles */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search roles..."
              value={roleSearchTerm}
              onChange={(e) => setRoleSearchTerm(e.target.value)}
              className="pl-10"
              disabled={loading}
            />
          </div>
          
          <div className="max-h-48 sm:max-h-64 overflow-y-auto space-y-3">
            {filteredRoles.length > 0 ? (
              filteredRoles.map((role) => (
                <div
                  key={role.id}
                  className="flex items-start space-x-2 p-2 rounded-lg hover:bg-muted/50 transition-colors"
                >
                  <Checkbox
                    id={`role-${role.id}`}
                    checked={selectedRoleIds.includes(role.id.toString())}
                    onCheckedChange={(checked) =>
                      onRoleToggle(role.id, checked as boolean)
                    }
                    className="mt-0.5"
                    disabled={loading}
                  />
                  <div className="grid gap-1 leading-none flex-1 min-w-0">
                    <label
                      htmlFor={`role-${role.id}`}
                      className="text-sm font-medium leading-none cursor-pointer peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                    >
                      {role.name}
                    </label>
                    {role.description && (
                      <p className="text-xs text-muted-foreground break-words">
                        {role.description}
                      </p>
                    )}
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                <Search className="h-8 w-8 mx-auto mb-2 opacity-50" />
                <p className="text-sm">No roles found matching "{roleSearchTerm}"</p>
              </div>
            )}
          </div>
          {selectedRoleIds.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-3 pt-3 border-t">
              {selectedRoleIds.map((roleId) => {
                const role = availableRoles.find((r) => r.id.toString() === roleId);
                return role ? (
                  <Badge
                    key={roleId}
                    variant="secondary"
                    className="text-xs"
                  >
                    {role.name}
                  </Badge>
                ) : null;
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* User Groups Card */}
      <Card>
        <CardHeader className="pb-4 sm:pb-6">
          <CardTitle className="flex items-center gap-2 text-lg sm:text-xl">
            <Users className="h-4 w-4 sm:h-5 sm:w-5" />
            User Groups
          </CardTitle>
          <CardDescription className="text-sm">
            Assign user groups to the user
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {/* Search Input for User Groups */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search user groups..."
              value={groupSearchTerm}
              onChange={(e) => setGroupSearchTerm(e.target.value)}
              className="pl-10"
              disabled={loading}
            />
          </div>
          
          <div className="max-h-48 sm:max-h-64 overflow-y-auto space-y-3">
            {filteredGroups.length > 0 ? (
              filteredGroups.map((userGroup) => (
                <div
                  key={userGroup.userGroupId}
                  className="flex items-start space-x-2 p-2 rounded-lg hover:bg-muted/50 transition-colors"
                >
                  <Checkbox
                    id={`userGroup-${userGroup.userGroupId}`}
                    checked={selectedGroupIds.includes(userGroup.userGroupId.toString())}
                    onCheckedChange={(checked) =>
                      onGroupToggle(userGroup.userGroupId, checked as boolean)
                    }
                    className="mt-0.5"
                    disabled={loading}
                  />
                  <div className="grid gap-1 leading-none flex-1 min-w-0">
                    <label
                      htmlFor={`userGroup-${userGroup.userGroupId}`}
                      className="text-sm font-medium leading-none cursor-pointer peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                    >
                      {userGroup.name}
                    </label>
                    {userGroup.description && (
                      <p className="text-xs text-muted-foreground break-words">
                        {userGroup.description}
                      </p>
                    )}
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                <Search className="h-8 w-8 mx-auto mb-2 opacity-50" />
                <p className="text-sm">No user groups found matching "{groupSearchTerm}"</p>
              </div>
            )}
          </div>
          {selectedGroupIds.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-3 pt-3 border-t">
              {selectedGroupIds.map((userGroupId) => {
                const userGroup = availableGroups.find((ug) => ug.userGroupId.toString() === userGroupId);
                return userGroup ? (
                  <Badge
                    key={userGroupId}
                    variant="secondary"
                    className="text-xs"
                  >
                    {userGroup.name}
                  </Badge>
                ) : null;
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default UserRoleGroupFormCard;