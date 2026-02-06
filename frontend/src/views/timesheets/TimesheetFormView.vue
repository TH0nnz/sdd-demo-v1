<template>
  <div class="timesheet-form-view">
    <div class="page-header">
      <h1>{{ isEditing ? '編輯工時' : '填報工時' }}</h1>
      <p class="subtitle">{{ isEditing ? '修改您的工時記錄' : '記錄您的工作時間' }}</p>
    </div>

    <div class="form-container">
      <TimesheetForm
        v-if="availableTasks.length > 0"
        :timesheet-id="timesheetId"
        :is-editing="isEditing"
        :available-tasks="availableTasks"
        @success="handleSuccess"
      />
      <div v-else class="no-tasks-message">
        <p>暫無可用的任務。請聯繫您的經理分配任務。</p>
      </div>
    </div>

    <!-- Recent Timesheets -->
    <div class="recent-timesheets">
      <h2>最近的工時記錄</h2>
      <div v-if="recentTimesheets.length > 0" class="timesheet-list">
        <div v-for="ts in recentTimesheets.slice(0, 5)" :key="ts.id" class="timesheet-item">
          <div class="item-header">
            <span class="date">{{ formatDate(ts.workDate) }}</span>
            <span class="task">{{ ts.taskName }}</span>
          </div>
          <div class="item-body">
            <span class="time">{{ ts.startTime }} ~ {{ ts.endTime }}</span>
            <span class="hours">{{ ts.calculatedHours.toFixed(1) }} 小時</span>
            <span v-if="ts.lunchDeducted" class="lunch-badge">已扣午休</span>
          </div>
          <div class="item-actions">
            <button @click="handleEdit(ts.id)" class="btn-small">編輯</button>
            <button @click="handleDelete(ts.id)" class="btn-small btn-delete">刪除</button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>暫無工時記錄</p>
      </div>
    </div>

    <!-- Success Message -->
    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
      <button @click="successMessage = ''" class="close-btn">×</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTimesheetStore } from '../../stores/timesheet';
import { useTaskStore } from '../../stores/task';
import TimesheetForm from '../../components/timesheets/TimesheetForm.vue';
import type { TaskResponse } from '../../types/task';

const route = useRoute();
const router = useRouter();
const timesheetStore = useTimesheetStore();
const taskStore = useTaskStore();

const timesheetId = computed(() => {
  const id = route.query.id;
  return id ? parseInt(id as string) : undefined;
});

const isEditing = computed(() => !!timesheetId.value);
const successMessage = ref('');

const availableTasks = ref<Array<{
  id: number;
  name: string;
  projectName: string;
  estimatedHours: number;
  usedHours: number;
}>>([]);

const recentTimesheets = computed(() => {
  return timesheetStore.timesheets;
});

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr + 'T00:00:00');
  const options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  };
  return date.toLocaleDateString('zh-TW', options);
};

const handleSuccess = async () => {
  successMessage.value = isEditing.value ? '工時已更新' : '工時已提交';
  
  // Refresh timesheets and redirect
  setTimeout(() => {
    router.push('/timesheets');
  }, 1500);
};

const handleEdit = (id: number) => {
  router.push({ query: { id } });
};

const handleDelete = async (id: number) => {
  if (confirm('確認刪除此工時記錄？')) {
    try {
      await timesheetStore.deleteTimesheet(id);
      successMessage.value = '工時已刪除';
      await timesheetStore.fetchTimesheets();
    } catch (err: any) {
      alert(err.message || '刪除失敗');
    }
  }
};

onMounted(async () => {
  try {
    // Fetch available tasks
    await taskStore.fetchTasks({ page: 0, size: 100 });
    
    availableTasks.value = taskStore.tasks
      .filter((t) => t.status !== 'COMPLETED')
      .map((t) => ({
        id: t.id,
        name: t.name,
        projectName: t.projectName,
        estimatedHours: t.estimatedHours,
        usedHours: t.usedHours,
      }));

    // Fetch recent timesheets
    const today = new Date().toISOString().split('T')[0];
    const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split('T')[0];
    
    await timesheetStore.fetchTimesheets({
      startDate: thirtyDaysAgo,
      endDate: today,
    });
  } catch (err: any) {
    console.error('Failed to load data:', err);
  }
});
</script>

<style scoped>
.timesheet-form-view {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  color: #333;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 1.1rem;
}

.form-container {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.no-tasks-message {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.recent-timesheets {
  margin-top: 3rem;
}

.recent-timesheets h2 {
  margin-top: 0;
  color: #333;
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
}

.timesheet-list {
  display: grid;
  gap: 1rem;
}

.timesheet-item {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: box-shadow 0.3s;
}

.timesheet-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex: 1;
}

.date {
  font-weight: 600;
  color: #333;
  min-width: 100px;
}

.task {
  color: #666;
}

.item-body {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
}

.time {
  font-family: monospace;
  color: #666;
}

.hours {
  font-weight: 600;
  color: #4CAF50;
  min-width: 60px;
  text-align: right;
}

.lunch-badge {
  background-color: #fff3e0;
  color: #e65100;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.item-actions {
  display: flex;
  gap: 0.5rem;
  margin-left: 1rem;
}

.btn-small {
  padding: 0.4rem 0.8rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background-color 0.3s;
}

.btn-small:hover {
  background-color: #f5f5f5;
}

.btn-delete {
  color: #d32f2f;
  border-color: #d32f2f;
}

.btn-delete:hover {
  background-color: #ffebee;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #999;
}

.alert {
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-success {
  background-color: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: inherit;
}
</style>
