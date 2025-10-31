import React from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { StatusBadge } from "@/components/common/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-utils";
import { Eye } from "lucide-react";
import type { Role, Permission } from "@/types";

interface RoleTableProps {
  roles: Role[];
  selectedRoleId?: number;
  showActions?: boolean;
  canManageRoles: boolean;
  onViewRole: (role: Role) => void;
}

const RoleTable: React.FC<RoleTableProps> = ({
  roles,
  selectedRoleId,
  showActions = true,
  canManageRoles,
  onViewRole,
}) => {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Name</TableHead>
          <TableHead>Description</TableHead>
          <TableHead>Status</TableHead>
          <TableHead>Permissions</TableHead>
          <TableHead>Created</TableHead>
          {showActions && <TableHead>Actions</TableHead>}
        </TableRow>
      </TableHeader>
      <TableBody>
        {roles.map((role) => (
          <TableRow
            key={role.id}
            className={selectedRoleId === role.id ? "bg-muted/50" : ""}
          >
            <TableCell className="font-medium">{role.name}</TableCell>
            <TableCell>{role.description}</TableCell>
            <TableCell>
              <StatusBadge
                status={normalizeEntityStatus(
                  "role",
                  role.status || "ACTIVE"
                )}
              />
            </TableCell>
            <TableCell>
              <div className="flex flex-wrap gap-1">
                {role.permissions
                  ?.slice(0, 3)
                  .map((permission: Permission) => (
                    <Badge
                      key={permission.id}
                      variant="outline"
                      className="text-xs"
                    >
                      {permission.name}
                    </Badge>
                  ))}
                {role.permissions && role.permissions.length > 3 && (
                  <Badge variant="outline" className="text-xs">
                    +{role.permissions.length - 3} more
                  </Badge>
                )}
              </div>
            </TableCell>
            <TableCell>
              {role.createdAt
                ? new Date(role.createdAt).toLocaleDateString()
                : ""}
            </TableCell>
            {showActions && (
              <TableCell>
                {canManageRoles && (
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
                )}
              </TableCell>
            )}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};

export default RoleTable;