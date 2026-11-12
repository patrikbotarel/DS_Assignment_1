import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    if (e) e.preventDefault();
    if (!username.trim() || !password) {
      setError('Please provide both username and password.');
      return;
    }

    try {
      setError(null);
      setLoading(true);
      const user = await login(username.trim(), password);
      if (user.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/client');
      }
    } catch (err) {
      setError(err.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (quickUser, quickPass) => {
    setUsername(quickUser);
    setPassword(quickPass);
    login(quickUser, quickPass)
      .then((user) => {
        if (user.role === 'ADMIN') {
          navigate('/admin');
        } else {
          navigate('/client');
        }
      })
      .catch((err) => {
        setError(err.message || 'Quick login failed.');
      });
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-brand">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ width: 44, height: 44 }}>
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" fill="#2563eb" stroke="none" />
          </svg>
          <h1>Energy Management</h1>
          <p>Sign in to access your smart energy dashboard</p>
        </div>

        {error && (
          <div className="alert alert-error">
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              className="form-control"
              placeholder="e.g. admin or client"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              className="form-control"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.5rem', padding: '0.75rem' }}
            disabled={loading}
          >
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div style={{ marginTop: '1.75rem', paddingTop: '1.25rem', borderTop: '1px solid var(--border)' }}>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'center', marginBottom: '0.6rem' }}>
            ⚡ Demo Credentials (Click for 1-Click Login):
          </p>
          <div className="quick-login-btns">
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => handleQuickLogin('admin', 'admin123')}
            >
              👑 Login as Admin
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => handleQuickLogin('client', 'client123')}
            >
              👤 Login as Client
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
