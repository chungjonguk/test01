import { useEffect, useState } from 'react'
import './App.css'

type HealthResponse = {
  message?: string
  status?: string
}

export default function App() {
  const [health, setHealth] = useState<HealthResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/status')
      .then((res) => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}`)
        }
        return res.json() as Promise<HealthResponse>
      })
      .then(setHealth)
      .catch((err: Error) => {
        setError(err.message || 'API 연결 실패')
      })
  }, [])

  return (
    <main className="app">
      <header className="app-header">
        <h1>PrintMall React</h1>
        <p>Vite + React + TypeScript 개발환경</p>
      </header>

      <section className="card">
        <h2>Spring Boot API 연동</h2>
        {error && (
          <p className="error">
            /api/status 호출 실패 — <code>run-server.bat</code> 으로 백엔드를 먼저
            기동하세요. ({error})
          </p>
        )}
        {!error && health && (
          <pre>{JSON.stringify(health, null, 2)}</pre>
        )}
        {!error && !health && <p>API 확인 중...</p>}
      </section>

      <section className="card muted">
        <h2>다음 단계</h2>
        <ul>
          <li>
            <code>frontend/src/</code> 에 화면·컴포넌트 추가
          </li>
          <li>
            REST API는 <code>/api/**</code> 로 프록시 (8081)
          </li>
          <li>
            가이드: <code>docs/react-dev-setup.txt</code>
          </li>
        </ul>
      </section>
    </main>
  )
}
