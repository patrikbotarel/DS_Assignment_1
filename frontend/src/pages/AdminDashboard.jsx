import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import Modal from '../components/Modal';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('users'); // 'users' | 'devices' | 'assignment'
  const [users, setUsers] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null); // { type: 'success' | 'error', text: '' }

  // Modals state
  const [userModalOpen, setUserModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [userFormData, setUserFormData] = useState({ username: '', password: '', name: '', role: 'CLIENT' });

  const [deviceModalOpen, setDeviceModalOpen] = useState(false);
  const [editingDevice, setEditingDevice] = useState(null);
  const [deviceFormData, setDeviceFormData] = useState({ name: '', maxConsumption: '', description: '', userId: '' });

  // Quick Assignment State
  const [selectedDeviceId, setSelectedDeviceId] = useState('');
  const [selectedUserId, setSelectedUserId] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const showNotification = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage(null), 5000);
  };

  const fetchData = async () => {
    try {
      setLoading(true);
      const [usersData, devicesData] = await Promise.all([
        api.get('/user'),
        api.get('/device')
      ]);
      setUsers(usersData || []);
      setDevices(devicesData || []);
    } catch (err) {
      showNotification('error', `Failed to load dashboard data: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  // ---------------- USER CRUD ----------------
  const openCreateUserModal = () => {
    setEditingUser(null);
    setUserFormData({ username: '', password: '', name: '', role: 'CLIENT' });
    setUserModalOpen(true);
  };

  const openEditUserModal = (u) => {
    setEditingUser(u);
    setUserFormData({ username: u.username, password: '', name: u.name || '', role: u.role || 'CLIENT' });
    setUserModalOpen(true);
  };

  const handleSaveUser = async (e) => {
    e.preventDefault();
    try {
      if (editingUser) {
        // Update user
        const updatePayload = {
          username: userFormData.username,
          name: userFormData.name,
          role: userFormData.role
        };
        if (userFormData.password) {
          updatePayload.password = userFormData.password;
        }
        await api.put(`/user/${editingUser.id}`, updatePayload);
        showNotification('success', `User '${userFormData.username}' updated successfully.`);
      } else {
        // Create user
        await api.post('/user', userFormData);
        showNotification('success', `User '${userFormData.username}' created successfully.`);
      }
      setUserModalOpen(false);
      fetchData();
    } catch (err) {
      showNotification('error', `Failed to save user: ${err.message}`);
    }
  };

  const handleDeleteUser = async (userId, username) => {
    if (!window.confirm(`Are you sure you want to delete user '${username}'? Associated devices will be disassociated and credentials deleted.`)) {
      return;
    }
    try {
      await api.del(`/user/${userId}`);
      showNotification('success', `User '${username}' deleted successfully.`);
      fetchData();
    } catch (err) {
      showNotification('error', `Failed to delete user: ${err.message}`);
    }
  };

  // ---------------- DEVICE CRUD ----------------
  const openCreateDeviceModal = () => {
    setEditingDevice(null);
    setDeviceFormData({ name: '', maxConsumption: '', description: '', userId: '' });
    setDeviceModalOpen(true);
  };

  const openEditDeviceModal = (d) => {
    setEditingDevice(d);
    setDeviceFormData({
      name: d.name,
      maxConsumption: d.maxConsumption || '',
      description: d.description || '',
      userId: d.userId || ''
    });
    setDeviceModalOpen(true);
  };

  const handleSaveDevice = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        name: deviceFormData.name,
        maxConsumption: parseFloat(deviceFormData.maxConsumption) || 0,
        description: deviceFormData.description,
        userId: deviceFormData.userId ? deviceFormData.userId : null
      };

      if (editingDevice) {
        await api.put(`/device/${editingDevice.id}`, payload);
        showNotification('success', `Device '${deviceFormData.name}' updated successfully.`);
      } else {
        await api.post('/device', payload);
        showNotification('success', `Device '${deviceFormData.name}' created successfully.`);
      }
      setDeviceModalOpen(false);
      fetchData();
    } catch (err) {
      showNotification('error', `Failed to save device: ${err.message}`);
    }
  };

  const handleDeleteDevice = async (deviceId, name) => {
    if (!window.confirm(`Are you sure you want to delete device '${name}'?`)) {
      return;
    }
    try {
      await api.del(`/device/${deviceId}`);
      showNotification('success', `Device '${name}' deleted successfully.`);
      fetchData();
    } catch (err) {
      showNotification('error', `Failed to delete device: ${err.message}`);
    }
  };

  // ---------------- ASSIGN DEVICE TO USER ----------------
  const handleAssignDevice = async (e) => {
    e.preventDefault();
    if (!selectedDeviceId || !selectedUserId) {
      showNotification('error', 'Please select both a device and a user.');
      return;
    }

    try {
      await api.post(`/device/${selectedDeviceId}/assign/${selectedUserId}`);
      const assignedDev = devices.find(d => d.id === selectedDeviceId);
      const assignedUser = users.find(u => u.id === selectedUserId);
      showNotification('success', `Assigned '${assignedDev?.name}' to user '${assignedUser?.username}' successfully.`);
      setSelectedDeviceId('');
      setSelectedUserId('');
      fetchData();
    } catch (err) {
      showNotification('error', `Failed to assign device: ${err.message}`);
    }
  };

  const handleUnassignDevice = async (deviceId, name) => {
    if (!window.confirm(`Unassign device '${name}' from its current user?`)) {
      return;
    }
    try {
      await api.post(`/device/${deviceId}/unassign`);
      showNotification('success', `Device '${name}' unassigned successfully.`);
      fetchData();
    } catch (err) {
      showNotification('error', `Failed to unassign device: ${err.message}`);
    }
  };

  // Helpers
  const getUserNameById = (userId) => {
    if (!userId) return <span style={{ color: 'var(--text-muted)' }}>Unassigned</span>;
    const found = users.find(u => u.id === userId);
    return found ? `${found.username} (${found.name || 'No name'})` : userId;
  };

  const totalConsumption = devices.reduce((sum, d) => sum + (d.maxConsumption || 0), 0);
  const assignedCount = devices.filter(d => !!d.userId).length;

  return (
    <div className="main-content">
      {/* Top Welcome & Notification */}
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 700 }}>Administrator Dashboard</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Centralized microservices management via Traefik API Gateway (Port 80)
          </p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={fetchData} disabled={loading}>
          🔄 Refresh
        </button>
      </div>

      {message && (
        <div className={`alert ${message.type === 'error' ? 'alert-error' : 'alert-success'}`}>
          <span>{message.type === 'error' ? '❌' : '✅'}</span>
          <span>{message.text}</span>
        </div>
      )}

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">👥</div>
          <div>
            <div className="stat-value">{users.length}</div>
            <div className="stat-label">Registered Users</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">⚡</div>
          <div>
            <div className="stat-value">{devices.length}</div>
            <div className="stat-label">Total Smart Meters</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">🔗</div>
          <div>
            <div className="stat-value">{assignedCount} / {devices.length}</div>
            <div className="stat-label">Assigned Devices</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📊</div>
          <div>
            <div className="stat-value">{totalConsumption.toFixed(1)} <span style={{ fontSize: '0.85rem' }}>kWh</span></div>
            <div className="stat-label">Total Max Capacity</div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs">
        <div
          className={`tab ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          👤 User Management ({users.length})
        </div>
        <div
          className={`tab ${activeTab === 'devices' ? 'active' : ''}`}
          onClick={() => setActiveTab('devices')}
        >
          ⚡ Device Management ({devices.length})
        </div>
        <div
          className={`tab ${activeTab === 'assignment' ? 'active' : ''}`}
          onClick={() => setActiveTab('assignment')}
        >
          🔗 User-Device Mapping
        </div>
      </div>

      {/* TAB 1: USERS CRUD */}
      {activeTab === 'users' && (
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">User Accounts</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                CRUD operations on User Microservice with automated sync to Credential & Device DBs
              </p>
            </div>
            <button className="btn btn-primary btn-sm" onClick={openCreateUserModal}>
              ➕ Add User
            </button>
          </div>

          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Full Name</th>
                  <th>Role</th>
                  <th>Assigned Devices</th>
                  <th>User UUID</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.length === 0 ? (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                      No users found.
                    </td>
                  </tr>
                ) : (
                  users.map((u) => {
                    const userDevicesCount = devices.filter(d => d.userId === u.id).length;
                    return (
                      <tr key={u.id}>
                        <td style={{ fontWeight: 600 }}>{u.username}</td>
                        <td>{u.name || '-'}</td>
                        <td>
                          <span className={`badge ${u.role === 'ADMIN' ? 'badge-admin' : 'badge-client'}`}>
                            {u.role}
                          </span>
                        </td>
                        <td>
                          <span
                            className={`badge ${userDevicesCount > 0 ? 'badge-admin' : 'badge-client'}`}
                            style={{ fontSize: '0.75rem', fontWeight: 600 }}
                          >
                            ⚡ {userDevicesCount} {userDevicesCount === 1 ? 'device' : 'devices'}
                          </span>
                        </td>
                        <td>
                          <span className="token-badge" title={u.id}>{u.id}</span>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                            <button
                              className="btn btn-secondary btn-sm"
                              onClick={() => openEditUserModal(u)}
                            >
                              ✏️ Edit
                            </button>
                            <button
                              className="btn btn-danger btn-sm"
                              onClick={() => handleDeleteUser(u.id, u.username)}
                            >
                              🗑️ Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: DEVICES CRUD */}
      {activeTab === 'devices' && (
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">Smart Energy Devices</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                CRUD operations on Device Microservice with logical foreign-key user reference
              </p>
            </div>
            <button className="btn btn-primary btn-sm" onClick={openCreateDeviceModal}>
              ➕ Add Device
            </button>
          </div>

          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>Device Name</th>
                  <th>Max Consumption (kWh)</th>
                  <th>Description</th>
                  <th>Assigned Client</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {devices.length === 0 ? (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                      No devices found.
                    </td>
                  </tr>
                ) : (
                  devices.map((d) => (
                    <tr key={d.id}>
                      <td style={{ fontWeight: 600 }}>{d.name}</td>
                      <td>
                        <strong style={{ color: 'var(--primary)' }}>{d.maxConsumption}</strong> kWh
                      </td>
                      <td>{d.description || '-'}</td>
                      <td>{getUserNameById(d.userId)}</td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => openEditDeviceModal(d)}
                          >
                            ✏️ Edit
                          </button>
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => handleDeleteDevice(d.id, d.name)}
                          >
                            🗑️ Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 3: DEVICE-USER MAPPING */}
      {activeTab === 'assignment' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', maxWidth: 900, margin: '0 auto' }}>
          {/* Assignment Form Card */}
          <div className="card">
            <h3 className="card-title" style={{ marginBottom: '0.5rem' }}>Assign Device to Client (One-to-Many)</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', marginBottom: '1.5rem' }}>
              Map any energy smart metering device to a specific client account. A single client can have <strong>multiple devices</strong> associated simultaneously.
            </p>

            <form onSubmit={handleAssignDevice}>
              <div className="form-group">
                <label htmlFor="selectDevice">Select Smart Meter Device</label>
                <select
                  id="selectDevice"
                  className="form-control"
                  value={selectedDeviceId}
                  onChange={(e) => setSelectedDeviceId(e.target.value)}
                  required
                >
                  <option value="">-- Choose a Device --</option>
                  {devices.map((d) => {
                    const currentOwner = users.find(u => u.id === d.userId);
                    return (
                      <option key={d.id} value={d.id}>
                        {d.name} ({d.maxConsumption} kWh) - [{currentOwner ? `Assigned to: ${currentOwner.username}` : 'Unassigned'}]
                      </option>
                    );
                  })}
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="selectUser">Select Client / User (Multiple Devices Allowed per User)</label>
                <select
                  id="selectUser"
                  className="form-control"
                  value={selectedUserId}
                  onChange={(e) => setSelectedUserId(e.target.value)}
                  required
                >
                  <option value="">-- Choose a User --</option>
                  {users.map((u) => {
                    const userDeviceCount = devices.filter(d => d.userId === u.id).length;
                    return (
                      <option key={u.id} value={u.id}>
                        {u.username} [{u.role}] {u.name ? `- ${u.name}` : ''} - ({userDeviceCount} device{userDeviceCount === 1 ? '' : 's'} assigned)
                      </option>
                    );
                  })}
                </select>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '0.5rem' }}>
                🔗 Save Device Assignment
              </button>
            </form>
          </div>

          {/* Current Allocations Table */}
          <div className="card">
            <div className="card-header">
              <div>
                <h3 className="card-title">Current Allocations & One-to-Many Client Map</h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  Live overview of all smart meters mapped to client accounts ({assignedCount} of {devices.length} assigned)
                </p>
              </div>
            </div>

            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>Device</th>
                    <th>Capacity</th>
                    <th>Assigned Client</th>
                    <th>Client Role</th>
                    <th style={{ textAlign: 'right' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {devices.length === 0 ? (
                    <tr>
                      <td colSpan="5" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                        No devices available.
                      </td>
                    </tr>
                  ) : (
                    devices.map((d) => {
                      const client = users.find(u => u.id === d.userId);
                      return (
                        <tr key={d.id}>
                          <td style={{ fontWeight: 600 }}>{d.name}</td>
                          <td>{d.maxConsumption} kWh</td>
                          <td>
                            {client ? (
                              <span style={{ fontWeight: 600, color: 'var(--primary)' }}>
                                👤 {client.username} {client.name ? `(${client.name})` : ''}
                              </span>
                            ) : (
                              <span style={{ color: 'var(--text-muted)' }}>Unassigned</span>
                            )}
                          </td>
                          <td>
                            {client ? (
                              <span className={`badge ${client.role === 'ADMIN' ? 'badge-admin' : 'badge-client'}`}>
                                {client.role}
                              </span>
                            ) : (
                              '-'
                            )}
                          </td>
                          <td style={{ textAlign: 'right' }}>
                            {d.userId ? (
                              <button
                                type="button"
                                className="btn btn-secondary btn-sm"
                                style={{ color: '#ef4444' }}
                                onClick={() => handleUnassignDevice(d.id, d.name)}
                              >
                                ❌ Unassign
                              </button>
                            ) : (
                              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Available</span>
                            )}
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* USER MODAL */}
      <Modal
        isOpen={userModalOpen}
        onClose={() => setUserModalOpen(false)}
        title={editingUser ? `Edit User: ${editingUser.username}` : 'Create New User'}
      >
        <form onSubmit={handleSaveUser}>
          <div className="modal-body">
            <div className="form-group">
              <label htmlFor="u_username">Username</label>
              <input
                id="u_username"
                type="text"
                className="form-control"
                value={userFormData.username}
                onChange={(e) => setUserFormData({ ...userFormData, username: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="u_password">
                Password {editingUser && <span style={{ fontWeight: 'normal', color: 'var(--text-muted)' }}>(leave blank to keep unchanged)</span>}
              </label>
              <input
                id="u_password"
                type="password"
                className="form-control"
                placeholder={editingUser ? '••••••••' : 'Enter password'}
                value={userFormData.password}
                onChange={(e) => setUserFormData({ ...userFormData, password: e.target.value })}
                required={!editingUser}
              />
            </div>

            <div className="form-group">
              <label htmlFor="u_name">Full Name</label>
              <input
                id="u_name"
                type="text"
                className="form-control"
                value={userFormData.name}
                onChange={(e) => setUserFormData({ ...userFormData, name: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label htmlFor="u_role">Role</label>
              <select
                id="u_role"
                className="form-control"
                value={userFormData.role}
                onChange={(e) => setUserFormData({ ...userFormData, role: e.target.value })}
              >
                <option value="CLIENT">CLIENT</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setUserModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {editingUser ? 'Save Changes' : 'Create User'}
            </button>
          </div>
        </form>
      </Modal>

      {/* DEVICE MODAL */}
      <Modal
        isOpen={deviceModalOpen}
        onClose={() => setDeviceModalOpen(false)}
        title={editingDevice ? `Edit Device: ${editingDevice.name}` : 'Create New Smart Meter Device'}
      >
        <form onSubmit={handleSaveDevice}>
          <div className="modal-body">
            <div className="form-group">
              <label htmlFor="d_name">Device Name</label>
              <input
                id="d_name"
                type="text"
                className="form-control"
                placeholder="e.g. Smart Meter Kitchen"
                value={deviceFormData.name}
                onChange={(e) => setDeviceFormData({ ...deviceFormData, name: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="d_max">Max Consumption (kWh)</label>
              <input
                id="d_max"
                type="number"
                step="0.1"
                min="0"
                className="form-control"
                placeholder="e.g. 150.0"
                value={deviceFormData.maxConsumption}
                onChange={(e) => setDeviceFormData({ ...deviceFormData, maxConsumption: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="d_desc">Description / Location</label>
              <input
                id="d_desc"
                type="text"
                className="form-control"
                placeholder="e.g. Ground Floor AC"
                value={deviceFormData.description}
                onChange={(e) => setDeviceFormData({ ...deviceFormData, description: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label htmlFor="d_user">Assign To User</label>
              <select
                id="d_user"
                className="form-control"
                value={deviceFormData.userId}
                onChange={(e) => setDeviceFormData({ ...deviceFormData, userId: e.target.value })}
              >
                <option value="">-- Unassigned --</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.username} ({u.role})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setDeviceModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {editingDevice ? 'Save Changes' : 'Create Device'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
