import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getApplications, withdrawApplication } from '../api'
import type { Application, Status } from '../types'

const STATUS_TEXT: Record<Status, string> = {
  SUBMITTED: 'Подана',
  WITHDRAWN: 'Отозвана',
}

export default function ApplicationList() {
  const [applications, setApplications] = useState<Application[]>([])
  const [error, setError] = useState('')

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
        setApplications((list) =>
          list.map((a) => (a.id === updated.id ? updated : a)),
        ),
      )
      .catch((e: Error) => setError(e.message))
  }

  return (
    <>
      <h1>Заявки</h1>

      <p>
        <Link to="/applications/new" className="button">
          Подать заявку
        </Link>
      </p>

      {error && <p className="error">{error}</p>}

      {applications.length === 0 && !error ? (
        <p>Заявок пока нет.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>№</th>
              <th>Название</th>
              <th>Направление</th>
              <th>Авторы</th>
              <th>Статус</th>
              <th>Действия</th>
            </tr>
          </thead>

          <tbody>
            {applications.map((application) => (
              <tr
                key={application.id}
                className={
                  application.status === 'WITHDRAWN' ? 'withdrawn' : ''
                }
              >
                <td>{application.id}</td>

                <td>{application.title}</td>

                <td>
                  {application.directionId}. {application.directionName}
                </td>

                <td>
                  {application.authors
                    .map(
                      (author) =>
                        `${author.lastName} ${author.firstName}`,
                    )
                    .join(', ')}
                </td>

                <td>{STATUS_TEXT[application.status]}</td>

                <td>
                  <a href={`/api/applications/${application.id}/file`}>
                    Скачать файл
                  </a>

                  {application.status === 'SUBMITTED' && (
                    <>
                      {' '}
                      <Link
                        to={`/applications/${application.id}/edit`}
                      >
                        Изменить
                      </Link>
                      {' '}
                      <button
                        className="danger"
                        onClick={() => withdraw(application.id)}
                      >
                        Отозвать
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  )
}
