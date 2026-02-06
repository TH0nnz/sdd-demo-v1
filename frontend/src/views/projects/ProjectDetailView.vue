<template>
  <div class="project-detail-view">
    <div class="page-header">
      <button @click="goBack" class="btn-back">← 返回</button>
      <h1>專案詳情</h1>
    </div>

    <div v-if="projectStore.loading" class="loading">載入中...</div>

    <div v-else-if="projectStore.currentProject" class="project-details">
      <div class="detail-card">
        <h2>{{ projectStore.currentProject.name }}</h2>
        
        <div class="detail-group">
          <span class="label">狀態：</span>
          <span :class="['status-badge', projectStore.currentProject.status.toLowerCase()]">
            {{ projectStore.currentProject.status === 'ACTIVE' ? '進行中' : '已結束' }}
          </span>
        </div>

        <div class="detail-group">
          <span class="label">描述：</span>
          <span>{{ projectStore.currentProject.description }}</span>
        </div>

        <div class="detail-group">
          <span class="label">經理：</span>
          <span>{{ projectStore.currentProject.manager.name }}</span>
        </div>

        <div class="detail-group">
          <span class="label">開始日期：</span>
          <span>{{ formatDate(projectStore.currentProject.startDate) }}</span>
        </div>

        <div class="detail-group">
          <span class="label">結束日期：</span>
          <span>{{ formatDate(projectStore.currentProject.endDate) }}</span>
        </div>

        <div class="detail-group">
          <span class="label">預算：</span>
          <span>{{ formatCurrency(projectStore.currentProject.budget) }}</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      無法載入專案信息
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useProjectStore } from '../../stores/project';

const router = useRouter();
const route = useRoute();
const projectStore = useProjectStore();

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-TW');
};

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('zh-TW', {
    style: 'currency',
    currency: 'TWD',
  }).format(amount);
};

const goBack = () => {
  router.push('/projects');
};

onMounted(async () => {
  const projectId = route.params.id;
  if (projectId) {
    try {
      await projectStore.getProjectById(Number(projectId));
    } catch (err) {
      console.error('Failed to load project:', err);
      router.push('/projects');
    }
  }
});
</script>

<style scoped>
.project-detail-view {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.btn-back {
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  font-size: 1rem;
}

.btn-back:hover {
  color: #0a6cff;
}

.page-header h1 {
  font-size: 1.75rem;
  color: #333;
  margin: 0;
}

.project-details {
  margin-bottom: 2rem;
}

.detail-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.detail-card h2 {
  font-size: 1.5rem;
  color: #333;
  margin: 0 0 1.5rem 0;
}

.detail-group {
  display: flex;
  justify-content: space-between;
  padding: 1rem 0;
  border-bottom: 1px solid #eee;
}

.detail-group:last-child {
  border-bottom: none;
}

.detail-group .label {
  font-weight: 500;
  color: #666;
  min-width: 120px;
}

.detail-group span:not(.label) {
  color: #333;
  flex: 1;
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

.loading,
.empty-state {
  padding: 2rem;
  text-align: center;
  background: white;
  border-radius: 8px;
  color: #666;
}
</style>
