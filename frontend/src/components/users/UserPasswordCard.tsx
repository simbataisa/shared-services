import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Key, Eye, EyeOff } from "lucide-react";
import type { PasswordChangeForm } from "@/types";

interface UserPasswordCardProps {
  onPasswordChange?: (passwordData: PasswordChangeForm) => void;
  loading?: boolean;
  canUpdate?: boolean;
  className?: string;
}

const UserPasswordCard: React.FC<UserPasswordCardProps> = ({
  onPasswordChange,
  loading = false,
  canUpdate = false,
  className = "",
}) => {
  const [passwordForm, setPasswordForm] = useState<PasswordChangeForm>({
    newPassword: "",
    confirmPassword: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState<string>("");

  const handlePasswordChange = (field: keyof PasswordChangeForm, value: string) => {
    setPasswordForm(prev => ({ ...prev, [field]: value }));
    setError(""); // Clear error when user types
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!passwordForm.newPassword || !passwordForm.confirmPassword) {
      setError("Both password fields are required");
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    if (passwordForm.newPassword.length < 8) {
      setError("Password must be at least 8 characters long");
      return;
    }

    onPasswordChange?.(passwordForm);
  };

  const resetForm = () => {
    setPasswordForm({ newPassword: "", confirmPassword: "" });
    setError("");
    setShowPassword(false);
    setShowConfirmPassword(false);
  };

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
          <Key className="mr-2 h-5 w-5" />
          Password Management
        </CardTitle>
        <CardDescription>
          Change the user's password. The user will need to use the new password on their next login.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {canUpdate ? (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <Alert variant="destructive">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <div className="space-y-2">
              <Label htmlFor="newPassword">New Password</Label>
              <div className="relative">
                <Input
                  id="newPassword"
                  type={showPassword ? "text" : "password"}
                  value={passwordForm.newPassword}
                  onChange={(e) => handlePasswordChange("newPassword", e.target.value)}
                  placeholder="Enter new password"
                  disabled={loading}
                  className="pr-10"
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={loading}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4" />
                  ) : (
                    <Eye className="h-4 w-4" />
                  )}
                </Button>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirm New Password</Label>
              <div className="relative">
                <Input
                  id="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  value={passwordForm.confirmPassword}
                  onChange={(e) => handlePasswordChange("confirmPassword", e.target.value)}
                  placeholder="Confirm new password"
                  disabled={loading}
                  className="pr-10"
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  disabled={loading}
                >
                  {showConfirmPassword ? (
                    <EyeOff className="h-4 w-4" />
                  ) : (
                    <Eye className="h-4 w-4" />
                  )}
                </Button>
              </div>
            </div>

            <div className="flex gap-2 pt-2">
              <Button
                type="submit"
                disabled={loading || !passwordForm.newPassword || !passwordForm.confirmPassword}
                className="flex-1"
              >
                {loading ? "Updating..." : "Update Password"}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={resetForm}
                disabled={loading}
              >
                Reset
              </Button>
            </div>
          </form>
        ) : (
          <div className="text-center py-4">
            <p className="text-sm text-gray-500">
              You don't have permission to change this user's password.
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default UserPasswordCard;