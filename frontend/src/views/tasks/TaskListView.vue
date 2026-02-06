<template>
  <div class="task-list-view">
    <div class="page-header">
      <h1>任務管理</h1>
      <button class="btn btn-primary" @click="navigateToCreate">+ 建立任務</button>
    </div>

    <div class="filters">
      <input
        v-model="searchQuery"
        type="text"
        class="filter-input search-input"
        placeholder="搜尋任務名稱..."
        @input="handleSearch"
      />
      <select v-model="filters.projectId" @change="fetchTasks" class="filter-input">
        <option value="">所有專案</option>
        <option v-for="project in projects" :key="project.id" :value="project.id">
          {{ project.name }}
        </option>
      </select>
      <select v-model="filters.status" @change="fetchTasks" class="filter-input">
        <option value="">所有狀態</option>
        <option value="TODO">待辦</option>
        <option value="IN_PROGRESS">進行中</option>
        <option value="COMPLETED">已完成</option>
      </select>
    </div>

    <div v-if="taskStore.loading" class="loading">載入中...</div>

    <div v-else-if="taskStore.tasks.length === 0" class="empty-state">
      沒有任務資料
    </div>

    <div v-else>
      <table class="tasks-table">
        <thead>
          <tr>
            <th>任務名稱</th>
            <th>專案</th>
            <th>指派人員</th>
            <th>預估時數</th>
            <th>已用時數</th>
            <th>狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="task in taskStore.tasks" :key="task.id">
            <td><strong>{{ task.name }}</strong></td>
            <td>{{ task.projectName }}</td>
            <td>{{ task.assigneeName }}</td>
            <td>{{ task.estimatedHours }}</td>
            <td>{{ task.usedHours }}</td>
            <td>
              <span :class="['status-badge', task.status.toLowerCase()]">
                {{ formatStatus(task.status) }}
              </span>
            </td>
            <td class="actions">
              <button @click="navigateToEdit(task.id)" class="btn-link">編輯</button>
              <button
                v-if="task.status !== 'COMPLETED'"
                @click="completeTaskHandler(task.id)"
                class="btn-link"
              >
                完成
              </button>
              <button @click="deleteTaskHandler(task.id)" class="btn-link danger">刪除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button
          :disabled="!taskStore.pagination.hasPrevious"
          @click="previousPage"
          class="btn btn-secondary"
        >
          上一頁
        </button>
        <span class="page-info">
          第 {{ taskStore.pagination.currentPage + 1 }} / {{ taskStore.pagination.totalPages }} 頁
        </span>
        <button
          :disabled="!taskStore.pagination.hasNext"
          @click="nextPage"
          class="btn btn-secondary"
        >
          下一頁
        </button>
      </div>
    </div>

    <div v-if="taskStore.error" class="error-message">
      {{ taskStore.error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useTaskStore } from '../../stores/task';
import { useProjectStore } from '../../stores/project';
import type { TaskStatus } from '../../types/task';

const router = useRouter();
const taskStore = useTaskStore();
const projectStore = useProjectStore();

const searchQuery = ref('');
const filters = ref({
  projectId: undefined as number | undefined,
  status: undefined as string | undefined,
});

const projects = ref<any[]>([]);

const formatStatus = (status: TaskStatus) => {
  const statusMap: Record<TaskStatus, string> = {
    TODO: '待辦',
    IN_PROGRESS: '進行中',
    COMPLETED: '已完成',
  };
  return statusMap[status] || status;
};

const fetchTasks = async () => {
  await taskStore.fetchTasks({
    projectId: filters.value.projectId || undefined,
    status: filters.value.status || undefined,
    page: taskStore.pagination.currentPage,
    size: taskStore.pagination.pageSize,
  });
};

const fetchProjects = async () => {
  try {
    await projectStore.fetchProjects({ page: 0, size: 100 });
    projects.value = projectStore.projects;
  } catch (err) {
    console.error('Failed to fetch projects:', err);
  }
};

const handleSearch = () => {
  fetchTasks();
};

const navigateToCreate = () => {
  router.push('/tasks/create');
};

const navigateToEdit = (taskId: number) => {
  router.push(`/tasks/${taskId}/edit`);
};

const completeTaskHandler = async (taskId: number) => {
  try {
    await taskStore.completeTask(taskId);
    alert('任務已完成');
  } catch (err) {
    alert('完成任務失敗');
    console.error('Failed to complete task:', err);
  }
};

const deleteTaskHandler = async (taskId: number) => {
  if (confirm('確定要刪除此任務嗎？')) {
    try {
      await taskStore.deleteTask(taskId);
      alert('任務已刪除');
    } catch (err) {
      alert('刪除任務失敗');
      console.error('Failed to delete task:', err);
    }
  }
};

const nextPage = async () => {
  if (taskStore.pagination.hasNext) {
    taskStore.pagination.currentPage++;
    await fetchTasks();
  }
};

const previousPage = async () => {
  if (taskStore.pagination.hasPrevious) {
    taskStore.pagination.currentPage--;
    await fetchTasks();
  }
};

onMounted(() => {
  fetchProjects();
  fetchTasks();
});
</script>

<style scoped>
.task-list-view {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 1.75rem;
  color: #333;
  margin: 0;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filter-input {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  font-size: 0.95rem;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.tasks-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tasks-table thead {
  background-color: #f9f9f9;
  border-bottom: 1px solid #ddd;
}

.tasks-table th,
.tasks-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.tasks-table th {
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.tasks-table tbody tr:hover {
  background-color: #f9f9f9;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.todo {
  background-color: #fde;
  color: #c61;
}

.status-badge.in_progress {
  background-color: #ffd;
  color: #880;
}

.status-badge.completed {
  background-color: #cfc;
  color: #060;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.btn-link {
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
  font-size: 0.9rem;
}

.btn-link:hover {
  color: #0a6cff;
}

.btn-link.danger {
  color: #f56c6c;
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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.page-info {
  color: #666;
  font-size: 0.95rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.95rem;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-primary:hover {
  background-color: #0a6cff;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #e8e8e8;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }

  .filter-input {
    width: 100%;
  }

  .tasks-table {
    font-size: 0.85rem;
  }

  .tasks-table th,
  .tasks-table td {
    padding: 0.75rem;
  }
}
</style>
