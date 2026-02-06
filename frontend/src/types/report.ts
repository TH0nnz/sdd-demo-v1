/**
 * Report management types and interfaces (T144)
 */

export interface TimesheetReportEntry {
  id: number;
  date: string; // ISO date format
  userName: string;
  userId: number;
  projectName: string;
  projectId: number;
  taskName: string;
  taskId: number;
  hours: string; // decimal as string
}

export interface TimesheetReportPageResponse {
  content: TimesheetReportEntry[];
  page: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export interface ProjectSummaryReport {
  projectId: number;
  projectName: string;
  totalHours: string; // decimal as string
  entryCount: number;
  assignedUserCount: number;
}

export interface UserSummaryReport {
  userId: number;
  userName: string;
  totalHours: string; // decimal as string
  entryCount: number;
  projectCount: number;
}

/**
 * Filter params for report queries
 */
export interface ReportFilterParams {
  departmentId: number;
  startDate?: string; // ISO date format
  endDate?: string; // ISO date format
  userId?: number;
  projectId?: number;
  page?: number;
  size?: number;
}

/**
 * Report filter state for UI
 */
export interface ReportFilterState {
  departmentId: number | null;
  startDate: string;
  endDate: string;
  userId: number | null;
  projectId: number | null;
  isLoading: boolean;
  errors: Record<string, string>;
}

/**
 * Report summary statistics
 */
export interface ReportSummary {
  totalHours: string;
  totalEntries: number;
  uniqueUsers: number;
  uniqueProjects: number;
  dateRange: {
    start: string;
    end: string;
  };
}
