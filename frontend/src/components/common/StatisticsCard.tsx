import React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export interface StatisticItem {
  label: string;
  value: string | number;
  className?: string;
  icon?: React.ReactNode;
}

export interface StatisticsCardProps {
  title?: string;
  description?: string;
  statistics: StatisticItem[];
  layout?: "list" | "grid";
  className?: string;
}

const StatisticsCard: React.FC<StatisticsCardProps> = ({
  title = "Statistics",
  description,
  statistics,
  layout = "list",
  className = "",
}) => {
  const renderListLayout = () => (
    <div className="space-y-4">
      {statistics.map((stat, index) => (
        <div key={index} className="flex justify-between items-center">
          <div className="flex items-center gap-2">
            {stat.icon && <span className="text-muted-foreground">{stat.icon}</span>}
            <span className="text-sm text-gray-600">{stat.label}</span>
          </div>
          <span className={`text-sm font-medium ${stat.className || "text-gray-900"}`}>
            {stat.value}
          </span>
        </div>
      ))}
    </div>
  );

  const renderGridLayout = () => (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
      {statistics.map((stat, index) => (
        <div key={index} className="text-center p-4 bg-muted/50 rounded-lg">
          {stat.icon && (
            <div className="flex justify-center mb-2">
              {stat.icon}
            </div>
          )}
          <div className={`text-2xl font-bold ${stat.className || "text-primary"}`}>
            {stat.value}
          </div>
          <div className="text-xs text-muted-foreground">{stat.label}</div>
        </div>
      ))}
    </div>
  );

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="text-lg font-semibold text-gray-900">
          {title}
        </CardTitle>
        {description && (
          <CardDescription>{description}</CardDescription>
        )}
      </CardHeader>
      <CardContent>
        {layout === "grid" ? renderGridLayout() : renderListLayout()}
      </CardContent>
    </Card>
  );
};

export default StatisticsCard;