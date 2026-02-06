<template>
  <div class="timesheet-list-view">
    <div class="page-header">
      <h1>工時記錄</h1>
      <button @click="goToForm" class="btn btn-primary">+ 新增工時</button>
    </div>

    <!-- Filters -->
    <div class="filters">
      <div class="filter-group">
        <label for="start-date">開始日期</label>
        <input v-model="filters.startDate" type="date" id="start-date" class="form-control" />
      </div>
      <div class="filter-group">
        <label for="end-date">結束日期</label>
        <input v-model="filters.endDate" type="date" id="end-date" class="form-control" />
      </div>
      <button @click="applyFilters" class="btn btn-secondary">查詢</button>
      <button @click="resetFilters" class="btn btn-secondary">重設</button>
    </div>

    <!-- Summary Stats -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-label">本月總計</span>
        <span class="stat-value">{{ totalHours.toFixed(1) }}</span>
        <span class="stat-unit">小時</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">記錄數量</span>
        <span class="stat-value">{{ timesheetStore.timesheets.length }}</span>
        <span class="stat-unit">筆</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">午休扣除</span>
        <span class="stat-value">{{ lunchDeductedCount }}</span>
        <span class="stat-unit">筆</span>
      </div>
    </div>

    <!-- Timesheets Table -->
    <div class="table-container">
      <div v-if="loading" class="loading">加載中...</div>
      <div v-else-if="displayedTimesheets.length === 0" class="empty-state">
        <p>暫無工時記錄</p>
      </div>
      <table v-else class="timesheets-table">
        <thead>
          <tr>
            <th>日期</th>
            <th>任務</th>
            <th>時間</th>
            <th>工時（小時）</th>
            <th>狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ts in displayedTimesheets" :key="ts.id" class="timesheet-row">
            <td class="date">{{ formatDate(ts.workDate) }}</td>
            <td class="task">
              <div class="task-info">
                <div class="task-name">{{ ts.taskName }}</div>
                <div class="project-name">{{ ts.projectName }}</div>
              </div>
            </td>
            <td class="time">{{ ts.startTime }} ~ {{ ts.endTime }}</td>
            <td class="hours">{{ ts.calculatedHours.toFixed(1) }}</td>
            <td class="status">
              <span v-if="ts.lunchDeducted" class="badge badge-warning">午休扣除</span>
              <span v-else class="badge badge-info">正常</span>
            </td>
            <td class="actions">
              <button @click="editTimesheet(ts.id)" class="btn-small btn-edit">編輯</button>
              <button @click="deleteTimesheet(ts.id)" class="btn-small btn-delete">刪除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="pagination.totalPages > 1" class="pagination">
      <button
        v-if="pagination.hasPrevious"
        @click="previousPage"
        class="btn btn-small"
      >
        上一頁
      </button>
      <span class="page-info">
        第 {{ pagination.currentPage }} / {{ pagination.totalPages }} 頁
      </span>
      <button
        v-if="pagination.hasNext"
        @click="nextPage"
        class="btn btn-small"
      >
        下一頁
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useTimesheetStore } from '../../stores/timesheet';

const router = useRouter();
const timesheetStore = useTimesheetStore();

const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);

const filters = ref({
  startDate: getDefaultStartDate(),
  endDate: new Date().toISOString().split('T')[0],
});

const pagination = computed(() => timesheetStore.pagination);
const displayedTimesheets = computed(() => timesheetStore.timesheets);

const totalHours = computed(() => {
  return displayedTimesheets.value.reduce((sum, ts) => sum + ts.calculatedHours, 0);
});

const lunchDeductedCount = computed(() => {
  return displayedTimesheets.value.filter((ts) => ts.lunchDeducted).length;
});

function getDefaultStartDate(): string {
  const date = new Date();
  date.setMonth(date.getMonth() - 1);
  return date.toISOString().split('T')[0];
}

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr + 'T00:00:00');
  const options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'short',
  };
  return date.toLocaleDateString('zh-TW', options);
};

const applyFilters = async () => {
  loading.value = true;
  try {
    currentPage.value = 1;
    await timesheetStore.fetchTimesheets({
      startDate: filters.value.startDate,
      endDate: filters.value.endDate,
      page: 0,
      size: pageSize.value,
    });
  } catch (err) {
    console.error('Failed to fetch timesheets:', err);
  } finally {
    loading.value = false;
  }
};

const resetFilters = async () => {
  filters.value = {
    startDate: getDefaultStartDate(),
    endDate: new Date().toISOString().split('T')[0],
  };
  await applyFilters();
};

const previousPage = async () => {
  if (pagination.value.hasPrevious) {
    currentPage.value--;
    await timesheetStore.fetchTimesheets({
      startDate: filters.value.startDate,
      endDate: filters.value.endDate,
      page: currentPage.value - 1,
      size: pageSize.value,
    });
  }
};

const nextPage = async () => {
  if (pagination.value.hasNext) {
    currentPage.value++;
    await timesheetStore.fetchTimesheets({
      startDate: filters.value.startDate,
      endDate: filters.value.endDate,
      page: currentPage.value - 1,
      size: pageSize.value,
    });
  }
};

const goToForm = () => {
  router.push('/timesheets/form');
};

const editTimesheet = (id: number) => {
  router.push({
    name: 'TimesheetForm',
    query: { id },
  });
};

const deleteTimesheet = async (id: number) => {
  if (confirm('確認刪除此工時記錄？')) {
    try {
      await timesheetStore.deleteTimesheet(id);
    } catch (err) {
      alert('刪除失敗');
    }
  }
};

onMounted(async () => {
  await applyFilters();
});
</script>

<style scoped>
.timesheet-list-view {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #333;
}

.btn {
  padding: 0.5rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
}

.btn-primary:hover {
  background-color: #45a049;
}

.btn-secondary {
  background-color: #2196F3;
  color: white;
  margin-left: 0.5rem;
}

.btn-secondary:hover {
  background-color: #0b7dda;
}

.filters {
  background: white;
  padding: 1.5rem;
  border-radius: 6px;
  margin-bottom: 2rem;
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-group label {
  margin-bottom: 0.4rem;
  font-weight: 500;
  color: #666;
}

.form-control {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.95rem;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-label {
  display: block;
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  color: #4CAF50;
}

.stat-unit {
  display: block;
  color: #999;
  font-size: 0.85rem;
}

.table-container {
  background: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.loading {
  padding: 2rem;
  text-align: center;
  color: #999;
}

.empty-state {
  padding: 2rem;
  text-align: center;
  color: #999;
}

.timesheets-table {
  width: 100%;
  border-collapse: collapse;
}

.timesheets-table thead {
  background-color: #f5f5f5;
  border-bottom: 2px solid #ddd;
}

.timesheets-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #333;
}

.timesheet-row {
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.timesheet-row:hover {
  background-color: #f9f9f9;
}

.timesheet-row td {
  padding: 1rem;
}

.date {
  font-weight: 500;
  color: #333;
}

.task-info {
  display: flex;
  flex-direction: column;
}

.task-name {
  font-weight: 500;
  color: #333;
}

.project-name {
  font-size: 0.85rem;
  color: #999;
}

.time {
  font-family: monospace;
  color: #666;
}

.hours {
  font-weight: 600;
  color: #4CAF50;
}

.badge {
  display: inline-block;
  padding: 0.4rem 0.8rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.badge-info {
  background-color: #e3f2fd;
  color: #1565c0;
}

.badge-warning {
  background-color: #fff3e0;
  color: #e65100;
}

.actions {
  display: flex;
  gap: 0.5rem;
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

.btn-edit {
  color: #2196F3;
  border-color: #2196F3;
}

.btn-edit:hover {
  background-color: #e3f2fd;
}

.btn-delete {
  color: #f44336;
  border-color: #f44336;
}

.btn-delete:hover {
  background-color: #ffebee;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.page-info {
  color: #666;
  font-size: 0.9rem;
}
</style>
