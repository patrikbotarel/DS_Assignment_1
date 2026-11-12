import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import AdminDashboard from './pages/AdminDashboard';
import ClientDashboard from './pages/ClientDashboard';
import UnauthorizedPage from './pages/UnauthorizedPage';

export default function App() {
  const { isAuthenticated, user } = useAuth();

  const getDefaultRedirect = () => {
    if (!isAuthenticated) return '/login';
    return user?.role === 'ADMIN' ? '/admin' : '/client';
  };

  return (
    <div className="app-container">
      {isAuthenticated && <Navbar />}

      <Routes>
        {/* Public Routes */}
        <Route
          path="/login"
          element={isAuthenticated ? <Navigate to={getDefaultRedirect()} replace /> : <LoginPage />}
        />

        {/* Protected Admin Routes */}
        <Route element={<ProtectedRoute allowedRole="ADMIN" />}>
          <Route path="/admin" element={<AdminDashboard />} />
        </Route>

        {/* Protected Client Routes */}
        <Route element={<ProtectedRoute allowedRole="CLIENT" />}>
          <Route path="/client" element={<ClientDashboard />} />
        </Route>

        {/* Unauthorized Route */}
        <Route path="/unauthorized" element={<UnauthorizedPage />} />

        {/* Root Redirect */}
        <Route path="/" element={<Navigate to={getDefaultRedirect()} replace />} />

        {/* Catch-all fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}
