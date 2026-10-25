import type { ReactNode } from 'react'

interface Props {
  label: string
  required?: boolean
  error?: string
  children: ReactNode
}

// Подпись + поле ввода (children) + текст ошибки под ним
export default function Field({ label, required, error, children }: Props) {
  return (
    <label>
      {label}
      {required && ' *'}
      {children}
      {error && <span className="field-error">{error}</span>}
    </label>
  )
}