import { useEffect, useState } from 'react'
import { getApplications, withdrawApplication } from '../api'
import type { Application, Status } from '../types'

const STATUS_TEXT: Record<Status, string> = {
  SUBMITTED: 'Подана',
  WITHDRAWN: 'Отозвана',
}

export default function ApplicationList() {
  // useState хранит данные экрана: когда они меняются, React перерисовывает страницу
  const [applications, setApplications] = useState<Application[]>([])
  const [error, setError] = useState('')

  // useEffect с пустым списком [] выполняется один раз, когда экран появился
  useEffect(() => {
    getApplications()
      .then(setApplications)
      .catch((e: Error) => setError(e.message))
  }, [])

  function withdraw(id: number) {
    if (!window.confirm('Отозвать заявку? Это действие нельзя отменить.')) {
      return
    }
    setError('')
    withdrawApplication(id)
      .then((updated) =>
        // подменить в списке старую заявку обновлённой
        setApplications((list) => list.map((a) => (a.id === updated.id ? updated : a))),
      )
      .catch((e: Error) => setError(e.message))
  }

  return (
    <>
      <h1>Заявки</h1>
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>№</th>
            <th>Название</th>
            <th>Направление</th>
            <th>Авторы</th>
            <th>Руководитель</th>
            <th>Статус</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          {applications.map((a) => (
            <tr key={a.id} className={a.status === 'WITHDRAWN' ? 'withdrawn' : ''}>
              <td>{a.id}</td>
              <td>{a.title}</td>
              <td>{a.directionId}. {a.directionName}</td>
              <td>{a.authors.map((x) => `${x.lastName} ${x.firstName}`).join(', ')}</td>
              <td>{a.supervisors.map((x) => `${x.lastName} ${x.firstName}`).join(', ')}</td>
              <td>{STATUS_TEXT[a.status]}</td>
              <td>
                <a href={`/api/applications/${a.id}/file`}>Скачать файл</a>
                {a.status === 'SUBMITTED' && (
                  <>
                    {' '}
                    <button onClick={() => withdraw(a.id)}>Отозвать</button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {applications.length === 0 && !error && <p>Заявок пока нет.</p>}
    </>
  )
}