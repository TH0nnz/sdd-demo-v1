import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { timesheetAPI } from '../api/timesheets';
import type {
  TimesheetResponse,
  TimesheetPageResponse,
  WorkHoursCalculationResponse,
  CreateTimesheetRequest,
  UpdateTimesheetRequest,
  TimesheetFilterParams,
} from '../types/timesheet';

/**
 * Timesheet store using Pinia (T128)
 * Manages timesheet state and API interactions
 */
export const useTimesheetStore = defineStore('timesheet', () => {
  const timesheets = ref<TimesheetResponse[]>([]);
  const selectedTimesheet = ref<TimesheetResponse | null>(null);
  const calculationPreview = ref<WorkHoursCalculationResponse | null>(null);
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

  /**
   * Fetch timesheets for current user with optional date range
   */
  const fetchTimesheets = async (params?: TimesheetFilterParams) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await timesheetAPI.listMyTimesheets(params);
      timesheets.value = response.data.content;
      pagination.value = response.data.page;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch timesheets';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Get timesheet by ID
   */
  const fetchTimesheetById = async (timesheetId: number) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await timesheetAPI.getTimesheet(timesheetId);
      selectedTimesheet.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch timesheet';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Calculate work hours preview
   */
  const calculatePreview = async (data: CreateTimesheetRequest) => {
    error.value = null;
    try {
      const response = await timesheetAPI.calculatePreview(data);
      calculationPreview.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to calculate preview';
      throw err;
    }
  };

  /**
   * Create a new timesheet
   */
  const createTimesheet = async (data: CreateTimesheetRequest) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await timesheetAPI.createTimesheet(data);
      timesheets.value.unshift(response.data);
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to create timesheet';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Update an existing timesheet
   */
  const updateTimesheet = async (
    timesheetId: number,
    data: UpdateTimesheetRequest
  ) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await timesheetAPI.updateTimesheet(timesheetId, data);
      const index = timesheets.value.findIndex((t) => t.id === timesheetId);
      if (index > -1) {
        timesheets.value[index] = response.data;
      }
      if (selectedTimesheet.value?.id === timesheetId) {
        selectedTimesheet.value = response.data;
      }
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to update timesheet';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Delete a timesheet
   */
  const deleteTimesheet = async (timesheetId: number) => {
    loading.value = true;
    error.value = null;
    try {
      await timesheetAPI.deleteTimesheet(timesheetId);
      timesheets.value = timesheets.value.filter((t) => t.id !== timesheetId);
      if (selectedTimesheet.value?.id === timesheetId) {
        selectedTimesheet.value = null;
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to delete timesheet';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Getter: timesheets by date
   */
  const timesheetsByDate = (date: string) => {
    return timesheets.value.filter((t) => t.workDate === date);
  };

  /**
   * Getter: timesheets by task
   */
  const timesheetsByTask = (taskId: number) => {
    return timesheets.value.filter((t) => t.taskId === taskId);
  };

  /**
   * Getter: total hours this month
   */
  const totalHoursThisMonth = computed(() => {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    return timesheets.value
      .filter((t) => {
        const date = new Date(t.workDate);
        return (
          date.getMonth() === currentMonth && date.getFullYear() === currentYear
        );
      })
      .reduce((sum, t) => sum + t.calculatedHours, 0);
  });

  /**
   * Getter: timesheets with lunch deduction
   */
  const timesheetsWithLunch = computed(() => {
    return timesheets.value.filter((t) => t.lunchDeducted);
  });

  const isLoading = computed(() => loading.value);
  const hasError = computed(() => error.value !== null);

  return {
    // State
    timesheets,
    selectedTimesheet,
    calculationPreview,
    loading,
    error,
    pagination,

    // Computed
    totalHoursThisMonth,
    timesheetsWithLunch,
    isLoading,
    hasError,

    // Actions
    fetchTimesheets,
    fetchTimesheetById,
    calculatePreview,
    createTimesheet,
    updateTimesheet,
    deleteTimesheet,
    timesheetsByDate,
    timesheetsByTask,
  };
});
