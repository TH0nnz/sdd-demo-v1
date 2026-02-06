<template>
  <div class="project-list-view">
    <div class="page-header">
      <h1>專案管理</h1>
      <button class="btn btn-primary" @click="navigateToCreate">+ 新增專案</button>
    </div>

    <div class="filters">
      <select v-model="filters.status" @change="fetchProjects" class="filter-input">
        <option value="">所有狀態</option>
        <option value="ACTIVE">進行中</option>
        <option value="CLOSED">已結束</option>
      </select>
    </div>

    <div v-if="projectStore.loading" class="loading">載入中...</div>

    <table v-else class="projects-table">
      <thead>
        <tr>
          <th>專案名稱</th>
          <th>描述</th>
          <th>經理</th>
          <th>開始日期</th>
          <th>結束日期</th>
          <th>預算</th>
          <th>狀態</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="project in projectStore.projects" :key="project.id">
          <td><strong>{{ project.name }}</strong></td>
          <td>{{ project.description }}</td>
          <td>{{ project.manager.name }}</td>
          <td>{{ formatDate(project.startDate) }}</td>
          <td>{{ formatDate(project.endDate) }}</td>
          <td>{{ formatCurrency(project.budget) }}</td>
          <td>
            <span :class="['status-badge', project.status.toLowerCase()]">
              {{ translateStatus(project.status) }}
            </span>
          </td>
          <td class="actions">
            <button @click="navigateToDetail(project.id)" class="btn-link">詳情</button>
            <button 
              v-if="project.status === 'ACTIVE'"
              @click="navigateToEdit(project.id)" 
              class="btn-link"
            >
              編輯
            </button>
            <button 
              v-if="project.status === 'ACTIVE'"
              @click="closeProjectHandler(project.id)" 
              class="btn-link danger"
            >
              關閉
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="projectStore.projects.length === 0 && !projectStore.loading" class="empty-state">
      沒有專案資料
    </div>

    <div v-if="projectStore.error" class="error-message">
      {{ projectStore.error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useProjectStore } from '../../stores/project';
import type { ProjectStatus } from '../../types/project';

const router = useRouter();
const projectStore = useProjectStore();

const filters = ref({
  status: '',
});

const translateStatus = (status: ProjectStatus) => {
  const statusMap: Record<ProjectStatus, string> = {
    ACTIVE: '進行中',
    CLOSED: '已結束',
  };
  return statusMap[status] || status;
};

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-TW');
};

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('zh-TW', {
    style: 'currency',
    currency: 'TWD',
  }).format(amount);
};

const fetchProjects = async () => {
  await projectStore.fetchProjects({
    status: filters.value.status || undefined,
    page: 0,
    size: 20,
  });
};

const navigateToCreate = () => {
  router.push('/projects/new');
};

const navigateToDetail = (projectId: number) => {
  router.push(`/projects/${projectId}`);
};

const navigateToEdit = (projectId: number) => {
  router.push(`/projects/${projectId}/edit`);
};

const closeProjectHandler = async (projectId: number) => {
  if (confirm('確定要關閉此專案嗎？')) {
    try {
      await projectStore.closeProject(projectId);
      alert('專案已成功關閉');
    } catch (err) {
      alert('關閉專案失敗');
      console.error('Failed to close project:', err);
    }
  }
};

onMounted(() => {
  fetchProjects();
});
</script>

<style scoped>
.project-list-view {
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
}

.filter-input {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.projects-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.projects-table thead {
  background-color: #f9f9f9;
  border-bottom: 1px solid #ddd;
}

.projects-table th,
.projects-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.projects-table th {
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.projects-table tbody tr:hover {
  background-color: #f9f9f9;
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
</style>
