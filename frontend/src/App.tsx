import { BrowserRouter as Router, Routes, Route, Navigate, Outlet } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import UserGroups from './pages/UserGroups'
import TenantList from './pages/TenantList'
import TenantCreate from './pages/TenantCreate'
import TenantDetail from './pages/TenantDetail'
import ProductList from './pages/ProductList'
import ProductCreate from './pages/ProductCreate'
import ProductDetail from './pages/ProductDetail'
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
        </Route>
        
        <Route path="*" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />} />
      </Routes>
    </Router>
  )
}

export default App
