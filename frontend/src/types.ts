export type Status = 'SUBMITTED' | 'WITHDRAWN'

export interface AuthUser {
  id: number
  lastName: string
  firstName: string
  middleName: string | null
  email: string
}

export interface Direction {
  id: number
  name: string
}

export interface Author {
  position: number
  userId: number
  lastName: string
  firstName: string
  middleName: string | null
  email: string
}

export interface Application {
  id: number
  title: string
  directionId: number
  directionName: string
  status: Status
  thesisFileName: string
  authors: Author[]
  createdAt: string
  updatedAt: string
}

export interface AuthorInput {
  userId: number
}

export interface ApplicationInput {
  title: string
  directionId: number
  submitterUserId: number
  authors: AuthorInput[]
}
