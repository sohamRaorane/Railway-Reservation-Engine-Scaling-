/**
 * Centralized HTTP client for the Railway Reservation Engine.
 *
 * - baseURL points to the monolith today (localhost:8080). When the
 *   backend is split into microservices behind a Gateway (Day 48),
 *   only the env variable VITE_API_BASE_URL needs to change.
 * - Attaches the JWT Bearer token from localStorage on every request.
 * - Handles 401 by attempting a refresh-token rotation once.
 * - Exposes a helper to generate Idempotency-Key for POST /bookings.
 */

import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Attach JWT if present
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// On 401, try to refresh the access token once using the stored refresh token
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Only attempt refresh for 401 and if we haven't already retried
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        return Promise.reject(error)
      }

      try {
        const res = await axios.post('/api/v1/auth/refresh', {
          refreshToken,
        })
        const newAccessToken = res.data.accessToken
        localStorage.setItem('accessToken', newAccessToken)
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        // Refresh failed — clear auth and redirect to login
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

/**
 * Generate a UUID v4 for Idempotency-Key header.
 * The backend uses this to make POST /bookings idempotent —
 * retrying the same key returns the same PNR instead of double-booking.
 */
export function generateIdempotencyKey() {
  return crypto.randomUUID()
}

export default api
