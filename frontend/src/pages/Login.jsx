/**
 * Login — authenticates an existing user.
 *
 * Flow: user enters email + password → AuthContext.login() calls
 * POST /api/v1/auth/login → backend verifies BCrypt hash via
 * AuthenticationManager → returns accessToken + refreshToken →
 * stored in localStorage → Header updates to show the logged-in user.
 *
 * Seeded test account: aarav@example.com / password
 * (all seeded users share the same password for easy testing).
 */

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { login, loading } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      // Backend returns 401 with "Bad credentials" for wrong password,
      // or 500 if the global exception handler wraps it — show the message
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Login failed. Check your credentials.'
      setError(msg)
    }
  }

  return (
    <div className="container" style={{ maxWidth: 440 }}>
      <h1 className="page-title">Login</h1>
      <p className="page-subtitle">Use a seeded account like aarav@example.com / password</p>

      <div className="card">
        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              placeholder="aarav@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Signing in...' : 'Login'}
          </button>
        </form>

        <p style={{ marginTop: 16, fontSize: 13, textAlign: 'center' }}>
          No account? <Link to="/register">Register here</Link>
        </p>
      </div>
    </div>
  )
}
