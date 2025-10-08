import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Plus } from "lucide-react";
import httpClient from "@/lib/httpClient";
import type { UserGroup, Role, Module, CreateGroupForm } from "@/types";
import SearchAndFilter from "@/components/common/SearchAndFilter";
import UserGroupsTable from "@/components/user-groups/UserGroupsTable";
import { Button } from "@/components/ui/button";
import { PermissionGuard } from "@/components/common/PermissionGuard";

const UserGroups: React.FC = () => {
  const [groups, setGroups] = useState<UserGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [memberCountFilter, setMemberCountFilter] = useState("all");
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [createForm, setCreateForm] = useState<CreateGroupForm>({
    name: "",
    description: "",
  });

  // Role assignment state
  const [isRoleDialogOpen, setIsRoleDialogOpen] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<UserGroup | null>(
    null
  );
  const [modules, setModules] = useState<Module[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [selectedModule, setSelectedModule] = useState<string>("");
  const [selectedRoles, setSelectedRoles] = useState<number[]>([]);
  const [roleLoading, setRoleLoading] = useState(false);

  // Filter groups based on search term and member count
  const filteredGroups = groups.filter((group) => {
    const matchesSearch =
      group.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (group.description?.toLowerCase() || "").includes(
        searchTerm.toLowerCase()
      );

    const matchesMemberCount =
      memberCountFilter === "all" ||
      (memberCountFilter === "empty" && group.memberCount === 0) ||
      (memberCountFilter === "small" &&
        group.memberCount > 0 &&
        group.memberCount <= 5) ||
      (memberCountFilter === "medium" &&
        group.memberCount > 5 &&
        group.memberCount <= 20) ||
      (memberCountFilter === "large" && group.memberCount > 20);

    return matchesSearch && matchesMemberCount;
  });

  useEffect(() => {
    fetchGroups();
  }, []);

  const fetchGroups = async () => {
    try {
      setLoading(true);
      const groups = await httpClient.getUserGroups();
      setGroups(groups);
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
      await httpClient.createUserGroup(createForm);
      setCreateForm({ name: "", description: "" });
      setIsCreateDialogOpen(false);
      await fetchGroups();
    } catch (e) {
      setError("Failed to create group");
    } finally {
      setLoading(false);
    }
  };

  const deleteGroup = async (groupId: number) => {
    try {
      setLoading(true);
      await httpClient.deleteUserGroup(groupId);
      await fetchGroups();
    } catch (e) {
      setError("Failed to delete group");
    } finally {
      setLoading(false);
    }
  };

  if (loading && groups.length === 0) {
    return <div className="p-6">Loading...</div>;
  }

  if (error) {
    return <div className="p-6 text-red-500">Error: {error}</div>;
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">User Groups</h1>
          <p className="text-muted-foreground">
            Manage permission groups and their members
          </p>
        </div>
      </div>

      {/* Search and Filter */}
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search user groups..."
        filters={[
          {
            label: "Member Count",
            value: memberCountFilter,
            onChange: setMemberCountFilter,
            options: [
              { value: "all", label: "All Groups" },
              { value: "empty", label: "Empty (0 members)" },
              { value: "small", label: "Small (1-5 members)" },
              { value: "medium", label: "Medium (6-20 members)" },
              { value: "large", label: "Large (20+ members)" },
            ],
            placeholder: "Filter by size",
            width: "w-48",
          },
        ]}
        actions={
          <PermissionGuard permission="user-groups:create">
            <Button asChild>
              <Link to="/user-groups/create">
                <Plus className="h-4 w-4 mr-2" />
                Create Group
              </Link>
            </Button>
          </PermissionGuard>
        }
      />

      {/* Groups Table */}
      <UserGroupsTable
        filteredGroups={filteredGroups}
        loading={loading}
        onDeleteGroup={deleteGroup}
      />
    </div>
  );
};

export default UserGroups;
