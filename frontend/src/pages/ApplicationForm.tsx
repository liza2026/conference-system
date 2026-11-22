import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  createApplication,
  getApplication,
  getDirections,
  searchUsers,
  updateApplication,
} from '../api'
import { ApiError } from '../ApiError'
import Field from '../components/Field'
import type { ApplicationInput, AuthUser, Direction } from '../types'
import { useAuth } from '../AuthContext'

export default function ApplicationForm() {
  const { user: currentUser } = useAuth()
  const { id } = useParams()
  const editing = id !== undefined
  const navigate = useNavigate()

  const [directions, setDirections] = useState<Direction[]>([])
  const [title, setTitle] = useState('')
  const [directionId, setDirectionId] = useState(0)
  const [authors, setAuthors] = useState<AuthUser[]>([])
  const [file, setFile] = useState<File | null>(null)
  const [currentFileName, setCurrentFileName] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  const [query, setQuery] = useState('')
  const [results, setResults] = useState<AuthUser[]>([])
  const [searching, setSearching] = useState(false)

  useEffect(() => {
  getDirections()
    .then(setDirections)
    .catch((e: Error) => setError(e.message))
}, [])

  // Загружаем заявку при редактировании.
  useEffect(() => {
    if (!editing) return

    getApplication(Number(id))
      .then((application) => {
        setTitle(application.title)
        setDirectionId(application.directionId)
        setAuthors(
          application.authors.map((author) => ({
            id: author.userId,
            lastName: author.lastName,
            firstName: author.firstName,
            middleName: author.middleName,
            email: author.email,
          })),
        )
        setCurrentFileName(application.thesisFileName)
      })
      .catch((e: Error) => setError(e.message))
  }, [id, editing])

  // При создании заявки текущий пользователь автоматически становится автором.
  useEffect(() => {
    if (!editing && currentUser) {
      setAuthors((previous) =>
        previous.some((author) => author.id === currentUser.id)
          ? previous
          : [currentUser, ...previous],
      )
    }
  }, [currentUser, editing])

  // Поиск пользователей с задержкой 500 мс.
  useEffect(() => {
    const value = query.trim()

    if (value.length < 2) {
      setResults([])
      return
    }

    const timer = window.setTimeout(() => {
      setSearching(true)

      searchUsers(value)
        .then(setResults)
        .catch((e: Error) => setError(e.message))
        .finally(() => setSearching(false))
    }, 500)

    return () => window.clearTimeout(timer)
  }, [query])

  function addAuthor(user: AuthUser) {
    if (authors.length >= 2 || authors.some((a) => a.id === user.id)) {
      return
    }

    setAuthors((previous) => [...previous, user])
    setQuery('')
    setResults([])
  }

  function removeAuthor(userId: number) {
    // Отправитель заявки не может быть удалён из списка авторов.
    if (userId === currentUser?.id) return

    setAuthors((previous) => previous.filter((a) => a.id !== userId))
  }

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setError('')
    setFieldErrors({})

    if (!currentUser) {
      setError('Не удалось определить текущего пользователя')
      return
    }

    if (!authors.some((author) => author.id === currentUser.id)) {
      setError('Отправитель заявки должен быть одним из авторов')
      return
    }

    if (authors.length === 0 || authors.length > 2) {
      setError('В заявке должно быть от одного до двух авторов')
      return
    }

    if (!editing && !file) {
      setError('Выберите файл тезисов')
      return
    }

    const data: ApplicationInput = {
      title,
      directionId,
      submitterUserId: currentUser.id,
      authors: authors.map((author) => ({ userId: author.id })),
    }

    const request = editing
      ? updateApplication(Number(id), data, file)
      : createApplication(data, file!)

    request
      .then(() => navigate('/'))
      .catch((err: Error) => {
        setError(err.message)

        const fields = err instanceof ApiError ? err.fieldErrors : {}
        setFieldErrors(fields)

        if (Object.keys(fields).length === 0) {
          window.scrollTo({ top: 0 })
        }
      })
  }

  return (
    <div className="narrow">
      <h1>{editing ? `Изменение заявки №${id}` : 'Подача заявки'}</h1>

      {error && Object.keys(fieldErrors).length === 0 && (
        <p className="error">{error}</p>
      )}

      <form onSubmit={submit}>
        <Field label="Название работы" required error={fieldErrors.title}>
          <input
            required
            maxLength={500}
            value={title}
            aria-invalid={!!fieldErrors.title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </Field>

        <Field label="Направление" required error={fieldErrors.directionId}>
          <select
            required
            value={directionId || ''}
            aria-invalid={!!fieldErrors.directionId}
            onChange={(e) => setDirectionId(Number(e.target.value))}
          >
            <option value="">— выберите —</option>
            {directions.map((direction) => (
              <option key={direction.id} value={direction.id}>
                {direction.id}. {direction.name}
              </option>
            ))}
          </select>
        </Field>

        <h2>Авторы</h2>

        {authors.map((author) => (
          <div key={author.id} className="author-item">
            <p>
              <strong>
                {author.lastName} {author.firstName} {author.middleName ?? ''}
              </strong>
              <br />
              {author.email}
              {author.id === currentUser?.id && ' (вы, отправитель)'}
            </p>

            {author.id !== currentUser?.id && (
              <button type="button" onClick={() => removeAuthor(author.id)}>
                Удалить
              </button>
            )}
          </div>
        ))}

        {authors.length < 2 && (
          <Field label="Найти соавтора">
            <input
              type="search"
              value={query}
              placeholder="Фамилия, имя или email"
              onChange={(e) => setQuery(e.target.value)}
            />

            {searching && <p>Поиск...</p>}

            {results.length > 0 && (
              <ul>
                {results
                  .filter((user) => !authors.some((a) => a.id === user.id))
                  .map((user) => (
                    <li key={user.id}>
                      {user.lastName} {user.firstName} {user.middleName ?? ''}
                      {' — '}
                      {user.email}
                      {' '}
                      <button type="button" onClick={() => addAuthor(user)}>
                        Добавить
                      </button>
                    </li>
                  ))}
              </ul>
            )}
          </Field>
        )}

        <p>В заявке может быть не более двух авторов.</p>

        <h2>Тезисы</h2>

        {editing && currentFileName && (
          <p>
            Текущий файл: {currentFileName}.
            Выберите новый, только если хотите его заменить.
          </p>
        )}

        <Field
          label="Файл тезисов (.doc или .docx)"
          required={!editing}
          error={fieldErrors.file}
        >
          <input
            type="file"
            accept=".doc,.docx"
            required={!editing}
            aria-invalid={!!fieldErrors.file}
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          />
        </Field>

        <p>
          <button type="submit" disabled={!currentUser}>
            {editing ? 'Сохранить' : 'Подать заявку'}
          </button>
          {' '}
          <Link to="/">Отмена</Link>
        </p>
      </form>
    </div>
  )
}
