import { client } from './client';
import type {
  TimesheetReportEntry,
  TimesheetReportPageResponse,
  ProjectSummaryReport,
  UserSummaryReport,
  ReportFilterParams,
} from '../types/report';

/**
 * Report API endpoints (T143)
 * Handles communication with backend report APIs
 */
export const reportAPI = {
  /**
   * Get paginated timesheet report with optional filters
   */
  getTimesheetReport: (params: ReportFilterParams) => {
    return client.get<TimesheetReportPageResponse>('/reports/timesheets', { params });
  },

  /**
   * Export timesheet report as CSV file
   */
  exportTimesheetReportCsv: (params: Omit<ReportFilterParams, 'page' | 'size'>) => {
    return client.get<string>('/reports/timesheets/export', { 
      params,
      responseType: 'blob'
    });
  },

  /**
   * Get project summary report
   */
  getProjectSummary: (params: Omit<ReportFilterParams, 'page' | 'size' | 'userId'>) => {
    return client.get<ProjectSummaryReport[]>('/reports/projects/summary', { params });
  },

  /**
   * Get user summary report
   */
  getUserSummary: (params: Omit<ReportFilterParams, 'page' | 'size' | 'projectId'>) => {
    return client.get<UserSummaryReport[]>('/reports/users/summary', { params });
  },

  /**
   * Download CSV as file
   */
  downloadCsv: async (csvData: Blob, filename: string = 'timesheet-report.csv') => {
    const url = URL.createObjectURL(csvData);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  },
};
