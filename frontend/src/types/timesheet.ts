/**
 * Timesheet management types and interfaces (T127)
 */

export interface Timesheet {
  id: number;
  userId: number;
  userName: string;
  taskId: number;
  taskName: string;
  projectId: number;
  projectName: string;
  workDate: string; // ISO date format
  startTime: string; // HH:mm format
  endTime: string; // HH:mm format
  calculatedHours: number;
  lunchDeducted: boolean;
  message: string;
  createdAt: string;
  updatedAt: string;
}

export interface TimesheetResponse {
  id: number;
  userId: number;
  userName: string;
  taskId: number;
  taskName: string;
  projectId: number;
  projectName: string;
  workDate: string;
  startTime: string;
  endTime: string;
  calculatedHours: number;
  lunchDeducted: boolean;
  message: string;
  createdAt: string;
  updatedAt: string;
}

export interface TimesheetPageResponse {
  content: TimesheetResponse[];
  page: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export interface WorkHoursCalculationResponse {
  calculatedHours: number;
  lunchDeducted: boolean;
  lunchHours: number;
  message: string;
  valid: boolean;
  validationMessage?: string;
}

export interface CreateTimesheetRequest {
  taskId: number;
  workDate: string; // ISO date format
  startTime: string; // HH:mm format
  endTime: string; // HH:mm format
}

export interface UpdateTimesheetRequest {
  startTime: string; // HH:mm format
  endTime: string; // HH:mm format
}

/**
 * Form state for timesheet creation/editing
 */
export interface TimesheetFormState {
  taskId: number | null;
  workDate: string;
  startTime: string;
  endTime: string;
  calculatedHours: number | null;
  lunchDeducted: boolean;
  isLoading: boolean;
  errors: Record<string, string>;
}

/**
 * Filter params for listing timesheets
 */
export interface TimesheetFilterParams {
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}
