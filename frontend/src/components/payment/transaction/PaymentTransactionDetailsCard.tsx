import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { 
  Receipt,
  CreditCard,
  Building,
  CheckCircle, 
  XCircle, 
  AlertTriangle,
  Clock
} from 'lucide-react';
import type { PaymentTransaction } from '@/types/payment';

interface PaymentTransactionDetailsCardProps {
  transaction: PaymentTransaction;
}

const PaymentTransactionDetailsCard: React.FC<PaymentTransactionDetailsCardProps> = ({ transaction }) => {
  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SUCCESS':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'FAILED':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'PROCESSING':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'SUCCESS':
        return <CheckCircle className="h-4 w-4" />;
      case 'FAILED':
        return <XCircle className="h-4 w-4" />;
      case 'PENDING':
      case 'PROCESSING':
        return <Clock className="h-4 w-4" />;
      default:
        return <AlertTriangle className="h-4 w-4" />;
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Receipt className="h-5 w-5" />
          Transaction Details
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2">
            <Label className="text-sm font-medium text-muted-foreground">Transaction Code</Label>
            <p className="text-sm font-mono">{transaction.transactionCode}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-sm font-medium text-muted-foreground">Amount</Label>
            <p className="text-lg font-semibold">
              {formatCurrency(transaction.amount, transaction.currency)}
            </p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-sm font-medium text-muted-foreground">Payment Method</Label>
            <div className="flex items-center gap-2">
              <CreditCard className="h-4 w-4" />
              <span className="text-sm">{transaction.paymentMethod}</span>
            </div>
          </div>
          
          <div className="space-y-2">
            <Label className="text-sm font-medium text-muted-foreground">Payment Gateway</Label>
            <div className="flex items-center gap-2">
              <Building className="h-4 w-4" />
              <span className="text-sm">{transaction.gatewayName || 'N/A'}</span>
            </div>
          </div>
          
          <div className="space-y-2">
            <Label className="text-sm font-medium text-muted-foreground">Transaction Type</Label>
            <Badge variant="outline">{transaction.transactionType}</Badge>
          </div>
          
          <div className="space-y-2">
            <Label className="text-sm font-medium text-muted-foreground">Status</Label>
            <Badge className={getStatusColor(transaction.transactionStatus)}>
              {getStatusIcon(transaction.transactionStatus)}
              <span className="ml-1">{transaction.transactionStatus}</span>
            </Badge>
          </div>
          
          {transaction.gatewayTransactionId && (
            <div className="space-y-2">
              <Label className="text-sm font-medium text-muted-foreground">Gateway Transaction ID</Label>
              <p className="text-sm font-mono">{transaction.gatewayTransactionId}</p>
            </div>
          )}
          
          {transaction.gatewayResponse && (
            <div className="space-y-2 md:col-span-2">
              <Label className="text-sm font-medium text-muted-foreground">Gateway Response</Label>
              <div className="bg-gray-50 border rounded-md p-3">
                <pre className="text-xs text-gray-700 whitespace-pre-wrap overflow-x-auto">
                  {JSON.stringify(transaction.gatewayResponse, null, 2)}
                </pre>
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default PaymentTransactionDetailsCard;