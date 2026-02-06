<template>
  <div class="time-request-list-view">
    <div class="page-header">
      <h1>時數申請批准</h1>
    </div>

    <div v-if="projectStore.loading" class="loading">載入中...</div>

    <table v-else class="requests-table">
      <thead>
        <tr>
          <th>申請人</th>
          <th>申請的時數</th>
          <th>狀態</th>
          <th>申請日期</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="request in projectStore.timeRequests" :key="request.id">
          <td>{{ request.requester.name }}</td>
          <td>{{ request.requestedHours }} 小時</td>
          <td>
            <span :class="['status-badge', request.status.toLowerCase()]">
              {{ translateStatus(request.status) }}
            </span>
          </td>
          <td>{{ formatDate(request.createdAt) }}</td>
          <td class="actions">
            <button 
              v-if="request.status === 'PENDING'"
              @click="showApprovalModal(request)" 
              class="btn-link"
            >
              處理
            </button>
            <button 
              v-else
              @click="viewDetails(request.id)" 
              class="btn-link"
            >
              詳情
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="projectStore.timeRequests.length === 0 && !projectStore.loading" class="empty-state">
      沒有待批准的時數申請
    </div>

    <div v-if="projectStore.error" class="error-message">
      {{ projectStore.error }}
    </div>

    <TimeRequestApprovalModal
      v-if="selectedRequest"
      :request="selectedRequest"
      :loading="projectStore.loading"
      @approve="handleApprove"
      @close="selectedRequest = null"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useProjectStore } from '../../stores/project';
import TimeRequestApprovalModal from '../../components/time-requests/TimeRequestApprovalModal.vue';
import type { TimeRequestResponse, TimeRequestStatus } from '../../types/project';

const projectStore = useProjectStore();
const selectedRequest = ref<TimeRequestResponse | null>(null);

const translateStatus = (status: TimeRequestStatus) => {
  const statusMap: Record<TimeRequestStatus, string> = {
    PENDING: '待批准',
    APPROVED: '已批准',
    REJECTED: '已拒絕',
  };
  return statusMap[status] || status;
};

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const showApprovalModal = (request: TimeRequestResponse) => {
  selectedRequest.value = request;
};

const viewDetails = (requestId: number) => {
  console.log('View details for request:', requestId);
};

const handleApprove = async (approved: boolean, notes: string) => {
  if (!selectedRequest.value) return;
  
  try {
    await projectStore.approveTimeRequest(selectedRequest.value.id, {
      approved,
      approvalNotes: notes,
      version: selectedRequest.value.version,
    });
    alert(approved ? '已批准申請' : '已拒絕申請');
    selectedRequest.value = null;
    await projectStore.fetchPendingTimeRequests({ page: 0, size: 20 });
  } catch (err) {
    console.error('Failed to approve time request:', err);
    alert('處理申請失敗');
  }
};

onMounted(() => {
  projectStore.fetchPendingTimeRequests({ page: 0, size: 20 });
});
</script>

<style scoped>
.time-request-list-view {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 1.75rem;
  color: #333;
  margin: 0;
}

.requests-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.requests-table thead {
  background-color: #f9f9f9;
  border-bottom: 1px solid #ddd;
}

.requests-table th,
.requests-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.requests-table th {
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.requests-table tbody tr:hover {
  background-color: #f9f9f9;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.pending {
  background-color: #fff9c4;
  color: #f57f17;
}

.status-badge.approved {
  background-color: #c8e6c9;
  color: #2e7d32;
}

.status-badge.rejected {
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
</style>
