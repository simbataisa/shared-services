import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
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
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Plus, User, Shield, Mail } from "lucide-react";

interface Role {
  id: number;
  name: string;
  description: string;
}

interface UserGroup {
  userGroupId: number;
  name: string;
  description: string;
}

interface CreateUserForm {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  roleIds: number[];
  userGroupIds: number[];
}

interface UserCreateProps {
  roles: Role[];
  userGroups: UserGroup[];
  onUserCreated: () => void;
  onError: (error: string) => void;
}

const UserCreate: React.FC<UserCreateProps> = ({
  roles,
  userGroups,
  onUserCreated,
  onError,
}) => {
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [createForm, setCreateForm] = useState<CreateUserForm>({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
    roleIds: [],
    userGroupIds: [],
  });

  const handleCreateUser = async () => {
    try {
      const response = await fetch("/api/v1/users", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(createForm),
      });

      if (!response.ok) {
        throw new Error("Failed to create user");
      }

      setIsCreateDialogOpen(false);
      setCreateForm({
        username: "",
        email: "",
        firstName: "",
        lastName: "",
        password: "",
        roleIds: [],
        userGroupIds: [],
      });
      onUserCreated();
    } catch (err) {
      onError(err instanceof Error ? err.message : "Failed to create user");
    }
  };

  return (
    <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Add User
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold text-gray-900">
            Create New User
          </DialogTitle>
          <DialogDescription className="text-gray-600">
            Fill in the information below to create a new user account.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          {/* User Information Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
                <User className="mr-2 h-5 w-5" />
                User Information
              </CardTitle>
              <CardDescription>
                Basic user account information and credentials.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <Label htmlFor="username" className="text-sm font-medium text-gray-700">
                    Username
                  </Label>
                  <Input
                    id="username"
                    type="text"
                    value={createForm.username}
                    onChange={(e) =>
                      setCreateForm({ ...createForm, username: e.target.value })
                    }
                    className="mt-1"
                    placeholder="Enter username"
                  />
                </div>

                <div>
                  <Label htmlFor="email" className="text-sm font-medium text-gray-700">
                    Email Address
                  </Label>
                  <Input
                    id="email"
                    type="email"
                    value={createForm.email}
                    onChange={(e) =>
                      setCreateForm({ ...createForm, email: e.target.value })
                    }
                    className="mt-1"
                    placeholder="Enter email address"
                  />
                </div>

                <div>
                  <Label htmlFor="firstName" className="text-sm font-medium text-gray-700">
                    First Name
                  </Label>
                  <Input
                    id="firstName"
                    type="text"
                    value={createForm.firstName}
                    onChange={(e) =>
                      setCreateForm({ ...createForm, firstName: e.target.value })
                    }
                    className="mt-1"
                    placeholder="Enter first name"
                  />
                </div>

                <div>
                  <Label htmlFor="lastName" className="text-sm font-medium text-gray-700">
                    Last Name
                  </Label>
                  <Input
                    id="lastName"
                    type="text"
                    value={createForm.lastName}
                    onChange={(e) =>
                      setCreateForm({ ...createForm, lastName: e.target.value })
                    }
                    className="mt-1"
                    placeholder="Enter last name"
                  />
                </div>

                <div className="md:col-span-2">
                  <Label htmlFor="password" className="text-sm font-medium text-gray-700">
                    Password
                  </Label>
                  <Input
                    id="password"
                    type="password"
                    value={createForm.password}
                    onChange={(e) =>
                      setCreateForm({ ...createForm, password: e.target.value })
                    }
                    className="mt-1"
                    placeholder="Enter password"
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Roles Assignment Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
                <Shield className="mr-2 h-5 w-5" />
                Role Assignment
              </CardTitle>
              <CardDescription>
                Select the roles to assign to this user.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {roles.length > 0 ? (
                  roles.map((role) => (
                    <div
                      key={role.id}
                      className="border border-gray-200 rounded-lg p-4"
                    >
                      <div className="flex items-center space-x-3">
                        <Checkbox
                          id={`role-${role.id}`}
                          checked={createForm.roleIds.includes(role.id)}
                          onCheckedChange={(checked) => {
                            if (checked) {
                              setCreateForm({
                                ...createForm,
                                roleIds: [...createForm.roleIds, role.id],
                              });
                            } else {
                              setCreateForm({
                                ...createForm,
                                roleIds: createForm.roleIds.filter(
                                  (id) => id !== role.id
                                ),
                              });
                            }
                          }}
                        />
                        <div className="flex-1">
                          <div className="flex items-center space-x-3">
                            <Label
                              htmlFor={`role-${role.id}`}
                              className="text-lg font-medium text-gray-900 cursor-pointer"
                            >
                              {role.name}
                            </Label>
                            <Badge variant="outline">Role</Badge>
                          </div>
                          <p className="mt-1 text-sm text-gray-600">
                            {role.description}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8">
                    <Shield className="mx-auto h-12 w-12 text-gray-400" />
                    <p className="mt-2 text-gray-500">No roles available.</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* User Groups Assignment Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
                <User className="mr-2 h-5 w-5" />
                User Group Assignment
              </CardTitle>
              <CardDescription>
                Select the user groups to assign to this user.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {userGroups.length > 0 ? (
                  userGroups.map((group) => (
                    <div
                      key={group.userGroupId}
                      className="border border-gray-200 rounded-lg p-4"
                    >
                      <div className="flex items-center space-x-3">
                        <Checkbox
                          id={`group-${group.userGroupId}`}
                          checked={createForm.userGroupIds.includes(
                            group.userGroupId
                          )}
                          onCheckedChange={(checked) => {
                            if (checked) {
                              setCreateForm({
                                ...createForm,
                                userGroupIds: [
                                  ...createForm.userGroupIds,
                                  group.userGroupId,
                                ],
                              });
                            } else {
                              setCreateForm({
                                ...createForm,
                                userGroupIds: createForm.userGroupIds.filter(
                                  (id) => id !== group.userGroupId
                                ),
                              });
                            }
                          }}
                        />
                        <div className="flex-1">
                          <div className="flex items-center space-x-3">
                            <Label
                              htmlFor={`group-${group.userGroupId}`}
                              className="text-lg font-medium text-gray-900 cursor-pointer"
                            >
                              {group.name}
                            </Label>
                            <Badge variant="outline">Group</Badge>
                          </div>
                          <p className="mt-1 text-sm text-gray-600">
                            {group.description}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8">
                    <User className="mx-auto h-12 w-12 text-gray-400" />
                    <p className="mt-2 text-gray-500">
                      No user groups available.
                    </p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        <DialogFooter className="pt-6 border-t border-gray-200">
          <Button
            variant="outline"
            onClick={() => setIsCreateDialogOpen(false)}
          >
            Cancel
          </Button>
          <Button onClick={handleCreateUser}>
            <Plus className="mr-2 h-4 w-4" />
            Create User
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default UserCreate;