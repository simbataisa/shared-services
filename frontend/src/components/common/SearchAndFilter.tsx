import React from "react";
import { Search } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { type SearchAndFilterProps } from "@/types/components";

export const SearchAndFilter: React.FC<SearchAndFilterProps> = ({
  searchTerm,
  onSearchChange,
  searchPlaceholder = "Search...",
  filters = [],
  actions,
  className = "",
}) => {
  return (
    <Card className={className}>
      <CardContent className="p-4">
        <div className="flex flex-col sm:flex-row gap-4">
          {/* Search Input */}
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder={searchPlaceholder}
              value={searchTerm}
              onChange={(e) => onSearchChange(e.target.value)}
              className="pl-10"
            />
          </div>

          {/* Filters */}
          {filters.length > 0 && (
            <div className="flex gap-2 flex-wrap">
              {filters.map((filter, index) => (
                <Select
                  key={index}
                  value={filter.value}
                  onValueChange={filter.onChange}
                >
                  <SelectTrigger className={filter.width || "w-40"}>
                    <SelectValue
                      placeholder={
                        filter.placeholder || `Filter by ${filter.label}`
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {filter.options.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              ))}
            </div>
          )}

          {/* Additional Actions */}
          {actions && <div className="flex gap-2 items-center">{actions}</div>}
        </div>
      </CardContent>
    </Card>
  );
};

export default SearchAndFilter;
