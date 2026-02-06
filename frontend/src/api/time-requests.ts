import { client } from './client';
import type { 
  TimeRequestResponse, 
  TimeRequestPageResponse,
  ApproveTimeRequestRequest
} from '../types/project';

/**
 * Time Request API endpoints for approval management
 */
export const timeRequestAPI = {
  /**
   * Get all pending time requests for the current manager
   */
  getPendingRequests: (params?: {
    page?: number;
    size?: number;
  }) => {
    return client.get<TimeRequestPageResponse>('/api/time-requests/pending', { params });
  },

  /**
   * Get time request by ID
   */
  getTimeRequestById: (requestId: number) => {
    return client.get<TimeRequestResponse>(`/api/time-requests/${requestId}`);
  },

  /**
   * Get time requests by project ID
   */
  getTimeRequestsByProject: (projectId: number, params?: {
    page?: number;
    size?: number;
  }) => {
    return client.get<TimeRequestPageResponse>(`/api/time-requests/project/${projectId}`, { params });
  },

  /**
   * Get user's own time requests
   */
  getMyTimeRequests: (params?: {
    page?: number;
    size?: number;
  }) => {
    return client.get<TimeRequestPageResponse>('/api/time-requests/my-requests', { params });
  },

  /**
   * Create a new time request
   */
  createTimeRequest: (data: any) => {
    return client.post<TimeRequestResponse>('/api/time-requests', data);
  },

  /**
   * Approve a time request
   */
  approveTimeRequest: (requestId: number, data: ApproveTimeRequestRequest) => {
    return client.post<TimeRequestResponse>(`/api/time-requests/${requestId}/approve`, data);
  },
};
