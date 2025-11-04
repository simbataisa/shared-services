import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  Outlet,
} from "react-router-dom";
import Layout from "./components/common/Layout";
import ProtectedRoute from "./components/common/ProtectedRoute";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import UserGroups from "./pages/UserGroups";
import UserGroupDetail from "./components/user-groups/UserGroupDetail";
import UserGroupCreate from "./components/user-groups/UserGroupCreate";
import UserList from "./pages/UserList";
import UserDetail from "./components/users/UserDetail";
import UserCreate from "./components/users/UserCreate";
import RoleList from "./pages/RoleList";
import RoleDetail from "./components/role/RoleDetail";
import RoleForm from "./components/role/RoleForm";
import PermissionList from "./pages/PermissionList";
import PermissionDetail from "./components/permission/PermissionDetail";
import TenantList from "./pages/TenantList";
import TenantCreate from "./components/tenant/TenantCreate";
import TenantDetail from "./components/tenant/TenantDetail";
import ProductList from "./pages/ProductList";
import ProductCreate from "./components/product/ProductCreate";
import ProductDetail from "./components/product/ProductDetail";
import ProductEdit from "./components/product/ProductEdit";
import ModuleList from "./pages/ModuleList";
import ModuleCreate from "./components/module/ModuleCreate";
import ModuleDetail from "./components/module/ModuleDetail";
import ModuleEdit from "./components/module/ModuleEdit";
import PaymentRequestList from "./components/payment/PaymentRequestList";
import PaymentRequestCreate from "./components/payment/PaymentRequestCreate";
import PaymentRequestDetail from "./components/payment/PaymentRequestDetail";
import PaymentTransactionList from "./components/payment/transaction/PaymentTransactionList";
import PaymentTransactionDetail from "./components/payment/transaction/PaymentTransactionDetail";
import PaymentRefundList from "./components/payment/refund/PaymentRefundList";
import PaymentAuditLogList from "./components/payment/audit/PaymentAuditLogList";
import Unauthorized from "./pages/Unauthorized";
import ErrorDemoPage from "./pages/ErrorDemoPage";
import { useAuth } from "./store/auth";
import GlobalLoader from "@/components/common/GlobalLoader";
import { ErrorBoundary } from "./components/common";
import PaymentRefundDetail from "./components/payment/refund/PaymentRefundDetail";

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <Router>
      <GlobalLoader />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/unauthorized" element={<Unauthorized />} />

        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout>
                <Outlet />
              </Layout>
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />

          <Route
            path="dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="user-groups"
            element={
              <ProtectedRoute permission="GROUP_MGMT:read">
                <UserGroups />
              </ProtectedRoute>
            }
          />

          <Route
            path="user-groups/create"
            element={
              <ProtectedRoute permission="GROUP_MGMT:create">
                <UserGroupCreate />
              </ProtectedRoute>
            }
          />

          <Route
            path="user-groups/:id"
            element={
              <ProtectedRoute permission="GROUP_MGMT:read">
                <UserGroupDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="users"
            element={
              <ProtectedRoute permission="USER_MGMT:read">
                <UserList />
              </ProtectedRoute>
            }
          />

          <Route
            path="users/create"
            element={
              <ProtectedRoute permission="USER_MGMT:create">
                <UserCreate />
              </ProtectedRoute>
            }
          />

          <Route
            path="users/:id"
            element={
              <ProtectedRoute permission="USER_MGMT:read">
                <ErrorBoundary>
                  <UserDetail />
                </ErrorBoundary>
              </ProtectedRoute>
            }
          />

          <Route
            path="users/:id/edit"
            element={
              <ProtectedRoute permission="USER_MGMT:update">
                <Navigate to="../" replace />
              </ProtectedRoute>
            }
          />

          <Route
            path="roles"
            element={
              <ProtectedRoute permission="ROLE_MGMT:read">
                <RoleList roles={[]} permissions={[]} />
              </ProtectedRoute>
            }
          />

          <Route
            path="roles/new"
            element={
              <ProtectedRoute permission="ROLE_MGMT:create">
                <RoleForm mode="create" />
              </ProtectedRoute>
            }
          />

          <Route
            path="roles/:id"
            element={
              <ProtectedRoute permission="ROLE_MGMT:read">
                <RoleDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="roles/:id/edit"
            element={
              <ProtectedRoute permission="ROLE_MGMT:update">
                <RoleForm mode="edit" />
              </ProtectedRoute>
            }
          />

          <Route
            path="permissions"
            element={
              <ProtectedRoute permission="PERMISSION_MGMT:read">
                <PermissionList />
              </ProtectedRoute>
            }
          />

          <Route
            path="permissions/:id"
            element={
              <ProtectedRoute permission="PERMISSION_MGMT:read">
                <PermissionDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="tenants"
            element={
              <ProtectedRoute permission="TENANT_MGMT:read">
                <TenantList />
              </ProtectedRoute>
            }
          />

          <Route
            path="tenants/create"
            element={
              <ProtectedRoute permission="TENANT_MGMT:create">
                <TenantCreate />
              </ProtectedRoute>
            }
          />

          <Route
            path="tenants/:id/edit"
            element={
              <ProtectedRoute permission="TENANT_MGMT:update">
                <TenantDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="tenants/:id"
            element={
              <ProtectedRoute permission="TENANT_MGMT:read">
                <TenantDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="products"
            element={
              <ProtectedRoute permission="PRODUCT_MGMT:read">
                <ProductList />
              </ProtectedRoute>
            }
          />

          <Route
            path="products/create"
            element={
              <ProtectedRoute permission="PRODUCT_MGMT:create">
                <ProductCreate />
              </ProtectedRoute>
            }
          />

          <Route
            path="products/:id"
            element={
              <ProtectedRoute permission="PRODUCT_MGMT:read">
                <ProductDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="products/:id/edit"
            element={
              <ProtectedRoute permission="PRODUCT_MGMT:update">
                <ProductEdit />
              </ProtectedRoute>
            }
          />

          <Route
            path="modules"
            element={
              <ProtectedRoute permission="MODULE_MGMT:read">
                <ModuleList />
              </ProtectedRoute>
            }
          />

          <Route
            path="modules/create"
            element={
              <ProtectedRoute permission="MODULE_MGMT:create">
                <ModuleCreate />
              </ProtectedRoute>
            }
          />

          <Route
            path="modules/:id"
            element={
              <ProtectedRoute permission="MODULE_MGMT:read">
                <ModuleDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="modules/:id/edit"
            element={
              <ProtectedRoute permission="MODULE_MGMT:update">
                <ModuleEdit />
              </ProtectedRoute>
            }
          />

          <Route
            path="payments"
            element={
              <ProtectedRoute permission="PAYMENT_MGMT:read">
                <PaymentRequestList />
              </ProtectedRoute>
            }
          />

          <Route
            path="payments/requests/new"
            element={
              <ProtectedRoute permission="PAYMENT_MGMT:create">
                <PaymentRequestCreate />
              </ProtectedRoute>
            }
          />

          <Route
            path="payments/requests/:id"
            element={
              <ProtectedRoute permission="PAYMENT_MGMT:read">
                <PaymentRequestDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="payments/transactions"
            element={
              <ProtectedRoute permission="PAYMENT_MGMT:read">
                <PaymentTransactionList />
              </ProtectedRoute>
            }
          />

          <Route
            path="payments/transactions/:id"
            element={
              <ProtectedRoute permission="PAYMENT_MGMT:read">
                <PaymentTransactionDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="payments/refunds"
            element={
              <ProtectedRoute permission="PAYMENT_MGMT:read">
                <PaymentRefundList data={[]} permissions={[]} />
              </ProtectedRoute>
            }
          />

          <Route
            path="payments/refunds/:id"
            element={
              <ProtectedRoute permission="PAYMENT_MGMT:read">
                <PaymentRefundDetail />
              </ProtectedRoute>
            }
          />

          <Route
            path="payments/audit-logs"
            element={
              <ProtectedRoute permission="PAYMENT_MGMT:read">
                <PaymentAuditLogList />
              </ProtectedRoute>
            }
          />

          <Route path="error-demo" element={<ErrorDemoPage />} />
        </Route>

        <Route
          path="*"
          element={
            <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
