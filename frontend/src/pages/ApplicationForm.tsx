import { useEffect, useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  ApiError,
  createApplication,
  getApplication,
  getDirections,
  updateApplication,
} from '../api'
import type { ApplicationInput, AuthorInput, Direction, SupervisorInput } from '../types'

const emptyAuthor = (): AuthorInput => ({
  lastName: '', firstName: '', middleName: '', groupName: '', phone: '', email: '', funding: 'BUDGET',
})
const emptySupervisor = (): SupervisorInput => ({
  lastName: '', firstName: '', middleName: '', post: '',
})

// Текстовые поля автора и руководителя: ключ в данных, подпись, обязательность
const AUTHOR_FIELDS: { key: keyof AuthorInput; label: string; required: boolean; type?: string }[] = [
  { key: 'lastName', label: 'Фамилия', required: true },
  { key: 'firstName', label: 'Имя', required: true },
  { key: 'middleName', label: 'Отчество (если есть)', required: false },
  { key: 'groupName', label: 'Группа', required: true },
  { key: 'phone', label: 'Телефон', required: true, type: 'tel' },
  { key: 'email', label: 'E-mail', required: true, type: 'email' },
]
const SUPERVISOR_FIELDS: { key: keyof SupervisorInput; label: string; required: boolean }[] = [
  { key: 'lastName', label: 'Фамилия', required: true },
  { key: 'firstName', label: 'Имя', required: true },
  { key: 'middleName', label: 'Отчество (если есть)', required: false },
  { key: 'post', label: 'Должность и кафедра', required: true },
]

// Подпись + поле + текст ошибки под ним
function Field({ label, required, error, children }: {
  label: string
  required?: boolean
  error?: string
  children: ReactNode
}) {
  return (
    <label>
      {label}
      {required && ' *'}
      {children}
      {error && <span className="field-error">{error}</span>}
    </label>
  )
}

export default function ApplicationForm() {
  // Есть id в адресе - изменение заявки, нет - подача новой
  const { id } = useParams()
  const editing = id !== undefined
  const navigate = useNavigate()

  const [directions, setDirections] = useState<Direction[]>([])
  const [form, setForm] = useState<ApplicationInput>({
    title: '',
    directionId: 0,
    authors: [emptyAuthor()],
    supervisors: [emptySupervisor()],
  })
  const [file, setFile] = useState<File | null>(null)
  const [currentFileName, setCurrentFileName] = useState('')
  const [error, setError] = useState('')
  // Ошибки от сервера по полям: "authors[0].lastName" -> "только буквы, пробел, дефис"
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  // При открытии экрана: загрузить направления, а при изменении - ещё и саму заявку
  useEffect(() => {
    getDirections().then(setDirections).catch((e: Error) => setError(e.message))
    if (editing) {
      getApplication(Number(id))
        .then((a) => {
          setForm({
            title: a.title,
            directionId: a.directionId,
            authors: a.authors.map((x) => ({
              lastName: x.lastName,
              firstName: x.firstName,
              middleName: x.middleName ?? '',
              groupName: x.groupName,
              phone: x.phone,
              email: x.email,
              funding: x.funding,
            })),
            supervisors: a.supervisors.map((x) => ({
              lastName: x.lastName,
              firstName: x.firstName,
              middleName: x.middleName ?? '',
              post: x.post,
            })),
          })
          setCurrentFileName(a.thesisFileName)
        })
        .catch((e: Error) => setError(e.message))
    }
  }, [id, editing])

  // Когда появились ошибки по полям, переводим фокус на первое ошибочное поле
  useEffect(() => {
    document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
  }, [fieldErrors])

  function setAuthor(index: number, key: keyof AuthorInput, value: string) {
    setForm((f) => ({
      ...f,
      authors: f.authors.map((a, i) => (i === index ? ({ ...a, [key]: value } as AuthorInput) : a)),
    }))
  }

  function setSupervisor(index: number, key: keyof SupervisorInput, value: string) {
    setForm((f) => ({
      ...f,
      supervisors: f.supervisors.map((s, i) =>
        i === index ? ({ ...s, [key]: value } as SupervisorInput) : s,
      ),
    }))
  }

  // Отправка формы. Пустые обязательные поля браузер ловит сам, до этого места не доходя.
  function submit(e: FormEvent) {
    e.preventDefault() // не даём браузеру перезагрузить страницу
    setError('')
    setFieldErrors({})
    const request = editing
      ? updateApplication(Number(id), form, file)
      : createApplication(form, file as File)
    request
      .then(() => navigate('/'))
      .catch((err: Error) => {
        setError(err.message)
        const fields = err instanceof ApiError ? err.fieldErrors : {}
        setFieldErrors(fields)
        // Ошибка не про поля (например, срок вышел): показываем текст наверху страницы
        if (Object.keys(fields).length === 0) window.scrollTo({ top: 0 })
      })
  }

  return (
    <div className="narrow">
      <h1>{editing ? `Изменение заявки №${id}` : 'Подача заявки'}</h1>
      {error && <p className="error">{error}</p>}

      <form onSubmit={submit}>
        <Field label="Название работы" required error={fieldErrors['title']}>
          <input
            required
            aria-invalid={!!fieldErrors['title']}
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
        </Field>

        <Field label="Направление" required error={fieldErrors['directionId']}>
          <select
            required
            aria-invalid={!!fieldErrors['directionId']}
            value={form.directionId || ''}
            onChange={(e) => setForm({ ...form, directionId: Number(e.target.value) })}
          >
            <option value="">— выберите —</option>
            {directions.map((d) => (
              <option key={d.id} value={d.id}>
                {d.id}. {d.name}
              </option>
            ))}
          </select>
        </Field>

        <h2>Авторы</h2>
        {form.authors.map((a, i) => (
          <fieldset key={i}>
            <legend>Автор {i + 1}</legend>
            {AUTHOR_FIELDS.map((f) => {
              const path = `authors[${i}].${f.key}`
              return (
                <Field key={f.key} label={f.label} required={f.required} error={fieldErrors[path]}>
                  <input
                    type={f.type ?? 'text'}
                    required={f.required}
                    aria-invalid={!!fieldErrors[path]}
                    value={a[f.key]}
                    onChange={(e) => setAuthor(i, f.key, e.target.value)}
                  />
                </Field>
              )
            })}
            <Field label="Финансовая основа обучения" error={fieldErrors[`authors[${i}].funding`]}>
              <select value={a.funding} onChange={(e) => setAuthor(i, 'funding', e.target.value)}>
                <option value="BUDGET">Бюджет</option>
                <option value="CONTRACT">Контракт</option>
              </select>
            </Field>
            {form.authors.length > 1 && (
              <button
                type="button"
                className="danger"
                onClick={() =>
                  setForm({ ...form, authors: form.authors.filter((_, k) => k !== i) })
                }
              >
                Удалить автора
              </button>
            )}
          </fieldset>
        ))}
        {form.authors.length < 2 && (
          <button
            type="button"
            onClick={() => setForm({ ...form, authors: [...form.authors, emptyAuthor()] })}
          >
            Добавить автора
          </button>
        )}

        <h2>Научные руководители</h2>
        {form.supervisors.map((s, i) => (
          <fieldset key={i}>
            <legend>Руководитель {i + 1}</legend>
            {SUPERVISOR_FIELDS.map((f) => {
              const path = `supervisors[${i}].${f.key}`
              return (
                <Field key={f.key} label={f.label} required={f.required} error={fieldErrors[path]}>
                  <input
                    required={f.required}
                    aria-invalid={!!fieldErrors[path]}
                    value={s[f.key]}
                    onChange={(e) => setSupervisor(i, f.key, e.target.value)}
                  />
                </Field>
              )
            })}
            {form.supervisors.length > 1 && (
              <button
                type="button"
                className="danger"
                onClick={() =>
                  setForm({ ...form, supervisors: form.supervisors.filter((_, k) => k !== i) })
                }
              >
                Удалить руководителя
              </button>
            )}
          </fieldset>
        ))}
        {form.supervisors.length < 2 && (
          <button
            type="button"
            onClick={() =>
              setForm({ ...form, supervisors: [...form.supervisors, emptySupervisor()] })
            }
          >
            Добавить руководителя
          </button>
        )}

        <h2>Тезисы</h2>
        {editing && (
          <p>Текущий файл: {currentFileName}. Выберите новый, только если хотите заменить.</p>
        )}
        <Field label="Файл тезисов (.doc или .docx)" required={!editing} error={fieldErrors['file']}>
          <input
            type="file"
            accept=".doc,.docx"
            required={!editing}
            aria-invalid={!!fieldErrors['file']}
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          />
        </Field>

        <p>
          <button type="submit">{editing ? 'Сохранить' : 'Подать заявку'}</button>
          <Link to="/">Отмена</Link>
        </p>
      </form>
    </div>
  )
}