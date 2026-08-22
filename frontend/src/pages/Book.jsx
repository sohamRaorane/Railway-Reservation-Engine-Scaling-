/**
 * Book — create a new booking with quota and berth preference.
 *
 * Calls POST /api/v1/bookings (BookingController.java:38) with:
 *   - Header Idempotency-Key: generated via crypto.randomUUID() so
 *     retried requests return the same PNR instead of double-booking.
 *   - Body BookingRequest: trainId, journeyDate, quotaCode, coachType, passengers[]
 *   - Auth: Bearer JWT from AuthContext (required).
 *
 * The backend validates quota eligibility (e.g. SS needs M≥60/F≥58,
 * LD needs F, DEF needs is_defence_personnel=true), locks the
 * quota allocation row, and assigns CONFIRMED / RAC / WAITLIST.
 */

import { useState } from 'react'
import api, { generateIdempotencyKey } from '../api/client'
import { useAuth } from '../context/AuthContext'

function tomorrowISO() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().slice(0, 10)
}

const EMPTY_PASSENGER = { name: '', age: '', gender: 'MALE', berthPreference: 'NO_PREFERENCE' }

export default function Book() {
  const { isAuthenticated } = useAuth()

  const [trainId, setTrainId] = useState('1')
  const [journeyDate, setJourneyDate] = useState(tomorrowISO())
  const [quotaCode, setQuotaCode] = useState('GN')
  const [coachType, setCoachType] = useState('SLEEPER')
  const [passengers, setPassengers] = useState([{ ...EMPTY_PASSENGER }])

  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function updatePassenger(idx, field, value) {
    setPassengers((prev) => prev.map((p, i) => (i === idx ? { ...p, [field]: value } : p)))
  }

  function addPassenger() {
    setPassengers((prev) => [...prev, { ...EMPTY_PASSENGER }])
  }

  function removePassenger(idx) {
    setPassengers((prev) => prev.filter((_, i) => i !== idx))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setResult(null)

    if (!isAuthenticated) {
      setError('Please log in first — booking requires authentication.')
      return
    }

    const payload = {
      trainId: Number(trainId),
      journeyDate,
      quotaCode,
      coachType,
      passengers: passengers.map((p) => ({
        name: p.name,
        age: Number(p.age),
        gender: p.gender,
        berthPreference: p.berthPreference,
      })),
    }

    setLoading(true)
    try {
      const res = await api.post('/api/v1/bookings', payload, {
        headers: { 'Idempotency-Key': generateIdempotencyKey() },
      })
      setResult(res.data)
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Booking failed.'
      // Backend validation errors often come as JSON with field messages
      const details = err.response?.data?.details || err.response?.data?.errors
      setError(details ? `${msg}: ${JSON.stringify(details)}` : msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">Book a Ticket</h1>
      <p className="page-subtitle">
        Quota matters: LD needs female passengers, SS needs M≥60/F≥58, DEF needs a defence user (anil@example.com).
      </p>

      {!isAuthenticated && <div className="alert alert-info">You are not logged in — please log in to book.</div>}
      {error && <div className="alert alert-error">{error}</div>}
      {result && (
        <div className="alert alert-success">
          Booked! PNR: <strong>{result.pnr}</strong> — Status: {result.bookingStatus} — Fare: ₹{result.totalFare}
          <br />
          <span style={{ fontSize: 12 }}>Tip: check this PNR on the PNR page or try cancelling it.</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="card" style={{ marginBottom: 16 }}>
          <h3 style={{ marginBottom: 12 }}>Journey</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label htmlFor="trainId">Train ID</label>
              <input id="trainId" value={trainId} onChange={(e) => setTrainId(e.target.value)} placeholder="1 = 12951 Rajdhani" required />
            </div>
            <div className="form-group">
              <label htmlFor="journeyDate">Journey Date</label>
              <input id="journeyDate" type="date" value={journeyDate} onChange={(e) => setJourneyDate(e.target.value)} required />
            </div>
            <div className="form-group">
              <label htmlFor="quota">Quota</label>
              <select id="quota" value={quotaCode} onChange={(e) => setQuotaCode(e.target.value)}>
                <option value="GN">GN — General</option>
                <option value="LD">LD — Ladies</option>
                <option value="SS">SS — Senior Citizen</option>
                <option value="DEF">DEF — Defence</option>
                <option value="TQ">TQ — Tatkal</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="coach">Coach Type</label>
              <select id="coach" value={coachType} onChange={(e) => setCoachType(e.target.value)}>
                <option value="SLEEPER">SLEEPER</option>
                <option value="AC_3_TIER">AC_3_TIER</option>
                <option value="AC_2_TIER">AC_2_TIER</option>
                <option value="AC_1_TIER">AC_1_TIER</option>
                <option value="GENERAL">GENERAL</option>
              </select>
            </div>
          </div>
        </div>

        <div className="card" style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3>Passengers ({passengers.length})</h3>
            <button type="button" className="btn btn-small btn-outline" onClick={addPassenger}>
              + Add
            </button>
          </div>

          {passengers.map((p, idx) => (
            <div key={idx} style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr auto', gap: 8, marginBottom: 12, alignItems: 'end' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Name</label>
                <input value={p.name} onChange={(e) => updatePassenger(idx, 'name', e.target.value)} placeholder="Full name" required />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Age</label>
                <input type="number" value={p.age} onChange={(e) => updatePassenger(idx, 'age', e.target.value)} placeholder="30" min="1" max="125" required />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Gender</label>
                <select value={p.gender} onChange={(e) => updatePassenger(idx, 'gender', e.target.value)}>
                  <option value="MALE">MALE</option>
                  <option value="FEMALE">FEMALE</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Berth</label>
                <select value={p.berthPreference} onChange={(e) => updatePassenger(idx, 'berthPreference', e.target.value)}>
                  <option value="NO_PREFERENCE">No Preference</option>
                  <option value="LOWER">LOWER</option>
                  <option value="MIDDLE">MIDDLE</option>
                  <option value="UPPER">UPPER</option>
                  <option value="SIDE_LOWER">SIDE_LOWER</option>
                  <option value="SIDE_UPPER">SIDE_UPPER</option>
                </select>
              </div>
              {passengers.length > 1 && (
                <button type="button" className="btn btn-small btn-outline" onClick={() => removePassenger(idx)} style={{ height: 34 }}>
                  Remove
                </button>
              )}
            </div>
          ))}
        </div>

        <button type="submit" className="btn" disabled={loading} style={{ width: '100%' }}>
          {loading ? 'Booking...' : 'Book Ticket'}
        </button>
      </form>
    </div>
  )
}
