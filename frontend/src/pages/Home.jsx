/**
 * Home — landing page that explains the system at a glance.
 * No backend call — just orientation for someone opening the app first time.
 */

import { Link } from 'react-router-dom'

export default function Home() {
  return (
    <div className="container">
      <h1 className="page-title">Railway Reservation Engine</h1>
      <p className="page-subtitle">
        Monolith visualization — quota-aware booking, RAC/waitlist promotion, chart preparation, and Razorpay payments.
      </p>

      <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))' }}>
        <div className="card">
          <h3>Search Trains</h3>
          <p style={{ fontSize: 13, color: '#64748b', margin: '8px 0 16px' }}>
            Find trains between stations (cached via Redis).
          </p>
          <Link to="/search" className="btn btn-small">Search</Link>
        </div>
        <div className="card">
          <h3>Book a Ticket</h3>
          <p style={{ fontSize: 13, color: '#64748b', margin: '8px 0 16px' }}>
            Quota-aware booking with berth preference and idempotent retry.
          </p>
          <Link to="/book" className="btn btn-small">Book</Link>
        </div>
        <div className="card">
          <h3>Check PNR</h3>
          <p style={{ fontSize: 13, color: '#64748b', margin: '8px 0 16px' }}>
            Lookup status, cancel, and see promotion results.
          </p>
          <Link to="/pnr" className="btn btn-small">Check PNR</Link>
        </div>
        <div className="card">
          <h3>Seeded Data</h3>
          <p style={{ fontSize: 13, color: '#64748b', margin: '8px 0' }}>
            6 trains, 20 stations, 7 schedules. Try <code>aarav@example.com / password</code> to log in and explore.
          </p>
        </div>
      </div>
    </div>
  )
}
