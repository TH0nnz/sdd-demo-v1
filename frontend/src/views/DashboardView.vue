<template>
  <div class="dashboard-view">
    <div class="dashboard-header">
      <h1>儀表板</h1>
      <p>歡迎使用工時管理系統</p>
    </div>

    <div class="dashboard-grid">
      <!-- Stats Cards -->
      <div class="stats-section">
        <div class="stat-card">
          <div class="stat-icon timesheets">⏰</div>
          <div class="stat-content">
            <div class="stat-label">本週工時</div>
            <div class="stat-value">{{ weeklyHours }}</div>
            <div class="stat-unit">小時</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon projects">📋</div>
          <div class="stat-content">
            <div class="stat-label">進行中的專案</div>
            <div class="stat-value">{{ activeProjects }}</div>
            <div class="stat-unit">個</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon tasks">✓</div>
          <div class="stat-content">
            <div class="stat-label">待完成任務</div>
            <div class="stat-value">{{ pendingTasks }}</div>
            <div class="stat-unit">項</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon pending">⚠️</div>
          <div class="stat-content">
            <div class="stat-label">待批准申請</div>
            <div class="stat-value">{{ pendingRequests }}</div>
            <div class="stat-unit">件</div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions-section">
        <h2>快速操作</h2>
        <div class="action-buttons">
          <button @click="navigateTo('/timesheets/new')" class="action-btn">
            <span class="icon">📝</span>
            <span>填報工時</span>
          </button>
          <button @click="navigateTo('/projects/new')" class="action-btn" v-if="canCreateProject">
            <span class="icon">➕</span>
            <span>新增專案</span>
          </button>
          <button @click="navigateTo('/tasks/create')" class="action-btn" v-if="canCreateTask">
            <span class="icon">✏️</span>
            <span>建立任務</span>
          </button>
          <button @click="navigateTo('/reports')" class="action-btn">
            <span class="icon">📊</span>
            <span>查看報表</span>
          </button>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="recent-activity-section">
        <h2>最近活動</h2>
        <div v-if="recentActivities.length > 0" class="activity-list">
          <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
            <div class="activity-icon">{{ activity.icon }}</div>
            <div class="activity-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-time">{{ formatTime(activity.timestamp) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>暫無最近活動</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { UserRole } from '@/types/auth'

const router = useRouter()
const authStore = useAuthStore()

const weeklyHours = ref(0)
const activeProjects = ref(0)
const pendingTasks = ref(0)
const pendingRequests = ref(0)
const recentActivities = ref<any[]>([])

const canCreateProject = computed(() => authStore.user?.role === UserRole.EXECUTIVE)
const canCreateTask = computed(() => authStore.user?.role === UserRole.PM)

const navigateTo = (path: string) => {
  router.push(path)
}

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 60) {
    return `${minutes} 分鐘前`
  } else if (hours < 24) {
    return `${hours} 小時前`
  } else if (days < 7) {
    return `${days} 天前`
  } else {
    return date.toLocaleDateString('zh-TW')
  }
}

onMounted(() => {
  // Initialize dashboard data
  // In a real app, this would fetch from the API
  weeklyHours.value = 40
  activeProjects.value = 3
  pendingTasks.value = 5
  pendingRequests.value = 2
  
  recentActivities.value = [
    { id: '1', title: '工時填報已提交', icon: '✓', timestamp: new Date(Date.now() - 3600000).toISOString() },
    { id: '2', title: '專案已更新', icon: '📋', timestamp: new Date(Date.now() - 7200000).toISOString() },
    { id: '3', title: '任務已指派', icon: '👤', timestamp: new Date(Date.now() - 86400000).toISOString() },
  ]
})
</script>

<style scoped lang="scss">
.dashboard-view {
  padding: 2rem;
}

.dashboard-header {
  margin-bottom: 2rem;

  h1 {
    font-size: 2rem;
    color: #333;
    margin: 0 0 0.5rem 0;
  }

  p {
    color: #999;
    margin: 0;
  }
}

.dashboard-grid {
  display: grid;
  gap: 2rem;
}

// Stats Section
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }
}

.stat-icon {
  font-size: 2.5rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;

  &.timesheets {
    background: #e3f2fd;
  }

  &.projects {
    background: #f3e5f5;
  }

  &.tasks {
    background: #e8f5e9;
  }

  &.pending {
    background: #fff3e0;
  }
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #999;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: #333;
}

.stat-unit {
  font-size: 0.85rem;
  color: #bbb;
}

// Quick Actions
.quick-actions-section {
  h2 {
    margin-top: 0;
    margin-bottom: 1rem;
    color: #333;
  }
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.action-btn {
  background: white;
  border: 2px solid #ddd;
  border-radius: 8px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;

  .icon {
    font-size: 2rem;
  }

  span:not(.icon) {
    font-size: 0.95rem;
    font-weight: 500;
    color: #333;
  }

  &:hover {
    border-color: #1976d2;
    background: #f5f5f5;
    transform: translateY(-2px);
  }
}

// Recent Activity
.recent-activity-section {
  h2 {
    margin-top: 0;
    margin-bottom: 1rem;
    color: #333;
  }

  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 6px;
}

.activity-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-content {
  flex: 1;
}

.activity-title {
  color: #333;
  font-weight: 500;
}

.activity-time {
  color: #999;
  font-size: 0.9rem;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #999;
}

@media (max-width: 768px) {
  .dashboard-view {
    padding: 1rem;
  }

  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
