import React, { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
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
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import {
  Plus,
  Edit,
  Trash2,
  Users,
  Shield,
  ChevronDown,
  Settings,
} from "lucide-react";
import api from "@/lib/api";
import RoleAssignmentDialog from "@/components/RoleAssignmentDialog";

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
}

interface Role {
  id: number;
  name: string;
  description: string;
  moduleId: number;
  moduleName: string;
}

interface Module {
  id: number;
  name: string;
  description: string;
}

interface CreateGroupForm {
  name: string;
  description: string;
}

const UserGroups: React.FC = () => {
  const [groups, setGroups] = useState<UserGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingGroup, setEditingGroup] = useState<UserGroup | null>(null);
  const [createForm, setCreateForm] = useState<CreateGroupForm>({
    name: "",
    description: "",
  });
  const [editForm, setEditForm] = useState<CreateGroupForm>({
    name: "",
    description: "",
  });

  // Role assignment state
  const [isRoleDialogOpen, setIsRoleDialogOpen] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<UserGroup | null>(null);
  const [modules, setModules] = useState<Module[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [selectedModule, setSelectedModule] = useState<string>("");
  const [selectedRoles, setSelectedRoles] = useState<number[]>([]);
  const [roleLoading, setRoleLoading] = useState(false);

  useEffect(() => {
    fetchGroups();
  }, []);

  const fetchGroups = async () => {
    try {
      setLoading(true);
      const { data } = await api.get("/v1/user-groups");
      const list = data?.data?.content || data?.data || [];
      setGroups(list);
      setError(null);
    } catch (e) {
      setError("Failed to fetch groups");
    } finally {
      setLoading(false);
    }
  };

  const createGroup = async () => {
    try {
      setLoading(true);
      await api.post("/v1/user-groups", createForm);
      setCreateForm({ name: "", description: "" });
      setIsCreateDialogOpen(false);
      await fetchGroups();
    } catch (e) {
      setError("Failed to create group");
    } finally {
      setLoading(false);
    }
  };

  const updateGroup = async () => {
    if (!editingGroup) return;

    try {
      setLoading(true);
      await api.put(`/v1/user-groups/${editingGroup.userGroupId}`, editForm);
      setEditForm({ name: "", description: "" });
      setEditingGroup(null);
      setIsEditDialogOpen(false);
      await fetchGroups();
    } catch (e) {
      setError("Failed to update group");
    } finally {
      setLoading(false);
    }
  };

  const deleteGroup = async (groupId: number) => {
    try {
      setLoading(true);
      await api.delete(`/v1/user-groups/${groupId}`);
      await fetchGroups();
    } catch (e) {
      setError("Failed to delete group");
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = (group: UserGroup) => {
    setEditingGroup(group);
    setEditForm({ name: group.name, description: group.description });
    setIsEditDialogOpen(true);
  };

  // Role assignment functions
  const fetchModulesAndRoles = async () => {
    try {
      setRoleLoading(true);
      const [modulesResponse, rolesResponse] = await Promise.all([
        api.get("/modules"),
        api.get("/roles"),
      ]);
      // API returns arrays directly, not nested in data.data.content
      setModules(modulesResponse.data || []);
      setRoles(rolesResponse.data || []);
    } catch (e) {
      setError("Failed to fetch modules and roles");
    } finally {
      setRoleLoading(false);
    }
  };

  const handleManageRoles = async (group: UserGroup) => {
    setSelectedGroup(group);
    setSelectedModule("");
    setSelectedRoles([]);
    setIsRoleDialogOpen(true);
    await fetchModulesAndRoles();
  };

  const assignRoles = async () => {
    if (!selectedGroup || !selectedModule || selectedRoles.length === 0) return;

    try {
      setRoleLoading(true);
      await api.post(`/v1/user-groups/${selectedGroup.userGroupId}/roles`, {
        moduleId: parseInt(selectedModule),
        roleIds: selectedRoles,
      });
      setIsRoleDialogOpen(false);
      setSelectedModule("");
      setSelectedRoles([]);
      await fetchGroups();
    } catch (e) {
      setError("Failed to assign roles");
    } finally {
      setRoleLoading(false);
    }
  };

  const removeRoleAssignment = async (
    groupId: number,
    assignmentId: number
  ) => {
    try {
      setLoading(true);
      await api.delete(`/v1/user-groups/${groupId}/roles`, {
        data: { roleAssignmentIds: [assignmentId] },
      });
      await fetchGroups();
    } catch (e) {
      setError("Failed to remove role assignment");
    } finally {
      setLoading(false);
    }
  };

  const handleRoleToggle = (roleId: number) => {
    setSelectedRoles((prev) =>
      prev.includes(roleId)
        ? prev.filter((id) => id !== roleId)
        : [...prev, roleId]
    );
  };

  if (loading && groups.length === 0) {
    return <div className="p-6">Loading...</div>;
  }

  if (error) {
    return <div className="p-6 text-red-500">Error: {error}</div>;
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">User Groups</h1>
          <p className="text-muted-foreground">
            Manage permission groups and their members
          </p>
        </div>
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              New Group
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Group</DialogTitle>
              <DialogDescription>
                Create a new user group to organize permissions and access
                control.
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="create-name">Name</Label>
                <Input
                  id="create-name"
                  value={createForm.name}
                  onChange={(e) =>
                    setCreateForm({ ...createForm, name: e.target.value })
                  }
                  placeholder="Enter group name"
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="create-description">Description</Label>
                <Textarea
                  id="create-description"
                  value={createForm.description}
                  onChange={(e) =>
                    setCreateForm({
                      ...createForm,
                      description: e.target.value,
                    })
                  }
                  placeholder="Enter group description"
                />
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setIsCreateDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={createGroup}
                disabled={!createForm.name.trim() || loading}
              >
                Create Group
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {groups.map((group) => (
          <Card key={group.userGroupId} className="relative">
            <CardHeader>
              <div className="flex justify-between items-start">
                <div>
                  <CardTitle className="text-lg">{group.name}</CardTitle>
                  <CardDescription className="mt-1">
                    {group.description || "No description provided"}
                  </CardDescription>
                </div>
                <div className="flex gap-1">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleEditClick(group)}
                  >
                    <Edit className="h-4 w-4" />
                  </Button>
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button variant="ghost" size="sm">
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
                          onClick={() => deleteGroup(group.userGroupId)}
                        >
                          Delete
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center gap-2">
                  <Users className="h-4 w-4 text-muted-foreground" />
                  <Badge variant="secondary">
                    {group.memberCount}{" "}
                    {group.memberCount === 1 ? "member" : "members"}
                  </Badge>
                </div>

                {/* Role Assignments Section */}
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Shield className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm font-medium">
                        Role Assignments
                      </span>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleManageRoles(group)}
                    >
                      <Settings className="h-3 w-3 mr-1" />
                      Manage
                    </Button>
                  </div>

                  {group.roleAssignments && group.roleAssignments.length > 0 ? (
                    <Collapsible>
                      <CollapsibleTrigger asChild>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="w-full justify-between p-2"
                        >
                          <span className="text-xs">
                            {group.roleAssignments.length} role
                            {group.roleAssignments.length !== 1 ? "s" : ""}{" "}
                            assigned
                          </span>
                          <ChevronDown className="h-3 w-3" />
                        </Button>
                      </CollapsibleTrigger>
                      <CollapsibleContent className="space-y-1 mt-2">
                        {group.roleAssignments.map((assignment) => (
                          <div
                            key={assignment.id}
                            className="flex items-center justify-between p-2 bg-muted/50 rounded text-xs"
                          >
                            <div>
                              <div className="font-medium">
                                {assignment.roleName}
                              </div>
                              <div className="text-muted-foreground">
                                {assignment.moduleName}
                              </div>
                            </div>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() =>
                                removeRoleAssignment(
                                  group.userGroupId,
                                  assignment.id
                                )
                              }
                              className="h-6 w-6 p-0"
                            >
                              <Trash2 className="h-3 w-3" />
                            </Button>
                          </div>
                        ))}
                      </CollapsibleContent>
                    </Collapsible>
                  ) : (
                    <div className="text-xs text-muted-foreground p-2 bg-muted/30 rounded">
                      No roles assigned
                    </div>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Group</DialogTitle>
            <DialogDescription>Update the group information.</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="edit-name">Name</Label>
              <Input
                id="edit-name"
                value={editForm.name}
                onChange={(e) =>
                  setEditForm({ ...editForm, name: e.target.value })
                }
                placeholder="Enter group name"
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="edit-description">Description</Label>
              <Textarea
                id="edit-description"
                value={editForm.description}
                onChange={(e) =>
                  setEditForm({ ...editForm, description: e.target.value })
                }
                placeholder="Enter group description"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsEditDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              onClick={updateGroup}
              disabled={!editForm.name.trim() || loading}
            >
              Update Group
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Role Assignment Dialog */}
      <RoleAssignmentDialog
        isOpen={isRoleDialogOpen}
        onOpenChange={setIsRoleDialogOpen}
        selectedGroup={selectedGroup}
        modules={modules}
        roles={roles}
        selectedModule={selectedModule}
        selectedRoles={selectedRoles}
        roleLoading={roleLoading}
        onModuleChange={setSelectedModule}
        onRoleToggle={handleRoleToggle}
        onAssignRoles={assignRoles}
      />

      {groups.length === 0 && !loading && (
        <div className="text-center py-12">
          <Users className="mx-auto h-12 w-12 text-muted-foreground" />
          <h3 className="mt-4 text-lg font-semibold">No groups found</h3>
          <p className="text-muted-foreground">
            Get started by creating your first user group.
          </p>
        </div>
      )}
    </div>
  );
};

export default UserGroups;