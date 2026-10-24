export type Funding = 'BUDGET' | 'CONTRACT'
export type Status = 'SUBMITTED' | 'WITHDRAWN'

export interface Author {
  position: number
  lastName: string
  firstName: string
  middleName: string | null
  groupName: string
  phone: string
  email: string
  funding: Funding
}

export interface Supervisor {
  position: number
  lastName: string
  firstName: string
  middleName: string | null
  post: string
}

export interface Application {
  id: number
  title: string
  directionId: number
  directionName: string
  status: Status
  thesisFileName: string
  authors: Author[]
  supervisors: Supervisor[]
  createdAt: string
  updatedAt: string
}

export interface Direction {
  id: number
  name: string
}

// Данные, которые участник вводит в форме (то, что принимает бэкенд)
export interface AuthorInput {
  lastName: string
  firstName: string
  middleName: string
  groupName: string
  phone: string
  email: string
  funding: Funding
}

export interface SupervisorInput {
  lastName: string
  firstName: string
  middleName: string
  post: string
}

export interface ApplicationInput {
  title: string
  directionId: number
  authors: AuthorInput[]
  supervisors: SupervisorInput[]
}