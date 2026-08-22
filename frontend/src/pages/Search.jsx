/**
 * Search — find trains between two stations on a given date.
 *
 * Calls GET /api/v1/trains/search?source=&destination=&date=
 * (TrainSearchController.java:32). The backend checks the routes table
 * to find trains that stop at both stations in order, then returns
 * the matching journey with departure/arrival times. Results are
 * cached in Redis via the service's cache-aside pattern.
 *
 * Seeded routes to try:
 *   BCT → NDLS (Mumbai Central → New Delhi) — 12951 Rajdhani
 *   CSMT → NDLS (Mumbai CSMT → New Delhi) — 12138 Punjab Mail
 *   BCT → ADI (Mumbai Central → Ahmedabad) — 12009 Shatabdi
 */

import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'

function tomorrowISO() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().slice(0, 10)
}

export default function Search() {
  const [source, setSource] = useState('BCT')
  const [destination, setDestination] = useState('NDLS')
  const [date, setDate] = useState(tomorrowISO())
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searched, setSearched] = useState(false)

  async function handleSearch(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    setSearched(true)

    try {
      const res = await api.get('/api/v1/trains/search', {
        params: { source, destination, date },
      })
      setResults(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Search failed. Check station codes and date.')
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">Search Trains</h1>
      <p className="page-subtitle">
        Try BCT → NDLS or CSMT → NDLS on tomorrow&apos;s date. Station codes from seed: BCT, ST, ADI, NDLS, CSMT, NGP, BPL, etc.
      </p>

      <div className="card" style={{ marginBottom: 24 }}>
        <form onSubmit={handleSearch} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr auto', gap: 12, alignItems: 'end' }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label htmlFor="source">Source</label>
            <input id="source" value={source} onChange={(e) => setSource(e.target.value.toUpperCase())} placeholder="BCT" required />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label htmlFor="dest">Destination</label>
            <input id="dest" value={destination} onChange={(e) => setDestination(e.target.value.toUpperCase())} placeholder="NDLS" required />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label htmlFor="date">Journey Date</label>
            <input id="date" type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
          </div>
          <button type="submit" className="btn" disabled={loading} style={{ height: 38 }}>
            {loading ? 'Searching...' : 'Search'}
          </button>
        </form>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {searched && !loading && !error && results.length === 0 && (
        <div className="alert alert-info">No trains found for that route and date. Try BCT → NDLS on {tomorrowISO()}.</div>
      )}

      {results.length > 0 && (
        <div className="card">
          <h3 style={{ marginBottom: 12 }}>Results ({results.length})</h3>
          <table>
            <thead>
              <tr>
                <th>Train</th>
                <th>Date</th>
                <th>Departure</th>
                <th>Arrival</th>
              </tr>
            </thead>
            <tbody>
              {results.map((r, idx) => (
                <tr key={idx}>
                  <td>
                    <strong>{r.trainNumber}</strong> — {r.trainName}
                  </td>
                  <td>{r.journeyDate}</td>
                  <td>{r.departureTime}</td>
                  <td>{r.arrivalTime}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p style={{ marginTop: 12, fontSize: 13, color: '#64748b' }}>
            Tip: For booking you need the trainId (12951 = id 1, 12138 = id 2). Check <Link to="/availability/1">availability</Link> next.
          </p>
        </div>
      )}
    </div>
  )
}
