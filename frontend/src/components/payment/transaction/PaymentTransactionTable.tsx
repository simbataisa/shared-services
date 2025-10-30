import React from 'react';
import { Link } from 'react-router-dom';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Skeleton } from '@/components/ui/skeleton';
import { Receipt, Eye } from 'lucide-react';
import type { PaymentTransaction } from '@/types/payment';
import { 
  PAYMENT_TRANSACTION_STATUS_MAPPINGS,
  PAYMENT_TRANSACTION_TYPE_MAPPINGS,
  PAYMENT_METHOD_TYPE_MAPPINGS 
} from '@/types/payment';
import { getTransactionStatusBadgeProps } from '@/lib/status-utils';

interface PaymentTransactionTableProps {
  transactions: PaymentTransaction[];
  loading?: boolean;
  showActions?: boolean;
  emptyMessage?: string;
}

// Utility functions
const formatCurrency = (amount: number, currency: string) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency || 'USD'
  }).format(amount);
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

export const PaymentTransactionTable: React.FC<PaymentTransactionTableProps> = ({
  transactions,
  loading = false,
  showActions = false,
  emptyMessage = "No transactions found."
}) => {
  if (loading) {
    return (
      <div className="space-y-2">
        {[...Array(3)].map((_, i) => (
          <Skeleton key={i} className="h-16 w-full" />
        ))}
      </div>
    );
  }

  if (transactions.length === 0) {
    return (
      <div className="text-center py-8">
        <Receipt className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
        <p className="text-muted-foreground">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="border rounded-lg overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Transaction Code</TableHead>
            <TableHead>Amount</TableHead>
            <TableHead>Payment Method</TableHead>
            <TableHead>Gateway</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Processed At</TableHead>
            <TableHead>Retry Count</TableHead>
            {showActions && <TableHead>Actions</TableHead>}
          </TableRow>
        </TableHeader>
        <TableBody>
          {transactions.map((transaction) => {
            const statusProps = getTransactionStatusBadgeProps(transaction.transactionStatus);
            
            return (
              <TableRow key={transaction.id}>
                <TableCell>
                  <div>
                    <p className="font-medium text-sm">{transaction.transactionCode}</p>
                    <p className="text-xs text-muted-foreground">{transaction.id}</p>
                  </div>
                </TableCell>
                <TableCell>
                  <div>
                    <p className="font-medium">
                      {formatCurrency(transaction.amount, transaction.currency)}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {PAYMENT_TRANSACTION_TYPE_MAPPINGS[transaction.transactionType]}
                    </p>
                  </div>
                </TableCell>
                <TableCell>
                  <Badge variant="outline">
                    {PAYMENT_METHOD_TYPE_MAPPINGS[transaction.paymentMethod]}
                  </Badge>
                </TableCell>
                <TableCell>
                  <div>
                    <p className="text-sm">{transaction.gatewayName || 'N/A'}</p>
                    {transaction.gatewayTransactionId && (
                      <p className="text-xs text-muted-foreground">
                        ID: {transaction.gatewayTransactionId}
                      </p>
                    )}
                  </div>
                </TableCell>
                <TableCell>
                  <Badge {...statusProps}>
                    {PAYMENT_TRANSACTION_STATUS_MAPPINGS[transaction.transactionStatus]}
                  </Badge>
                </TableCell>
                <TableCell>
                  <div>
                    {transaction.processedAt ? (
                      <p className="text-sm">{formatDate(transaction.processedAt)}</p>
                    ) : (
                      <p className="text-sm text-muted-foreground">Not processed</p>
                    )}
                    <p className="text-xs text-muted-foreground">
                      Created: {formatDate(transaction.createdAt)}
                    </p>
                  </div>
                </TableCell>
                <TableCell>
                  <div className="text-center">
                    <p className="text-sm">{transaction.retryCount}/{transaction.maxRetries}</p>
                  </div>
                </TableCell>
                {showActions && (
                  <TableCell>
                    <Button 
                      variant="outline" 
                      size="sm"
                      asChild
                    >
                      <Link to={`/payments/transactions/${transaction.id}`}>
                        <Eye className="h-4 w-4" />
                      </Link>
                    </Button>
                  </TableCell>
                )}
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </div>
  );
};

export default PaymentTransactionTable;