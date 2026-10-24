import type { Application, ApplicationInput, Direction } from './types'

// Если бэкенд ответил ошибкой, достаём из ответа понятный текст (поле "detail")
async function check(response: Response): Promise<Response> {
  if (!response.ok) {
    let message = `Ошибка ${response.status}`
    try {
      const problem = await response.json()
      if (problem.detail) message = problem.detail
    } catch {
      // тело ответа не JSON, остаётся общий текст
    }
    throw new Error(message)
  }
  return response
}

export async function getApplications(): Promise<Application[]> {
  const response = await check(await fetch('/api/applications'))
  return response.json()
}

export async function withdrawApplication(id: number): Promise<Application> {
  const response = await check(
    await fetch(`/api/applications/${id}/withdraw`, { method: 'POST' }),
  )
  return response.json()
}

export async function getApplication(id: number): Promise<Application> {
  const response = await check(await fetch(`/api/applications/${id}`))
  return response.json()
}

export async function getDirections(): Promise<Direction[]> {
  const response = await check(await fetch('/api/directions'))
  return response.json()
}

// Тело запроса: часть "data" (JSON с данными) и часть "file" (файл тезисов), как ждёт контроллер
function buildBody(data: ApplicationInput, file: File | null): FormData {
  const body = new FormData()
  body.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  if (file) body.append('file', file)
  return body
}

export async function createApplication(data: ApplicationInput, file: File): Promise<Application> {
  const response = await check(
    await fetch('/api/applications', { method: 'POST', body: buildBody(data, file) }),
  )
  return response.json()
}

export async function updateApplication(
  id: number,
  data: ApplicationInput,
  file: File | null,
): Promise<Application> {
  const response = await check(
    await fetch(`/api/applications/${id}`, { method: 'PUT', body: buildBody(data, file) }),
  )
  return response.json()
}