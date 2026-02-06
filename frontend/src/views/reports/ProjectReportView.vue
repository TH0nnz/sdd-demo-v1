<template>
  <div class="project-report-view">
    <div class="report-header">
      <h1 class="report-title">Project Report</h1>
      <p class="report-description">View aggregated work hours and project statistics</p>
    </div>

    <!-- Filter Panel (Simplified for projects) -->
    <div class="filter-panel">
      <div class="filter-group">
        <label for="start-date">Start Date</label>
        <input 
          id="start-date"
          v-model="filters.startDate"
          type="date"
        />
      </div>

      <div class="filter-group">
        <label for="end-date">End Date</label>
        <input 
          id="end-date"
          v-model="filters.endDate"
          type="date"
        />
      </div>

      <div class="filter-actions">
        <button 
          class="btn btn-primary"
          @click="handleFilterChange"
          :disabled="reportStore.isLoading"
        >
          Apply
        </button>
        <button 
          class="btn btn-secondary"
          @click="handleClearFilters"
        >
          Clear
        </button>
      </div>
    </div>

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
    <div v-if="!reportStore.isLoading && !reportStore.hasError && reportStore.projectSummary.length === 0" class="empty-state">
      <p>No project data found for the selected period.</p>
    </div>

    <!-- Project Summary Table -->
    <div v-if="reportStore.projectSummary.length > 0 && !reportStore.isLoading" class="table-container">
      <h2 class="section-title">Project Summary</h2>
      <table class="report-table">
        <thead>
          <tr>
            <th>Project Name</th>
            <th>Total Hours</th>
            <th>Entries</th>
            <th>Team Members</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="project in reportStore.projectSummary" 
            :key="project.projectId"
            class="report-row"
          >
            <td class="project-name">{{ project.projectName }}</td>
            <td class="hours-cell">{{ project.totalHours }}</td>
            <td class="entry-count">{{ project.entryCount }}</td>
            <td class="user-count">{{ project.assignedUserCount }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useReportStore } from '../../stores/report';
import { useDepartmentStore } from '../../stores/department';
import type { ReportFilterParams } from '../../types/report';

const reportStore = useReportStore();
const departmentStore = useDepartmentStore();

const filters = ref({
  startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
  endDate: new Date().toISOString().split('T')[0],
});

const handleFilterChange = async () => {
  const departmentId = departmentStore.selectedDepartment?.id;
  if (!departmentId) {
    console.error('Department ID is required');
    return;
  }

  try {
    await reportStore.fetchProjectSummary({
      departmentId,
      startDate: filters.value.startDate,
      endDate: filters.value.endDate,
    });
  } catch (err) {
    console.error('Failed to fetch project summary:', err);
  }
};

const handleClearFilters = async () => {
  filters.value.startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  filters.value.endDate = new Date().toISOString().split('T')[0];
  reportStore.clearFilters();
};

const retryFetch = async () => {
  await handleFilterChange();
};

onMounted(async () => {
  // Load initial project summary
  const departmentId = departmentStore.selectedDepartment?.id;
  if (departmentId) {
    await handleFilterChange();
  }
});
</script>

<style scoped>
.project-report-view {
  padding: 20px;
  max-width: 1200px;
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

.filter-panel {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.filter-group input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.filter-group input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.filter-actions {
  display: flex;
  gap: 10px;
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

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
  padding: 16px 16px 0 16px;
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

.project-name {
  font-weight: 500;
  color: #333;
}

.hours-cell {
  font-weight: 600;
  color: #28a745;
}

.entry-count,
.user-count {
  text-align: center;
  color: #666;
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
