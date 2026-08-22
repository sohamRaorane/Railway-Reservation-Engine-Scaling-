/**
 * Register — creates a new user account.
 *
 * Maps 1:1 to auth/dto/RegisterRequest.java:15 — the five fields
 * (name, email, password, gender, dateOfBirth) are sent as JSON to
 * POST /api/v1/auth/register. The backend hashes the password with
 * BCrypt and initializes role=USER, kycStatus=PENDING, and
 * is_defence_personnel=false (fixed in User.java:85 + AuthService.java:69).
 *
 * Gender note: the DB CHECK allows MALE/FEMALE/OTHER, while the Java
 * enum is MALE/FEMALE/OTHERS — we only offer MALE/FEMALE here to stay
 * safe on both sides. Date of birth drives Senior Citizen quota
 * eligibility (M ≥60, F ≥58) validated on the booking side.
 */

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { register, loading } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    gender: 'MALE',
    dateOfBirth: '',
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccess('')

    try {
      await register(form)
      setSuccess('Account created! Redirecting to login...')
      setTimeout(() => navigate('/login'), 1200)
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Registration failed. The email may already be taken.'
      setError(msg)
    }
  }

  return (
    <div className="container" style={{ maxWidth: 480 }}>
      <h1 className="page-title">Create Account</h1>
      <p className="page-subtitle">All fields are required. You will log in right after.</p>

      <div className="card">
        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Full Name</label>
            <input
              id="name"
              type="text"
              placeholder="Soham Raorane"
              value={form.name}
              onChange={(e) => update('name', e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              placeholder="soham@example.com"
              value={form.email}
              onChange={(e) => update('email', e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="at least 6 characters"
              value={form.password}
              onChange={(e) => update('password', e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="gender">Gender</label>
            <select
              id="gender"
              value={form.gender}
              onChange={(e) => update('gender', e.target.value)}
            >
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="dob">Date of Birth</label>
            <input
              id="dob"
              type="date"
              value={form.dateOfBirth}
              onChange={(e) => update('dateOfBirth', e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Creating...' : 'Register'}
          </button>
        </form>

        <p style={{ marginTop: 16, fontSize: 13, textAlign: 'center' }}>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>
    </div>
  )
}
