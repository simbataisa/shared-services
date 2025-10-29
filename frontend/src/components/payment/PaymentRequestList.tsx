import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  CreditCard,
  Edit,
  Trash2,
  Eye,
  MoreHorizontal,
  Plus,
  Check,
  X,
  Ban
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { getStatusIcon } from "@/lib/status-utils";
import { paymentApi } from "@/lib/paymentApi";
import type { PaymentRequest, PaymentRequestStatus } from "@/types/payment";
import { PAYMENT_REQUEST_STATUS_MAPPINGS, PAYMENT_METHOD_TYPE_MAPPINGS } from "@/types/payment";
import SearchAndFilter from "@/components/common/SearchAndFilter";
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
import { Badge } from "@/components/ui/badge";

export default function PaymentRequestList() {
  const [paymentRequests, setPaymentRequests] = useState<PaymentRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchPaymentRequests();
  }, [currentPage, statusFilter]);

  const fetchPaymentRequests = async () => {
    try {
      setLoading(true);
      let response;
      
      if (statusFilter) {
        response = await paymentApi.requests.getByStatus(statusFilter as PaymentRequestStatus, currentPage - 1, 10);
      } else {
        response = await paymentApi.requests.getAll(currentPage - 1, 10);
      }
      
      setPaymentRequests(response.data.content || []);
      setTotalPages(response.data.totalPages || 1);
    } catch (error) {
      console.error("Error fetching payment requests:", error);
      setPaymentRequests([]);
    } finally {
      setLoading(false);
    }
  };

  const handleApproveRequest = async (id: string) => {
    try {
      await paymentApi.requests.approve(id);
      fetchPaymentRequests(); // Refresh the list
    } catch (error) {
      console.error("Error approving request:", error);
    }
  };

  const handleRejectRequest = async (id: string) => {
    try {
      await paymentApi.requests.reject(id);
      fetchPaymentRequests(); // Refresh the list
    } catch (error) {
      console.error("Error rejecting request:", error);
    }
  };

  const handleCancelRequest = async (id: string) => {
    try {
      await paymentApi.requests.cancel(id);
      fetchPaymentRequests(); // Refresh the list
    } catch (error) {
      console.error("Error cancelling request:", error);
    }
  };

  const filteredRequests = paymentRequests.filter((request) =>
    searchTerm === "" ||
    request.requestCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
    request.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
    request.requestorName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    request.requestorEmail.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const statusOptions = [
    { value: "all", label: "All Statuses" },
    { value: "PENDING", label: "Pending" },
    { value: "APPROVED", label: "Approved" },
    { value: "REJECTED", label: "Rejected" },
    { value: "CANCELLED", label: "Cancelled" },
  ];

  const getStatusColor = (status: PaymentRequestStatus) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "APPROVED":
        return "bg-green-100 text-green-800 border-green-200";
      case "REJECTED":
        return "bg-red-100 text-red-800 border-red-200";
      case "CANCELLED":
        return "bg-gray-100 text-gray-800 border-gray-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-10 w-32" />
        </div>
        <Card>
          <CardContent className="p-6">
            <div className="space-y-4">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Payment Requests</h1>
          <p className="text-muted-foreground">
            Manage and track payment requests ({totalElements} total)
          </p>
        </div>
      </div>

      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search by code, description, requestor..."
        filters={[
          {
            label: "Status",
            value: statusFilter,
            onChange: setStatusFilter,
            options: statusOptions,
            placeholder: "All Statuses",
            width: "200px"
          }
        ]}
        actions={
          <PermissionGuard permission="PAYMENT_MGMT:create">
            <Button asChild>
              <Link to="/payments/requests/new">
                <Plus className="mr-2 h-4 w-4" />
                New Request
              </Link>
            </Button>
          </PermissionGuard>
        }
      />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="h-5 w-5" />
            Payment Requests
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Request Code</TableHead>
                <TableHead>Requestor</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Payment Method</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Due Date</TableHead>
                <TableHead>Created</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredRequests.map((request) => (
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
                      <div className="font-medium">{request.requestorName}</div>
                      <div className="text-sm text-muted-foreground">
                        {request.requestorEmail}
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
                      {PAYMENT_METHOD_TYPE_MAPPINGS[request.paymentMethod]}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge className={getStatusColor(request.status)}>
                      {PAYMENT_REQUEST_STATUS_MAPPINGS[request.status]}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {request.dueDate ? formatDate(request.dueDate) : "No due date"}
                  </TableCell>
                  <TableCell>{formatDate(request.createdAt)}</TableCell>
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
                              <DropdownMenuItem onClick={() => handleApproveRequest(request.id.toString())}>
                                <Check className="mr-2 h-4 w-4" />
                                Approve
                              </DropdownMenuItem>
                            </PermissionGuard>
                            <PermissionGuard permission="PAYMENT_MGMT:verify">
                              <DropdownMenuItem onClick={() => handleRejectRequest(request.id.toString())}>
                                <X className="mr-2 h-4 w-4" />
                                Reject
                              </DropdownMenuItem>
                            </PermissionGuard>
                          </>
                        )}
                        
                        {(request.status === "PENDING" || request.status === "APPROVED") && (
                          <PermissionGuard permission="PAYMENT_MGMT:void">
                            <DropdownMenuItem onClick={() => handleCancelRequest(request.id.toString())}>
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

          {filteredRequests.length === 0 && (
            <div className="text-center py-8">
              <CreditCard className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-semibold text-gray-900">
                No payment requests found
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                {searchTerm || statusFilter !== "all"
                  ? "Try adjusting your search or filter criteria."
                  : "Get started by creating a new payment request."}
              </p>
              {(!searchTerm && statusFilter === "all") && (
                <div className="mt-6">
                  <PermissionGuard permission="PAYMENT_MGMT:create">
                    <Button asChild>
                      <Link to="/payments/requests/new">
                        <Plus className="mr-2 h-4 w-4" />
                        New Payment Request
                      </Link>
                    </Button>
                  </PermissionGuard>
                </div>
              )}
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between px-2 py-4">
              <div className="text-sm text-muted-foreground">
                Showing {currentPage * 10 + 1} to {Math.min((currentPage + 1) * 10, totalElements)} of {totalElements} results
              </div>
              <div className="flex items-center space-x-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                >
                  Previous
                </Button>
                <div className="flex items-center space-x-1">
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    const pageNum = Math.max(0, Math.min(totalPages - 5, currentPage - 2)) + i;
                    return (
                      <Button
                        key={pageNum}
                        variant={currentPage === pageNum ? "default" : "outline"}
                        size="sm"
                        onClick={() => setCurrentPage(pageNum)}
                      >
                        {pageNum + 1}
                      </Button>
                    );
                  })}
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                  disabled={currentPage >= totalPages - 1}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}