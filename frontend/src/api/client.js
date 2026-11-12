/**
 * Centralized API Client for Energy Management System
 * Routes all HTTP traffic through API Gateway (Traefik, port 80).
 * Injects JWT token and logs explicit traceability information to browser console.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost';

export async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = localStorage.getItem('ems_token');

  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  // Attach JWT token if available
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;

    // DEMO LOGGING (Required for presentation to professor)
    console.group(`%c🚀 [HTTP REQUEST] ${options.method || 'GET'} ${url}`, 'color: #2563eb; font-weight: bold;');
    console.log(
      `%c[JWT TOKEN ATTACHED]:%c\nBearer ${token}`,
      'color: #10b981; font-weight: bold;',
      'color: #64748b; font-family: monospace; font-size: 11px;'
    );
    if (options.body) {
      console.log('%c[PAYLOAD]:%c', 'color: #f59e0b; font-weight: bold;', 'color: inherit;', options.body);
    }
    console.groupEnd();
  } else {
    console.log(`%cℹ️ [HTTP REQUEST - NO AUTH] ${options.method || 'GET'} ${url}`, 'color: #64748b;');
  }

  const response = await fetch(url, {
    ...options,
    headers
  });

  // Log response status for traceability
  console.log(
    `%c📡 [HTTP RESPONSE] ${response.status} ${response.statusText} %c${options.method || 'GET'} ${endpoint}`,
    response.ok ? 'color: #10b981; font-weight: bold;' : 'color: #ef4444; font-weight: bold;',
    'color: #64748b;'
  );

  if (response.status === 401) {
    console.warn('[HTTP 401] Unauthorized access - clearing stored token');
    localStorage.removeItem('ems_token');
    localStorage.removeItem('ems_user');
    if (!window.location.pathname.includes('/login')) {
      window.location.href = '/login';
    }
    throw new Error('Unauthorized: Invalid or expired session.');
  }

  if (response.status === 403) {
    console.warn('[HTTP 403] Forbidden - Insufficient role privileges for resource:', endpoint);
    throw new Error('Forbidden: You do not have permission to perform this action.');
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const errorMsg = (data && (data.message || data.error)) || `HTTP ${response.status}: ${response.statusText}`;
    throw new Error(errorMsg);
  }

  return data;
}

export const api = {
  get: (endpoint, options = {}) => request(endpoint, { ...options, method: 'GET' }),
  post: (endpoint, body, options = {}) => request(endpoint, { ...options, method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: (endpoint, body, options = {}) => request(endpoint, { ...options, method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  patch: (endpoint, body, options = {}) => request(endpoint, { ...options, method: 'PATCH', body: body ? JSON.stringify(body) : undefined }),
  del: (endpoint, options = {}) => request(endpoint, { ...options, method: 'DELETE' })
};
