import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { reportAPI } from '../api/reports';
import type {
  TimesheetReportEntry,
  TimesheetReportPageResponse,
  ProjectSummaryReport,
  UserSummaryReport,
  ReportFilterParams,
  ReportFilterState,
  ReportSummary,
} from '../types/report';

/**
 * Report store using Pinia
 * Manages report state and API interactions
 */
export const useReportStore = defineStore('report', () => {
  const timesheetReport = ref<TimesheetReportEntry[]>([]);
  const projectSummary = ref<ProjectSummaryReport[]>([]);
  const userSummary = ref<UserSummaryReport[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const pagination = ref({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });

  const filterState = ref<ReportFilterState>({
    departmentId: null,
    startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
    userId: null,
    projectId: null,
    isLoading: false,
    errors: {},
  });

  /**
   * Fetch timesheet report with filters
   */
  const fetchTimesheetReport = async (params: ReportFilterParams) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await reportAPI.getTimesheetReport(params);
      timesheetReport.value = response.data.content;
      pagination.value = response.data.page;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch timesheet report';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Export timesheet report as CSV
   */
  const exportTimesheetReportCsv = async (params: Omit<ReportFilterParams, 'page' | 'size'>) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await reportAPI.exportTimesheetReportCsv(params);
      await reportAPI.downloadCsv(response.data, 'timesheet-report.csv');
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to export timesheet report';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Fetch project summary report
   */
  const fetchProjectSummary = async (params: Omit<ReportFilterParams, 'page' | 'size' | 'userId'>) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await reportAPI.getProjectSummary(params);
      projectSummary.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch project summary';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Fetch user summary report
   */
  const fetchUserSummary = async (params: Omit<ReportFilterParams, 'page' | 'size' | 'projectId'>) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await reportAPI.getUserSummary(params);
      userSummary.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch user summary';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Update filter state
   */
  const updateFilter = (key: keyof Omit<ReportFilterState, 'isLoading' | 'errors'>, value: any) => {
    filterState.value[key] = value;
  };

  /**
   * Clear filters
   */
  const clearFilters = () => {
    filterState.value = {
      departmentId: null,
      startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      endDate: new Date().toISOString().split('T')[0],
      userId: null,
      projectId: null,
      isLoading: false,
      errors: {},
    };
  };

  /**
   * Calculate report summary
   */
  const reportSummary = computed((): ReportSummary => {
    const totalHours = timesheetReport.value
      .reduce((sum, entry) => sum + parseFloat(entry.hours), 0)
      .toFixed(2);

    const uniqueUsers = new Set(timesheetReport.value.map(e => e.userId)).size;
    const uniqueProjects = new Set(timesheetReport.value.map(e => e.projectId)).size;

    return {
      totalHours: totalHours.toString(),
      totalEntries: timesheetReport.value.length,
      uniqueUsers,
      uniqueProjects,
      dateRange: {
        start: filterState.value.startDate,
        end: filterState.value.endDate,
      },
    };
  });

  /**
   * Get total hours by project
   */
  const hoursByProject = computed(() => {
    const result: Record<string, number> = {};
    timesheetReport.value.forEach(entry => {
      if (!result[entry.projectName]) {
        result[entry.projectName] = 0;
      }
      result[entry.projectName] += parseFloat(entry.hours);
    });
    return result;
  });

  /**
   * Get total hours by user
   */
  const hoursByUser = computed(() => {
    const result: Record<string, number> = {};
    timesheetReport.value.forEach(entry => {
      if (!result[entry.userName]) {
        result[entry.userName] = 0;
      }
      result[entry.userName] += parseFloat(entry.hours);
    });
    return result;
  });

  const isLoading = computed(() => loading.value);
  const hasError = computed(() => error.value !== null);

  return {
    // State
    timesheetReport,
    projectSummary,
    userSummary,
    loading,
    error,
    pagination,
    filterState,

    // Computed
    reportSummary,
    hoursByProject,
    hoursByUser,
    isLoading,
    hasError,

    // Actions
    fetchTimesheetReport,
    exportTimesheetReportCsv,
    fetchProjectSummary,
    fetchUserSummary,
    updateFilter,
    clearFilters,
  };
});
