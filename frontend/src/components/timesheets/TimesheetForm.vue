<template>
  <form @submit.prevent="handleSubmit" class="timesheet-form">
    <div class="form-group">
      <label for="task-id">任務</label>
      <select
        id="task-id"
        v-model.number="formData.taskId"
        class="form-control"
        required
      >
        <option value="">-- 請選擇任務 --</option>
        <option v-for="task in availableTasks" :key="task.id" :value="task.id">
          {{ task.projectName }} - {{ task.name }}
        </option>
      </select>
      <span v-if="errors.taskId" class="error-text">{{ errors.taskId }}</span>
    </div>

    <div class="form-group">
      <label for="work-date">工作日期</label>
      <input
        id="work-date"
        v-model="formData.workDate"
        type="date"
        class="form-control"
        required
        :max="todayDate"
      />
      <span v-if="errors.workDate" class="error-text">{{ errors.workDate }}</span>
    </div>

    <div class="time-inputs-row">
      <div class="form-group">
        <label for="start-time">開始時間</label>
        <input
          id="start-time"
          v-model="calculator.startTime"
          type="time"
          class="form-control"
          required
          @change="onTimeChange"
        />
        <span v-if="errors.startTime" class="error-text">{{ errors.startTime }}</span>
      </div>

      <div class="form-group">
        <label for="end-time">結束時間</label>
        <input
          id="end-time"
          v-model="calculator.endTime"
          type="time"
          class="form-control"
          required
          @change="onTimeChange"
        />
        <span v-if="errors.endTime" class="error-text">{{ errors.endTime }}</span>
      </div>
    </div>

    <!-- Work Hours Calculator Preview -->
    <WorkHoursCalculator :fetch-preview="false" />

    <!-- Lunch Alert -->
    <div v-if="calculator.localCalculation.lunchDeducted" class="alert alert-info">
      <h4>☀️ 午休提示</h4>
      <p>
        您的工作時間包含午休時間 (12:00-13:00)，系統將自動扣除 1 小時。
      </p>
      <p>實際計算工時：{{ calculator.localCalculation.hours.toFixed(1) }} 小時</p>
    </div>

    <!-- Task Hours Alert -->
    <div v-if="taskExceedsEstimate" class="alert alert-warning">
      <h4>⚠️ 工時超出提醒</h4>
      <p>
        此任務的工時將超過估計工時。任務經理將會收到通知。
      </p>
    </div>

    <!-- Form Actions -->
    <div class="form-actions">
      <button
        type="submit"
        :disabled="isLoading || !calculator.isValidTimeRange()"
        class="btn btn-primary"
      >
        <span v-if="isLoading">處理中...</span>
        <span v-else>{{ isEditing ? '更新' : '提交' }}</span>
      </button>
      <button
        v-if="isEditing"
        type="button"
        @click="handleDelete"
        :disabled="isLoading"
        class="btn btn-danger"
      >
        刪除
      </button>
      <button type="button" @click="handleReset" class="btn btn-secondary">
        重設
      </button>
    </div>

    <!-- Error Display -->
    <div v-if="submitError" class="alert alert-danger">
      {{ submitError }}
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useWorkHoursCalculator } from '../../hooks/useWorkHoursCalculator';
import { useTimesheetStore } from '../../stores/timesheet';
import WorkHoursCalculator from './WorkHoursCalculator.vue';
import type { CreateTimesheetRequest, UpdateTimesheetRequest } from '../../types/timesheet';

interface Props {
  timesheetId?: number;
  isEditing?: boolean;
  availableTasks?: Array<{
    id: number;
    name: string;
    projectName: string;
    estimatedHours: number;
    usedHours: number;
  }>;
  onSuccess?: () => void;
}

const props = withDefaults(defineProps<Props>(), {
  isEditing: false,
  availableTasks: () => [],
});

const timesheetStore = useTimesheetStore();
const calculator = useWorkHoursCalculator();

const formData = ref({
  taskId: null as number | null,
  workDate: new Date().toISOString().split('T')[0],
  startTime: '09:00',
  endTime: '17:00',
});

const errors = ref<Record<string, string>>({});
const isLoading = ref(false);
const submitError = ref('');

const todayDate = computed(() => new Date().toISOString().split('T')[0]);

const selectedTask = computed(() => {
  return props.availableTasks.find((t) => t.id === formData.value.taskId);
});

const taskExceedsEstimate = computed(() => {
  if (!selectedTask.value) return false;
  const newHours = calculator.localCalculation.value.hours;
  return (
    selectedTask.value.usedHours + newHours >
    selectedTask.value.estimatedHours
  );
});

const onTimeChange = () => {
  // Validate time range
  if (!calculator.isValidTimeRange()) {
    errors.value.startTime = 'Invalid time range';
  } else {
    delete errors.value.startTime;
  }
};

const handleSubmit = async () => {
  errors.value = {};
  submitError.value = '';
  isLoading.value = true;

  try {
    // Validate form
    if (!formData.value.taskId) {
      errors.value.taskId = '請選擇任務';
    }
    if (!formData.value.workDate) {
      errors.value.workDate = '請選擇工作日期';
    }
    if (!calculator.isValidTimeRange()) {
      errors.value.startTime = '無效的時間範圍';
    }

    if (Object.keys(errors.value).length > 0) {
      isLoading.value = false;
      return;
    }

    if (props.isEditing && props.timesheetId) {
      // Update
      const updateData: UpdateTimesheetRequest = {
        startTime: calculator.startTime.value,
        endTime: calculator.endTime.value,
      };
      await timesheetStore.updateTimesheet(props.timesheetId, updateData);
    } else {
      // Create
      const createData: CreateTimesheetRequest = {
        taskId: formData.value.taskId,
        workDate: formData.value.workDate,
        startTime: calculator.startTime.value,
        endTime: calculator.endTime.value,
      };
      await timesheetStore.createTimesheet(createData);
    }

    props.onSuccess?.();
  } catch (err: any) {
    submitError.value = err.message || '提交失敗，請稍後再試';
  } finally {
    isLoading.value = false;
  }
};

const handleDelete = async () => {
  if (props.timesheetId && confirm('確認刪除此工時記錄？')) {
    isLoading.value = true;
    try {
      await timesheetStore.deleteTimesheet(props.timesheetId);
      props.onSuccess?.();
    } catch (err: any) {
      submitError.value = err.message || '刪除失敗';
    } finally {
      isLoading.value = false;
    }
  }
};

const handleReset = () => {
  formData.value = {
    taskId: null,
    workDate: new Date().toISOString().split('T')[0],
    startTime: '09:00',
    endTime: '17:00',
  };
  calculator.startTime.value = '09:00';
  calculator.endTime.value = '17:00';
  errors.value = {};
  submitError.value = '';
};

onMounted(async () => {
  if (props.isEditing && props.timesheetId) {
    try {
      const timesheet = await timesheetStore.fetchTimesheetById(props.timesheetId);
      if (timesheet) {
        formData.value.taskId = timesheet.taskId;
        formData.value.workDate = timesheet.workDate;
        calculator.startTime.value = timesheet.startTime;
        calculator.endTime.value = timesheet.endTime;
      }
    } catch (err: any) {
      submitError.value = '加載工時記錄失敗';
    }
  }
});
</script>

<style scoped>
.timesheet-form {
  max-width: 600px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-control:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 4px rgba(76, 175, 80, 0.3);
}

.error-text {
  display: block;
  color: #d32f2f;
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

.time-inputs-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.alert {
  padding: 1rem;
  margin-bottom: 1.5rem;
  border-radius: 4px;
}

.alert h4 {
  margin: 0 0 0.5rem 0;
}

.alert p {
  margin: 0;
}

.alert-info {
  background-color: #e3f2fd;
  border: 1px solid #90caf9;
  color: #1565c0;
}

.alert-warning {
  background-color: #fff3e0;
  border: 1px solid #ffe0b2;
  color: #e65100;
}

.alert-danger {
  background-color: #ffebee;
  border: 1px solid #ef9a9a;
  color: #c62828;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn {
  padding: 0.5rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-danger {
  background-color: #f44336;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background-color: #da190b;
}

.btn-secondary {
  background-color: #2196F3;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #0b7dda;
}
</style>
