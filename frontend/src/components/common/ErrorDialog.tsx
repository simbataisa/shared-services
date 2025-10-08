import React, { useState } from 'react';
import { 
  AlertTriangle, 
  XCircle, 
  Info, 
  Clock,
  ExternalLink,
  RefreshCw,
  Copy,
  Check
} from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { cn } from '@/lib/utils';
import type { ErrorDialogProps, BaseError, ErrorWithActions } from '@/types/errors';

const severityConfig = {
  low: {
    icon: Info,
    className: 'text-blue-600',
    badgeVariant: 'secondary' as const
  },
  medium: {
    icon: AlertTriangle,
    className: 'text-yellow-600',
    badgeVariant: 'outline' as const
  },
  high: {
    icon: AlertTriangle,
    className: 'text-orange-600',
    badgeVariant: 'destructive' as const
  },
  critical: {
    icon: XCircle,
    className: 'text-red-600',
    badgeVariant: 'destructive' as const
  }
};

const typeLabels = {
  validation: 'Validation Error',
  network: 'Network Error',
  authentication: 'Authentication Error',
  authorization: 'Authorization Error',
  server: 'Server Error',
  client: 'Client Error',
  unknown: 'Unknown Error'
};

function hasActions(error: BaseError | ErrorWithActions): error is ErrorWithActions {
  return 'actions' in error && Array.isArray(error.actions);
}

export function ErrorDialog({
  open,
  onOpenChange,
  error,
  onAction,
  title,
  showDetails = true,
  showTimestamp = true
}: ErrorDialogProps) {
  const [isProcessing, setIsProcessing] = useState<string | null>(null);
  const [copiedField, setCopiedField] = useState<string | null>(null);

  const config = severityConfig[error.severity];
  const IconComponent = config.icon;

  const handleAction = async (actionId: string) => {
    if (isProcessing) return;
    
    setIsProcessing(actionId);
    
    try {
      if (hasActions(error)) {
        const action = error.actions.find(a => a.id === actionId);
        if (action?.handler) {
          await action.handler();
        }
      }
      onAction?.(actionId);
    } catch (err) {
      console.error('Error executing action:', err);
    } finally {
      setIsProcessing(null);
    }
  };

  const handleCopy = async (text: string, field: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedField(field);
      setTimeout(() => setCopiedField(null), 2000);
    } catch (err) {
      console.error('Failed to copy to clipboard:', err);
    }
  };

  const formatTimestamp = (timestamp: Date) => {
    return new Intl.DateTimeFormat('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      timeZoneName: 'short'
    }).format(timestamp);
  };

  const errorDetails = [
    { label: 'Error ID', value: error.id, copyable: true },
    { label: 'Type', value: typeLabels[error.type] },
    { label: 'Severity', value: error.severity.toUpperCase() },
    ...(error.code ? [{ label: 'Code', value: error.code.toString(), copyable: true }] : []),
    ...(showTimestamp ? [{ label: 'Timestamp', value: formatTimestamp(error.timestamp), copyable: true }] : [])
  ];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] flex flex-col">
        <DialogHeader>
          <div className="flex items-center gap-3">
            <IconComponent className={cn('h-6 w-6', config.className)} />
            <div className="flex-1 min-w-0">
              <DialogTitle className="text-left">
                {title || typeLabels[error.type]}
              </DialogTitle>
              <div className="flex items-center gap-2 mt-1">
                <Badge variant={config.badgeVariant} className="text-xs">
                  {error.severity.toUpperCase()}
                </Badge>
                {error.code && (
                  <Badge variant="outline" className="text-xs font-mono">
                    {error.code}
                  </Badge>
                )}
              </div>
            </div>
          </div>
        </DialogHeader>

        <div className="flex-1 min-h-0">
          <DialogDescription className="text-base mb-4 whitespace-pre-wrap">
            {error.message}
          </DialogDescription>

          {showDetails && (error.details || error.stack || showTimestamp) && (
            <Tabs defaultValue="details" className="h-full">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="details">Details</TabsTrigger>
                {error.details && <TabsTrigger value="description">Description</TabsTrigger>}
                {error.stack && <TabsTrigger value="technical">Technical</TabsTrigger>}
              </TabsList>

              <TabsContent value="details" className="mt-4">
                <div className="h-48 overflow-y-auto">
                  <div className="space-y-3">
                    {errorDetails.map((detail, index) => (
                      <div key={index} className="flex items-center justify-between py-2">
                        <span className="text-sm font-medium text-muted-foreground">
                          {detail.label}:
                        </span>
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-mono max-w-xs truncate">
                            {detail.value}
                          </span>
                          {detail.copyable && (
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-6 w-6 p-0"
                              onClick={() => handleCopy(detail.value.toString(), detail.label)}
                            >
                              {copiedField === detail.label ? (
                                <Check className="h-3 w-3 text-green-600" />
                              ) : (
                                <Copy className="h-3 w-3" />
                              )}
                            </Button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </TabsContent>

              {error.details && (
                <TabsContent value="description" className="mt-4">
                  <div className="h-48 overflow-y-auto">
                    <div className="p-4 bg-muted/50 rounded-md">
                      <p className="text-sm whitespace-pre-wrap">
                        {error.details}
                      </p>
                    </div>
                  </div>
                </TabsContent>
              )}

              {error.stack && (
                <TabsContent value="technical" className="mt-4">
                  <div className="h-48 overflow-y-auto">
                    <div className="p-4 bg-muted/50 rounded-md relative">
                      <Button
                        variant="ghost"
                        size="sm"
                        className="absolute top-2 right-2 h-6 w-6 p-0"
                        onClick={() => handleCopy(error.stack!, 'Stack Trace')}
                      >
                        {copiedField === 'Stack Trace' ? (
                          <Check className="h-3 w-3 text-green-600" />
                        ) : (
                          <Copy className="h-3 w-3" />
                        )}
                      </Button>
                      <pre className="text-xs font-mono whitespace-pre-wrap pr-8">
                        {error.stack}
                      </pre>
                    </div>
                  </div>
                </TabsContent>
              )}
            </Tabs>
          )}
        </div>

        <DialogFooter className="flex-shrink-0">
          <div className="flex flex-wrap gap-2 w-full">
            {/* Error Actions */}
            {hasActions(error) && error.actions.length > 0 && (
              <>
                {error.actions.map((action) => {
                  const isPrimary = action.id === error.primaryAction;
                  const isLoading = isProcessing === action.id;
                  
                  if (action.href) {
                    return (
                      <Button
                        key={action.id}
                        variant={action.variant || (isPrimary ? 'default' : 'outline')}
                        size="sm"
                        asChild
                        disabled={action.disabled}
                      >
                        <a href={action.href} target="_blank" rel="noopener noreferrer">
                          {action.label}
                          <ExternalLink className="h-3 w-3 ml-1" />
                        </a>
                      </Button>
                    );
                  }

                  return (
                    <Button
                      key={action.id}
                      variant={action.variant || (isPrimary ? 'default' : 'outline')}
                      size="sm"
                      onClick={() => handleAction(action.id)}
                      disabled={action.disabled || isProcessing !== null}
                    >
                      {isLoading && <RefreshCw className="h-3 w-3 mr-1 animate-spin" />}
                      {action.label}
                    </Button>
                  );
                })}
                <Separator orientation="vertical" className="h-6" />
              </>
            )}
            
            {/* Default Close Button */}
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isProcessing !== null}
            >
              Close
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default ErrorDialog;