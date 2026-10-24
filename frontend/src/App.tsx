import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import ApplicationList from './pages/ApplicationList'
import ApplicationForm from './pages/ApplicationForm'

export default function App() {
  return (
    <BrowserRouter>
      <header>
        <Link to="/">Конкурс НИРС: заявки</Link>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<ApplicationList />} />
          <Route path="/applications/new" element={<ApplicationForm />} />
          <Route path="/applications/:id/edit" element={<ApplicationForm />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}