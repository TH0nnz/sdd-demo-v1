<template>
  <div class="task-form">
    <form @submit.prevent="submitForm">
      <div class="form-group">
        <label for="name">任務名稱 *</label>
        <input
          id="name"
          v-model="form.name"
          type="text"
          class="form-input"
          placeholder="輸入任務名稱"
          required
        />
        <span v-if="errors.name" class="error-text">{{ errors.name }}</span>
      </div>

      <div class="form-group">
        <label for="description">描述</label>
        <textarea
          id="description"
          v-model="form.description"
          class="form-input"
          rows="4"
          placeholder="輸入任務描述"
        ></textarea>
      </div>

      <div class="form-row">
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
          <label for="estimatedHours">預估時數 *</label>
          <input
            id="estimatedHours"
            v-model.number="form.estimatedHours"
            type="number"
            class="form-input"
            placeholder="輸入預估時數"
            min="0"
            step="0.5"
            required
          />
          <span v-if="errors.estimatedHours" class="error-text">{{ errors.estimatedHours }}</span>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="assigneeId">指派人員 *</label>
          <select
            id="assigneeId"
            v-model="form.assigneeId"
            class="form-input"
            required
          >
            <option value="">請選擇人員</option>
            <option v-for="user in users" :key="user.id" :value="user.id">
              {{ user.name }}
            </option>
          </select>
          <span v-if="errors.assigneeId" class="error-text">{{ errors.assigneeId }}</span>
        </div>

        <div class="form-group">
          <label for="status">狀態</label>
          <select id="status" v-model="form.status" class="form-input">
            <option value="TODO">待辦</option>
            <option value="IN_PROGRESS">進行中</option>
            <option value="COMPLETED">已完成</option>
          </select>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
          {{ isSubmitting ? '提交中...' : '提交' }}
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
import type { CreateTaskRequest, UpdateTaskRequest } from '../../types/task';
import type { ProjectResponse } from '../../types/project';
import type { User } from '../../types/auth';

interface Props {
  initialData?: CreateTaskRequest & { status?: string };
  projects: ProjectResponse[];
  users: User[];
  isSubmitting?: boolean;
  submitError?: string | null;
}

interface Emits {
  (e: 'submit', data: CreateTaskRequest | UpdateTaskRequest): void;
  (e: 'cancel'): void;
}

const props = withDefaults(defineProps<Props>(), {
  isSubmitting: false,
  submitError: null,
});

const emit = defineEmits<Emits>();

const form = ref({
  name: '',
  description: '',
  estimatedHours: 0,
  projectId: 0,
  assigneeId: 0,
  status: 'TODO' as const,
});

const errors = ref({
  name: '',
  description: '',
  estimatedHours: '',
  projectId: '',
  assigneeId: '',
  status: '',
});

const formError = ref('');

watch(
  () => props.initialData,
  (newData) => {
    if (newData) {
      form.value = {
        name: newData.name || '',
        description: newData.description || '',
        estimatedHours: newData.estimatedHours || 0,
        projectId: newData.projectId || 0,
        assigneeId: newData.assigneeId || 0,
        status: newData.status || 'TODO',
      };
    }
  },
  { immediate: true }
);

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
    name: '',
    description: '',
    estimatedHours: '',
    projectId: '',
    assigneeId: '',
    status: '',
  };
  let isValid = true;

  if (!form.value.name.trim()) {
    errors.value.name = '任務名稱為必填';
    isValid = false;
  }

  if (!form.value.projectId) {
    errors.value.projectId = '專案為必填';
    isValid = false;
  }

  if (!form.value.assigneeId) {
    errors.value.assigneeId = '指派人員為必填';
    isValid = false;
  }

  if (form.value.estimatedHours <= 0) {
    errors.value.estimatedHours = '預估時數必須大於0';
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
.task-form {
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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
  .form-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
