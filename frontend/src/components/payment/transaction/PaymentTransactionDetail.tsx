import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Hash, Clock, AlertTriangle } from "lucide-react";
import { paymentApi } from "@/lib/paymentApi";
import { DetailHeaderCard } from "@/components/common";
import PaymentTransactionDetailsCard from "./PaymentTransactionDetailsCard";
import { PaymentTrxStatusCard } from "./PaymentTrxStatusCard";
import { PaymentTrxStatsCard } from "./PaymentTrxStatsCard";
import type { PaymentTransaction } from "@/types/payment";

const PaymentTransactionDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [transaction, setTransaction] = useState<PaymentTransaction | null>(
    null
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchTransactionDetails();
    }
  }, [id]);

  const fetchTransactionDetails = async () => {
    if (!id) return;

    try {
      setLoading(true);
      setError(null);

      const response = await paymentApi.transactions.getById(id);
      if (response.success) {
        setTransaction(response.data);
      } else {
        setError("Transaction not found");
      }
    } catch (err) {
      console.error("Error fetching transaction details:", err);
      setError("Failed to load transaction details");
    } finally {
      setLoading(false);
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

  if (error || !transaction) {
    return (
      <div className="container mx-auto p-6">
        <Alert>
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>
            {error || "Transaction not found"}
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <DetailHeaderCard
        title={`Transaction ${transaction.transactionCode}`}
        description={`Amount: ${formatCurrency(
          transaction.amount,
          transaction.currency
        )}`}
        breadcrumbs={[
          { label: "Payment Transactions", href: "/payments/transactions" },
          { label: transaction.transactionCode },
        ]}
      />

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Information */}
        <div className="lg:col-span-2 space-y-6">
          {/* Transaction Details */}
          <PaymentTransactionDetailsCard transaction={transaction} />

          {/* Payment Request Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Hash className="h-5 w-5" />
                Related Payment Request
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium">{transaction.paymentRequestId}</p>
                  <p className="text-sm text-muted-foreground">
                    Payment Request ID
                  </p>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() =>
                    navigate(
                      `/payments/requests/${transaction.paymentRequestId}`
                    )
                  }
                >
                  View Request
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column - Sidebar */}
        <div className="space-y-6">
          {/* Status Summary */}
          <PaymentTrxStatusCard transaction={transaction} />

          {/* Statistics */}
          <PaymentTrxStatsCard transaction={transaction} />

          {/* Audit Information */}
          <Card>
            <CardHeader>
              <CardTitle>Audit Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label className="text-sm font-medium text-muted-foreground">
                  Created At
                </Label>
                <p className="text-sm">{formatDate(transaction.createdAt)}</p>
              </div>

              <div className="space-y-2">
                <Label className="text-sm font-medium text-muted-foreground">
                  Updated At
                </Label>
                <p className="text-sm">{formatDate(transaction.updatedAt)}</p>
              </div>

              {transaction.createdBy && (
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Created By
                  </Label>
                  <p className="text-sm">{transaction.createdBy}</p>
                </div>
              )}

              {transaction.updatedBy && (
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-muted-foreground">
                    Updated By
                  </Label>
                  <p className="text-sm">{transaction.updatedBy}</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default PaymentTransactionDetail;
