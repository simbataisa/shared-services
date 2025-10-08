import React from "react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { User as UserIcon } from "lucide-react";
import type { CreateUserForm } from "@/types";

interface UserInfoFormCardProps {
  formData: CreateUserForm;
  onFormChange: (field: keyof CreateUserForm, value: string) => void;
  errors?: Partial<Record<keyof CreateUserForm, string>>;
  className?: string;
}

const UserInfoFormCard: React.FC<UserInfoFormCardProps> = ({
  formData,
  onFormChange,
  errors = {},
  className = "",
}) => {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
          <UserIcon className="mr-2 h-5 w-5" />
          User Information
        </CardTitle>
        <CardDescription>
          Enter the basic user account information and details.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
          <div className="space-y-2">
            <Label htmlFor="username" className="text-sm font-medium text-gray-700">
              Username *
            </Label>
            <Input
              id="username"
              type="text"
              value={formData.username}
              onChange={(e) => onFormChange("username", e.target.value)}
              placeholder="Enter username"
              className={errors.username ? "border-red-500" : ""}
            />
            {errors.username && (
              <p className="text-sm text-red-600">{errors.username}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="email" className="text-sm font-medium text-gray-700">
              Email *
            </Label>
            <Input
              id="email"
              type="email"
              value={formData.email}
              onChange={(e) => onFormChange("email", e.target.value)}
              placeholder="Enter email address"
              className={errors.email ? "border-red-500" : ""}
            />
            {errors.email && (
              <p className="text-sm text-red-600">{errors.email}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="firstName" className="text-sm font-medium text-gray-700">
              First Name *
            </Label>
            <Input
              id="firstName"
              type="text"
              value={formData.firstName}
              onChange={(e) => onFormChange("firstName", e.target.value)}
              placeholder="Enter first name"
              className={errors.firstName ? "border-red-500" : ""}
            />
            {errors.firstName && (
              <p className="text-sm text-red-600">{errors.firstName}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="lastName" className="text-sm font-medium text-gray-700">
              Last Name *
            </Label>
            <Input
              id="lastName"
              type="text"
              value={formData.lastName}
              onChange={(e) => onFormChange("lastName", e.target.value)}
              placeholder="Enter last name"
              className={errors.lastName ? "border-red-500" : ""}
            />
            {errors.lastName && (
              <p className="text-sm text-red-600">{errors.lastName}</p>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default UserInfoFormCard;