import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import ApplicationList from './pages/ApplicationList'

export default function App() {
  return (
    <BrowserRouter>
      <header>
        <Link to="/">Конкурс НИРС: заявки</Link>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<ApplicationList />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}