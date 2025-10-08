import React, { useState } from 'react';
import { 
  AlertTriangle, 
  XCircle, 
  Info, 
  X, 
  ChevronDown, 
  ChevronUp, 
  Clock,
  ExternalLink,
  RefreshCw
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { cn } from '@/lib/utils';
import type { ErrorCardProps, BaseError, ErrorWithActions, ErrorSeverity } from '@/types/errors';

const severityConfig = {
  low: {
    icon: Info,
    variant: 'default' as const,
    badgeVariant: 'secondary' as const,
    className: 'border-blue-200 bg-blue-50 text-blue-900'
  },
  medium: {
    icon: AlertTriangle,
    variant: 'warning' as const,
    badgeVariant: 'outline' as const,
    className: 'border-yellow-200 bg-yellow-50 text-yellow-900'
  },
  high: {
    icon: AlertTriangle,
    variant: 'destructive' as const,
    badgeVariant: 'destructive' as const,
    className: 'border-orange-200 bg-orange-50 text-orange-900'
  },
  critical: {
    icon: XCircle,
    variant: 'destructive' as const,
    badgeVariant: 'destructive' as const,
    className: 'border-red-200 bg-red-50 text-red-900'
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

export function ErrorCard({
  error,
  onDismiss,
  onAction,
  dismissible = true,
  className,
  variant,
  showTimestamp = true,
  showDetails = false,
  collapsible = true
}: ErrorCardProps) {
  const [isExpanded, setIsExpanded] = useState(!collapsible);
  const [isProcessing, setIsProcessing] = useState<string | null>(null);

  const config = severityConfig[error.severity];
  const IconComponent = config.icon;
  const cardVariant = variant || config.variant;

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

  const formatTimestamp = (timestamp: Date) => {
    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(timestamp);
  };

  return (
    <Card className={cn(
      'relative',
      config.className,
      className
    )}>
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3 flex-1 min-w-0">
            <IconComponent className="h-5 w-5 mt-0.5 flex-shrink-0" />
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1">
                <CardTitle className="text-base font-medium truncate">
                  {typeLabels[error.type]}
                </CardTitle>
                <Badge variant={config.badgeVariant} className="text-xs">
                  {error.severity.toUpperCase()}
                </Badge>
                {error.code && (
                  <Badge variant="outline" className="text-xs font-mono">
                    {error.code}
                  </Badge>
                )}
              </div>
              <p className="text-sm text-muted-foreground break-words">
                {error.message}
              </p>
              {showTimestamp && (
                <div className="flex items-center gap-1 mt-2 text-xs text-muted-foreground">
                  <Clock className="h-3 w-3" />
                  {formatTimestamp(error.timestamp)}
                </div>
              )}
            </div>
          </div>
          
          <div className="flex items-center gap-1 flex-shrink-0">
            {collapsible && (error.details || error.stack) && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsExpanded(!isExpanded)}
                className="h-8 w-8 p-0"
              >
                {isExpanded ? (
                  <ChevronUp className="h-4 w-4" />
                ) : (
                  <ChevronDown className="h-4 w-4" />
                )}
              </Button>
            )}
            {dismissible && (
              <Button
                variant="ghost"
                size="sm"
                onClick={onDismiss}
                className="h-8 w-8 p-0"
              >
                <X className="h-4 w-4" />
              </Button>
            )}
          </div>
        </div>
      </CardHeader>

      <CardContent className="pt-0">
        {/* Error Details */}
        {(error.details || error.stack) && (
          <Collapsible open={isExpanded} onOpenChange={setIsExpanded}>
            <CollapsibleContent className="space-y-3">
              {error.details && (
                <div className="p-3 bg-muted/50 rounded-md">
                  <h4 className="text-sm font-medium mb-2">Details</h4>
                  <p className="text-sm text-muted-foreground whitespace-pre-wrap">
                    {error.details}
                  </p>
                </div>
              )}
              
              {showDetails && error.stack && (
                <div className="p-3 bg-muted/50 rounded-md">
                  <h4 className="text-sm font-medium mb-2">Stack Trace</h4>
                  <pre className="text-xs text-muted-foreground whitespace-pre-wrap font-mono overflow-x-auto">
                    {error.stack}
                  </pre>
                </div>
              )}
            </CollapsibleContent>
          </Collapsible>
        )}

        {/* Actions */}
        {hasActions(error) && error.actions.length > 0 && (
          <div className="flex flex-wrap gap-2 mt-4 pt-3 border-t">
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
          </div>
        )}
      </CardContent>
    </Card>
  );
}

export default ErrorCard;