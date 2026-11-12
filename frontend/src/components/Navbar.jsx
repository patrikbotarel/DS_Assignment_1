import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';

export default function Navbar() {
  const { user, token, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const copyTokenToClipboard = () => {
    if (token) {
      navigator.clipboard.writeText(token);
      alert('JWT Token copied to clipboard!');
    }
  };

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" fill="#2563eb" stroke="none" />
        </svg>
        <span>EnergyMS</span>
      </Link>

      <div className="navbar-user">
        {user && (
          <>
            <div className="user-info">
              <span>{user.username}</span>
              <span className={`badge ${isAdmin ? 'badge-admin' : 'badge-client'}`}>
                {user.role}
              </span>
            </div>

            {token && (
              <button
                className="btn btn-secondary btn-sm"
                title="Click to copy JWT token for demo"
                onClick={copyTokenToClipboard}
              >
                📋 Copy JWT
              </button>
            )}

            <button className="btn btn-secondary btn-sm" onClick={handleLogout}>
              Logout
            </button>
          </>
        )}
      </div>
    </header>
  );
}
