import React from "react";
import { Link } from "react-router-dom";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import type { ProductWithModules } from "@/types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Edit, Trash2, Eye } from "lucide-react";
import { StatusBadge } from "@/components/common/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-utils";

interface ProductTableProps {
  products: ProductWithModules[];
  onDeleteProduct: (productId: string) => void;
}

export function ProductTable({ products, onDeleteProduct }: ProductTableProps) {
  return (
    <div className="space-y-4">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Code</TableHead>
            <TableHead>Version</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Description</TableHead>
            <TableHead>Modules</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {products.map((product) => (
            <React.Fragment key={product.id}>
              <TableRow className="hover:bg-muted/50">
                <TableCell className="font-medium">
                  {product.name}
                </TableCell>
                <TableCell className="font-mono text-sm">
                  {product.code}
                </TableCell>
                <TableCell>{product.version}</TableCell>
                <TableCell>
                  <StatusBadge
                    status={normalizeEntityStatus(
                      "product",
                      product.productStatus
                    )}
                  />
                </TableCell>
                <TableCell className="max-w-xs truncate">
                  {product.description || "-"}
                </TableCell>
                <TableCell>
                  <Badge variant="outline">
                    {product.modules?.length || 0}
                  </Badge>
                </TableCell>
                <TableCell className="text-right">
                  <Button
                    variant="ghost"
                    size="icon"
                    asChild
                    className="text-blue-600 hover:text-blue-700"
                  >
                    <Link
                      to={`/products/${product.id}`}
                      title="View Details"
                    >
                      <Eye className="h-4 w-4" />
                    </Link>
                  </Button>
                  <PermissionGuard permission="PRODUCT_MGMT:update">
                    <Button
                      variant="ghost"
                      size="icon"
                      asChild
                      className="text-yellow-600 hover:text-yellow-700"
                    >
                      <Link
                        to={`/products/${product.id}/edit`}
                        className="flex items-center text-yellow-600 hover:text-yellow-700"
                      >
                        <Edit className="mr-2 h-4 w-4" />
                      </Link>
                    </Button>
                  </PermissionGuard>
                  <PermissionGuard permission="PRODUCT_MGMT:delete">
                    <Button
                      variant="ghost"
                      size="icon"
                      asChild
                      className="text-red-600 hover:text-red-700"
                      onClick={() =>
                        onDeleteProduct(product.id.toString())
                      }
                    >
                      <Link
                        to={`#`}
                        className="flex items-center text-red-600 hover:text-red-700"
                      >
                        <Trash2 className="mr-2 h-4 w-4" />
                      </Link>
                    </Button>
                  </PermissionGuard>
                </TableCell>
              </TableRow>
            </React.Fragment>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}