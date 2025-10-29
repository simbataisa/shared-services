import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import StatusDisplayCard from '@/components/common/StatusDisplayCard';
import { 
  CheckCircle, 
  XCircle, 
  Ban, 
  RefreshCw
} from 'lucide-react';
import type { PaymentRequest } from '@/types/payment';

interface PaymentRequestStatusCardProps {
  paymentRequest: PaymentRequest;
  actionLoading: string | null;
  onAction: (action: 'approve' | 'reject' | 'cancel' | 'void' | 'refund' | 'partial_refund', requestId: string) => void;
}

export const PaymentRequestStatusCard: React.FC<PaymentRequestStatusCardProps> = ({
  paymentRequest,
  actionLoading,
  onAction
}) => {
  const handleAction = (action: 'approve' | 'reject' | 'cancel' | 'void' | 'refund' | 'partial_refund') => {
    onAction(action, paymentRequest.id);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Payment Request Management</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <StatusDisplayCard
          title="Current Status"
          description="Manage the payment request's current status"
          status={paymentRequest.status}
        />

        <div className="space-y-2">
          {paymentRequest.status === 'PENDING' && (
            <>
              <Button
                onClick={() => handleAction('approve')}
                disabled={actionLoading === 'approve'}
                className="w-full bg-green-600 hover:bg-green-700"
              >
                {actionLoading === 'approve' ? (
                  <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <CheckCircle className="h-4 w-4 mr-2" />
                )}
                Approve
              </Button>
              <Button
                variant="destructive"
                onClick={() => handleAction('reject')}
                disabled={actionLoading === 'reject'}
                className="w-full"
              >
                {actionLoading === 'reject' ? (
                  <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <XCircle className="h-4 w-4 mr-2" />
                )}
                Reject
              </Button>
            </>
          )}
          
          {['PENDING', 'APPROVED'].includes(paymentRequest.status) && (
            <Button
              variant="outline"
              onClick={() => handleAction('cancel')}
              disabled={actionLoading === 'cancel'}
              className="w-full"
            >
              {actionLoading === 'cancel' ? (
                <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
              ) : (
                <Ban className="h-4 w-4 mr-2" />
              )}
              Cancel
            </Button>
          )}

          {paymentRequest.status === 'COMPLETED' && (
            <>
              <Button
                variant="outline"
                onClick={() => handleAction('void')}
                disabled={actionLoading === 'void'}
                className="w-full border-red-500 text-red-600 hover:bg-red-50"
              >
                {actionLoading === 'void' ? (
                  <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Ban className="h-4 w-4 mr-2" />
                )}
                Void Payment
              </Button>
              <Button
                variant="outline"
                onClick={() => handleAction('refund')}
                disabled={actionLoading === 'refund'}
                className="w-full border-blue-500 text-blue-600 hover:bg-blue-50"
              >
                {actionLoading === 'refund' ? (
                  <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <RefreshCw className="h-4 w-4 mr-2" />
                )}
                Full Refund
              </Button>
              <Button
                variant="outline"
                onClick={() => handleAction('partial_refund')}
                disabled={actionLoading === 'partial_refund'}
                className="w-full border-purple-500 text-purple-600 hover:bg-purple-50"
              >
                {actionLoading === 'partial_refund' ? (
                  <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <RefreshCw className="h-4 w-4 mr-2" />
                )}
                Partial Refund
              </Button>
            </>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default PaymentRequestStatusCard;