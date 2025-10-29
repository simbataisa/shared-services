import React from 'react';
import { StatisticsCard } from '@/components/common';
import type { PaymentRequest, PaymentTransaction, PaymentRefund } from '@/types/payment';

export interface PaymentRequestStatsCardProps {
  paymentRequest: PaymentRequest;
  transactions: PaymentTransaction[];
  refunds: PaymentRefund[];
}

const PaymentRequestStatsCard: React.FC<PaymentRequestStatsCardProps> = ({
  paymentRequest,
  transactions,
  refunds,
}) => {
  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  };

  const statistics = [
    {
      label: "Amount",
      value: formatCurrency(paymentRequest.amount, paymentRequest.currency),
      className: "text-green-600"
    },
    {
      label: "Transactions",
      value: transactions.length.toString(),
      className: "text-blue-600"
    },
    {
      label: "Refunds",
      value: refunds.length.toString(),
      className: "text-orange-600"
    }
  ];

  return (
    <StatisticsCard
      title="Payment Statistics"
      statistics={statistics}
    />
  );
};

export default PaymentRequestStatsCard;