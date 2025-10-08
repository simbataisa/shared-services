import React from "react";
import { Shield } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getStatusColor, getStatusIcon } from "@/lib/status-icons";
import type { RoleDetails } from "@/types";
import { Label } from "../ui/label";

interface RoleInfoCardProps {
  role: RoleDetails;
}

export const RoleInfoCard: React.FC<RoleInfoCardProps> = ({ role }) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center">
          <Shield className="mr-2 h-5 w-5" />
          Role Information
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="text-sm font-medium text-gray-500">
              Role Name
            </label>
            <p className="mt-1 text-sm text-gray-900">{role.name}</p>
          </div>
          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Status
            </Label>
            <div
              className={`inline-flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${getStatusColor(
                role.roleStatus
              )}`}
            >
              {getStatusIcon(role.roleStatus)}
              {role.roleStatus}
            </div>
          </div>
          <div className="md:col-span-2">
            <label className="text-sm font-medium text-gray-500">
              Description
            </label>
            <p className="mt-1 text-sm text-gray-900">
              {role.description || "No description provided"}
            </p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">
              Created At
            </label>
            <p className="mt-1 text-sm text-gray-900">
              {new Date(role.createdAt).toLocaleDateString("en-US", {
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
            </p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">
              Last Updated
            </label>
            <p className="mt-1 text-sm text-gray-900">
              {new Date(role.updatedAt).toLocaleDateString("en-US", {
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
