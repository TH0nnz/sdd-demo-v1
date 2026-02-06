<template>
  <div class="task-card">
    <div class="card-header">
      <div class="task-title">
        <h3>{{ task.name }}</h3>
        <span :class="['status-badge', task.status.toLowerCase()]">
          {{ formatStatus(task.status) }}
        </span>
      </div>
      <div class="card-actions">
        <button @click="$emit('edit')" class="btn-icon" title="編輯">✎</button>
        <button @click="$emit('complete')" class="btn-icon" title="完成">✓</button>
        <button @click="$emit('delete')" class="btn-icon danger" title="刪除">✕</button>
      </div>
    </div>

    <div class="card-body">
      <p class="description">{{ task.description }}</p>
      <div class="meta-info">
        <div class="meta-item">
          <span class="label">專案：</span>
          <span class="value">{{ task.projectName }}</span>
        </div>
        <div class="meta-item">
          <span class="label">指派：</span>
          <span class="value">{{ task.assigneeName }}</span>
        </div>
      </div>

      <div class="hours-info">
        <div class="hours-display">
          <span class="used">{{ task.usedHours }}</span>
          <span class="separator">/</span>
          <span class="estimated">{{ task.estimatedHours }}</span>
          <span class="unit">小時</span>
        </div>
        <div class="progress-bar">
          <div class="progress" :style="{ width: calculateProgress() + '%' }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TaskResponse } from '../../types/task';

interface Props {
  task: TaskResponse;
}

interface Emits {
  (e: 'edit'): void;
  (e: 'complete'): void;
  (e: 'delete'): void;
}

withDefaults(defineProps<Props>(), {});
defineEmits<Emits>();

const formatStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    TODO: '待辦',
    IN_PROGRESS: '進行中',
    COMPLETED: '已完成',
  };
  return statusMap[status] || status;
};

const calculateProgress = () => {
  if (props.task.estimatedHours === 0) return 0;
  const progress = (props.task.usedHours / props.task.estimatedHours) * 100;
  return Math.min(progress, 100);
};

const props = withDefaults(defineProps<Props>(), {});
</script>

<style scoped>
.task-card {
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1.5rem;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.task-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #ddd;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  gap: 1rem;
}

.task-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.task-title h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
  word-break: break-word;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
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

.card-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  background: none;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  font-size: 1rem;
  color: #666;
  transition: all 0.2s;
}

.btn-icon:hover {
  border-color: #409eff;
  color: #409eff;
  background-color: #f5f5f5;
}

.btn-icon.danger {
  color: #f56c6c;
}

.btn-icon.danger:hover {
  border-color: #f56c6c;
  background-color: #fef;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.description {
  margin: 0 0 1rem 0;
  color: #666;
  font-size: 0.95rem;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.label {
  font-weight: 500;
  color: #999;
}

.value {
  color: #333;
}

.hours-info {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

.hours-display {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.used {
  font-weight: 600;
  color: #409eff;
  font-size: 1.2rem;
}

.separator {
  color: #ccc;
}

.estimated {
  color: #999;
}

.unit {
  color: #999;
  font-size: 0.85rem;
  margin-left: 0.25rem;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background-color: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: #409eff;
  transition: width 0.3s;
}

@media (max-width: 768px) {
  .task-card {
    padding: 1rem;
  }

  .task-title h3 {
    font-size: 1rem;
  }
}
</style>
