import { BrowserRouter as Router, Routes, Route, Navigate, Outlet } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import UserGroups from './pages/UserGroups'
import UserList from './pages/UserList'
import UserEdit from './pages/UserEdit'
import RoleList from './pages/RoleList'
import PermissionList from './pages/PermissionList'
import TenantList from './pages/TenantList'
import TenantCreate from './components/TenantCreate'
import TenantEdit from './components/TenantEdit'
import TenantDetail from './components/TenantDetail'
import ProductList from './pages/ProductList'
import ProductCreate from './components/ProductCreate'
import ProductDetail from './components/ProductDetail'
import ProductEdit from './components/ProductEdit'
import ModuleList from './pages/ModuleList'
import ModuleCreate from './components/ModuleCreate'
import ModuleDetail from './components/ModuleDetail'
import ModuleEdit from './components/ModuleEdit'
import Unauthorized from './pages/Unauthorized'
import { useAuth } from './store/auth'

function App() {
  const { isAuthenticated } = useAuth()

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        
        <Route path="/" element={
          <ProtectedRoute>
            <Layout>
              <Outlet />
            </Layout>
          </ProtectedRoute>
        }>
          <Route index element={<Navigate to="/dashboard" replace />} />
          
          <Route path="dashboard" element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          } />
          
          <Route path="user-groups" element={
            <ProtectedRoute permission="user-groups:read">
              <UserGroups />
            </ProtectedRoute>
          } />
          
          <Route path="users" element={
            <ProtectedRoute permission="user:read">
              <UserList />
            </ProtectedRoute>
          } />
          
          <Route path="users/:id/edit" element={
            <ProtectedRoute permission="user:update">
              <UserEdit />
            </ProtectedRoute>
          } />
          
          <Route path="roles" element={
            <ProtectedRoute permission="role:read">
              <RoleList />
            </ProtectedRoute>
          } />
          
          <Route path="permissions" element={
            <ProtectedRoute permission="permission:read">
              <PermissionList />
            </ProtectedRoute>
          } />
          
          <Route path="tenants" element={
            <ProtectedRoute permission="tenants:read">
              <TenantList />
            </ProtectedRoute>
          } />
          
          <Route path="tenants/create" element={
            <ProtectedRoute permission="tenants:create">
              <TenantCreate />
            </ProtectedRoute>
          } />
          
          <Route path="tenants/:id/edit" element={
            <ProtectedRoute permission="tenants:update">
              <TenantEdit />
            </ProtectedRoute>
          } />
          
          <Route path="tenants/:id" element={
            <ProtectedRoute permission="tenants:read">
              <TenantDetail />
            </ProtectedRoute>
          } />
          
          <Route path="products" element={
             <ProtectedRoute permission="product:read">
               <ProductList />
             </ProtectedRoute>
           } />
           
           <Route path="products/create" element={
             <ProtectedRoute permission="product:create">
               <ProductCreate />
             </ProtectedRoute>
           } />
           
           <Route path="products/:id" element={
             <ProtectedRoute permission="product:read">
               <ProductDetail />
             </ProtectedRoute>
           } />
           
           <Route path="products/:id/edit" element={
             <ProtectedRoute permission="product:update">
               <ProductEdit />
             </ProtectedRoute>
           } />
           
           <Route path="modules" element={
             <ProtectedRoute permission="module:read">
               <ModuleList />
             </ProtectedRoute>
           } />
           
           <Route path="modules/create" element={
             <ProtectedRoute permission="module:create">
               <ModuleCreate />
             </ProtectedRoute>
           } />
           
           <Route path="modules/:id" element={
             <ProtectedRoute permission="module:read">
               <ModuleDetail />
             </ProtectedRoute>
           } />
           
           <Route path="modules/:id/edit" element={
             <ProtectedRoute permission="module:update">
               <ModuleEdit />
             </ProtectedRoute>
           } />
        </Route>
        
        <Route path="*" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />} />
      </Routes>
    </Router>
  )
}

export default App
