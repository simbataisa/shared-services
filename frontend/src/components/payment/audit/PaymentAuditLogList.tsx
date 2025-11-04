import React, { useState, useEffect, useMemo } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { FileText, Receipt, Undo2 } from "lucide-react";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import { paymentApi } from "@/lib/paymentApi";
import type { PaymentAuditLog } from "@/types/payment";
import type { TableFilter } from "@/types/components";
import { PaymentAuditLogTable } from "./PaymentAuditLogTable";
import { useNavigate } from "react-router-dom";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

const PaymentAuditLogList: React.FC = () => {
  const [auditLogs, setAuditLogs] = useState<PaymentAuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [actionFilter, setActionFilter] = useState("all");
  const navigate = useNavigate();

  // Dialog state for informative and predictable navigation
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogLog, setDialogLog] = useState<PaymentAuditLog | null>(null);
  const [dialogMode, setDialogMode] = useState<"choose" | "info">("info");

  const actionOptions = [
    { value: "all", label: "All Actions" },
    { value: "CREATE", label: "Create" },
    { value: "UPDATE", label: "Update" },
    { value: "DELETE", label: "Delete" },
    { value: "APPROVE", label: "Approve" },
    { value: "REJECT", label: "Reject" },
    { value: "CANCEL", label: "Cancel" },
    { value: "PROCESS", label: "Process" },
    { value: "REFUND", label: "Refund" },
    { value: "RETRY", label: "Retry" },
  ];

  const fetchAuditLogs = async () => {
    try {
      setLoading(true);
      const response = await paymentApi.auditLogs.getAll(0, 100);
      const logs = response.data.content || [];
      setAuditLogs(logs);
    } catch (error) {
      console.error("Error fetching audit logs:", error);
      setAuditLogs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAuditLogs();
  }, []);

  const getActionColor = (action: string): string => {
    switch (action.toUpperCase()) {
      case "CREATE":
        return "bg-green-100 text-green-800";
      case "UPDATE":
        return "bg-blue-100 text-blue-800";
      case "DELETE":
        return "bg-red-100 text-red-800";
      case "APPROVE":
        return "bg-emerald-100 text-emerald-800";
      case "REJECT":
        return "bg-orange-100 text-orange-800";
      case "CANCEL":
        return "bg-gray-100 text-gray-800";
      case "PROCESS":
        return "bg-purple-100 text-purple-800";
      case "REFUND":
        return "bg-yellow-100 text-yellow-800";
      case "RETRY":
        return "bg-indigo-100 text-indigo-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const filteredData = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();
    let data = [...auditLogs];

    if (actionFilter && actionFilter !== "all") {
      data = data.filter((log) => log.action === actionFilter);
    }

    if (normalizedSearch) {
      data = data.filter((log) => {
        const fields = [
          String(log.id || ""),
          String(log.action || ""),
          String(log.username || ""),
          String(log.ipAddress || ""),
          String(log.paymentRequestId || ""),
          String(log.paymentTransactionId || ""),
          String(log.paymentRefundId || ""),
        ];
        return fields.some((f) => f.toLowerCase().includes(normalizedSearch));
      });
    }

    return data;
  }, [auditLogs, searchTerm, actionFilter]);

  const filters: TableFilter[] = [
    {
      label: "Action",
      value: actionFilter,
      onChange: setActionFilter,
      options: actionOptions,
      placeholder: "All Actions",
      width: "200px",
    },
  ];

  const actions = <div className="flex gap-2"></div>;

  const handleViewLog = (log: PaymentAuditLog) => {
    // Build related navigation targets deterministically
    const targets: { label: string; path: string; icon: React.ReactNode }[] = [];
    if (log.paymentRefundId) {
      targets.push({
        label: "View Refund",
        path: `/payments/refunds/${log.paymentRefundId}`,
        icon: <Undo2 className="h-4 w-4" />,
      });
    }
    if (log.paymentTransactionId) {
      targets.push({
        label: "View Transaction",
        path: `/payments/transactions/${log.paymentTransactionId}`,
        icon: <Receipt className="h-4 w-4" />,
      });
    }
    if (log.paymentRequestId) {
      targets.push({
        label: "View Request",
        path: `/payments/requests/${log.paymentRequestId}`,
        icon: <FileText className="h-4 w-4" />,
      });
    }

    if (targets.length === 1) {
      // Predictable: direct navigation when exactly one related entity
      navigate(targets[0].path);
      return;
    }

    // Informative: open a dialog to choose when multiple or none
    setDialogLog(log);
    setDialogMode(targets.length > 1 ? "choose" : "info");
    setDialogOpen(true);
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
    <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 space-y-4 sm:space-y-0">
        <div className="space-y-1">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight flex items-center gap-2">
            <FileText className="h-8 w-8" />
            Payment Audit Logs
          </h1>
          <p className="text-sm sm:text-base text-muted-foreground">
            Manage payment audit logs and their actions
          </p>
        </div>
      </div>

      <PaymentAuditLogTable
        data={filteredData}
        loading={loading}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search by action, user, IP..."
        filters={filters}
        actions={actions}
        onViewLog={handleViewLog}
      />

      {/* Informative dialog for multiple/no related entities */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            {dialogMode === "choose" ? (
              <DialogTitle>Select Related Entity</DialogTitle>
            ) : (
              <DialogTitle>Audit Log Details</DialogTitle>
            )}
            <DialogDescription>
              {dialogMode === "choose"
                ? "This audit log is linked to multiple entities. Choose where to navigate."
                : "No related payment entity is attached to this audit log."}
            </DialogDescription>
          </DialogHeader>

          {dialogMode === "info" && dialogLog && (
            <div className="space-y-2 text-sm">
              <div className="flex items-center gap-2">
                <span className="font-medium">Action:</span>
                <span className="badge badge-outline px-2 py-0.5 text-xs">
                  {dialogLog.action}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-medium">Log ID:</span>
                <span className="font-mono">{dialogLog.id}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-medium">User:</span>
                <span>{dialogLog.username || "System"}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-medium">IP Address:</span>
                <span>{dialogLog.ipAddress || "-"}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-medium">Created At:</span>
                <span>{formatDateTime(dialogLog.createdAt)}</span>
              </div>
            </div>
          )}

          {dialogMode === "choose" && dialogLog && (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
              {dialogLog.paymentRefundId && (
                <Button
                  variant="outline"
                  onClick={() => {
                    setDialogOpen(false);
                    navigate(`/payments/refunds/${dialogLog.paymentRefundId}`);
                  }}
                >
                  <Undo2 className="h-4 w-4 mr-2" /> Refund
                </Button>
              )}
              {dialogLog.paymentTransactionId && (
                <Button
                  variant="outline"
                  onClick={() => {
                    setDialogOpen(false);
                    navigate(
                      `/payments/transactions/${dialogLog.paymentTransactionId}`
                    );
                  }}
                >
                  <Receipt className="h-4 w-4 mr-2" /> Transaction
                </Button>
              )}
              {dialogLog.paymentRequestId && (
                <Button
                  variant="outline"
                  onClick={() => {
                    setDialogOpen(false);
                    navigate(`/payments/requests/${dialogLog.paymentRequestId}`);
                  }}
                >
                  <FileText className="h-4 w-4 mr-2" /> Request
                </Button>
              )}
            </div>
          )}

          <DialogFooter>
            <Button variant="secondary" onClick={() => setDialogOpen(false)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {auditLogs.length === 0 && !loading && (
        <Card>
          <CardContent className="text-center py-8">
            <FileText className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground">No audit logs found</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default PaymentAuditLogList;
