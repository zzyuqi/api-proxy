import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Auto-attach Basic Auth header
api.interceptors.request.use(config => {
  const auth = localStorage.getItem('admin_auth')
  if (auth) {
    config.headers.Authorization = 'Basic ' + auth
  }
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('admin_auth')
      window.location.hash = '#/login'
    }
    return Promise.reject(err)
  }
)

// Admin init
export const initAdmin = (username, password) =>
  api.post('/admin/init', { username, password })

// Routes
export const getRoutes = () => api.get('/admin/routes')
export const createRoute = data => api.post('/admin/routes', data)
export const updateRoute = (id, data) => api.put(`/admin/routes/${id}`, data)
export const deleteRoute = id => api.delete(`/admin/routes/${id}`)

// API Keys
export const getApiKeys = () => api.get('/admin/api-keys')
export const createApiKey = name => api.post('/admin/api-keys', { name })
export const updateApiKey = (id, data) => api.put(`/admin/api-keys/${id}`, data)
export const deleteApiKey = id => api.delete(`/admin/api-keys/${id}`)

// Models
export const getModels = () => api.get('/admin/models')
export const createModel = data => api.post('/admin/models', data)
export const updateModel = (id, data) => api.put(`/admin/models/${id}`, data)
export const deleteModel = id => api.delete(`/admin/models/${id}`)

// Logs
export const getLogs = params => api.get('/admin/logs', { params })

// Users
export const getUsers = () => api.get('/admin/users')
export const createUser = data => api.post('/admin/users', data)
export const updateUser = (id, data) => api.put(`/admin/users/${id}`, data)
export const deleteUser = id => api.delete(`/admin/users/${id}`)
export const getUserStats = () => api.get('/admin/users/stats')

// Token allocation
export const allocateTokens = (userId, data) => api.post(`/admin/users/${userId}/tokens`, data)
export const getUserTokenRecords = userId => api.get(`/admin/users/${userId}/tokens`)

// Auth (no auth header needed)
export const userRegister = data => api.post('/auth/register', data)
export const userLogin = data => api.post('/auth/login', data)

// Public routes (no auth needed)
export const getPublicRoutes = () => api.get('/public/routes')

export default api
