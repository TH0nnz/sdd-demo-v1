<template>
  <div class="report-summary-card">
    <div class="summary-header">
      <h3 class="summary-title">Report Summary</h3>
    </div>

    <div class="summary-content">
      <div class="summary-item">
        <span class="summary-label">Total Hours</span>
        <span class="summary-value">{{ summary.totalHours }}</span>
      </div>

      <div class="summary-item">
        <span class="summary-label">Total Entries</span>
        <span class="summary-value">{{ summary.totalEntries }}</span>
      </div>

      <div class="summary-item">
        <span class="summary-label">Unique Users</span>
        <span class="summary-value">{{ summary.uniqueUsers }}</span>
      </div>

      <div class="summary-item">
        <span class="summary-label">Unique Projects</span>
        <span class="summary-value">{{ summary.uniqueProjects }}</span>
      </div>

      <div class="summary-item full-width">
        <span class="summary-label">Date Range</span>
        <span class="summary-value">
          {{ formatDate(summary.dateRange.start) }} to {{ formatDate(summary.dateRange.end) }}
        </span>
      </div>
    </div>

    <!-- Hours by Project -->
    <div v-if="Object.keys(hoursByProject).length > 0" class="breakdown-section">
      <h4 class="breakdown-title">Hours by Project</h4>
      <div class="breakdown-list">
        <div 
          v-for="(hours, project) in hoursByProject" 
          :key="project"
          class="breakdown-item"
        >
          <span class="breakdown-name">{{ project }}</span>
          <span class="breakdown-value">{{ hours.toFixed(2) }}h</span>
        </div>
      </div>
    </div>

    <!-- Hours by User -->
    <div v-if="Object.keys(hoursByUser).length > 0" class="breakdown-section">
      <h4 class="breakdown-title">Hours by User</h4>
      <div class="breakdown-list">
        <div 
          v-for="(hours, user) in hoursByUser" 
          :key="user"
          class="breakdown-item"
        >
          <span class="breakdown-name">{{ user }}</span>
          <span class="breakdown-value">{{ hours.toFixed(2) }}h</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ReportSummary } from '../types/report';

interface Props {
  summary: ReportSummary;
  hoursByProject?: Record<string, number>;
  hoursByUser?: Record<string, number>;
}

withDefaults(defineProps<Props>(), {
  hoursByProject: () => ({}),
  hoursByUser: () => ({}),
});

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric' 
  });
};
</script>

<style scoped>
.report-summary-card {
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.summary-header {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 12px;
  margin-bottom: 16px;
}

.summary-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.summary-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.summary-item.full-width {
  grid-column: 1 / -1;
}

.summary-label {
  font-size: 12px;
  font-weight: 500;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #007bff;
}

.breakdown-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.breakdown-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #333;
}

.breakdown-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f9f9f9;
  border-radius: 4px;
  font-size: 14px;
}

.breakdown-name {
  color: #333;
  font-weight: 500;
}

.breakdown-value {
  color: #28a745;
  font-weight: 600;
}
</style>
