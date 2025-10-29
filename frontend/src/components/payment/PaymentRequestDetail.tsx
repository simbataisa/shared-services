import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { 
  ArrowLeft, 
  CheckCircle, 
  XCircle, 
  Ban, 
  RefreshCw,
  DollarSign,
  FileText,
  History,
  AlertTriangle,
  Activity,
  Receipt
} from 'lucide-react';
import { paymentApi } from '@/lib/paymentApi';
import { DetailHeaderCard, StatisticsCard } from '@/components/common';
import { PaymentRequestDetailsCard } from './PaymentRequestDetailsCard';
import { PaymentTransactionTable } from './PaymentTransactionTable';
import type { 
  PaymentRequest, 
  PaymentTransaction, 
  PaymentRefund, 
  PaymentAuditLog,
  PaymentRequestStatus 
} from '@/types/payment';
import { 
  PAYMENT_REQUEST_STATUS_MAPPINGS,
  PAYMENT_TRANSACTION_STATUS_MAPPINGS,
  PAYMENT_TRANSACTION_TYPE_MAPPINGS,
  PAYMENT_METHOD_TYPE_MAPPINGS 
} from '@/types/payment';

const PaymentRequestDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
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

  const handleAction = async (action: 'approve' | 'reject' | 'cancel', requestId: string) => {
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

  const getStatusColor = (status: PaymentRequestStatus) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'FAILED':
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'CANCELLED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-blue-100 text-blue-800';
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
          { label: "Payment Requests", href: "/payments/requests" },
          { label: paymentRequest.title },
        ]}
        actions={
          <Badge className={getStatusColor(paymentRequest.status)}>
            {PAYMENT_REQUEST_STATUS_MAPPINGS[paymentRequest.status]}
          </Badge>
        }
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
                    transactions={transactions}
                    loading={transactionsLoading}
                    emptyMessage="No transactions found for this payment request."
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
           {/* Statistics Card */}
           <StatisticsCard
             title="Payment Statistics"
             statistics={[
               {
                 label: "Amount",
                 value: formatCurrency(paymentRequest.amount, paymentRequest.currency),
                 icon: <DollarSign className="h-4 w-4" />,
                 className: "text-green-600"
               },
               {
                 label: "Transactions",
                 value: transactions.length.toString(),
                 icon: <Receipt className="h-4 w-4" />,
                 className: "text-blue-600"
               },
               {
                 label: "Refunds",
                 value: refunds.length.toString(),
                 icon: <RefreshCw className="h-4 w-4" />,
                 className: "text-orange-600"
               },
               {
                 label: "Status",
                 value: paymentRequest.status,
                 icon: <Activity className="h-4 w-4" />,
                 className: paymentRequest.status === 'COMPLETED' ? "text-green-600" : 
                           paymentRequest.status === 'PENDING' ? "text-yellow-600" : "text-red-600"
               }
             ]}
           />

           {/* Actions Card */}
           <Card>
             <CardHeader>
               <CardTitle>Actions</CardTitle>
             </CardHeader>
             <CardContent>
               <div className="space-y-2">
                 {paymentRequest.status === 'PENDING' && (
                   <>
                     <Button
                       onClick={() => handleAction('approve', paymentRequest.id)}
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
                       onClick={() => handleAction('reject', paymentRequest.id)}
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
                     onClick={() => handleAction('cancel', paymentRequest.id)}
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
               </div>
             </CardContent>
           </Card>

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