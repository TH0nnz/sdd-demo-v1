<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>批准時數申請</h2>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="modal-body">
        <div class="request-info">
          <div class="info-group">
            <span class="label">申請人：</span>
            <span class="value">{{ request.requester.name }}</span>
          </div>
          <div class="info-group">
            <span class="label">申請時數：</span>
            <span class="value">{{ request.requestedHours }} 小時</span>
          </div>
          <div class="info-group">
            <span class="label">申請日期：</span>
            <span class="value">{{ formatDate(request.createdAt) }}</span>
          </div>
          <div class="info-group">
            <span class="label">狀態：</span>
            <span :class="['value', 'status', request.status.toLowerCase()]">
              {{ translateStatus(request.status) }}
            </span>
          </div>
        </div>

        <div class="approval-form">
          <div class="form-group">
            <label for="approved">決定 *</label>
            <div class="radio-group">
              <label class="radio-label">
                <input
                  v-model="formData.approved"
                  type="radio"
                  :value="true"
                  :disabled="loading"
                />
                批准
              </label>
              <label class="radio-label">
                <input
                  v-model="formData.approved"
                  type="radio"
                  :value="false"
                  :disabled="loading"
                />
                拒絕
              </label>
            </div>
          </div>

          <div class="form-group">
            <label for="notes">批准意見</label>
            <textarea
              v-model="formData.notes"
              class="form-input"
              rows="4"
              :disabled="loading"
              placeholder="請輸入批准或拒絕的原因（可選）"
            />
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button 
          type="button" 
          class="btn btn-secondary" 
          @click="$emit('close')" 
          :disabled="loading"
        >
          取消
        </button>
        <button 
          type="button" 
          :class="['btn', formData.approved ? 'btn-success' : 'btn-danger']" 
          @click="handleSubmit" 
          :disabled="loading"
        >
          {{ formData.approved ? '批准' : '拒絕' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { TimeRequestResponse, TimeRequestStatus } from '../../types/project';

interface Props {
  request: TimeRequestResponse;
  loading?: boolean;
}

interface Emits {
  (e: 'approve', approved: boolean, notes: string): void;
  (e: 'close'): void;
}

withDefaults(defineProps<Props>(), {
  loading: false,
});

const emit = defineEmits<Emits>();

const formData = ref({
  approved: true,
  notes: '',
});

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
    hour: '2-digit',
    minute: '2-digit',
  });
};

const handleSubmit = () => {
  emit('approve', formData.value.approved, formData.value.notes);
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  font-size: 1.25rem;
  color: #333;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 1.5rem;
  flex: 1;
}

.request-info {
  background: #f9f9f9;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1.5rem;
}

.info-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
}

.info-group .label {
  font-weight: 500;
  color: #666;
}

.info-group .value {
  color: #333;
}

.info-group .value.status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.info-group .value.status.pending {
  background-color: #fff9c4;
  color: #f57f17;
}

.approval-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #333;
  font-size: 0.95rem;
}

.radio-group {
  display: flex;
  gap: 1.5rem;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: normal;
}

.radio-label input {
  cursor: pointer;
}

.radio-label:has(input:disabled) {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
  resize: vertical;
}

.form-input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.form-input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.modal-footer {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #eee;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary {
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background-color: #e0e0e0;
}

.btn-secondary:disabled {
  background-color: #f0f0f0;
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-success {
  background-color: #67c23a;
  color: white;
}

.btn-success:hover {
  background-color: #55a826;
}

.btn-success:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-danger {
  background-color: #f56c6c;
  color: white;
}

.btn-danger:hover {
  background-color: #dd5545;
}

.btn-danger:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>
