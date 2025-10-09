import React, { useState } from "react";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StatusBadge } from "@/components/common/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-utils";
import { User as UserIcon, Edit, Save, X } from "lucide-react";
import type { User as UserType } from "@/types";
import httpClient from "@/lib/httpClient";
import { getStatusColor, getStatusIcon } from "@/lib/status-utils";

interface UserInfoCardProps {
  user: UserType;
  showExtendedInfo?: boolean;
  className?: string;
  canUpdate?: boolean;
  onUserUpdated?: (updatedUser: any) => void;
}

const UserInfoCard: React.FC<UserInfoCardProps> = ({
  user,
  showExtendedInfo = false,
  className = "",
  canUpdate = false,
  onUserUpdated,
}) => {
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    username: user.username,
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    phoneNumber: user.phoneNumber || "",
  });

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setError(null);
  };

  const handleSave = async () => {
    try {
      setLoading(true);
      setError(null);

      const updateData = {
        username: formData.username,
        email: formData.email,
        firstName: formData.firstName,
        lastName: formData.lastName,
        phoneNumber: formData.phoneNumber || undefined,
      };

      const updatedUser = await httpClient.updateUser(user.id, updateData);

      if (onUserUpdated) {
        onUserUpdated(updatedUser);
      }

      setIsEditing(false);
    } catch (error: any) {
      console.error("Error updating user:", error);
      setError(
        error.response?.data?.message || "Failed to update user information"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      username: user.username,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      phoneNumber: user.phoneNumber || "",
    });
    setError(null);
    setIsEditing(false);
  };

  return (
    <Card className={className}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
          <UserIcon className="mr-2 h-5 w-5" />
          User Information
        </CardTitle>
        {canUpdate && (
          <div className="flex space-x-2">
            {isEditing ? (
              <>
                <Button
                  onClick={handleSave}
                  disabled={loading}
                  size="sm"
                  className="h-8"
                >
                  <Save className="mr-1 h-3 w-3" />
                  Save
                </Button>
                <Button
                  onClick={handleCancel}
                  disabled={loading}
                  variant="outline"
                  size="sm"
                  className="h-8"
                >
                  <X className="mr-1 h-3 w-3" />
                  Cancel
                </Button>
              </>
            ) : (
              <Button
                onClick={() => setIsEditing(true)}
                variant="outline"
                size="sm"
                className="h-8"
              >
                <Edit className="mr-1 h-3 w-3" />
                Edit
              </Button>
            )}
          </div>
        )}
        <CardDescription>
          Basic user account information and details.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {error && (
          <div className="text-sm text-red-600 bg-red-50 p-2 rounded mb-4">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Username
            </Label>
            {isEditing ? (
              <Input
                value={formData.username}
                onChange={(e) => handleInputChange("username", e.target.value)}
                disabled={loading}
                className="mt-1 text-sm"
              />
            ) : (
              <p className="mt-1 text-sm text-gray-900 font-mono bg-gray-50 px-2 py-1 rounded">
                {user.username}
              </p>
            )}
          </div>

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Email
            </Label>
            {isEditing ? (
              <Input
                type="email"
                value={formData.email}
                onChange={(e) => handleInputChange("email", e.target.value)}
                disabled={loading}
                className="mt-1 text-sm"
              />
            ) : (
              <p className="mt-1 text-sm text-gray-900">
                {user.email}
                {user.emailVerified && (
                  <Badge variant="outline" className="ml-2 text-xs">
                    Verified
                  </Badge>
                )}
              </p>
            )}
          </div>

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              First Name
            </Label>
            {isEditing ? (
              <Input
                value={formData.firstName}
                onChange={(e) => handleInputChange("firstName", e.target.value)}
                disabled={loading}
                className="mt-1 text-sm"
              />
            ) : (
              <p className="mt-1 text-sm text-gray-900">{user.firstName}</p>
            )}
          </div>

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Last Name
            </Label>
            {isEditing ? (
              <Input
                value={formData.lastName}
                onChange={(e) => handleInputChange("lastName", e.target.value)}
                disabled={loading}
                className="mt-1 text-sm"
              />
            ) : (
              <p className="mt-1 text-sm text-gray-900">{user.lastName}</p>
            )}
          </div>

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Phone Number
            </Label>
            {isEditing ? (
              <Input
                value={formData.phoneNumber}
                onChange={(e) =>
                  handleInputChange("phoneNumber", e.target.value)
                }
                disabled={loading}
                placeholder="Enter phone number"
                className="mt-1 text-sm"
              />
            ) : (
              <p className="mt-1 text-sm text-gray-900">
                {user.phoneNumber || "Not provided"}
              </p>
            )}
          </div>

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Status
            </Label>
            <div
              className={`inline-flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${getStatusColor(
                user.userStatus
              )}`}
            >
              {getStatusIcon(user.userStatus)}
              {user.userStatus}
            </div>
          </div>

          {showExtendedInfo && (
            <>
              <div>
                <Label className="block text-sm font-medium text-gray-700">
                  Created
                </Label>
                <p className="mt-1 text-sm text-gray-900">
                  {formatDate(user.createdAt)}
                </p>
                {user.createdBy && (
                  <p className="text-xs text-gray-500">by {user.createdBy}</p>
                )}
              </div>

              <div>
                <Label className="block text-sm font-medium text-gray-700">
                  Last Updated
                </Label>
                <p className="mt-1 text-sm text-gray-900">
                  {formatDate(user.updatedAt)}
                </p>
                {user.updatedBy && (
                  <p className="text-xs text-gray-500">by {user.updatedBy}</p>
                )}
              </div>

              {user.lastLogin && (
                <div>
                  <Label className="block text-sm font-medium text-gray-700">
                    Last Login
                  </Label>
                  <p className="mt-1 text-sm text-gray-900">
                    {formatDate(user.lastLogin)}
                  </p>
                </div>
              )}
            </>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default UserInfoCard;
