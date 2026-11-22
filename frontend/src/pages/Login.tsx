import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import { ApiError } from '../ApiError'
import Field from '../components/Field'

export default function Login() {
  const { user, loading, signIn } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const from = location.state?.from?.pathname || '/'
  const registered = location.state?.registered === true

  if (loading) {
    return <p>Проверка авторизации...</p>
  }

  if (user) {
    return <Navigate to="/" replace />
  }

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setError('')
    setSubmitting(true)

    try {
      await signIn(email, password)
      navigate(from, { replace: true })
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : 'Не удалось выполнить вход',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="narrow">
      <h1>Вход в систему</h1>

      {error && <p className="error">{error}</p>}

      {registered && (
        <p>
          Регистрация прошла успешно! Теперь войдите в свой аккаунт.
        </p>
      )}
      
      <form onSubmit={submit}>
        <Field label="E-mail" required>
          <input
            type="email"
            required
            autoComplete="username"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </Field>

        <Field label="Пароль" required>
          <input
            type="password"
            required
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </Field>

        <button type="submit" disabled={submitting}>
          {submitting ? 'Вход...' : 'Войти'}
        </button>
      </form>
      <p>
        Ещё нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
      </p>
    </div>
  )
}
