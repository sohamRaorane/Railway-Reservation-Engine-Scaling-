/**
 * PNR — lookup a booking by its PNR and optionally cancel it.
 *
 * - Lookup: GET /api/v1/bookings/{pnr} (BookingController.java:69)
 *   returns pnr, bookingStatus, totalFare, passengers[] with seat/berth.
 * - Cancel: POST /api/v1/bookings/{pnr}/cancel (BookingController.java:77)
 *   releases the seat(s), persists a refund record, and publishes a
 *   booking.cancelled Kafka event with freedSeatCount (= one promotion
 *   per freed seat). Try cancelling PNRSEED001 to see RAC promotion
 *   of Vikram (RAC) and Priya (waitlist) on the next availability check.
 */

import { useState } from 'react'
import api from '../api/client'

function statusBadge(status) {
  const map = {
    CONFIRMED: 'badge-confirmed',
    WAITLISTED: 'badge-waitlist',
    WAITLIST: 'badge-waitlist',
    RAC: 'badge-rac',
    CANCELLED: 'badge-cancelled',
    PENDING_PAYMENT: 'badge-waitlist',
  }
  return map[status] || ''
}

export default function Pnr() {
  const [pnr, setPnr] = useState('')
  const [booking, setBooking] = useState(null)
  const [cancelResult, setCancelResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleLookup(e) {
    e.preventDefault()
    setError('')
    setCancelResult(null)
    setLoading(true)

    try {
      const res = await api.get(`/api/v1/bookings/${pnr.trim().toUpperCase()}`)
      setBooking(res.data)
    } catch (err) {
      setBooking(null)
      setError(err.response?.data?.message || 'PNR not found. Try a seeded PNR like PNRSEED001.')
    } finally {
      setLoading(false)
    }
  }

  async function handleCancel() {
    if (!booking) return
    if (!window.confirm(`Cancel booking ${booking.pnr}? This will release seats and trigger waitlist promotion.`)) return

    setError('')
    setCancelResult(null)

    try {
      const res = await api.post(`/api/v1/bookings/${booking.pnr}/cancel`)
      setCancelResult(res.data)
      setBooking((prev) => ({ ...prev, bookingStatus: res.data.bookingStatus }))
    } catch (err) {
      setError(err.response?.data?.message || 'Cancellation failed.')
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">PNR Lookup</h1>
      <p className="page-subtitle">Seeded PNRs to try: PNRSEED001 (CONFIRMED), PNRSEED002 (WAITLIST), PNRSEED003 (RAC), PNRSEED007 (CANCELLED)</p>

      <div className="card" style={{ marginBottom: 24 }}>
        <form onSubmit={handleLookup} style={{ display: 'flex', gap: 12 }}>
          <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
            <label htmlFor="pnr">PNR</label>
            <input id="pnr" value={pnr} onChange={(e) => setPnr(e.target.value)} placeholder="PNRSEED001" required />
          </div>
          <button type="submit" className="btn" disabled={loading} style={{ alignSelf: 'end', height: 38 }}>
            {loading ? 'Looking up...' : 'Lookup'}
          </button>
        </form>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {cancelResult && (
        <div className="alert alert-success">
          {cancelResult.message} — Refund: ₹{cancelResult.refundAmount} — New status: {cancelResult.bookingStatus}
        </div>
      )}

      {booking && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <div>
              <h3>PNR: {booking.pnr}</h3>
              <p style={{ fontSize: 13, color: '#64748b' }}>Fare: ₹{booking.totalFare}</p>
            </div>
            <span className={`badge ${statusBadge(booking.bookingStatus)}`}>{booking.bookingStatus}</span>
          </div>

          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Coach</th>
                <th>Seat</th>
                <th>Berth</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {booking.passengers.map((p, idx) => (
                <tr key={idx}>
                  <td>{p.name}</td>
                  <td>{p.coachNumber || '—'}</td>
                  <td>{p.seatNumber ?? '—'}</td>
                  <td>{p.berthType || '—'}</td>
                  <td>
                    <span className={`badge ${statusBadge(p.passengerStatus)}`}>{p.passengerStatus}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {booking.bookingStatus !== 'CANCELLED' && (
            <div style={{ marginTop: 16 }}>
              <button className="btn btn-outline" onClick={handleCancel}>
                Cancel Booking
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
