/**
 * Header — top navigation bar shown on every page.
 *
 * Links map 1:1 to the backend's feature slices so the frontend
 * stays aligned with the future microservice boundaries (Search,
 * Booking-Core, Payment, Notification).
 */

import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Header.css'

export default function Header() {
  const { isAuthenticated, user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <header className="header">
      <div className="header-inner">
        <Link to="/" className="header-logo">
          Railway Engine
        </Link>

        <nav className="header-nav">
          <Link to="/">Home</Link>
          <Link to="/search">Search</Link>
          <Link to="/book">Book</Link>
          <Link to="/pnr">PNR</Link>
          <Link to="/payment">Payment</Link>
          <Link to="/chart">Chart</Link>
        </nav>

        <div className="header-auth">
          {isAuthenticated ? (
            <>
              <span className="header-user">{user?.email}</span>
              <button className="btn btn-small" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-small btn-outline">
                Login
              </Link>
              <Link to="/register" className="btn btn-small">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
