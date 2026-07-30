import React, { useState, useEffect } from 'react'

async function safeFetchJson(url, options) {
  const res = await fetch(url, options)
  const text = await res.text()
  try {
    const data = text ? JSON.parse(text) : {}
    return { res, data }
  } catch {
    throw new Error(`Server returned error (${res.status} ${res.statusText}). Ensure backend server is running.`)
  }
}

export default function App() {
  const [activeTab, setActiveTab] = useState('qr')

  const [expirationTime, setExpirationTime] = useState(10)
  const [qrData, setQrData] = useState(null)
  const [qrLoading, setQrLoading] = useState(false)
  const [qrError, setQrError] = useState('')
  const [timeLeft, setTimeLeft] = useState(null)

  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0])
  const [attendanceList, setAttendanceList] = useState([])
  const [attendanceLoading, setAttendanceLoading] = useState(false)
  const [attendanceError, setAttendanceError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    if (!qrData?.expiresAt) return
    const timer = setInterval(() => {
      const diff = Math.max(0, Math.floor((new Date(qrData.expiresAt).getTime() - Date.now()) / 1000))
      setTimeLeft(diff)
      if (diff === 0) clearInterval(timer)
    }, 1000)
    return () => clearInterval(timer)
  }, [qrData])

  const fetchAttendance = async (dateStr) => {
    if (!dateStr) return
    setAttendanceLoading(true)
    setAttendanceError('')
    try {
      const { data } = await safeFetchJson(`/attendance/${dateStr}`)
      if (data.success) {
        setAttendanceList(data.attendance || [])
      } else {
        setAttendanceError(data.message || 'Failed to fetch attendance')
      }
    } catch (err) {
      setAttendanceError(err.message)
    } finally {
      setAttendanceLoading(false)
    }
  }

  useEffect(() => {
    if (activeTab === 'attendance') {
      fetchAttendance(selectedDate)
    }
  }, [activeTab, selectedDate])

  const handleGenerateQR = async (e) => {
    e.preventDefault()
    setQrLoading(true)
    setQrError('')
    try {
      const { data } = await safeFetchJson('/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ expirationTime: Number(expirationTime) })
      })
      if (data.success) {
        setQrData(data)
      } else {
        setQrError(data.message || 'Failed to generate QR Code')
      }
    } catch (err) {
      setQrError(err.message)
    } finally {
      setQrLoading(false)
    }
  }

  const exportCSV = () => {
    if (!attendanceList.length) return
    const headers = ['Roll Number', 'Name', 'Email', 'Department', 'Year', 'Marked Time']
    const rows = attendanceList.map((item) => {
      const s = item.student || {}
      const time = item.createdAt ? new Date(item.createdAt).toLocaleString() : 'N/A'
      return [`"${s.rollNo || ''}"`, `"${s.name || ''}"`, `"${s.email || ''}"`, `"${s.department || ''}"`, `"${s.year || ''}"`, `"${time}"`].join(',')
    })
    const csvContent = [headers.join(','), ...rows].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `attendance_${selectedDate}.csv`
    link.click()
    URL.revokeObjectURL(url)
  }

  const formatTime = (seconds) => {
    if (seconds == null) return '--:--'
    const m = String(Math.floor(seconds / 60)).padStart(2, '0')
    const s = String(seconds % 60).padStart(2, '0')
    return `${m}:${s}`
  }

  const filteredAttendance = attendanceList.filter((item) => {
    const s = item.student || {}
    const term = searchTerm.toLowerCase()
    return (
      (s.name && s.name.toLowerCase().includes(term)) ||
      (s.rollNo && s.rollNo.toLowerCase().includes(term)) ||
      (s.department && s.department.toLowerCase().includes(term))
    )
  })

  return (
    <div className="app-container">
      <header className="header">
        <span className="logo">Attendance</span>
        <nav className="nav">
          <button
            onClick={() => setActiveTab('qr')}
            className={`nav-link ${activeTab === 'qr' ? 'active' : ''}`}
          >
            Generate QR
          </button>
          <button
            onClick={() => setActiveTab('attendance')}
            className={`nav-link ${activeTab === 'attendance' ? 'active' : ''}`}
          >
            View Attendance
          </button>
        </nav>
      </header>

      <main className="container">
        {activeTab === 'qr' && (
          <div className="grid-2">
            <div className="card">
              <h2 className="card-title">Generate QR</h2>
              <form onSubmit={handleGenerateQR}>
                <div className="form-group">
                  <label className="form-label">Expiration (minutes)</label>
                  <input
                    type="number"
                    min="1"
                    max="180"
                    value={expirationTime}
                    onChange={(e) => setExpirationTime(e.target.value)}
                    required
                    className="form-input"
                  />
                </div>

                <button type="submit" disabled={qrLoading} className="btn">
                  {qrLoading ? 'Generating...' : 'Generate QR Code'}
                </button>

                {qrError && <div className="alert">{qrError}</div>}
              </form>
            </div>

            <div className="qr-box">
              {qrData ? (
                <>
                  <img src={qrData.qrUrl} alt="Attendance QR Code" className="qr-img" />
                  <div className="timer">
                    Expires in: {formatTime(timeLeft)}
                  </div>
                </>
              ) : (
                <div style={{ color: '#9ca3af' }}>No QR Code generated yet</div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'attendance' && (
          <div className="card">
            <div className="controls">
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
                <div>
                  <label className="form-label">Date</label>
                  <input
                    type="date"
                    value={selectedDate}
                    onChange={(e) => setSelectedDate(e.target.value)}
                    className="form-input"
                    style={{ width: 'auto' }}
                  />
                </div>

                <div>
                  <label className="form-label">Search</label>
                  <input
                    type="text"
                    placeholder="Search name, roll no..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="form-input"
                  />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button onClick={() => fetchAttendance(selectedDate)} className="btn-outline">
                  Refresh
                </button>
                <button
                  onClick={exportCSV}
                  disabled={filteredAttendance.length === 0}
                  className="btn"
                >
                  Download CSV
                </button>
              </div>
            </div>

            <div className="table-wrapper">
              {attendanceLoading ? (
                <div style={{ padding: '2rem', textAlign: 'center', color: '#6b7280' }}>
                  Loading...
                </div>
              ) : attendanceError ? (
                <div className="alert">{attendanceError}</div>
              ) : filteredAttendance.length === 0 ? (
                <div style={{ padding: '2rem', textAlign: 'center', color: '#6b7280' }}>
                  No attendance records for {selectedDate}.
                </div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Roll Number</th>
                      <th>Name</th>
                      <th>Department</th>
                      <th>Year</th>
                      <th>Time</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredAttendance.map((item, idx) => {
                      const s = item.student || {}
                      return (
                        <tr key={item._id || idx}>
                          <td style={{ color: '#9ca3af' }}>{idx + 1}</td>
                          <td style={{ fontWeight: 600 }}>{s.rollNo || 'N/A'}</td>
                          <td>{s.name || 'N/A'}</td>
                          <td>{s.department || 'N/A'}</td>
                          <td>Year {s.year || '-'}</td>
                          <td style={{ color: '#6b7280' }}>
                            {item.createdAt ? new Date(item.createdAt).toLocaleTimeString() : 'N/A'}
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}
