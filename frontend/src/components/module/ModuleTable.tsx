import { Link } from "react-router-dom";
import { Edit, Trash2, Eye } from "lucide-react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { normalizeEntityStatus } from "@/lib/status-utils";
import { type Module } from "@/types";

interface ModuleTableProps {
  modules: Module[];
  onDelete: (moduleId: number) => void;
}

export default function ModuleTable({ modules, onDelete }: ModuleTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Module</TableHead>
          <TableHead>Product</TableHead>
          <TableHead>Status</TableHead>
          <TableHead>Last Updated</TableHead>
          <TableHead className="text-right">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {modules.map((module) => (
          <TableRow key={module.id}>
            <TableCell>
              <div>
                <div className="font-medium">{module.name}</div>
                <div className="text-sm text-muted-foreground">
                  {module.description}
                </div>
                {module.code && (
                  <div className="text-xs text-muted-foreground mt-1">
                    Code: {module.code}
                  </div>
                )}
              </div>
            </TableCell>
            <TableCell>
              <div className="text-sm">{module.productName}</div>
            </TableCell>
            <TableCell>
              <StatusBadge status={module.moduleStatus} />
            </TableCell>
            <TableCell className="text-sm text-muted-foreground">
              {module.updatedAt
                ? new Date(module.updatedAt).toLocaleDateString()
                : "N/A"}
            </TableCell>
            <TableCell className="text-right">
              <div className="flex justify-end space-x-2">
                <Button
                  variant="ghost"
                  size="sm"
                  asChild
                  className="text-blue-600 hover:text-blue-700"
                >
                  <Link to={`/modules/${module.id}`}>
                    <Eye className="h-4 w-4" />
                  </Link>
                </Button>
                <PermissionGuard permission="MODULE_MGMT:update">
                  <Button
                    variant="ghost"
                    size="sm"
                    asChild
                    className="text-yellow-600 hover:text-yellow-700"
                  >
                    <Link to={`/modules/${module.id}/edit`}>
                      <Edit className="h-4 w-4" />
                    </Link>
                  </Button>
                </PermissionGuard>
                <PermissionGuard permission="MODULE_MGMT:delete">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onDelete(module.id)}
                    className="text-red-600 hover:text-red-700"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </PermissionGuard>
              </div>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
