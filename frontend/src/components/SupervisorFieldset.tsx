import type { SupervisorInput } from '../types'
import Field from './Field'

const FIELDS: { key: keyof SupervisorInput; label: string; required: boolean }[] = [
  { key: 'lastName', label: 'Фамилия', required: true },
  { key: 'firstName', label: 'Имя', required: true },
  { key: 'middleName', label: 'Отчество (если есть)', required: false },
  { key: 'post', label: 'Должность и кафедра', required: true },
]

interface Props {
  index: number
  supervisor: SupervisorInput
  errors: Record<string, string>
  canRemove: boolean
  onChange: (key: keyof SupervisorInput, value: string) => void
  onRemove: () => void
}

export default function SupervisorFieldset({
  index, supervisor, errors, canRemove, onChange, onRemove,
}: Props) {
  return (
    <fieldset>
      <legend>Руководитель {index + 1}</legend>
      {FIELDS.map((f) => {
        const path = `supervisors[${index}].${f.key}`
        return (
          <Field key={f.key} label={f.label} required={f.required} error={errors[path]}>
            <input
              required={f.required}
              aria-invalid={!!errors[path]}
              value={supervisor[f.key]}
              onChange={(e) => onChange(f.key, e.target.value)}
            />
          </Field>
        )
      })}
      {canRemove && (
        <button type="button" className="danger" onClick={onRemove}>
          Удалить руководителя
        </button>
      )}
    </fieldset>
  )
}