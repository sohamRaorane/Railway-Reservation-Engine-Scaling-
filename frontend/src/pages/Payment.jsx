/**
 * Payment — initiate a Razorpay order for a PENDING_PAYMENT booking.
 *
 * Calls POST /payment/initiate (PaymentController.java:30) with { pnr }.
 * The backend creates a Razorpay order via RazorpayClient and returns
 * orderId + keyId + amount/currency — the data needed to open the
 * Razorpay Checkout. The actual capture is confirmed via the
 * POST /payment/webhook server-to-server callback (signature verified
 * with X-Razorpay-Signature). This page just visualizes the order
 * creation step; the webhook is exercised by Razorpay's sandbox.
 *
 * Try PNRSEED005 — it is the only seeded PENDING_PAYMENT booking.
 */

import { useState } from 'react'
import api from '../api/client'

export default function Payment() {
  const [pnr, setPnr] = useState('PNRSEED005')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleInitiate(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    setResult(null)

    try {
      const res = await api.post('/payment/initiate', { pnr: pnr.trim().toUpperCase() })
      setResult(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Payment initiation failed. Is the booking PENDING_PAYMENT and owned by you?')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">Payment</h1>
      <p className="page-subtitle">Create a Razorpay order for a PENDING_PAYMENT booking. Seeded example: PNRSEED005 (Anil Verma, DEF quota).</p>

      <div className="card" style={{ marginBottom: 24 }}>
        <form onSubmit={handleInitiate} style={{ display: 'flex', gap: 12, alignItems: 'end' }}>
          <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
            <label htmlFor="pnr">PNR</label>
            <input id="pnr" value={pnr} onChange={(e) => setPnr(e.target.value)} placeholder="PNRSEED005" required />
          </div>
          <button type="submit" className="btn" disabled={loading} style={{ height: 38 }}>
            {loading ? 'Creating...' : 'Initiate Payment'}
          </button>
        </form>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {result && (
        <div className="card">
          <h3 style={{ marginBottom: 12 }}>Order Created</h3>
          <table>
            <tbody>
              <tr>
                <td>Order ID</td>
                <td><code>{result.orderId}</code></td>
              </tr>
              <tr>
                <td>Key ID</td>
                <td><code>{result.keyId}</code></td>
              </tr>
              <tr>
                <td>Amount</td>
                <td>₹{result.amount} {result.currency}</td>
              </tr>
              <tr>
                <td>Booking PNR</td>
                <td>{result.bookingPnr}</td>
              </tr>
            </tbody>
          </table>
          <p style={{ marginTop: 12, fontSize: 13, color: '#64748b' }}>
            In a real checkout, the frontend would open Razorpay Checkout with this orderId and keyId. Capture is confirmed via the webhook.
          </p>
        </div>
      )}
    </div>
  )
}
