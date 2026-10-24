import type { Application } from './types'

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