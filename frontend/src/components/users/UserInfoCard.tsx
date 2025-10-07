import React from "react";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StatusBadge } from "../StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-colors";
import { User as UserIcon, Mail, Calendar } from "lucide-react";
import type { User as UserType } from "./types";

interface UserInfoCardProps {
  user: UserType;
  showExtendedInfo?: boolean;
  className?: string;
}

const UserInfoCard: React.FC<UserInfoCardProps> = ({
  user,
  showExtendedInfo = false,
  className = "",
}) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
          <UserIcon className="mr-2 h-5 w-5" />
          User Information
        </CardTitle>
        <CardDescription>
          Basic user account information and details.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Username
            </Label>
            <p className="mt-1 text-sm text-gray-900 font-mono bg-gray-50 px-2 py-1 rounded">
              {user.username}
            </p>
          </div>

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Email
            </Label>
            <p className="mt-1 text-sm text-gray-900">
              {user.email}
              {user.emailVerified && (
                <Badge variant="outline" className="ml-2 text-xs">
                  Verified
                </Badge>
              )}
            </p>
          </div>

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              First Name
            </Label>
            <p className="mt-1 text-sm text-gray-900">
              {user.firstName}
            </p>
          </div>

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Last Name
            </Label>
            <p className="mt-1 text-sm text-gray-900">
              {user.lastName}
            </p>
          </div>

          {user.phoneNumber && (
            <div>
              <Label className="block text-sm font-medium text-gray-700">
                Phone Number
              </Label>
              <p className="mt-1 text-sm text-gray-900">
                {user.phoneNumber}
              </p>
            </div>
          )}

          <div>
            <Label className="block text-sm font-medium text-gray-700">
              Status
            </Label>
            <div className="mt-1">
              <StatusBadge
                status={normalizeEntityStatus("user", user.userStatus)}
              />
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
                  <p className="text-xs text-gray-500">
                    by {user.createdBy}
                  </p>
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
                  <p className="text-xs text-gray-500">
                    by {user.updatedBy}
                  </p>
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