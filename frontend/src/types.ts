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