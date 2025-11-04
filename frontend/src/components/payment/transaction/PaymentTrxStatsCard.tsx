import React from 'react';
import StatisticsCard, { type StatisticItem } from '@/components/common/StatisticsCard';
import type { PaymentTransaction } from '@/types/payment';

interface PaymentTrxStatsCardProps {
  transaction: PaymentTransaction;
}

export const PaymentTrxStatsCard: React.FC<PaymentTrxStatsCardProps> = ({
  transaction
}) => {
  const formatDate = (dateString: string | Date) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const statistics: StatisticItem[] = [
    {
      label: 'Retry Count',
      value: transaction.retryCount || 0,
    },
    ...(transaction.processedAt ? [{
      label: 'Processed At',
      value: formatDate(transaction.processedAt),
    }] : []),
  ];

  return (
    <StatisticsCard
      title="Transaction Statistics"
      description="Key metrics and status information for this transaction"
      statistics={statistics}
      layout="list"
    />
  );
};

export default PaymentTrxStatsCard;