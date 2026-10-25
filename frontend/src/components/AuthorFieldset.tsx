import type { AuthorInput } from '../types'
import Field from './Field'

const FIELDS: { key: keyof AuthorInput; label: string; required: boolean; type?: string }[] = [
  { key: 'lastName', label: 'Фамилия', required: true },
  { key: 'firstName', label: 'Имя', required: true },
  { key: 'middleName', label: 'Отчество (если есть)', required: false },
  { key: 'groupName', label: 'Группа', required: true },
  { key: 'phone', label: 'Телефон', required: true, type: 'tel' },
  { key: 'email', label: 'E-mail', required: true, type: 'email' },
]

interface Props {
  index: number
  author: AuthorInput
  errors: Record<string, string> // ошибки всей формы: "authors[0].lastName" -> текст
  canRemove: boolean
  onChange: (key: keyof AuthorInput, value: string) => void
  onRemove: () => void
}

export default function AuthorFieldset({ index, author, errors, canRemove, onChange, onRemove }: Props) {
  return (
    <fieldset>
      <legend>Автор {index + 1}</legend>
      {FIELDS.map((f) => {
        const path = `authors[${index}].${f.key}`
        return (
          <Field key={f.key} label={f.label} required={f.required} error={errors[path]}>
            <input
              type={f.type ?? 'text'}
              required={f.required}
              aria-invalid={!!errors[path]}
              value={author[f.key]}
              onChange={(e) => onChange(f.key, e.target.value)}
            />
          </Field>
        )
      })}
      <Field label="Финансовая основа обучения" error={errors[`authors[${index}].funding`]}>
        <select value={author.funding} onChange={(e) => onChange('funding', e.target.value)}>
          <option value="BUDGET">Бюджет</option>
          <option value="CONTRACT">Контракт</option>
        </select>
      </Field>
      {canRemove && (
        <button type="button" className="danger" onClick={onRemove}>
          Удалить автора
        </button>
      )}
    </fieldset>
  )
}