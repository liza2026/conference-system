import { ApiError } from './ApiError'
import type {
  Application,
  ApplicationInput,
  AuthUser,
  Direction,
} from './types'

async function check(response: Response): Promise<Response> {
  if (!response.ok) {
    let message = `Ошибка ${response.status}`
    let fieldErrors: Record<string, string> = {}

    try {
      const problem = await response.json()

      if (problem.detail) {
        message = problem.detail
      }

      if (problem.errors) {
        fieldErrors = problem.errors
      }
    } catch {
      // Тело ответа не JSON.
    }

    if (message === `Ошибка ${response.status}`) {
      switch (response.status) {
        case 401:
          message = 'Неверный email или пароль.'
          break
        case 403:
          message = 'У вас нет доступа к этому действию.'
          break
        case 404:
          message = 'Запрашиваемый ресурс не найден.'
          break
        case 500:
          message = 'Внутренняя ошибка сервера. Попробуйте позже.'
          break
        default:
          message = 'Не удалось выполнить запрос. Попробуйте позже.'
      }
    }

    throw new ApiError(message, fieldErrors)
  }

  return response
}

export async function login(
  email: string,
  password: string,
): Promise<AuthUser> {
  const response = await check(
    await fetch('/api/auth/login', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    }),
  )

  return response.json()
}

export async function registerUser(data: {
  lastName: string
  firstName: string
  middleName: string | null
  email: string
  password: string
}): Promise<AuthUser> {
  const response = await check(
    await fetch('/api/users', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }),
  )

  return response.json()
}

export async function logout(): Promise<void> {
  await check(
    await fetch('/api/auth/logout', {
      method: 'POST',
      credentials: 'same-origin',
    }),
  )
}

export async function getCurrentUser(): Promise<AuthUser> {
  const response = await check(
    await fetch('/api/auth/me', {
      credentials: 'same-origin',
    }),
  )

  return response.json()
}

export async function searchUsers(query: string): Promise<AuthUser[]> {
  const params = new URLSearchParams({ q: query })

  const response = await check(
    await fetch(`/api/users/search?${params}`, {
      credentials: 'same-origin',
    }),
  )

  return response.json()
}

export async function getApplications(): Promise<Application[]> {
  const response = await check(await fetch('/api/applications'))
  return response.json()
}

export async function withdrawApplication(id: number): Promise<Application> {
  const response = await check(
    await fetch(`/api/applications/${id}/withdraw`, {
      method: 'POST',
    }),
  )

  return response.json()
}

export async function getApplication(id: number): Promise<Application> {
  const response = await check(
    await fetch(`/api/applications/${id}`),
  )

  return response.json()
}

export async function getDirections(): Promise<Direction[]> {
  const response = await check(
    await fetch('/api/directions'),
  )

  return response.json()
}

function buildBody(data: ApplicationInput, file: File | null): FormData {
  const body = new FormData()

  body.append(
    'data',
    new Blob([JSON.stringify(data)], {
      type: 'application/json',
    }),
  )

  if (file) {
    body.append('file', file)
  }

  return body
}

export async function createApplication(
  data: ApplicationInput,
  file: File,
): Promise<Application> {
  const response = await check(
    await fetch('/api/applications', {
      method: 'POST',
      body: buildBody(data, file),
    }),
  )

  return response.json()
}

export async function updateApplication(
  id: number,
  data: ApplicationInput,
  file: File | null,
): Promise<Application> {
  const response = await check(
    await fetch(`/api/applications/${id}`, {
      method: 'PUT',
      body: buildBody(data, file),
    }),
  )

  return response.json()
}
