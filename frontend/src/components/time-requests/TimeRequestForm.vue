<template>
  <div class="time-request-form">
    <form @submit.prevent="submitForm">
      <div class="form-group">
        <label for="projectId">專案 *</label>
        <select
          id="projectId"
          v-model="form.projectId"
          class="form-input"
          required
        >
          <option value="">請選擇專案</option>
          <option v-for="project in projects" :key="project.id" :value="project.id">
            {{ project.name }}
          </option>
        </select>
        <span v-if="errors.projectId" class="error-text">{{ errors.projectId }}</span>
      </div>

      <div class="form-group">
        <label for="reason">申請理由 *</label>
        <textarea
          id="reason"
          v-model="form.reason"
          class="form-input"
          rows="4"
          placeholder="輸入申請時數的理由"
          required
        ></textarea>
        <span v-if="errors.reason" class="error-text">{{ errors.reason }}</span>
      </div>

      <div class="form-group">
        <label for="requestedHours">申請時數 *</label>
        <input
          id="requestedHours"
          v-model.number="form.requestedHours"
          type="number"
          class="form-input"
          placeholder="輸入申請時數"
          min="0.5"
          step="0.5"
          required
        />
        <span v-if="errors.requestedHours" class="error-text">{{ errors.requestedHours }}</span>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
          {{ isSubmitting ? '提交中...' : '提交申請' }}
        </button>
        <button type="button" class="btn btn-secondary" @click="$emit('cancel')">
          取消
        </button>
      </div>

      <div v-if="formError" class="error-message">
        {{ formError }}
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import type { ProjectResponse } from '../../types/project';

interface TimeRequestFormData {
  projectId: number;
  reason: string;
  requestedHours: number;
}

interface Props {
  projects: ProjectResponse[];
  isSubmitting?: boolean;
  submitError?: string | null;
}

interface Emits {
  (e: 'submit', data: TimeRequestFormData): void;
  (e: 'cancel'): void;
}

withDefaults(defineProps<Props>(), {
  isSubmitting: false,
  submitError: null,
});

const emit = defineEmits<Emits>();

const form = ref({
  projectId: 0,
  reason: '',
  requestedHours: 0,
});

const errors = ref({
  projectId: '',
  reason: '',
  requestedHours: '',
});

const formError = ref('');

const props = withDefaults(defineProps<Props>(), {
  isSubmitting: false,
  submitError: null,
});

watch(
  () => props.submitError,
  (newError) => {
    if (newError) {
      formError.value = newError;
    }
  }
);

const validateForm = () => {
  errors.value = {
    projectId: '',
    reason: '',
    requestedHours: '',
  };
  let isValid = true;

  if (!form.value.projectId) {
    errors.value.projectId = '專案為必填';
    isValid = false;
  }

  if (!form.value.reason.trim()) {
    errors.value.reason = '申請理由為必填';
    isValid = false;
  }

  if (form.value.requestedHours <= 0) {
    errors.value.requestedHours = '申請時數必須大於0';
    isValid = false;
  }

  return isValid;
};

const submitForm = () => {
  formError.value = '';
  if (validateForm()) {
    emit('submit', form.value);
  }
};
</script>

<style scoped>
.time-request-form {
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

textarea.form-input {
  resize: vertical;
}

.error-text {
  display: block;
  color: #f56c6c;
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

.error-message {
  padding: 1rem;
  background-color: #fee;
  color: #c33;
  border-radius: 4px;
  margin-top: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0a6cff;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
}

.btn-secondary:hover {
  background-color: #e8e8e8;
}

@media (max-width: 768px) {
  .time-request-form {
    padding: 1rem;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
