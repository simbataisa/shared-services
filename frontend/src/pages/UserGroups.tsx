import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Plus } from "lucide-react";
import httpClient from "@/lib/httpClient";
import type { UserGroup, CreateGroupForm } from "@/types";
import UserGroupsTable from "@/components/user-groups/UserGroupsTable";
import { Button } from "@/components/ui/button";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { type TableFilter } from "@/types/components";

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
      const response = await httpClient.getUserGroups();
      console.log("response", response);
      // Extract the content array from the paginated response
      setGroups(response.content || []);
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

  // Define filters for the table
  const filters: TableFilter[] = [
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
  ];

  // Define actions for the table
  const actions = (
    <PermissionGuard permission="GROUP_MGMT:create">
      <Button asChild>
        <Link to="/user-groups/create">
          <Plus className="h-4 w-4 mr-2" />
          Create Group
        </Link>
      </Button>
    </PermissionGuard>
  );

  if (loading && groups.length === 0) {
    return <div className="p-6">Loading...</div>;
  }

  if (error) {
    return <div className="p-6 text-red-500">Error: {error}</div>;
  }

  return (
    <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 space-y-4 sm:space-y-0">
        <div className="space-y-1">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">User Groups</h1>
          <p className="text-sm sm:text-base text-muted-foreground">
            Manage permission groups and their members
          </p>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-md">
          Error: {error}
        </div>
      )}

      {/* User Groups Table with integrated search and filters */}
      <div className="w-full">
        <UserGroupsTable
          userGroups={filteredGroups}
          loading={loading}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Search user groups..."
          filters={filters}
          actions={actions}
          onDeleteGroup={deleteGroup}
        />
      </div>
    </div>
  );
};

export default UserGroups;
