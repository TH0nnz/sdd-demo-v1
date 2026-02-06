<template>
  <div class="report-list-view">
    <div class="report-header">
      <h1 class="report-title">Reports</h1>
      <p class="report-description">Access department work hour reports and statistics</p>
    </div>

    <div class="report-cards">
      <div 
        class="report-card"
        @click="navigateTo('/reports/timesheets')"
      >
        <div class="card-icon">📊</div>
        <h2 class="card-title">Timesheet Report</h2>
        <p class="card-description">
          View and analyze detailed timesheet entries by date, employee, and project
        </p>
        <button class="card-button">View Report</button>
      </div>

      <div 
        class="report-card"
        @click="navigateTo('/reports/projects')"
      >
        <div class="card-icon">📈</div>
        <h2 class="card-title">Project Report</h2>
        <p class="card-description">
          Analyze work hours and team allocation across projects
        </p>
        <button class="card-button">View Report</button>
      </div>

      <div 
        class="report-card"
        @click="navigateTo('/reports/users')"
      >
        <div class="card-icon">👥</div>
        <h2 class="card-title">User Report</h2>
        <p class="card-description">
          Review individual employee work hours and productivity metrics
        </p>
        <button class="card-button">View Report</button>
      </div>
    </div>

    <!-- Quick Stats -->
    <div class="quick-stats">
      <h2 class="stats-title">Quick Stats (This Month)</h2>
      <div class="stats-grid">
        <div class="stat-card">
          <span class="stat-label">Total Hours</span>
          <span class="stat-value">{{ totalHoursThisMonth }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Active Projects</span>
          <span class="stat-value">{{ activeProjectCount }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Team Members</span>
          <span class="stat-value">{{ teamMemberCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useReportStore } from '../../stores/report';
import { useProjectStore } from '../../stores/project';
import { useUserStore } from '../../stores/user';
import { useDepartmentStore } from '../../stores/department';

const router = useRouter();
const reportStore = useReportStore();
const projectStore = useProjectStore();
const userStore = useUserStore();
const departmentStore = useDepartmentStore();

const activeProjectCount = computed(() => {
  return projectStore.projects.filter(p => p.status === 'ACTIVE').length;
});

const teamMemberCount = computed(() => {
  return userStore.users.length;
});

const totalHoursThisMonth = computed(() => {
  const now = new Date();
  const currentMonth = now.getMonth();
  const currentYear = now.getFullYear();

  return reportStore.timesheetReport
    .filter(entry => {
      const date = new Date(entry.date);
      return date.getMonth() === currentMonth && date.getFullYear() === currentYear;
    })
    .reduce((sum, entry) => sum + parseFloat(entry.hours), 0)
    .toFixed(2);
});

const navigateTo = (path: string) => {
  router.push(path);
};

onMounted(async () => {
  // Load initial data if not already loaded
  if (!projectStore.projects.length) {
    await projectStore.fetchProjects();
  }
  if (!userStore.users.length) {
    await userStore.fetchUsers();
  }
});
</script>

<style scoped>
.report-list-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.report-header {
  margin-bottom: 40px;
}

.report-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #333;
}

.report-description {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.report-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.report-card {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.report-card:hover {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.15);
  transform: translateY(-2px);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #333;
}

.card-description {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
  flex: 1;
  line-height: 1.5;
}

.card-button {
  padding: 10px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
  align-self: flex-start;
}

.card-button:hover {
  background-color: #0056b3;
}

.quick-stats {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 24px;
}

.stats-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background-color: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #007bff;
}
</style>
