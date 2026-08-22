/**
 * AuthContext — holds the authenticated user and tokens for the whole app.
 *
 * Why Context and not Redux: this app is a visualization layer for the
 * monolith's auth flow (register → login → JWT + refresh). A single
 * context is simpler, easier to review, and matches the backend's
 * stateless-JWT + DB-backed refresh-token design.
 *
 * Tokens are stored in localStorage so a page refresh does not log the user out.
 * The API client (api/client.js) reads accessToken from here on every request.
 */

import { createContext, useContext, useState, useEffect } from 'react'
import api from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })
  const [loading, setLoading] = useState(false)

  const isAuthenticated = !!user

  // Register a new user — delegates to POST /api/v1/auth/register
  async function register(payload) {
    setLoading(true)
    try {
      const res = await api.post('/api/v1/auth/register', payload)
      return res.data
    } finally {
      setLoading(false)
    }
  }

  // Login — stores accessToken, refreshToken, and user info
  async function login(email, password) {
    setLoading(true)
    try {
      const res = await api.post('/api/v1/auth/login', { email, password })
      const { accessToken, refreshToken } = res.data

      // Decode user info from the refresh flow or store minimal info
      // The backend returns tokens; we keep email as the identifier for now
      const userData = { email }
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(userData))
      setUser(userData)
      return res.data
    } finally {
      setLoading(false)
    }
  }

  // Logout — revokes the refresh token on the server, then clears local state
  async function logout() {
    const refreshToken = localStorage.getItem('refreshToken')
    try {
      if (refreshToken) {
        await api.post('/api/v1/auth/logout', { refreshToken })
      }
    } catch {
      // Even if server logout fails, clear local state
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      setUser(null)
    }
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, loading, register, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
