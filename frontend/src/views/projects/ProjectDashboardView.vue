<template>
  <div class="project-dashboard-view">
    <div class="page-header">
      <div>
        <h1>{{ currentProject?.name || '專案儀表板' }}</h1>
        <p class="project-status">
          狀態：
          <span :class="['status-badge', currentProject?.status.toLowerCase()]">
            {{ currentProject?.status === 'ACTIVE' ? '進行中' : '已結束' }}
          </span>
        </p>
      </div>
      <button @click="refreshData" class="btn btn-secondary">重新整理</button>
    </div>

    <div v-if="projectStore.loading" class="loading">載入中...</div>

    <div v-else-if="currentProject">
      <div class="statistics">
        <div class="stat-card">
          <div class="stat-label">總時數</div>
          <div class="stat-value">{{ totalAllocatedHours }}</div>
          <div class="stat-unit">小時</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已使用時數</div>
          <div class="stat-value">{{ totalUsedHours }}</div>
          <div class="stat-unit">小時</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">剩餘時數</div>
          <div class="stat-value">{{ remainingHours }}</div>
          <div class="stat-unit">小時</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">任務總數</div>
          <div class="stat-value">{{ tasksByProject.length }}</div>
          <div class="stat-unit">個</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ completedTaskCount }}</div>
          <div class="stat-unit">個</div>
        </div>
      </div>

      <div class="dashboard-content">
        <div class="section">
          <h2>任務狀態分佈</h2>
          <div class="status-breakdown">
            <div class="breakdown-item">
              <span class="label">待辦</span>
              <span class="count">{{ getTaskCountByStatus('TODO') }}</span>
            </div>
            <div class="breakdown-item">
              <span class="label">進行中</span>
              <span class="count">{{ getTaskCountByStatus('IN_PROGRESS') }}</span>
            </div>
            <div class="breakdown-item">
              <span class="label">已完成</span>
              <span class="count">{{ getTaskCountByStatus('COMPLETED') }}</span>
            </div>
          </div>
        </div>

        <div class="section">
          <h2>任務列表</h2>
          <div v-if="tasksByProject.length === 0" class="empty-state">
            尚無任務
          </div>
          <div v-else class="task-groups">
            <div v-for="status in ['TODO', 'IN_PROGRESS', 'COMPLETED']" :key="status" class="task-group">
              <h3 :class="['group-title', status.toLowerCase()]">
                {{ formatStatus(status) }}
              </h3>
              <div class="task-items">
                <div
                  v-for="task in getTasksByStatus(status as any)"
                  :key="task.id"
                  class="task-item"
                >
                  <div class="task-info">
                    <strong>{{ task.name }}</strong>
                    <p class="task-details">
                      {{ task.assigneeName }} • {{ task.usedHours }} / {{ task.estimatedHours }} 小時
                    </p>
                  </div>
                  <div class="task-progress">
                    <div class="progress-bar">
                      <div class="progress" :style="{ width: calculateProgress(task) + '%' }"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="projectStore.error" class="error-message">
      {{ projectStore.error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useProjectStore } from '../../stores/project';
import { useTaskStore } from '../../stores/task';
import type { TaskStatus } from '../../types/task';

const route = useRoute();
const projectStore = useProjectStore();
const taskStore = useTaskStore();

const projectId = computed(() => {
  return parseInt(route.params.projectId as string, 10);
});

const currentProject = computed(() => {
  return projectStore.currentProject;
});

const tasksByProject = computed(() => {
  return taskStore.tasksByProject(projectId.value);
});

const totalAllocatedHours = computed(() => {
  return tasksByProject.value.reduce((sum, task) => sum + task.estimatedHours, 0);
});

const totalUsedHours = computed(() => {
  return tasksByProject.value.reduce((sum, task) => sum + task.usedHours, 0);
});

const remainingHours = computed(() => {
  return totalAllocatedHours.value - totalUsedHours.value;
});

const completedTaskCount = computed(() => {
  return tasksByProject.value.filter((task) => task.status === 'COMPLETED').length;
});

const formatStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    TODO: '待辦',
    IN_PROGRESS: '進行中',
    COMPLETED: '已完成',
  };
  return statusMap[status] || status;
};

const getTaskCountByStatus = (status: TaskStatus) => {
  return tasksByProject.value.filter((task) => task.status === status).length;
};

const getTasksByStatus = (status: TaskStatus) => {
  return tasksByProject.value.filter((task) => task.status === status);
};

const calculateProgress = (task: any) => {
  if (task.estimatedHours === 0) return 0;
  const progress = (task.usedHours / task.estimatedHours) * 100;
  return Math.min(progress, 100);
};

const refreshData = async () => {
  await fetchProjectData();
};

const fetchProjectData = async () => {
  try {
    await projectStore.getProjectById(projectId.value);
    await taskStore.fetchTasks({
      projectId: projectId.value,
      page: 0,
      size: 100,
    });
  } catch (err) {
    console.error('Failed to fetch project data:', err);
  }
};

onMounted(() => {
  fetchProjectData();
});
</script>

<style scoped>
.project-dashboard-view {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 1.75rem;
  color: #333;
  margin: 0 0 0.5rem 0;
}

.project-status {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.active {
  background-color: #c8e6c9;
  color: #2e7d32;
}

.status-badge.closed {
  background-color: #ffcdd2;
  color: #c62828;
}

.statistics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stat-label {
  color: #999;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 0.25rem;
}

.stat-unit {
  color: #ccc;
  font-size: 0.8rem;
}

.dashboard-content {
  display: grid;
  gap: 2rem;
}

.section {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section h2 {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  color: #333;
}

.status-breakdown {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 4px;
}

.breakdown-item .label {
  font-weight: 500;
  color: #333;
}

.breakdown-item .count {
  font-size: 1.5rem;
  font-weight: 600;
  color: #409eff;
}

.task-groups {
  display: grid;
  gap: 1.5rem;
}

.task-group {
  border-top: 1px solid #eee;
  padding-top: 1.5rem;
}

.task-group:first-child {
  border-top: none;
  padding-top: 0;
}

.group-title {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.group-title.todo {
  color: #c61;
}

.group-title.in_progress {
  color: #880;
}

.group-title.completed {
  color: #060;
}

.task-items {
  display: grid;
  gap: 1rem;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 4px;
  gap: 1rem;
}

.task-info {
  flex: 1;
}

.task-info strong {
  display: block;
  color: #333;
  margin-bottom: 0.25rem;
}

.task-details {
  margin: 0;
  font-size: 0.85rem;
  color: #999;
}

.task-progress {
  flex: 0 0 200px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: #409eff;
  transition: width 0.3s;
}

.loading,
.empty-state {
  padding: 2rem;
  text-align: center;
  background: white;
  border-radius: 8px;
  color: #666;
}

.error-message {
  padding: 1rem;
  background-color: #fee;
  color: #c33;
  border-radius: 4px;
  margin-top: 1rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.95rem;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background-color: #e8e8e8;
}

@media (max-width: 768px) {
  .project-dashboard-view {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
  }

  .statistics {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1rem;
  }

  .status-breakdown {
    grid-template-columns: 1fr;
  }

  .task-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .task-progress {
    width: 100%;
  }
}
</style>
