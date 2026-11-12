import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function UnauthorizedPage() {
  const { user } = useAuth();
  const returnPath = user?.role === 'ADMIN' ? '/admin' : '/client';

  return (
    <div className="main-content" style={{ textAlign: 'center', paddingTop: '5rem' }}>
      <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>⛔</div>
      <h1 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '0.75rem' }}>403 - Access Forbidden</h1>
      <p style={{ color: 'var(--text-muted)', maxWidth: 480, margin: '0 auto 2rem' }}>
        You do not have the required role privileges to access this page. The system enforces strict Role-Based Access Control (RBAC).
      </p>
      <Link to={returnPath} className="btn btn-primary">
        Return to My Dashboard
      </Link>
    </div>
  );
}
