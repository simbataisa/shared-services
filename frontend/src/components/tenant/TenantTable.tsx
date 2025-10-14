import { Link } from "react-router-dom";
import { Building2, Eye } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { normalizeEntityStatus, getTenantTypeLabel } from "@/lib/status-utils";
import { type Tenant } from "@/types/entities";

interface TenantTableProps {
  tenants: Tenant[];
}

export default function TenantTable({ tenants }: TenantTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Tenant</TableHead>
          <TableHead>Type</TableHead>
          <TableHead>Status</TableHead>
          <TableHead className="text-right">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {tenants.map((tenant) => (
          <TableRow key={tenant.id}>
            <TableCell>
              <div className="flex items-center gap-3">
                <div className="bg-primary/10 p-2 rounded-full">
                  <Building2 className="h-4 w-4 text-primary" />
                </div>
                <div>
                  <div className="font-medium">{tenant.name}</div>
                  <div className="text-sm text-muted-foreground font-mono">
                    {tenant.code}
                  </div>
                </div>
              </div>
            </TableCell>
            <TableCell>
              <span className="text-sm">{getTenantTypeLabel(tenant.type)}</span>
            </TableCell>
            <TableCell>
              <div className="flex items-center gap-2">
                <StatusBadge status={tenant.status} showIcon={true} />
              </div>
            </TableCell>
            <TableCell className="text-right">
              <div className="flex items-center justify-end gap-2">
                <PermissionGuard permission="TENANT_MGMT:read">
                  <Button
                    variant="ghost"
                    size="icon"
                    asChild
                    className="text-blue-600 hover:text-blue-700"
                  >
                    <Link to={`/tenants/${tenant.id}`} title="View Details">
                      <Eye className="h-4 w-4" />
                    </Link>
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
