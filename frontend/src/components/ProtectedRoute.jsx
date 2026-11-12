import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ allowedRole, children }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRole && user?.role !== allowedRole) {
    // If Admin attempts to access Client page, redirect to Admin dashboard
    if (user?.role === 'ADMIN') {
      return <Navigate to="/admin" replace />;
    }
    // If Client attempts to access Admin page, redirect to Unauthorized page
    return <Navigate to="/unauthorized" replace />;
  }

  return children ? children : <Outlet />;
}
