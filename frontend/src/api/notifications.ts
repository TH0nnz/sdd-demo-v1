/**
 * Notification API Client
 * T063: API client for notification-related operations
 */

import client from './client'
import type { Notification } from '@/types/notification'

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

/**
 * Get notifications for the current user
 */
export const getNotifications = async (params?: NotificationListParams): Promise<NotificationListResponse> => {
  const response = await client.get<NotificationListResponse>('/api/notifications', { params })
  return response.data
}

/**
 * Mark a notification as read
 */
export const markAsRead = async (id: number): Promise<Notification> => {
  const response = await client.put<Notification>(`/api/notifications/${id}/read`)
  return response.data
}

/**
 * Mark all notifications as read
 */
export const markAllAsRead = async (): Promise<void> => {
  await client.put('/api/notifications/read-all')
}
