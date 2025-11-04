import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import {
  CheckCircle,
  XCircle,
  Ban,
  RefreshCw,
  DollarSign,
  History,
  AlertTriangle,
  Activity,
  Receipt,
  ArrowLeft,
} from "lucide-react";
import { paymentApi } from "@/lib/paymentApi";
import { DetailHeaderCard, StatisticsCard } from "@/components/common";
import { useAuth } from "@/store/auth";
// TODO: Create these components
// import { PaymentRefundDetailsCard } from './PaymentRefundDetailsCard';
// import { PaymentRefundStatusCard } from './PaymentRefundStatusCard';
import type {
  PaymentRefund,
  PaymentTransaction,
  PaymentAuditLog,
  PaymentTransactionStatus,
} from "@/types/payment";
import { PAYMENT_TRANSACTION_STATUS_MAPPINGS } from "@/types/payment";

const PaymentRefundDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();

  const [paymentRefund, setPaymentRefund] = useState<PaymentRefund | null>(
    null
  );
  const [paymentTransaction, setPaymentTransaction] =
    useState<PaymentTransaction | null>(null);
  const [auditLogs, setAuditLogs] = useState<PaymentAuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [auditLogsLoading, setAuditLogsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchPaymentRefundDetails();
    }
  }, [id]);

  const fetchPaymentRefundDetails = async () => {
    if (!id) return;

    try {
      setLoading(true);
      setError(null);
      setAuditLogsLoading(true);

      console.log("Fetching payment refund details for ID:", id);
      // Fetch payment refund details
      const refundResponse = await paymentApi.refunds.getById(id);
      if (refundResponse.success) {
        setPaymentRefund(refundResponse.data);

        // Fetch related payment transaction
        if (refundResponse.data.paymentTransactionId) {
          const transactionResponse = await paymentApi.transactions.getById(
            refundResponse.data.paymentTransactionId
          );
          if (transactionResponse.success) {
            setPaymentTransaction(transactionResponse.data);
          }
        }
      }

      // Fetch audit logs for this refund
      const auditLogsResponse = await paymentApi.auditLogs.getByRefund(id);
      if (auditLogsResponse.success) {
        setAuditLogs(auditLogsResponse.data.content || []);
      }
      setAuditLogsLoading(false);
    } catch (err) {
      console.error("Error fetching payment refund details:", err);
      setError("Failed to load payment refund details");
      setAuditLogsLoading(false);
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (action: "cancel" | "retry", refundId: string) => {
    try {
      setActionLoading(action);

      let response;
      switch (action) {
        case "cancel":
          response = await paymentApi.refunds.cancel(refundId);
          break;
        case "retry":
          // TODO: Implement retry endpoint when available
          console.log("Retry action not yet implemented in backend");
          setActionLoading(null);
          return;
      }

      if (response.success) {
        setSuccess(`Payment refund ${action}ed successfully`);
        setError(null);
        fetchPaymentRefundDetails(); // Refresh data
      } else {
        setError(`Failed to ${action} payment refund`);
        setSuccess(null);
      }
    } catch (err) {
      console.error(`Error ${action}ing payment refund:`, err);
      setError(`Failed to ${action} payment refund`);
      setSuccess(null);
    } finally {
      setActionLoading(null);
    }
  };

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: currency,
    }).format(amount);
  };

  const formatDate = (dateString: string | Date) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="space-y-6">
          <div className="h-8 bg-gray-200 rounded animate-pulse" />
          <div className="h-64 bg-gray-200 rounded animate-pulse" />
          <div className="h-32 bg-gray-200 rounded animate-pulse" />
        </div>
      </div>
    );
  }

  if (error || !paymentRefund) {
    return (
      <div className="container mx-auto p-6">
        <Alert>
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>
            {error || "Payment refund not found"}
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Success/Error Messages */}
      {success && (
        <Alert className="border-green-200 bg-green-50 mb-6">
          <CheckCircle className="h-4 w-4 text-green-600" />
          <AlertDescription className="text-green-800">
            {success}
          </AlertDescription>
        </Alert>
      )}

      {error && (
        <Alert className="border-red-200 bg-red-50 mb-6">
          <AlertTriangle className="h-4 w-4 text-red-600" />
          <AlertDescription className="text-red-800">{error}</AlertDescription>
        </Alert>
      )}

      {/* Header */}
      <DetailHeaderCard
        title={`Refund ${paymentRefund.refundCode}`}
        description={`Refund Amount: ${formatCurrency(
          paymentRefund.refundAmount,
          paymentRefund.currency
        )}`}
        breadcrumbs={[
          ...(hasPermission("PAYMENT_MGMT:read")
            ? [{ label: "Payment Refunds", href: "/payments/refunds" }]
            : []),
          { label: `Refund ${paymentRefund.refundCode}` },
        ]}
      />

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Information */}
        <div className="lg:col-span-2 space-y-6">
          {/* Payment Refund Details */}
          <Card>
            <CardHeader>
              <CardTitle>Refund Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Refund Code
                  </Label>
                  <p className="text-sm">{paymentRefund.refundCode}</p>
                </div>
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Status
                  </Label>
                  <Badge
                    variant={
                      paymentRefund.refundStatus === "SUCCESS"
                        ? "default"
                        : "secondary"
                    }
                  >
                    {
                      PAYMENT_TRANSACTION_STATUS_MAPPINGS[
                        paymentRefund.refundStatus
                      ]
                    }
                  </Badge>
                </div>
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Amount
                  </Label>
                  <p className="text-sm font-bold">
                    {formatCurrency(
                      paymentRefund.refundAmount,
                      paymentRefund.currency
                    )}
                  </p>
                </div>
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Currency
                  </Label>
                  <p className="text-sm">{paymentRefund.currency}</p>
                </div>
                <div className="space-y-2 col-span-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Reason
                  </Label>
                  <p className="text-sm">{paymentRefund.reason}</p>
                </div>
                {paymentRefund.externalRefundId && (
                  <div className="space-y-2 col-span-2">
                    <Label className="text-sm font-medium text-muted-foreground">
                      External Refund ID
                    </Label>
                    <p className="text-sm font-mono">
                      {paymentRefund.externalRefundId}
                    </p>
                  </div>
                )}
                {paymentRefund.gatewayName && (
                  <div className="space-y-2">
                    <Label className="text-sm font-medium text-muted-foreground">
                      Gateway
                    </Label>
                    <p className="text-sm">{paymentRefund.gatewayName}</p>
                  </div>
                )}
                {paymentRefund.errorCode && (
                  <div className="space-y-2">
                    <Label className="text-sm font-medium text-muted-foreground">
                      Error Code
                    </Label>
                    <p className="text-sm text-red-600">
                      {paymentRefund.errorCode}
                    </p>
                  </div>
                )}
                {paymentRefund.errorMessage && (
                  <div className="space-y-2 col-span-2">
                    <Label className="text-sm font-medium text-muted-foreground">
                      Error Message
                    </Label>
                    <p className="text-sm text-red-600">
                      {paymentRefund.errorMessage}
                    </p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Related Records */}
          <Card>
            <CardHeader>
              <CardTitle>Related Records</CardTitle>
            </CardHeader>
            <CardContent>
              <Tabs defaultValue="transaction" className="w-full">
                <TabsList className="grid w-full grid-cols-2">
                  <TabsTrigger value="transaction">
                    <DollarSign className="h-4 w-4 mr-2" />
                    Original Transaction
                  </TabsTrigger>
                  <TabsTrigger value="audit">
                    <History className="h-4 w-4 mr-2" />
                    Audit Log ({auditLogs.length})
                  </TabsTrigger>
                </TabsList>

                <TabsContent value="transaction" className="mt-4">
                  {paymentTransaction ? (
                    <div className="border rounded-lg p-4">
                      <div className="flex justify-between items-start mb-4">
                        <div>
                          <h3 className="font-medium">
                            {paymentTransaction.transactionCode}
                          </h3>
                          <p className="text-sm text-muted-foreground">
                            {formatCurrency(
                              paymentTransaction.amount,
                              paymentTransaction.currency
                            )}
                          </p>
                        </div>
                        <Badge
                          variant={
                            paymentTransaction.transactionStatus === "SUCCESS"
                              ? "default"
                              : "secondary"
                          }
                        >
                          {
                            PAYMENT_TRANSACTION_STATUS_MAPPINGS[
                              paymentTransaction.transactionStatus
                            ]
                          }
                        </Badge>
                      </div>
                      <div className="space-y-2">
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">
                            Payment Method:
                          </span>
                          <span className="text-sm">
                            {paymentTransaction.paymentMethod}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">
                            Gateway:
                          </span>
                          <span className="text-sm">
                            {paymentTransaction.gatewayName || "N/A"}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">
                            Processed At:
                          </span>
                          <span className="text-sm">
                            {paymentTransaction.processedAt
                              ? formatDate(paymentTransaction.processedAt)
                              : "N/A"}
                          </span>
                        </div>
                      </div>
                      <div className="mt-4">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            navigate(
                              `/payments/transactions/${paymentTransaction.id}`
                            )
                          }
                        >
                          <Receipt className="h-4 w-4 mr-2" />
                          View Transaction Details
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <p className="text-muted-foreground">
                      Transaction details not available.
                    </p>
                  )}
                </TabsContent>

                <TabsContent value="audit" className="mt-4">
                  {auditLogsLoading ? (
                    <div className="space-y-2">
                      {[...Array(3)].map((_, i) => (
                        <Skeleton key={i} className="h-16 w-full" />
                      ))}
                    </div>
                  ) : auditLogs.length > 0 ? (
                    <div className="space-y-2">
                      {auditLogs.map((log) => (
                        <div key={log.id} className="border rounded-lg p-4">
                          <div className="flex justify-between items-start">
                            <div>
                              <p className="font-medium">{log.action}</p>
                              <p className="text-sm text-muted-foreground">
                                {formatDate(log.createdAt)}
                              </p>
                            </div>
                            <Badge variant="outline">{log.action}</Badge>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-muted-foreground">
                      No audit logs found.
                    </p>
                  )}
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>
        </div>

        {/* Right Column - Sidebar */}
        <div className="space-y-6">
          {/* Status Card */}
          <Card>
            <CardHeader>
              <CardTitle>Refund Actions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label className="text-sm font-medium text-muted-foreground">
                  Current Status
                </Label>
                <Badge
                  variant={
                    paymentRefund.refundStatus === "SUCCESS"
                      ? "default"
                      : "secondary"
                  }
                >
                  {
                    PAYMENT_TRANSACTION_STATUS_MAPPINGS[
                      paymentRefund.refundStatus
                    ]
                  }
                </Badge>
              </div>

              {paymentRefund.refundStatus === "PENDING" && (
                <div className="space-y-2">
                  <Button
                    variant="destructive"
                    size="sm"
                    onClick={() => handleAction("cancel", paymentRefund.id)}
                    disabled={actionLoading === "cancel"}
                    className="w-full"
                  >
                    {actionLoading === "cancel" ? (
                      <>
                        <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                        Cancelling...
                      </>
                    ) : (
                      <>
                        <Ban className="h-4 w-4 mr-2" />
                        Cancel Refund
                      </>
                    )}
                  </Button>
                </div>
              )}

              {paymentRefund.refundStatus === "FAILED" && (
                <div className="space-y-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleAction("retry", paymentRefund.id)}
                    disabled={actionLoading === "retry"}
                    className="w-full"
                  >
                    {actionLoading === "retry" ? (
                      <>
                        <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                        Retrying...
                      </>
                    ) : (
                      <>
                        <RefreshCw className="h-4 w-4 mr-2" />
                        Retry Refund
                      </>
                    )}
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Statistics Card */}
          <Card>
            <CardHeader>
              <CardTitle>Refund Statistics</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label className="text-sm font-medium text-muted-foreground">
                  Refund Amount
                </Label>
                <p className="text-2xl font-bold">
                  {formatCurrency(
                    paymentRefund.refundAmount,
                    paymentRefund.currency
                  )}
                </p>
              </div>

              <div className="space-y-2">
                <Label className="text-sm font-medium text-muted-foreground">
                  Status
                </Label>
                <Badge
                  variant={
                    paymentRefund.refundStatus === "SUCCESS"
                      ? "default"
                      : "secondary"
                  }
                >
                  {
                    PAYMENT_TRANSACTION_STATUS_MAPPINGS[
                      paymentRefund.refundStatus
                    ]
                  }
                </Badge>
              </div>

              {paymentRefund.processedAt && (
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Processed At
                  </Label>
                  <p className="text-sm">
                    {formatDate(paymentRefund.processedAt)}
                  </p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Audit Information Card */}
          <Card>
            <CardHeader>
              <CardTitle>Audit Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label className="text-sm font-medium text-muted-foreground">
                  Created At
                </Label>
                <p className="text-sm">{formatDate(paymentRefund.createdAt)}</p>
              </div>

              <div className="space-y-2">
                <Label className="text-sm font-medium text-muted-foreground">
                  Updated At
                </Label>
                <p className="text-sm">{formatDate(paymentRefund.updatedAt)}</p>
              </div>

              {paymentRefund.createdBy && (
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Created By
                  </Label>
                  <p className="text-sm">{paymentRefund.createdBy}</p>
                </div>
              )}

              {paymentRefund.updatedBy && (
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Updated By
                  </Label>
                  <p className="text-sm">{paymentRefund.updatedBy}</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default PaymentRefundDetail;
