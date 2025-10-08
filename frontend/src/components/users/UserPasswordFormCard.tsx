import React, { useState } from "react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Key, Eye, EyeOff } from "lucide-react";
import type { CreateUserForm } from "@/types";

interface UserPasswordFormCardProps {
  formData: CreateUserForm;
  onFormChange: (field: keyof CreateUserForm, value: string) => void;
  errors?: Partial<Record<keyof CreateUserForm, string>>;
  className?: string;
}

const UserPasswordFormCard: React.FC<UserPasswordFormCardProps> = ({
  formData,
  onFormChange,
  errors = {},
  className = "",
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [confirmPasswordError, setConfirmPasswordError] = useState("");

  const handleConfirmPasswordChange = (value: string) => {
    setConfirmPassword(value);
    if (value && formData.password && value !== formData.password) {
      setConfirmPasswordError("Passwords do not match");
    } else {
      setConfirmPasswordError("");
    }
  };

  const handlePasswordChange = (value: string) => {
    onFormChange("password", value);
    if (confirmPassword && value !== confirmPassword) {
      setConfirmPasswordError("Passwords do not match");
    } else {
      setConfirmPasswordError("");
    }
  };

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
          <Key className="mr-2 h-5 w-5" />
          Password Setup
        </CardTitle>
        <CardDescription>
          Set the initial password for the new user account.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="password" className="text-sm font-medium text-gray-700">
              Password *
            </Label>
            <div className="relative">
              <Input
                id="password"
                type={showPassword ? "text" : "password"}
                value={formData.password}
                onChange={(e) => handlePasswordChange(e.target.value)}
                placeholder="Enter password"
                className={`pr-10 ${errors.password ? "border-red-500" : ""}`}
              />
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? (
                  <EyeOff className="h-4 w-4" />
                ) : (
                  <Eye className="h-4 w-4" />
                )}
              </Button>
            </div>
            {errors.password && (
              <p className="text-sm text-red-600">{errors.password}</p>
            )}
            <p className="text-xs text-gray-500">
              Password must be at least 8 characters long
            </p>
          </div>

          <div className="space-y-2">
            <Label htmlFor="confirmPassword" className="text-sm font-medium text-gray-700">
              Confirm Password *
            </Label>
            <div className="relative">
              <Input
                id="confirmPassword"
                type={showConfirmPassword ? "text" : "password"}
                value={confirmPassword}
                onChange={(e) => handleConfirmPasswordChange(e.target.value)}
                placeholder="Confirm password"
                className={`pr-10 ${confirmPasswordError ? "border-red-500" : ""}`}
              />
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              >
                {showConfirmPassword ? (
                  <EyeOff className="h-4 w-4" />
                ) : (
                  <Eye className="h-4 w-4" />
                )}
              </Button>
            </div>
            {confirmPasswordError && (
              <p className="text-sm text-red-600">{confirmPasswordError}</p>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default UserPasswordFormCard;