/**
 * Chart — manually trigger chart preparation for a schedule.
 *
 * Calls POST /api/v1/schedules/{id}/prepare-chart
 * (ScheduleController.java:22). The backend's ChartPreparationService
 * uses a compare-and-swap single-winner pattern — exactly one caller
 * succeeds in moving OPEN → CHART_PREPARED, others get a clean
 * "already prepared" outcome. Uses the same path as the scheduled job.
 *
 * Seeded schedules: 1–7 (check with SELECT id, journey_date FROM schedules).
 * Try schedule 1 (12951, tomorrow) after booking some tickets to see
 * RAC → CONFIRMED finalization.
 */

import { useState } from 'react'
import api from '../api/client'

export default function Chart() {
  const [scheduleId, setScheduleId] = useState('1')
  const [result, setResult] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handlePrepare(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    setResult('')

    try {
      const res = await api.post(`/api/v1/schedules/${scheduleId}/prepare-chart`)
      setResult(typeof res.data === 'string' ? res.data : JSON.stringify(res.data))
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Chart preparation failed. Is the schedule OPEN and are you authenticated?')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">Chart Preparation</h1>
      <p className="page-subtitle">Manually trigger the single-winner CAS chart finalization. Schedules 1–7 exist in seed data.</p>

      <div className="card" style={{ marginBottom: 24 }}>
        <form onSubmit={handlePrepare} style={{ display: 'flex', gap: 12, alignItems: 'end' }}>
          <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
            <label htmlFor="scheduleId">Schedule ID</label>
            <input id="scheduleId" value={scheduleId} onChange={(e) => setScheduleId(e.target.value)} placeholder="1" required />
          </div>
          <button type="submit" className="btn" disabled={loading} style={{ height: 38 }}>
            {loading ? 'Preparing...' : 'Prepare Chart'}
          </button>
        </form>
      </div>

      {error && <div className="alert alert-error">{String(error)}</div>}
      {result && <div className="alert alert-success">{String(result)}</div>}

      <div className="card">
        <h3 style={{ marginBottom: 8 }}>How to check</h3>
        <p style={{ fontSize: 13, color: '#64748b' }}>
          Run <code>SELECT id, train_id, journey_date, status FROM schedules ORDER BY id</code> in pgAdmin
          to see available schedule IDs and their current status before/after preparation.
        </p>
      </div>
    </div>
  )
}
