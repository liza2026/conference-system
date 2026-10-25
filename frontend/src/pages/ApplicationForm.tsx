import { useEffect, useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { createApplication, getApplication, getDirections, updateApplication } from '../api'
import type { ApplicationInput, AuthorInput, Direction, SupervisorInput } from '../types'

const emptyAuthor = (): AuthorInput => ({
  lastName: '', firstName: '', middleName: '', groupName: '', phone: '', email: '', funding: 'BUDGET',
})
const emptySupervisor = (): SupervisorInput => ({
  lastName: '', firstName: '', middleName: '', post: '',
})

// Текстовые поля автора и руководителя: подпись и ключ в данных
const AUTHOR_FIELDS: { key: keyof AuthorInput; label: string }[] = [
  { key: 'lastName', label: 'Фамилия' },
  { key: 'firstName', label: 'Имя' },
  { key: 'middleName', label: 'Отчество (если есть)' },
  { key: 'groupName', label: 'Группа' },
  { key: 'phone', label: 'Телефон' },
  { key: 'email', label: 'E-mail' },
]
const SUPERVISOR_FIELDS: { key: keyof SupervisorInput; label: string }[] = [
  { key: 'lastName', label: 'Фамилия' },
  { key: 'firstName', label: 'Имя' },
  { key: 'middleName', label: 'Отчество (если есть)' },
  { key: 'post', label: 'Должность и кафедра' },
]
const FIELD_LABELS: Record<string, string> = {
  title: 'Название работы',
  directionId: 'Направление',
  lastName: 'Фамилия',
  firstName: 'Имя',
  middleName: 'Отчество',
  groupName: 'Группа',
  phone: 'Телефон',
  email: 'E-mail',
  post: 'Должность и кафедра',
  authors: 'Авторы',
  supervisors: 'Руководители',
}

// "authors[0].firstName: не должно быть пустым" -> "Автор 1, Имя: не должно быть пустым"
function humanize(message: string): string {
  return message
    .replace(/(authors|supervisors)\[(\d+)\]\.(\w+)/g, (_, list, i, field) =>
      `${list === 'authors' ? 'Автор' : 'Руководитель'} ${Number(i) + 1}, ${FIELD_LABELS[field] ?? field}`)
    .replace(/\b(title|directionId|authors|supervisors)\b(?=:)/g, (field) => FIELD_LABELS[field] ?? field)
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label>
      {label}
      {children}
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

  function submit(e: FormEvent) {
    e.preventDefault()
    setError('')
    if (!form.directionId) {
      setError('Выберите направление')
      return
    }
    if (!editing && !file) {
      setError('Выберите файл тезисов (.doc или .docx)')
      return
    }
    const request = editing
      ? updateApplication(Number(id), form, file)
      : createApplication(form, file as File)
    request.then(() => navigate('/')).catch((err: Error) => setError(err.message))
  }

  return (
    <>
      <h1>{editing ? `Изменение заявки №${id}` : 'Подача заявки'}</h1>
      {error && (
        <ul className="error">
          {humanize(error).split('; ').map((m) => (
            <li key={m}>{m}</li>
          ))}
        </ul>
      )}

      <form onSubmit={submit}>
        <Field label="Название работы">
          <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </Field>

        <Field label="Направление">
          <select
            value={form.directionId}
            onChange={(e) => setForm({ ...form, directionId: Number(e.target.value) })}
          >
            <option value={0}>— выберите —</option>
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
            {AUTHOR_FIELDS.map((f) => (
              <Field key={f.key} label={f.label}>
                <input value={a[f.key]} onChange={(e) => setAuthor(i, f.key, e.target.value)} />
              </Field>
            ))}
            <Field label="Финансовая основа обучения">
              <select value={a.funding} onChange={(e) => setAuthor(i, 'funding', e.target.value)}>
                <option value="BUDGET">Бюджет</option>
                <option value="CONTRACT">Контракт</option>
              </select>
            </Field>
            {form.authors.length > 1 && (
              <button
                type="button"
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
            {SUPERVISOR_FIELDS.map((f) => (
              <Field key={f.key} label={f.label}>
                <input value={s[f.key]} onChange={(e) => setSupervisor(i, f.key, e.target.value)} />
              </Field>
            ))}
            {form.supervisors.length > 1 && (
              <button
                type="button"
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
          <p>
            Текущий файл: {currentFileName}. Выберите новый, только если хотите заменить.
          </p>
        )}
        <input
          type="file"
          accept=".doc,.docx"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />

        <p>
          <button type="submit">{editing ? 'Сохранить' : 'Подать заявку'}</button>
          <Link to="/">Отмена</Link>
        </p>
      </form>
    </>
  )
}