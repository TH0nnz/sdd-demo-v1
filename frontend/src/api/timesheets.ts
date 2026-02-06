import { client } from './client';
import type {
  TimesheetResponse,
  TimesheetPageResponse,
  WorkHoursCalculationResponse,
  CreateTimesheetRequest,
  UpdateTimesheetRequest
} from '../types/timesheet';

/**
 * Timesheet API endpoints (T126)
 * Handles communication with backend timesheet APIs
 */
export const timesheetAPI = {
  /**
   * Get all timesheets for current user with optional date range
   */
  listMyTimesheets: (params?: {
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
  }) => {
    return client.get<TimesheetPageResponse>('/api/timesheets', { params });
  },

  /**
   * Get timesheet by ID
   */
  getTimesheet: (timesheetId: number) => {
    return client.get<TimesheetResponse>(`/api/timesheets/${timesheetId}`);
  },

  /**
   * Calculate work hours preview without saving
   * Used for real-time calculation in form
   */
  calculatePreview: (data: CreateTimesheetRequest) => {
    return client.post<WorkHoursCalculationResponse>(
      '/api/timesheets/calculate-preview',
      data
    );
  },

  /**
   * Create a new timesheet entry
   * Automatically deducts lunch break (12:00-13:00)
   */
  createTimesheet: (data: CreateTimesheetRequest) => {
    return client.post<TimesheetResponse>('/api/timesheets', data);
  },

  /**
   * Update an existing timesheet
   * Only allowed within 3 working days of work date
   */
  updateTimesheet: (timesheetId: number, data: UpdateTimesheetRequest) => {
    return client.put<TimesheetResponse>(`/api/timesheets/${timesheetId}`, data);
  },

  /**
   * Delete a timesheet entry
   */
  deleteTimesheet: (timesheetId: number) => {
    return client.delete(`/api/timesheets/${timesheetId}`);
  },
};
