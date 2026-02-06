<template>
  <div class="executive-task-list-view">
    <el-card class="page-header-card">
      <div class="page-header">
        <div>
          <h1 class="page-title">我的任務</h1>
          <p class="page-subtitle">查看已指派給您的任務和進度</p>
        </div>
        <el-button type="primary" @click="refreshTasks">
          <el-icon><Refresh /></el-icon>
          重新整理
        </el-button>
      </div>
    </el-card>

    <el-card class="content-card">
      <!-- Filters -->
      <div class="filters">
        <el-input
          v-model="searchQuery"
          placeholder="搜尋任務..."
          prefix-icon="Search"
          clearable
          @input="handleSearch"
          style="width: 300px"
        />
        <el-select
          v-model="statusFilter"
          placeholder="任務狀態"
          clearable
          @change="fetchTasks"
          style="width: 150px"
        >
          <el-option label="進行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已關閉" value="CLOSED" />
        </el-select>
      </div>

      <!-- Loading State -->
      <el-skeleton v-if="taskStore.loading" :rows="5" animated />

      <!-- Empty State -->
      <el-empty
        v-else-if="taskStore.tasks.length === 0"
        description="目前沒有指派給您的任務"
      />

      <!-- Task List -->
      <div v-else class="task-list">
        <el-table
          :data="taskStore.tasks"
          stripe
          :default-sort="{ prop: 'createdAt', order: 'descending' }"
        >
          <el-table-column prop="name" label="任務名稱" min-width="200">
            <template #default="{ row }">
              <div class="task-name">
                <strong>{{ row.name }}</strong>
                <p v-if="row.description" class="task-description">
                  {{ row.description }}
                </p>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="projectName" label="所屬專案" width="180" />

          <el-table-column label="時數" width="200">
            <template #default="{ row }">
              <div class="hours-info">
                <el-progress
                  :percentage="calculateProgress(row.usedHours, row.estimatedHours)"
                  :status="getProgressStatus(row.usedHours, row.estimatedHours)"
                >
                  <span class="progress-text">
                    {{ row.usedHours }} / {{ row.estimatedHours }}h
                  </span>
                </el-progress>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="狀態" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ formatStatus(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button-group>
                <el-button
                  size="small"
                  @click="navigateToTimesheet(row.id)"
                >
                  填報工時
                </el-button>
                <el-button
                  v-if="row.status === 'IN_PROGRESS'"
                  size="small"
                  type="success"
                  @click="handleCompleteTask(row.id)"
                >
                  完成
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.totalElements"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTaskStore } from '@/stores/task'

const router = useRouter()
const taskStore = useTaskStore()

const searchQuery = ref('')
const statusFilter = ref('')
const pagination = computed(() => taskStore.pagination)

// Fetch tasks on mount
onMounted(() => {
  fetchTasks()
})

// Fetch tasks
const fetchTasks = async () => {
  try {
    await taskStore.fetchTasks({
      status: statusFilter.value || undefined,
      page: pagination.value.currentPage,
      size: pagination.value.pageSize,
    })
  } catch (error: any) {
    ElMessage.error(error.message || '載入任務失敗')
  }
}

// Refresh tasks
const refreshTasks = () => {
  fetchTasks()
  ElMessage.success('已重新整理')
}

// Handle search
const handleSearch = () => {
  // Implement client-side search
  // In production, this should be server-side search
}

// Handle page change
const handlePageChange = (page: number) => {
  pagination.value.currentPage = page
  fetchTasks()
}

// Handle page size change
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  fetchTasks()
}

// Navigate to timesheet form
const navigateToTimesheet = (taskId: number) => {
  router.push({ name: 'TimesheetForm', query: { taskId } })
}

// Complete task
const handleCompleteTask = async (taskId: number) => {
  try {
    await ElMessageBox.confirm(
      '確定要將此任務標記為完成嗎？',
      '確認',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await taskStore.completeTask(taskId)
    ElMessage.success('任務已完成')
    await fetchTasks()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失敗')
    }
  }
}

// Calculate progress percentage
const calculateProgress = (used: number, estimated: number): number => {
  if (estimated === 0) return 0
  return Math.min(Math.round((used / estimated) * 100), 100)
}

// Get progress status
const getProgressStatus = (used: number, estimated: number): string => {
  const percentage = (used / estimated) * 100
  if (percentage >= 90) return 'exception'
  if (percentage >= 75) return 'warning'
  return 'success'
}

// Get status type for tag
const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CLOSED: 'info',
    PENDING_REASSIGNMENT: 'warning',
  }
  return typeMap[status] || 'info'
}

// Format status text
const formatStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    IN_PROGRESS: '進行中',
    COMPLETED: '已完成',
    CLOSED: '已關閉',
    PENDING_REASSIGNMENT: '待重新指派',
  }
  return statusMap[status] || status
}
</script>

<style scoped lang="scss">
.executive-task-list-view {
  padding: 20px;
}

.page-header-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
}

.page-subtitle {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.content-card {
  .filters {
    display: flex;
    gap: 16px;
    margin-bottom: 20px;
  }

  .task-list {
    .task-name {
      strong {
        display: block;
        margin-bottom: 4px;
        color: var(--el-text-color-primary);
      }

      .task-description {
        margin: 0;
        font-size: 13px;
        color: var(--el-text-color-secondary);
        line-height: 1.4;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .hours-info {
      .progress-text {
        font-size: 12px;
        color: var(--el-text-color-regular);
      }
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
