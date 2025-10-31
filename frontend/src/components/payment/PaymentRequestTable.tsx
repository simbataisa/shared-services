import { Link } from "react-router-dom";
import {
  Edit,
  Trash2,
  Eye,
  MoreHorizontal,
  Check,
  X,
  Ban,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { getStatusIcon, getPaymentRequestStatusColor } from "@/lib/status-utils";
import { formatCurrency, formatDate } from "@/lib/utils";
import type { PaymentRequest, PaymentRequestStatus } from "@/types/payment";
import { PAYMENT_REQUEST_STATUS_MAPPINGS, PAYMENT_METHOD_TYPE_MAPPINGS } from "@/types/payment";

interface PaymentRequestTableProps {
  requests: PaymentRequest[];
  getPaymentRequestStatusColor: (status: PaymentRequestStatus) => string;
  onApproveRequest: (id: string) => void;
  onRejectRequest: (id: string) => void;
  onCancelRequest: (id: string) => void;
  currentPage: number;
  totalPages: number;
  totalElements: number;
  onPageChange: (page: number) => void;
}

export function PaymentRequestTable({
  requests,
  getPaymentRequestStatusColor,
  onApproveRequest,
  onRejectRequest,
  onCancelRequest,
  currentPage,
  totalPages,
  totalElements,
  onPageChange,
}: PaymentRequestTableProps) {
  return (
    <div>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Request Code</TableHead>
            <TableHead>Payer</TableHead>
            <TableHead>Amount</TableHead>
            <TableHead>Payment Method</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Expires At</TableHead>
            <TableHead>Created</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {requests.map((request) => (
            <TableRow key={request.id}>
              <TableCell className="font-medium">
                <Link
                  to={`/payments/requests/${request.id}`}
                  className="text-blue-600 hover:text-blue-800 hover:underline"
                >
                  {request.requestCode}
                </Link>
              </TableCell>
              <TableCell>
                <div>
                  <div className="font-medium">{request.payerName}</div>
                  <div className="text-sm text-muted-foreground">
                    {request.payerEmail}
                  </div>
                </div>
              </TableCell>
              <TableCell>
                <div className="font-medium">
                   {formatCurrency(request.amount, request.currency)}
                 </div>
              </TableCell>
              <TableCell>
                <Badge variant="outline">
                  {PAYMENT_METHOD_TYPE_MAPPINGS[request.preSelectedPaymentMethod]}
                </Badge>
              </TableCell>
              <TableCell>
                <Badge className={getPaymentRequestStatusColor(request.status)}>
                  {PAYMENT_REQUEST_STATUS_MAPPINGS[request.status]}
                </Badge>
              </TableCell>
              <TableCell>
                {request.expiresAt ? formatDate(request.expiresAt.toString()) : "No expiry"}
              </TableCell>
              <TableCell>{formatDate(request.createdAt.toString())}</TableCell>
              <TableCell className="text-right">
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" className="h-8 w-8 p-0">
                      <MoreHorizontal className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem asChild>
                      <Link to={`/payments/requests/${request.id}`}>
                        <Eye className="mr-2 h-4 w-4" />
                        View Details
                      </Link>
                    </DropdownMenuItem>
                    
                    {request.status === "PENDING" && (
                      <>
                        <PermissionGuard permission="PAYMENT_MGMT:verify">
                          <DropdownMenuItem onClick={() => onApproveRequest(request.id.toString())}>
                            <Check className="mr-2 h-4 w-4" />
                            Approve
                          </DropdownMenuItem>
                        </PermissionGuard>
                        <PermissionGuard permission="PAYMENT_MGMT:verify">
                          <DropdownMenuItem onClick={() => onRejectRequest(request.id.toString())}>
                            <X className="mr-2 h-4 w-4" />
                            Reject
                          </DropdownMenuItem>
                        </PermissionGuard>
                      </>
                    )}
                    
                    {(request.status === "PENDING" || request.status === "APPROVED") && (
                      <PermissionGuard permission="PAYMENT_MGMT:void">
                        <DropdownMenuItem onClick={() => onCancelRequest(request.id.toString())}>
                          <Ban className="mr-2 h-4 w-4" />
                          Cancel
                        </DropdownMenuItem>
                      </PermissionGuard>
                    )}
                    
                    <PermissionGuard permission="PAYMENT_MGMT:update">
                      <DropdownMenuItem asChild>
                        <Link to={`/payments/requests/${request.id}/edit`}>
                          <Edit className="mr-2 h-4 w-4" />
                          Edit
                        </Link>
                      </DropdownMenuItem>
                    </PermissionGuard>
                  </DropdownMenuContent>
                </DropdownMenu>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between px-2 py-4">
          <div className="text-sm text-muted-foreground">
            Showing {(currentPage - 1) * 10 + 1} to {Math.min(currentPage * 10, totalElements)} of {totalElements} results
          </div>
          <div className="flex items-center space-x-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPageChange(Math.max(1, currentPage - 1))}
              disabled={currentPage === 1}
            >
              Previous
            </Button>
            <div className="flex items-center space-x-1">
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const pageNum = Math.max(1, Math.min(totalPages - 4, currentPage - 2)) + i;
                return (
                  <Button
                    key={pageNum}
                    variant={currentPage === pageNum ? "default" : "outline"}
                    size="sm"
                    onClick={() => onPageChange(pageNum)}
                  >
                    {pageNum}
                  </Button>
                );
              })}
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPageChange(Math.min(totalPages, currentPage + 1))}
              disabled={currentPage >= totalPages}
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}