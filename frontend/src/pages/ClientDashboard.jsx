import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';

export default function ClientDashboard() {
  const { user } = useAuth();
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (user?.id) {
      fetchMyDevices();
    }
  }, [user]);

  const fetchMyDevices = async () => {
    try {
      setLoading(true);
      setError(null);
      // Client strictly requests their own devices: GET /device/user/{userId} via Traefik Gateway
      const data = await api.get(`/device/user/${user.id}`);
      setDevices(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load your assigned devices.');
    } finally {
      setLoading(false);
    }
  };

  const totalCapacity = devices.reduce((sum, d) => sum + (d.maxConsumption || 0), 0);

  return (
    <div className="main-content">
      {/* Header */}
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 700 }}>Client Energy Portal</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Welcome, <strong>{user?.username}</strong>. View your assigned smart metering equipment.
          </p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={fetchMyDevices} disabled={loading}>
          🔄 Refresh Devices
        </button>
      </div>

      {/* RBAC Info Banner */}
      <div className="alert alert-info">
        <span>🛡️</span>
        <div>
          <strong>Role-Based Access Control (RBAC):</strong> You are authenticated with the <strong>CLIENT</strong> role.
          You can exclusively view devices linked directly to your Account ID (<span className="token-badge">{user?.id}</span>).
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          <span>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {/* Summary Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#e0e7ff', color: '#4338ca' }}>⚡</div>
          <div>
            <div className="stat-value">{devices.length}</div>
            <div className="stat-label">Assigned Smart Meters</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#ecfdf5', color: '#059669' }}>📈</div>
          <div>
            <div className="stat-value">{totalCapacity.toFixed(1)} <span style={{ fontSize: '0.85rem' }}>kWh</span></div>
            <div className="stat-label">Total Hourly Allocation</div>
          </div>
        </div>
      </div>

      {/* Devices List */}
      <div className="card">
        <div className="card-header">
          <div>
            <h3 className="card-title">My Smart Energy Devices</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              Real-time devices transmitting telemetry to your account
            </p>
          </div>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Loading your devices...
          </div>
        ) : devices.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>🔌</div>
            <h4>No devices assigned yet</h4>
            <p style={{ fontSize: '0.9rem', marginTop: '0.25rem' }}>
              Please contact the system Administrator to assign smart metering equipment to your account.
            </p>
          </div>
        ) : (
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>Device Name</th>
                  <th>Max Hourly Consumption (kWh)</th>
                  <th>Location / Description</th>
                  <th>Device ID</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {devices.map((device) => (
                  <tr key={device.id}>
                    <td style={{ fontWeight: 600 }}>{device.name}</td>
                    <td>
                      <span style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--primary)' }}>
                        {device.maxConsumption}
                      </span>{' '}
                      <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>kWh</span>
                    </td>
                    <td>{device.description || 'General Location'}</td>
                    <td>
                      <span className="token-badge" title={device.id}>{device.id}</span>
                    </td>
                    <td>
                      <span className="badge" style={{ background: '#ecfdf5', color: '#065f46', border: '1px solid #a7f3d0' }}>
                        Active
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
