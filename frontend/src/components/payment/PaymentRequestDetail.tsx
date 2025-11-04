import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { 
  CheckCircle, 
  XCircle, 
  Ban, 
  RefreshCw,
  DollarSign,
  History,
  AlertTriangle,
  Activity,
  Receipt
} from 'lucide-react';
import { paymentApi } from '@/lib/paymentApi';
import { DetailHeaderCard, StatisticsCard } from '@/components/common';
import { useAuth } from '@/store/auth';
import { PaymentRequestDetailsCard } from './PaymentRequestDetailsCard';
import { PaymentTransactionTable } from './transaction/PaymentTransactionTable';
import PaymentRequestStatsCard from './PaymentRequestStatsCard';
import PaymentRequestStatusCard from './PaymentRequestStatusCard';
import type { 
  PaymentRequest, 
  PaymentTransaction, 
  PaymentRefund, 
  PaymentAuditLog,
  PaymentRequestStatus 
} from '@/types/payment';
import { 
  PAYMENT_REQUEST_STATUS_MAPPINGS,
} from '@/types/payment';

const PaymentRequestDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();
  
  const [paymentRequest, setPaymentRequest] = useState<PaymentRequest | null>(null);
  const [transactions, setTransactions] = useState<PaymentTransaction[]>([]);
  const [refunds, setRefunds] = useState<PaymentRefund[]>([]);
  const [auditLogs, setAuditLogs] = useState<PaymentAuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [transactionsLoading, setTransactionsLoading] = useState(true);
  const [refundsLoading, setRefundsLoading] = useState(true);
  const [auditLogsLoading, setAuditLogsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchPaymentRequestDetails();
    }
  }, [id]);

  const fetchPaymentRequestDetails = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(null);
      setTransactionsLoading(true);
      setRefundsLoading(true);
      setAuditLogsLoading(true);

      // Fetch payment request details
      const requestResponse = await paymentApi.requests.getById(id);
      if (requestResponse.success) {
        setPaymentRequest(requestResponse.data);
      }

      // Fetch related transactions
      const transactionsResponse = await paymentApi.transactions.getByRequest(id);
      if (transactionsResponse.success) {
        setTransactions(transactionsResponse.data.content || []);
      }
      setTransactionsLoading(false);

      // Fetch refunds for each transaction
      const allRefunds: PaymentRefund[] = [];
      for (const transaction of transactionsResponse.data.content || []) {
        const refundsResponse = await paymentApi.refunds.getByTransaction(transaction.id);
        if (refundsResponse.success) {
          allRefunds.push(...(refundsResponse.data.content || []));
        }
      }
      setRefunds(allRefunds);
      setRefundsLoading(false);

      // Fetch audit logs
      const auditLogsResponse = await paymentApi.auditLogs.getByPaymentRequest(id);
      if (auditLogsResponse.success) {
        setAuditLogs(auditLogsResponse.data.content || []);
      }
      setAuditLogsLoading(false);

    } catch (err) {
      console.error('Error fetching payment request details:', err);
      setError('Failed to load payment request details');
      setTransactionsLoading(false);
      setRefundsLoading(false);
      setAuditLogsLoading(false);
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (action: 'approve' | 'reject' | 'cancel' | 'void' | 'refund' | 'partial_refund', requestId: string) => {
    try {
      setActionLoading(action);
      
      let response;
      switch (action) {
        case 'approve':
          response = await paymentApi.requests.approve(requestId);
          break;
        case 'reject':
          response = await paymentApi.requests.reject(requestId);
          break;
        case 'cancel':
          response = await paymentApi.requests.cancel(requestId);
          break;
        case 'void':
          // TODO: Implement void endpoint when available
          console.log('Void action not yet implemented in backend');
          setActionLoading(null);
          return;
        case 'refund':
          // TODO: Implement full refund logic
          console.log('Full refund action not yet implemented');
          setActionLoading(null);
          return;
        case 'partial_refund':
          // TODO: Implement partial refund logic
          console.log('Partial refund action not yet implemented');
          setActionLoading(null);
          return;
          break;
      }

      if (response.success) {
        setSuccess(`Payment request ${action}d successfully`);
        setError(null);
        fetchPaymentRequestDetails(); // Refresh data
      } else {
        setError(`Failed to ${action} payment request`);
        setSuccess(null);
      }
    } catch (err) {
      console.error(`Error ${action}ing payment request:`, err);
      setError(`Failed to ${action} payment request`);
      setSuccess(null);
    } finally {
      setActionLoading(null);
    }
  };

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

  if (error || !paymentRequest) {
    return (
      <div className="container mx-auto p-6">
        <Alert>
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>
            {error || 'Payment request not found'}
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
          <AlertDescription className="text-red-800">
            {error}
          </AlertDescription>
        </Alert>
      )}

      {/* Header */}
      <DetailHeaderCard
        title={paymentRequest.title}
        description={`Request Code: ${paymentRequest.requestCode}`}
        breadcrumbs={[
          ...(hasPermission("PAYMENT_MGMT:read") 
            ? [{ label: "Payment Requests", href: "/payments" }] 
            : []
          ),
          { label: paymentRequest.title },
        ]}
      />

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Information */}
        <div className="lg:col-span-2 space-y-6">
          {/* Payment Request Details */}
          <PaymentRequestDetailsCard paymentRequest={paymentRequest} />

          {/* Related Records */}
          <Card>
            <CardHeader>
              <CardTitle>Related Records</CardTitle>
            </CardHeader>
            <CardContent>
              <Tabs defaultValue="transactions" className="w-full">
                <TabsList className="grid w-full grid-cols-3">
                   <TabsTrigger value="transactions">
                     <DollarSign className="h-4 w-4 mr-2" />
                     Transactions ({transactions.length})
                   </TabsTrigger>
                   <TabsTrigger value="refunds">
                     <RefreshCw className="h-4 w-4 mr-2" />
                     Refunds ({refunds.length})
                   </TabsTrigger>
                   <TabsTrigger value="audit">
                     <History className="h-4 w-4 mr-2" />
                     Audit Log ({auditLogs.length})
                   </TabsTrigger>
                 </TabsList>

                 <TabsContent value="transactions" className="mt-4">
                  <PaymentTransactionTable
                    data={transactions}
                    loading={transactionsLoading}
                  />
                </TabsContent>

                 <TabsContent value="refunds" className="mt-4">
                   {refundsLoading ? (
                     <div className="space-y-2">
                       {[...Array(3)].map((_, i) => (
                         <Skeleton key={i} className="h-16 w-full" />
                       ))}
                     </div>
                   ) : refunds.length > 0 ? (
                     <div className="space-y-2">
                       {refunds.map((refund) => (
                         <div key={refund.id} className="border rounded-lg p-4">
                           <div className="flex justify-between items-start">
                             <div>
                               <p className="font-medium">{refund.id}</p>
                               <p className="text-sm text-muted-foreground">
                                 {formatCurrency(refund.refundAmount, refund.currency)}
                               </p>
                             </div>
                             <Badge variant={refund.refundStatus === 'SUCCESS' ? 'default' : 'secondary'}>
                               {refund.refundStatus}
                             </Badge>
                           </div>
                         </div>
                       ))}
                     </div>
                   ) : (
                     <p className="text-muted-foreground">No refunds found.</p>
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
                     <p className="text-muted-foreground">No audit logs found.</p>
                   )}
                 </TabsContent>
               </Tabs>
             </CardContent>
           </Card>
         </div>

         {/* Right Column - Sidebar */}
         <div className="space-y-6">
          {/* Status Card */}
           <PaymentRequestStatusCard
             paymentRequest={paymentRequest}
             actionLoading={actionLoading}
             onAction={handleAction}
           />

           {/* Statistics Card */}
           <PaymentRequestStatsCard
             paymentRequest={paymentRequest}
             transactions={transactions}
             refunds={refunds}
           />


           {/* Audit Information Card */}
           <Card>
             <CardHeader>
               <CardTitle>Audit Information</CardTitle>
             </CardHeader>
             <CardContent className="space-y-4">
               <div className="space-y-2">
                 <Label className="text-sm font-medium text-muted-foreground">Created At</Label>
                 <p className="text-sm">{formatDate(paymentRequest.createdAt)}</p>
               </div>
               
               <div className="space-y-2">
                 <Label className="text-sm font-medium text-muted-foreground">Updated At</Label>
                 <p className="text-sm">{formatDate(paymentRequest.updatedAt)}</p>
               </div>
               
               {paymentRequest.createdBy && (
                 <div className="space-y-2">
                   <Label className="text-sm font-medium text-muted-foreground">Created By</Label>
                   <p className="text-sm">{paymentRequest.createdBy}</p>
                 </div>
               )}
               
               {paymentRequest.updatedBy && (
                 <div className="space-y-2">
                   <Label className="text-sm font-medium text-muted-foreground">Updated By</Label>
                   <p className="text-sm">{paymentRequest.updatedBy}</p>
                 </div>
               )}
             </CardContent>
           </Card>
         </div>
       </div>
     </div>
  );
};

export default PaymentRequestDetail;