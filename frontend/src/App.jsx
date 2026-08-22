/**
 * App — central router for the Railway Reservation Engine frontend.
 *
 * Each route maps to one backend feature slice so the UI stays aligned
 * with the future microservice boundaries. Protected routes (book/cancel)
 * redirect to /login when no JWT is present.
 */

import { Routes, Route } from 'react-router-dom'
import Header from './components/Header.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Search from './pages/Search.jsx'
import Availability from './pages/Availability.jsx'
import Book from './pages/Book.jsx'
import Pnr from './pages/Pnr.jsx'
import Payment from './pages/Payment.jsx'
import Chart from './pages/Chart.jsx'
import './App.css'

export default function App() {
  return (
    <>
      <Header />
      <main className="app-main">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/search" element={<Search />} />
          <Route path="/availability/:trainId" element={<Availability />} />
          <Route path="/book" element={<Book />} />
          <Route path="/pnr" element={<Pnr />} />
          <Route path="/payment" element={<Payment />} />
          <Route path="/chart" element={<Chart />} />
        </Routes>
      </main>
    </>
  )
}
