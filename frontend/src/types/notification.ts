/**
 * Notification Types
 * T063: Types for notification system
 */

export interface Notification {
  id: number
  userId: number
  type: NotificationType
  message: string
  relatedEntityType?: string | null
  relatedEntityId?: number | null
  read: boolean
  createdAt: string
}

export enum NotificationType {
  INFO = 'INFO',
  WARNING = 'WARNING',
  ERROR = 'ERROR',
  SUCCESS = 'SUCCESS',
  TASK_ASSIGNED = 'TASK_ASSIGNED',
  HOURS_LOW = 'HOURS_LOW',
  HOURS_REQUEST_APPROVED = 'HOURS_REQUEST_APPROVED',
  HOURS_REQUEST_REJECTED = 'HOURS_REQUEST_REJECTED',
}

export interface NotificationListParams {
  page?: number
  size?: number
  unreadOnly?: boolean
}

export interface NotificationListResponse {
  content: Notification[]
  totalElements: number
  totalPages: number
  currentPage: number
  unreadCount: number
}
