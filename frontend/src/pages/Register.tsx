import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import { ApiError } from '../ApiError'
import { registerUser } from '../api'
import Field from '../components/Field'

export default function Register() {
  const { user, loading } = useAuth()
  const navigate = useNavigate()

  const [lastName, setLastName] = useState('')
  const [firstName, setFirstName] = useState('')
  const [middleName, setMiddleName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

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
      await registerUser({
        lastName,
        firstName,
        middleName: middleName.trim() || null,
        email,
        password,
      })

      navigate('/login', {
        replace: true,
        state: { registered: true },
      })
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
      } else {
        setError('Не удалось зарегистрироваться. Попробуйте позже.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="narrow">
      <h1>Регистрация</h1>

      {error && <p className="error">{error}</p>}

      <form onSubmit={submit}>
        <Field label="Фамилия" required>
          <input
            type="text"
            required
            maxLength={100}
            autoComplete="family-name"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
          />
        </Field>

        <Field label="Имя" required>
          <input
            type="text"
            required
            maxLength={100}
            autoComplete="given-name"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
          />
        </Field>

        <Field label="Отчество">
          <input
            type="text"
            maxLength={100}
            value={middleName}
            onChange={(e) => setMiddleName(e.target.value)}
          />
        </Field>

        <Field label="E-mail" required>
          <input
            type="email"
            required
            maxLength={255}
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </Field>

        <Field label="Пароль" required>
          <div className="password-field">
            <input
              type={showPassword ? 'text' : 'password'}
              required
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />

            <button
              type="button"
              onClick={() => setShowPassword((value) => !value)}
              aria-label={showPassword ? 'Скрыть пароль' : 'Показать пароль'}
            >
              {showPassword ? 'Скрыть' : 'Показать'}
            </button>
          </div>
          <small>Минимум 8 символов.</small>
        </Field>

        <button type="submit" disabled={submitting}>
          {submitting ? 'Регистрация...' : 'Зарегистрироваться'}
        </button>
      </form>

      <p>
        Уже есть аккаунт? <Link to="/login">Войти</Link>
      </p>
    </div>
  )
}
