<template>
  <div class="notification-dropdown">
    <el-dropdown
      trigger="click"
      @visible-change="handleDropdownVisibleChange"
      placement="bottom-end"
    >
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-button :icon="Bell" circle />
      </el-badge>
      
      <template #dropdown>
        <el-dropdown-menu class="notification-menu">
          <div class="notification-header">
            <span class="notification-title">通知</span>
            <el-button
              v-if="unreadCount > 0"
              link
              size="small"
              @click="handleMarkAllAsRead"
            >
              全部標記為已讀
            </el-button>
          </div>
          
          <el-scrollbar max-height="400px">
            <div v-if="loading" class="notification-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>載入中...</span>
            </div>
            
            <div v-else-if="notifications.length === 0" class="notification-empty">
              <el-empty description="暫無通知" :image-size="60" />
            </div>
            
            <div v-else class="notification-list">
              <div
                v-for="notification in notifications"
                :key="notification.id"
                class="notification-item"
                :class="{ 'is-unread': !notification.read }"
                @click="handleNotificationClick(notification)"
              >
                <div class="notification-content">
                  <div class="notification-type">
                    <el-tag
                      :type="getNotificationTypeColor(notification.type)"
                      size="small"
                      effect="plain"
                    >
                      {{ getNotificationTypeText(notification.type) }}
                    </el-tag>
                  </div>
                  <div class="notification-message">
                    {{ notification.message }}
                  </div>
                  <div class="notification-time">
                    {{ formatTime(notification.createdAt) }}
                  </div>
                </div>
                <div v-if="!notification.read" class="unread-indicator"></div>
              </div>
            </div>
          </el-scrollbar>
          
          <div class="notification-footer">
            <el-button link @click="handleViewAll">
              查看全部通知
            </el-button>
          </div>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNotifications, markAsRead, markAllAsRead } from '@/api/notifications'
import type { Notification } from '@/types/notification'

const router = useRouter()
const notifications = ref<Notification[]>([])
const unreadCount = ref(0)
const loading = ref(false)
let pollingInterval: number | null = null

// Fetch notifications
const fetchNotifications = async () => {
  try {
    const response = await getNotifications({ unreadOnly: false, size: 10 })
    notifications.value = response.content
    unreadCount.value = response.unreadCount
  } catch (error: any) {
    console.error('Failed to fetch notifications:', error)
  }
}

// Start polling every 5 seconds (T069)
const startPolling = () => {
  if (pollingInterval) return
  pollingInterval = window.setInterval(fetchNotifications, 5000)
}

// Stop polling
const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
}

// Handle dropdown visibility change
const handleDropdownVisibleChange = (visible: boolean) => {
  if (visible) {
    fetchNotifications()
  }
}

// Handle notification click
const handleNotificationClick = async (notification: Notification) => {
  if (!notification.read) {
    try {
      await markAsRead(notification.id)
      notification.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('Failed to mark notification as read:', error)
    }
  }
  
  // Navigate based on notification type
  if (notification.relatedEntityType === 'TASK' && notification.relatedEntityId) {
    router.push(`/tasks/${notification.relatedEntityId}`)
  } else if (notification.relatedEntityType === 'TIMESHEET' && notification.relatedEntityId) {
    router.push(`/timesheets`)
  } else if (notification.relatedEntityType === 'PROJECT' && notification.relatedEntityId) {
    router.push(`/projects/${notification.relatedEntityId}`)
  }
}

// Mark all as read
const handleMarkAllAsRead = async () => {
  loading.value = true
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
    ElMessage.success('已標記全部為已讀')
  } catch (error: any) {
    ElMessage.error(error.message || '標記失敗')
  } finally {
    loading.value = false
  }
}

// View all notifications
const handleViewAll = () => {
  router.push('/notifications')
}

// Get notification type color
const getNotificationTypeColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    INFO: 'info',
    WARNING: 'warning',
    ERROR: 'danger',
    SUCCESS: 'success',
    TASK_ASSIGNED: 'primary',
    HOURS_LOW: 'warning',
    HOURS_REQUEST_APPROVED: 'success',
    HOURS_REQUEST_REJECTED: 'danger',
  }
  return colorMap[type] || 'info'
}

// Get notification type text
const getNotificationTypeText = (type: string): string => {
  const textMap: Record<string, string> = {
    INFO: '資訊',
    WARNING: '警告',
    ERROR: '錯誤',
    SUCCESS: '成功',
    TASK_ASSIGNED: '任務指派',
    HOURS_LOW: '時數不足',
    HOURS_REQUEST_APPROVED: '時數核准',
    HOURS_REQUEST_REJECTED: '時數駁回',
  }
  return textMap[type] || '通知'
}

// Format time
const formatTime = (time: string): string => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (minutes < 1) return '剛剛'
  if (minutes < 60) return `${minutes} 分鐘前`
  if (hours < 24) return `${hours} 小時前`
  if (days < 7) return `${days} 天前`
  
  return date.toLocaleDateString('zh-TW', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchNotifications()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="scss">
.notification-dropdown {
  display: inline-block;
}

.notification-badge {
  :deep(.el-badge__content) {
    background-color: #f56c6c;
  }
}

.notification-menu {
  width: 360px;
  max-width: 90vw;
  padding: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  .notification-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.notification-loading,
.notification-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--el-text-color-secondary);
  gap: 8px;
}

.notification-list {
  .notification-item {
    position: relative;
    display: flex;
    align-items: center;
    padding: 16px 20px;
    cursor: pointer;
    transition: background-color 0.2s;
    border-bottom: 1px solid var(--el-border-color-lighter);
    
    &:hover {
      background-color: var(--el-fill-color-light);
    }
    
    &.is-unread {
      background-color: var(--el-color-primary-light-9);
    }
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .notification-content {
    flex: 1;
    min-width: 0;
  }
  
  .notification-type {
    margin-bottom: 8px;
  }
  
  .notification-message {
    font-size: 14px;
    color: var(--el-text-color-primary);
    margin-bottom: 6px;
    line-height: 1.5;
    word-wrap: break-word;
  }
  
  .notification-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
  
  .unread-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: var(--el-color-primary);
    flex-shrink: 0;
    margin-left: 12px;
  }
}

.notification-footer {
  padding: 12px 20px;
  text-align: center;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
