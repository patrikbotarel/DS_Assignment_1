import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('ems_token') || null);
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('ems_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Validate that stored state is intact
    if (token && !user) {
      localStorage.removeItem('ems_token');
      setToken(null);
    }
    setLoading(false);
  }, [token, user]);

  const login = async (username, password) => {
    // Auth login endpoint through Traefik Gateway: POST /auth/login
    const data = await api.post('/auth/login', { username, password });
    
    const userData = {
      id: data.id,
      username: data.username,
      role: (data.role || 'CLIENT').toUpperCase()
    };

    localStorage.setItem('ems_token', data.token);
    localStorage.setItem('ems_user', JSON.stringify(userData));

    setToken(data.token);
    setUser(userData);

    return userData;
  };

  const logout = () => {
    localStorage.removeItem('ems_token');
    localStorage.removeItem('ems_user');
    setToken(null);
    setUser(null);
  };

  const value = {
    token,
    user,
    loading,
    isAuthenticated: !!token && !!user,
    isAdmin: user?.role === 'ADMIN',
    isClient: user?.role === 'CLIENT',
    login,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
