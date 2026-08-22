/**
 * Availability — live seat counts per train-date-quota.
 *
 * Calls GET /api/v1/trains/{trainId}/availability?date=&quota=
 * (TrainAvailabilityController.java:22). The service uses a cache-aside
 * Redis lookup; allocations are partitioned by (schedule, coach, quota)
 * so each quota has its own pool. Change quota to see LD/SS/DEF
 * difference — they have smaller pools than GN.
 *
 * Seeded quotas: GN, LD, SS, DEF, TQ
 * Seeded trains: 1=12951 Rajdhani, 2=12138 Punjab Mail, 3=12009 Shatabdi
 */

import { useState } from 'react'
import api from '../api/client'

function tomorrowISO() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().slice(0, 10)
}

export default function Availability() {
  const [trainId, setTrainId] = useState('1')
  const [date, setDate] = useState(tomorrowISO())
  const [quota, setQuota] = useState('GN')
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleCheck(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    setData(null)

    try {
      const res = await api.get(`/api/v1/trains/${trainId}/availability`, {
        params: { date, quota },
      })
      setData(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Availability check failed. Verify trainId, date, and quota.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">Seat Availability</h1>
      <p className="page-subtitle">Check live counts per quota. Try train 1 (12951) with GN, then LD to see the smaller pool.</p>

      <div className="card" style={{ marginBottom: 24 }}>
        <form onSubmit={handleCheck} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr auto', gap: 12, alignItems: 'end' }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label htmlFor="trainId">Train ID</label>
            <input id="trainId" value={trainId} onChange={(e) => setTrainId(e.target.value)} placeholder="1" required />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label htmlFor="date">Date</label>
            <input id="date" type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label htmlFor="quota">Quota</label>
            <select id="quota" value={quota} onChange={(e) => setQuota(e.target.value)}>
              <option value="GN">GN — General</option>
              <option value="LD">LD — Ladies</option>
              <option value="SS">SS — Senior Citizen</option>
              <option value="DEF">DEF — Defence</option>
              <option value="TQ">TQ — Tatkal</option>
            </select>
          </div>
          <button type="submit" className="btn" disabled={loading} style={{ height: 38 }}>
            {loading ? 'Checking...' : 'Check'}
          </button>
        </form>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {data && (
        <div className="card">
          <h3 style={{ marginBottom: 12 }}>
            {data.trainNumber} — {data.trainName} ({data.quota}) on {data.journeyDate}
          </h3>
          <div className="grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
            <div style={{ textAlign: 'center', padding: 16, background: '#f8fafc', borderRadius: 8 }}>
              <div style={{ fontSize: 28, fontWeight: 700 }}>{data.totalAvailableSeats}</div>
              <div style={{ fontSize: 13, color: '#64748b' }}>Available Seats</div>
            </div>
            <div style={{ textAlign: 'center', padding: 16, background: '#fff7ed', borderRadius: 8 }}>
              <div style={{ fontSize: 28, fontWeight: 700 }}>{data.totalRacAvailable}</div>
              <div style={{ fontSize: 13, color: '#64748b' }}>RAC Available</div>
            </div>
            <div style={{ textAlign: 'center', padding: 16, background: '#fefce8', borderRadius: 8 }}>
              <div style={{ fontSize: 28, fontWeight: 700 }}>{data.totalWaitlistSeats}</div>
              <div style={{ fontSize: 13, color: '#64748b' }}>Waitlist Seats</div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
