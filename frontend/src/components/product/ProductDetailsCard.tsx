import React from "react";
import { StatusBadge, StatusCard } from "@/components/common";
import type { Product } from "@/types/entities";

interface ProductDetailsCardProps {
  product: Product;
}

const ProductDetailsCard: React.FC<ProductDetailsCardProps> = ({ product }) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusBadge = (status: string) => {
    return <StatusBadge status={status} />;
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <label className="block text-sm font-medium text-gray-700">
          Product Code
        </label>
        <p className="mt-1 text-sm text-gray-900 font-mono bg-gray-50 px-2 py-1 rounded">
          {product.code}
        </p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Category
        </label>
        <p className="mt-1 text-sm text-gray-900 capitalize">
          {product.category || "N/A"}
        </p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Version
        </label>
        <p className="mt-1 text-sm text-gray-900 font-mono">
          {product.version || "N/A"}
        </p>
      </div>

      <StatusCard 
        label="Status" 
        status={product.productStatus} 
      />

      <div className="md:col-span-2">
        <label className="block text-sm font-medium text-gray-700">
          Description
        </label>
        <p className="mt-1 text-sm text-gray-700">
          {product.description || "No description provided"}
        </p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Created
        </label>
        <p className="mt-1 text-sm text-gray-900">
          {formatDate(product.createdAt)}
        </p>
        <p className="text-xs text-gray-500">by {product.createdBy}</p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Updated
        </label>
        <p className="mt-1 text-sm text-gray-900">
          {formatDate(product.updatedAt)}
        </p>
        <p className="text-xs text-gray-500">by {product.updatedBy || "N/A"}</p>
      </div>
    </div>
  );
};

export default ProductDetailsCard;
