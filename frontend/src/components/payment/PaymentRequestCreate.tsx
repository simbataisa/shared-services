import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { CreditCard, Plus, ArrowLeft } from "lucide-react";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { paymentApi } from "@/lib/paymentApi";
import type { CreatePaymentRequestDto, PaymentMethodType } from "@/types/payment";

const PaymentRequestCreate: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [formData, setFormData] = useState<CreatePaymentRequestDto>({
    tenantId: 1, // Default tenant ID - should be set based on current user context
    title: "",
    amount: 0,
    currency: "USD",
    payerName: "",
    payerEmail: "",
    payerPhone: "",
    allowedPaymentMethods: ["CREDIT_CARD", "DEBIT_CARD"],
    preSelectedPaymentMethod: "CREDIT_CARD",
    expiresAt: undefined,
    metadata: {}
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.amount || formData.amount <= 0) {
      newErrors.amount = "Amount must be greater than 0";
    }

    if (!formData.currency.trim()) {
      newErrors.currency = "Currency is required";
    }

    if (!formData.title.trim()) {
      newErrors.title = "Title is required";
    }

    if (!formData.payerName.trim()) {
      newErrors.payerName = "Payer name is required";
    }

    if (!formData.payerEmail.trim()) {
      newErrors.payerEmail = "Payer email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.payerEmail)) {
      newErrors.payerEmail = "Please enter a valid email address";
    }

    if (!formData.allowedPaymentMethods || formData.allowedPaymentMethods.length === 0) {
      newErrors.allowedPaymentMethods = "At least one payment method is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      await paymentApi.requests.create(formData);
      setSuccess("Payment request created successfully!");
      
      // Redirect to payment requests list after a short delay
      setTimeout(() => {
        navigate("/payments/requests");
      }, 2000);
    } catch (err: any) {
      console.error("Error creating payment request:", err);
      setError(err.response?.data?.message || "Failed to create payment request. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof CreatePaymentRequestDto, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    
    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: ""
      }));
    }
  };

  const paymentMethodOptions = [
    { value: "CREDIT_CARD", label: "Credit Card" },
    { value: "DEBIT_CARD", label: "Debit Card" },
    { value: "BANK_TRANSFER", label: "Bank Transfer" },
    { value: "DIGITAL_WALLET", label: "Digital Wallet" },
    { value: "CASH", label: "Cash" },
    { value: "CHECK", label: "Check" },
    { value: "OTHER", label: "Other" }
  ];

  const currencyOptions = [
    { value: "USD", label: "USD - US Dollar" },
    { value: "EUR", label: "EUR - Euro" },
    { value: "GBP", label: "GBP - British Pound" },
    { value: "JPY", label: "JPY - Japanese Yen" },
    { value: "CAD", label: "CAD - Canadian Dollar" },
    { value: "AUD", label: "AUD - Australian Dollar" }
  ];

  return (
    <PermissionGuard permission="PAYMENT_MGMT:create">
      <div className="container mx-auto py-6 space-y-6">
        {/* Breadcrumb */}
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to="/payments">Payments</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to="/payments/requests">Payment Requests</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbPage>Create New Request</BreadcrumbPage>
          </BreadcrumbList>
        </Breadcrumb>

        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <div>
              <h1 className="text-3xl font-bold tracking-tight">Create Payment Request</h1>
              <p className="text-muted-foreground">
                Create a new payment request for processing
              </p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Form */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center">
                  <CreditCard className="h-5 w-5 mr-2" />
                  Payment Request Details
                </CardTitle>
                <CardDescription>
                  Fill in the details for the new payment request
                </CardDescription>
              </CardHeader>
              <CardContent>
                {error && (
                  <Alert variant="destructive" className="mb-6">
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
                )}

                {success && (
                  <Alert className="mb-6 border-green-200 bg-green-50">
                    <AlertDescription className="text-green-800">
                      {success}
                    </AlertDescription>
                  </Alert>
                )}

                <form onSubmit={handleSubmit} className="space-y-6">
                  {/* Amount and Currency */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="amount">Amount *</Label>
                      <Input
                        id="amount"
                        type="number"
                        step="0.01"
                        min="0"
                        value={formData.amount}
                        onChange={(e) => handleInputChange("amount", parseFloat(e.target.value) || 0)}
                        className={errors.amount ? "border-red-500" : ""}
                        placeholder="0.00"
                      />
                      {errors.amount && (
                        <p className="text-sm text-red-500">{errors.amount}</p>
                      )}
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="currency">Currency *</Label>
                      <Select
                        value={formData.currency}
                        onValueChange={(value) => handleInputChange("currency", value)}
                      >
                        <SelectTrigger className={errors.currency ? "border-red-500" : ""}>
                          <SelectValue placeholder="Select currency" />
                        </SelectTrigger>
                        <SelectContent>
                          {currencyOptions.map((option) => (
                            <SelectItem key={option.value} value={option.value}>
                              {option.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      {errors.currency && (
                        <p className="text-sm text-red-500">{errors.currency}</p>
                      )}
                    </div>
                  </div>

                  {/* Title */}
                  <div className="space-y-2">
                    <Label htmlFor="title">Title *</Label>
                    <Textarea
                      id="title"
                      value={formData.title}
                      onChange={(e) => handleInputChange("title", e.target.value)}
                      className={errors.title ? "border-red-500" : ""}
                      placeholder="Enter payment request title..."
                      rows={3}
                    />
                    {errors.title && (
                      <p className="text-sm text-red-500">{errors.title}</p>
                    )}
                  </div>

                  {/* Payer Information */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="payerName">Payer Name *</Label>
                      <Input
                        id="payerName"
                        value={formData.payerName}
                        onChange={(e) => handleInputChange("payerName", e.target.value)}
                        className={errors.payerName ? "border-red-500" : ""}
                        placeholder="Enter payer name"
                      />
                      {errors.payerName && (
                        <p className="text-sm text-red-500">{errors.payerName}</p>
                      )}
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="payerEmail">Payer Email *</Label>
                      <Input
                        id="payerEmail"
                        type="email"
                        value={formData.payerEmail}
                        onChange={(e) => handleInputChange("payerEmail", e.target.value)}
                        className={errors.payerEmail ? "border-red-500" : ""}
                        placeholder="Enter payer email"
                      />
                      {errors.payerEmail && (
                        <p className="text-sm text-red-500">{errors.payerEmail}</p>
                      )}
                    </div>
                  </div>

                  {/* Payer Phone (Optional) */}
                  <div className="space-y-2">
                    <Label htmlFor="payerPhone">Payer Phone</Label>
                    <Input
                      id="payerPhone"
                      value={formData.payerPhone || ""}
                      onChange={(e) => handleInputChange("payerPhone", e.target.value)}
                      placeholder="Enter payer phone (optional)"
                    />
                  </div>

                  {/* Payment Methods and Expiry Date */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="preSelectedPaymentMethod">Pre-selected Payment Method</Label>
                      <Select
                        value={formData.preSelectedPaymentMethod || ""}
                        onValueChange={(value) => handleInputChange("preSelectedPaymentMethod", value as PaymentMethodType)}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Select pre-selected payment method" />
                        </SelectTrigger>
                        <SelectContent>
                          {paymentMethodOptions.map((option) => (
                            <SelectItem key={option.value} value={option.value}>
                              {option.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="expiresAt">Expires At</Label>
                      <Input
                        id="expiresAt"
                        type="datetime-local"
                        value={formData.expiresAt ? new Date(formData.expiresAt).toISOString().slice(0, 16) : ""}
                        onChange={(e) => handleInputChange("expiresAt", e.target.value ? new Date(e.target.value) : undefined)}
                        min={new Date().toISOString().slice(0, 16)}
                      />
                    </div>
                  </div>

                  {/* Form Actions */}
                  <div className="flex justify-end space-x-3 pt-6 border-t">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => navigate("/payments/requests")}
                      disabled={loading}
                    >
                      Cancel
                    </Button>
                    <Button type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          Creating...
                        </>
                      ) : (
                        <>
                          <Plus className="h-4 w-4 mr-2" />
                          Create Request
                        </>
                      )}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          </div>

          {/* Right Column - Info */}
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Payment Request Guidelines</CardTitle>
                <CardDescription>
                  Important information about creating payment requests
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-start">
                    <CreditCard className="h-5 w-5 text-primary mt-0.5 mr-3" />
                    <div>
                      <h4 className="font-medium">Amount & Currency</h4>
                      <p className="text-sm text-muted-foreground">
                        Specify the exact amount and currency for the payment request.
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex items-start">
                    <Plus className="h-5 w-5 text-primary mt-0.5 mr-3" />
                    <div>
                      <h4 className="font-medium">Clear Description</h4>
                      <p className="text-sm text-muted-foreground">
                        Provide a detailed description of what the payment is for.
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start">
                    <ArrowLeft className="h-5 w-5 text-primary mt-0.5 mr-3" />
                    <div>
                      <h4 className="font-medium">Due Date</h4>
                      <p className="text-sm text-muted-foreground">
                        Set an appropriate due date for when the payment should be processed.
                      </p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Next Steps</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2 text-sm text-muted-foreground">
                  <p>1. Request will be created with PENDING status</p>
                  <p>2. Approval workflow will be initiated</p>
                  <p>3. You'll receive notifications on status changes</p>
                  <p>4. Payment will be processed once approved</p>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </PermissionGuard>
  );
};

export default PaymentRequestCreate;