import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ArrowLeft } from 'lucide-react';
import StatusDisplayCard from '@/components/common/StatusDisplayCard';
import { getTransactionStatusBadgeProps, getStatusIcon } from '@/lib/status-utils';
import type { PaymentTransaction } from '@/types/payment';

interface PaymentTrxStatusCardProps {
  transaction: PaymentTransaction;
}

export const PaymentTrxStatusCard: React.FC<PaymentTrxStatusCardProps> = ({
  transaction
}) => {
  const navigate = useNavigate();
  
  const formatDate = (dateString: string | Date) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="space-y-6">
      {/* Status Card */}
      <Card>
        <CardHeader>
          <CardTitle>Transaction Status Management</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <StatusDisplayCard
            title="Current Status"
            description="View the transaction's current status and details"
            status={transaction.transactionStatus}
          />
        </CardContent>
      </Card>

      {/* Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Actions</CardTitle>
        </CardHeader>
        <CardContent>
          <Button 
            variant="outline" 
            className="w-full"
            onClick={() => navigate('/payments/transactions')}
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Transactions
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentTrxStatusCard;