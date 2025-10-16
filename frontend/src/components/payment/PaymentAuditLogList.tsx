import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Eye, FileText, User, Clock } from "lucide-react";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { paymentApi } from "@/lib/paymentApi";
import type { PaymentAuditLog } from "@/types/payment";

const PaymentAuditLogList: React.FC = () => {
  const [auditLogs, setAuditLogs] = useState<PaymentAuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [actionFilter, setActionFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const pageSize = 10;

  const actionOptions = [
    { value: "", label: "All Actions" },
    { value: "CREATE", label: "Create" },
    { value: "UPDATE", label: "Update" },
    { value: "DELETE", label: "Delete" },
    { value: "APPROVE", label: "Approve" },
    { value: "REJECT", label: "Reject" },
    { value: "CANCEL", label: "Cancel" },
    { value: "PROCESS", label: "Process" },
    { value: "REFUND", label: "Refund" },
    { value: "RETRY", label: "Retry" }
  ];

  const fetchAuditLogs = async () => {
    try {
      setLoading(true);
      const response = await paymentApi.auditLogs.getAll(currentPage - 1, 10);
      setAuditLogs(response.data.content || []);
      setTotalPages(response.data.totalPages || 1);
    } catch (error) {
      console.error("Error fetching audit logs:", error);
      setAuditLogs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAuditLogs();
  }, [currentPage, searchTerm, actionFilter]);

  const getActionColor = (action: string): string => {
    switch (action.toUpperCase()) {
      case "CREATE": return "bg-green-100 text-green-800";
      case "UPDATE": return "bg-blue-100 text-blue-800";
      case "DELETE": return "bg-red-100 text-red-800";
      case "APPROVE": return "bg-emerald-100 text-emerald-800";
      case "REJECT": return "bg-orange-100 text-orange-800";
      case "CANCEL": return "bg-gray-100 text-gray-800";
      case "PROCESS": return "bg-purple-100 text-purple-800";
      case "REFUND": return "bg-yellow-100 text-yellow-800";
      case "RETRY": return "bg-indigo-100 text-indigo-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading && auditLogs.length === 0) {
    return (
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Payment Audit Logs</CardTitle>
          </CardHeader>
          <CardContent>
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
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Payment Audit Logs
          </CardTitle>
        </CardHeader>
        <CardContent>
          <SearchAndFilter
            searchTerm={searchTerm}
            onSearchChange={setSearchTerm}
            searchPlaceholder="Search by action, user, IP address..."
            filters={[
              {
                label: "Action",
                value: actionFilter,
                onChange: setActionFilter,
                options: actionOptions,
                placeholder: "All Actions",
                width: "200px"
              }
            ]}
          />
        </CardContent>
      </Card>

      <div className="grid gap-4">
        {auditLogs.map((log) => (
          <Card key={log.id} className="hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <FileText className="h-4 w-4" />
                    <div>
                      <div className="flex items-center space-x-2">
                        <Badge className={getActionColor(log.action)}>
                          {log.action}
                        </Badge>
                        <span className="text-sm font-medium">#{log.id}</span>
                      </div>
                      <p className="text-sm text-muted-foreground mt-1">
                        {log.username ? `by ${log.username}` : 'System action'}
                      </p>
                    </div>
                  </div>
                  
                  <div className="text-center">
                    <div className="flex items-center space-x-2 text-sm text-muted-foreground">
                      <Clock className="h-4 w-4" />
                      <span>{formatDateTime(log.createdAt)}</span>
                    </div>
                    {log.ipAddress && (
                      <p className="text-xs text-muted-foreground mt-1">
                        IP: {log.ipAddress}
                      </p>
                    )}
                  </div>
                </div>

                <div className="flex items-center space-x-4">
                  <div className="text-right">
                    <div className="flex flex-col space-y-1">
                      {log.paymentRequestId && (
                        <Badge variant="outline" className="text-xs">
                          Request: {log.paymentRequestId}
                        </Badge>
                      )}
                      {log.paymentTransactionId && (
                        <Badge variant="outline" className="text-xs">
                          Transaction: {log.paymentTransactionId}
                        </Badge>
                      )}
                      {log.paymentRefundId && (
                        <Badge variant="outline" className="text-xs">
                          Refund: {log.paymentRefundId}
                        </Badge>
                      )}
                    </div>
                  </div>

                  <div className="flex space-x-2">
                    <PermissionGuard permission="payment:audit:read">
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4" />
                      </Button>
                    </PermissionGuard>
                  </div>
                </div>
              </div>

              {(log.oldValues || log.newValues) && (
                <div className="mt-4 pt-4 border-t">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {log.oldValues && Object.keys(log.oldValues).length > 0 && (
                      <div>
                        <p className="text-sm font-medium text-muted-foreground mb-2">Old Values</p>
                        <div className="bg-red-50 p-3 rounded-md">
                          <pre className="text-xs text-red-800 whitespace-pre-wrap">
                            {JSON.stringify(log.oldValues, null, 2)}
                          </pre>
                        </div>
                      </div>
                    )}
                    {log.newValues && Object.keys(log.newValues).length > 0 && (
                      <div>
                        <p className="text-sm font-medium text-muted-foreground mb-2">New Values</p>
                        <div className="bg-green-50 p-3 rounded-md">
                          <pre className="text-xs text-green-800 whitespace-pre-wrap">
                            {JSON.stringify(log.newValues, null, 2)}
                          </pre>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        ))}
      </div>

      {auditLogs.length === 0 && !loading && (
        <Card>
          <CardContent className="text-center py-8">
            <FileText className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground">No audit logs found</p>
          </CardContent>
        </Card>
      )}

      {totalPages > 1 && (
        <div className="flex justify-center space-x-2">
          <Button
            variant="outline"
            onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
            disabled={currentPage === 1}
          >
            Previous
          </Button>
          <span className="flex items-center px-4">
            Page {currentPage} of {totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
            disabled={currentPage === totalPages}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
};

export default PaymentAuditLogList;