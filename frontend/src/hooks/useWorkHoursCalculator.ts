import { ref, watch, computed } from 'vue';
import { useTimesheetStore } from '../stores/timesheet';
import type { CreateTimesheetRequest, WorkHoursCalculationResponse } from '../types/timesheet';

/**
 * Composable for real-time work hours calculation (T129)
 * Provides reactive calculation with lunch deduction preview
 */
export const useWorkHoursCalculator = () => {
  const timesheetStore = useTimesheetStore();
  const startTime = ref<string>('09:00');
  const endTime = ref<string>('17:00');
  const preview = ref<WorkHoursCalculationResponse | null>(null);
  const isCalculating = ref(false);
  const error = ref<string | null>(null);

  /**
   * Parse time string (HH:mm) to minutes since midnight
   */
  const timeToMinutes = (timeStr: string): number => {
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours * 60 + minutes;
  };

  /**
   * Calculate hours between two times
   */
  const calculateRawHours = (): number => {
    try {
      const startMinutes = timeToMinutes(startTime.value);
      const endMinutes = timeToMinutes(endTime.value);

      if (endMinutes <= startMinutes) {
        return 0;
      }

      return (endMinutes - startMinutes) / 60;
    } catch {
      return 0;
    }
  };

  /**
   * Check if work period overlaps lunch (12:00-13:00)
   */
  const shouldDeductLunch = (): boolean => {
    try {
      const startMinutes = timeToMinutes(startTime.value);
      const endMinutes = timeToMinutes(endTime.value);

      const lunchStart = 12 * 60; // 12:00 in minutes
      const lunchEnd = 13 * 60; // 13:00 in minutes

      return startMinutes < lunchEnd && endMinutes > lunchStart;
    } catch {
      return false;
    }
  };

  /**
   * Round hours to nearest 0.5
   */
  const roundToNearestHalf = (hours: number): number => {
    return Math.round(hours * 2) / 2;
  };

  /**
   * Calculate work hours locally (before API call)
   */
  const calculateLocalHours = (): {
    hours: number;
    lunchDeducted: boolean;
  } => {
    try {
      const rawHours = calculateRawHours();
      const lunchDeducted = shouldDeductLunch();
      const deductedHours = lunchDeducted ? rawHours - 1 : rawHours;
      const roundedHours = Math.max(0, roundToNearestHalf(deductedHours));

      return {
        hours: roundedHours,
        lunchDeducted,
      };
    } catch {
      return {
        hours: 0,
        lunchDeducted: false,
      };
    }
  };

  /**
   * Fetch preview from API
   */
  const fetchPreview = async (taskId: number, workDate: string) => {
    isCalculating.value = true;
    error.value = null;
    try {
      const request: CreateTimesheetRequest = {
        taskId,
        workDate,
        startTime: startTime.value,
        endTime: endTime.value,
      };

      const result = await timesheetStore.calculatePreview(request);
      preview.value = result;

      if (!result.valid) {
        error.value = result.validationMessage || 'Invalid time input';
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to calculate hours';
      preview.value = null;
    } finally {
      isCalculating.value = false;
    }
  };

  /**
   * Watch for time changes and calculate locally
   */
  const localCalculation = computed(() => {
    return calculateLocalHours();
  });

  /**
   * Validate time format
   */
  const isValidTimeFormat = (time: string): boolean => {
    const regex = /^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/;
    return regex.test(time);
  };

  /**
   * Validate time range
   */
  const isValidTimeRange = (): boolean => {
    if (!isValidTimeFormat(startTime.value) || !isValidTimeFormat(endTime.value)) {
      return false;
    }

    const startMinutes = timeToMinutes(startTime.value);
    const endMinutes = timeToMinutes(endTime.value);

    return endMinutes > startMinutes;
  };

  /**
   * Get formatted message
   */
  const message = computed(() => {
    if (preview.value?.message) {
      return preview.value.message;
    }

    const local = localCalculation.value;
    if (local.lunchDeducted) {
      return `工時: ${local.hours.toFixed(1)} 小時 (已自動扣除午休)`;
    }
    return `工時: ${local.hours.toFixed(1)} 小時`;
  });

  return {
    startTime,
    endTime,
    preview,
    isCalculating,
    error,
    localCalculation,
    message,

    // Methods
    fetchPreview,
    calculateLocalHours,
    calculateRawHours,
    shouldDeductLunch,
    isValidTimeFormat,
    isValidTimeRange,
  };
};
