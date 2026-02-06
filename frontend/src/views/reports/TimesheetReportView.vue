<template>
  <div class="timesheet-report-view">
    <div class="report-header">
      <h1 class="report-title">Timesheet Report</h1>
      <p class="report-description">View and analyze work hour entries by date, employee, and project</p>
    </div>

    <!-- Filter Panel -->
    <ReportFilterPanel 
      :filter-state="reportStore.filterState"
      :is-loading="reportStore.isLoading"
      :available-users="departmentUsers"
      :available-projects="departmentProjects"
      @filter-change="handleFilterChange"
      @export="handleExport"
      @clear-filters="handleClearFilters"
    />

    <!-- Summary Card -->
    <ReportSummaryCard 
      v-if="reportStore.timesheetReport.length > 0"
      :summary="reportStore.reportSummary"
      :hours-by-project="reportStore.hoursByProject"
      :hours-by-user="reportStore.hoursByUser"
    />

    <!-- Loading State -->
    <div v-if="reportStore.isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>Loading report data...</p>
    </div>

    <!-- Error State -->
    <div v-if="reportStore.hasError" class="error-container">
      <p class="error-message">{{ reportStore.error }}</p>
      <button class="btn btn-primary" @click="retryFetch">
        Retry
      </button>
    </div>

    <!-- Empty State -->
    <div v-if="!reportStore.isLoading && !reportStore.hasError && reportStore.timesheetReport.length === 0" class="empty-state">
      <p>No timesheet entries found for the selected criteria.</p>
    </div>

    <!-- Report Table -->
    <div v-if="reportStore.timesheetReport.length > 0 && !reportStore.isLoading" class="table-container">
      <table class="report-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Employee</th>
            <th>Project</th>
            <th>Task</th>
            <th>Hours</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="entry in paginatedEntries" 
            :key="entry.id"
            class="report-row"
          >
            <td>{{ formatDate(entry.date) }}</td>
            <td>{{ entry.userName }}</td>
            <td>{{ entry.projectName }}</td>
            <td>{{ entry.taskName }}</td>
            <td class="hours-cell">{{ entry.hours }}</td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="reportStore.pagination.totalPages > 1" class="pagination">
        <button 
          :disabled="!reportStore.pagination.hasPrevious"
          @click="previousPage"
          class="btn btn-secondary"
        >
          Previous
        </button>
        <span class="page-info">
          Page {{ reportStore.pagination.currentPage + 1 }} of {{ reportStore.pagination.totalPages }}
          ({{ reportStore.pagination.totalElements }} total entries)
        </span>
        <button 
          :disabled="!reportStore.pagination.hasNext"
          @click="nextPage"
          class="btn btn-secondary"
        >
          Next
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useReportStore } from '../../stores/report';
import { useUserStore } from '../../stores/user';
import { useProjectStore } from '../../stores/project';
import { useDepartmentStore } from '../../stores/department';
import ReportFilterPanel from '../../components/reports/ReportFilterPanel.vue';
import ReportSummaryCard from '../../components/reports/ReportSummaryCard.vue';
import type { ReportFilterParams } from '../../types/report';

const reportStore = useReportStore();
const userStore = useUserStore();
const projectStore = useProjectStore();
const departmentStore = useDepartmentStore();

const currentPage = ref(0);

const departmentUsers = computed(() => {
  return userStore.users.map(u => ({ id: u.id, name: u.name }));
});

const departmentProjects = computed(() => {
  return projectStore.projects.map(p => ({ id: p.id, name: p.name }));
});

const paginatedEntries = computed(() => {
  return reportStore.timesheetReport;
});

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric' 
  });
};

const handleFilterChange = async (filters: any) => {
  currentPage.value = 0;
  if (!reportStore.filterState.departmentId) {
    console.error('Department ID is required');
    return;
  }

  try {
    await reportStore.fetchTimesheetReport({
      departmentId: reportStore.filterState.departmentId,
      startDate: filters.startDate,
      endDate: filters.endDate,
      userId: filters.userId,
      projectId: filters.projectId,
      page: 0,
      size: 20,
    });
  } catch (err) {
    console.error('Failed to fetch report:', err);
  }
};

const handleExport = async (filters: any) => {
  if (!reportStore.filterState.departmentId) {
    console.error('Department ID is required');
    return;
  }

  try {
    await reportStore.exportTimesheetReportCsv({
      departmentId: reportStore.filterState.departmentId,
      startDate: filters.startDate,
      endDate: filters.endDate,
      userId: filters.userId,
      projectId: filters.projectId,
    });
  } catch (err) {
    console.error('Failed to export report:', err);
  }
};

const handleClearFilters = async () => {
  reportStore.clearFilters();
  currentPage.value = 0;
};

const nextPage = async () => {
  if (reportStore.pagination.hasNext) {
    currentPage.value++;
    await handleFilterChange({
      startDate: reportStore.filterState.startDate,
      endDate: reportStore.filterState.endDate,
      userId: reportStore.filterState.userId,
      projectId: reportStore.filterState.projectId,
    });
  }
};

const previousPage = async () => {
  if (reportStore.pagination.hasPrevious) {
    currentPage.value--;
    await handleFilterChange({
      startDate: reportStore.filterState.startDate,
      endDate: reportStore.filterState.endDate,
      userId: reportStore.filterState.userId,
      projectId: reportStore.filterState.projectId,
    });
  }
};

const retryFetch = async () => {
  await handleFilterChange({
    startDate: reportStore.filterState.startDate,
    endDate: reportStore.filterState.endDate,
    userId: reportStore.filterState.userId,
    projectId: reportStore.filterState.projectId,
  });
};

onMounted(async () => {
  // Load initial data
  if (!userStore.users.length) {
    await userStore.fetchUsers();
  }
  if (!projectStore.projects.length) {
    await projectStore.fetchProjects();
  }
});
</script>

<style scoped>
.timesheet-report-view {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.report-header {
  margin-bottom: 30px;
}

.report-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #333;
}

.report-description {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 16px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-message {
  color: #721c24;
  margin: 0;
  flex: 1;
}

.empty-state {
  background-color: #e7f3ff;
  border: 1px solid #b3d9ff;
  border-radius: 4px;
  padding: 40px;
  text-align: center;
  color: #004085;
}

.table-container {
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow-x: auto;
  margin-bottom: 20px;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.report-table thead {
  background-color: #f8f9fa;
  border-bottom: 2px solid #dee2e6;
}

.report-table th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
}

.report-table td {
  padding: 12px;
  border-bottom: 1px solid #dee2e6;
}

.report-row:hover {
  background-color: #f9f9f9;
}

.hours-cell {
  font-weight: 600;
  color: #28a745;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background-color: #f9f9f9;
  border-top: 1px solid #ddd;
  border-radius: 0 0 4px 4px;
}

.page-info {
  font-size: 14px;
  color: #666;
  min-width: 300px;
  text-align: center;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #545b62;
}
</style>
