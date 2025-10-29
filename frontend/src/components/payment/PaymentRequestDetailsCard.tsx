import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { 
  CreditCard,
  Clock,
  User,
  Mail,
  Phone,
  Calendar,
  DollarSign
} from 'lucide-react';
import type { PaymentRequest } from '@/types/payment';
import { PAYMENT_METHOD_TYPE_MAPPINGS } from '@/types/payment';
interface PaymentRequestDetailsCardProps {
  paymentRequest: PaymentRequest;
}

const formatCurrency = (amount: number, currency: string) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
  }).format(amount);
};

const formatDate = (dateString: string | Date) => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const PaymentRequestDetailsCard: React.FC<PaymentRequestDetailsCardProps> = ({
  paymentRequest
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center">
          <CreditCard className="h-5 w-5 mr-2" />
          Payment Request Details
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <div className="flex items-center text-sm text-muted-foreground">
              <DollarSign className="h-4 w-4 mr-2" />
              Amount
            </div>
            <p className="text-lg font-semibold">
              {formatCurrency(paymentRequest.amount, paymentRequest.currency)}
            </p>
          </div>
          
          <div className="space-y-2">
            <div className="flex items-center text-sm text-muted-foreground">
              <User className="h-4 w-4 mr-2" />
              Payer Name
            </div>
            <p>{paymentRequest.payerName}</p>
          </div>
          
          <div className="space-y-2">
            <div className="flex items-center text-sm text-muted-foreground">
              <Mail className="h-4 w-4 mr-2" />
              Payer Email
            </div>
            <p>{paymentRequest.payerEmail}</p>
          </div>
          
          {paymentRequest.payerPhone && (
            <div className="space-y-2">
              <div className="flex items-center text-sm text-muted-foreground">
                <Phone className="h-4 w-4 mr-2" />
                Payer Phone
              </div>
              <p>{paymentRequest.payerPhone}</p>
            </div>
          )}
          
          <div className="space-y-2">
            <div className="flex items-center text-sm text-muted-foreground">
              <Calendar className="h-4 w-4 mr-2" />
              Created At
            </div>
            <p>{formatDate(paymentRequest.createdAt)}</p>
          </div>
          
          {paymentRequest.expiresAt && (
            <div className="space-y-2">
              <div className="flex items-center text-sm text-muted-foreground">
                <Clock className="h-4 w-4 mr-2" />
                Expires At
              </div>
              <p>{formatDate(paymentRequest.expiresAt)}</p>
            </div>
          )}
        </div>

        <Separator />

        <div className="space-y-2">
          <h4 className="font-medium">Allowed Payment Methods</h4>
          <div className="flex flex-wrap gap-2">
            {paymentRequest.allowedPaymentMethods.map((method) => (
              <Badge key={method} variant="outline">
                {PAYMENT_METHOD_TYPE_MAPPINGS[method]}
              </Badge>
            ))}
          </div>
        </div>

        {paymentRequest.preSelectedPaymentMethod && (
          <div className="space-y-2">
            <h4 className="font-medium">Pre-selected Payment Method</h4>
            <Badge variant="secondary">
              {PAYMENT_METHOD_TYPE_MAPPINGS[paymentRequest.preSelectedPaymentMethod]}
            </Badge>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default PaymentRequestDetailsCard;