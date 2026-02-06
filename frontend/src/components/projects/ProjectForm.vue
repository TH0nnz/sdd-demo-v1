<template>
  <form @submit.prevent="handleSubmit" class="project-form">
    <div class="form-group">
      <label for="name">專案名稱 *</label>
      <input
        id="name"
        v-model="formData.name"
        type="text"
        class="form-input"
        required
        minlength="2"
        maxlength="100"
        placeholder="請輸入專案名稱"
      />
    </div>

    <div class="form-group">
      <label for="description">描述 *</label>
      <textarea
        id="description"
        v-model="formData.description"
        class="form-input"
        required
        minlength="10"
        maxlength="500"
        rows="4"
        placeholder="請輸入專案描述"
      />
    </div>

    <div class="form-row">
      <div class="form-group">
        <label for="startDate">開始日期 *</label>
        <input
          id="startDate"
          v-model="formData.startDate"
          type="date"
          class="form-input"
          required
        />
      </div>

      <div class="form-group">
        <label for="endDate">結束日期 *</label>
        <input
          id="endDate"
          v-model="formData.endDate"
          type="date"
          class="form-input"
          required
        />
      </div>
    </div>

    <div class="form-group">
      <label for="budget">預算 (TWD) *</label>
      <input
        id="budget"
        v-model.number="formData.budget"
        type="number"
        class="form-input"
        required
        min="0"
        step="1000"
        placeholder="請輸入預算"
      />
    </div>

    <div class="form-actions">
      <button type="submit" class="btn btn-primary" :disabled="loading">
        {{ isEdit ? '更新' : '建立' }}
      </button>
      <button type="button" class="btn btn-secondary" @click="$emit('cancel')" :disabled="loading">
        取消
      </button>
    </div>

    <div v-if="error" class="error-message">{{ error }}</div>
  </form>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { ProjectResponse, CreateProjectRequest, UpdateProjectRequest } from '../../types/project';

interface Props {
  project?: ProjectResponse;
  loading?: boolean;
}

interface Emits {
  (e: 'submit', data: CreateProjectRequest | UpdateProjectRequest): void;
  (e: 'cancel'): void;
}

withDefaults(defineProps<Props>(), {
  loading: false,
});

const emit = defineEmits<Emits>();

const props = defineProps<Props>();

const formData = ref({
  name: '',
  description: '',
  startDate: '',
  endDate: '',
  budget: 0,
  version: 0,
});

const error = ref<string | null>(null);

const isEdit = computed(() => !!props.project);
const loading = computed(() => props.loading);

onMounted(() => {
  if (props.project) {
    formData.value.name = props.project.name;
    formData.value.description = props.project.description;
    formData.value.startDate = props.project.startDate.split('T')[0];
    formData.value.endDate = props.project.endDate.split('T')[0];
    formData.value.budget = props.project.budget;
    formData.value.version = props.project.version;
  }
});

const handleSubmit = () => {
  error.value = null;

  if (new Date(formData.value.startDate) >= new Date(formData.value.endDate)) {
    error.value = '結束日期必須晚於開始日期';
    return;
  }

  if (isEdit.value) {
    const updateData: UpdateProjectRequest = {
      name: formData.value.name,
      description: formData.value.description,
      startDate: formData.value.startDate,
      endDate: formData.value.endDate,
      budget: formData.value.budget,
      version: formData.value.version,
    };
    emit('submit', updateData);
  } else {
    const createData: CreateProjectRequest = {
      name: formData.value.name,
      description: formData.value.description,
      startDate: formData.value.startDate,
      endDate: formData.value.endDate,
      budget: formData.value.budget,
    };
    emit('submit', createData);
  }
};
</script>

<style scoped>
.project-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1.5rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  font-weight: 500;
  color: #333;
  font-size: 0.95rem;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
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

.form-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-primary:hover {
  background-color: #0a6cff;
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background-color: #e0e0e0;
}

.error-message {
  padding: 0.75rem;
  background-color: #fee;
  color: #c33;
  border-radius: 4px;
  font-size: 0.95rem;
}
</style>
