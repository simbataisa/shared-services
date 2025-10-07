import React from "react";
import { Link } from "react-router-dom";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { StatusBadge } from "@/components/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-colors";
import { Edit, Trash2, Users, Eye } from "lucide-react";

interface RoleAssignment {
  id: number;
  userGroupId: number;
  userGroupName: string;
  moduleId: number;
  moduleName: string;
  roleId: number;
  roleName: string;
  roleDescription: string;
  createdAt: string;
  updatedAt: string;
}

interface UserGroup {
  userGroupId: number;
  name: string;
  description: string;
  memberCount: number;
  roleAssignments?: RoleAssignment[];
  status?: 'active' | 'inactive' | 'pending';
}

interface UserGroupsTableProps {
  filteredGroups: UserGroup[];
  loading: boolean;
  onDeleteGroup: (groupId: number) => void;
}

const UserGroupsTable: React.FC<UserGroupsTableProps> = ({
  filteredGroups,
  loading,
  onDeleteGroup,
}) => {
  if (filteredGroups.length === 0 && !loading) {
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
    <div className="border rounded-lg">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Description</TableHead>
            <TableHead>Members</TableHead>
            <TableHead>Roles</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {filteredGroups.map((group) => (
            <TableRow key={group.userGroupId}>
              <TableCell className="font-medium">
                <div className="flex items-center gap-2">
                  <Users className="h-4 w-4 text-muted-foreground" />
                  {group.name}
                </div>
              </TableCell>
              <TableCell>
                <div className="max-w-xs truncate">
                  {group.description || (
                    <span className="text-muted-foreground italic">
                      No description provided
                    </span>
                  )}
                </div>
              </TableCell>
              <TableCell>
                <Badge variant="secondary">
                  {group.memberCount}{" "}
                  {group.memberCount === 1 ? "member" : "members"}
                </Badge>
              </TableCell>
              <TableCell>
                <Badge variant="outline">
                  {group.roleAssignments?.length || 0} roles
                </Badge>
              </TableCell>
              <TableCell>
                <StatusBadge 
                  status={normalizeEntityStatus('role', group.status || 'ACTIVE')}
                />
              </TableCell>
              <TableCell className="text-right">
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
                  <Button
                    variant="ghost"
                    size="sm"
                    asChild
                    className="text-yellow-600 hover:text-yellow-700"
                  >
                    <Link to={`/user-groups/${group.userGroupId}/edit`}>
                      <Edit className="h-4 w-4" />
                    </Link>
                  </Button>
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-red-600 hover:text-red-700"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Delete Group</AlertDialogTitle>
                        <AlertDialogDescription>
                          Are you sure you want to delete "{group.name}"? This
                          action cannot be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                          onClick={() => onDeleteGroup(group.userGroupId)}
                        >
                          Delete
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default UserGroupsTable;