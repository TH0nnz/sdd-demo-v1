<template>
  <form @submit.prevent="handleSubmit" class="user-form">
    <div class="form-group">
      <label for="name">姓名 *</label>
      <input
        id="name"
        v-model="formData.name"
        type="text"
        class="form-input"
        required
        minlength="2"
        maxlength="100"
        placeholder="請輸入姓名"
      />
    </div>

    <div class="form-group">
      <label for="email">電子郵件 *</label>
      <input
        id="email"
        v-model="formData.email"
        type="email"
        class="form-input"
        required
        :disabled="isEdit"
        placeholder="請輸入電子郵件"
      />
    </div>

    <div class="form-group">
      <label for="role">角色 *</label>
      <select id="role" v-model="formData.role" class="form-input" required>
        <option value="">請選擇角色</option>
        <option value="EXECUTIVE">管理層</option>
        <option value="PM">PM</option>
        <option value="MANAGER">部門主管</option>
        <option value="EMPLOYEE">執行人員</option>
        <option value="HR">HR</option>
      </select>
    </div>

    <div class="form-group">
      <label for="departmentId">部門</label>
      <select id="departmentId" v-model="formData.departmentId" class="form-input">
        <option value="">選擇部門（可選）</option>
        <option v-for="dept in departments" :key="dept.id" :value="dept.id">
          {{ dept.name }}
        </option>
      </select>
    </div>

    <div v-if="!isEdit" class="form-group">
      <label for="password">初始密碼（可選）</label>
      <input
        id="password"
        v-model="formData.initialPassword"
        type="password"
        class="form-input"
        minlength="8"
        placeholder="不提供則系統自動生成"
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
import { useUserStore } from '../../stores/user';
import type { UserResponse, CreateUserRequest, UpdateUserRequest } from '../../types/user';
import type { DepartmentResponse } from '../../types/department';

interface Props {
  user?: UserResponse;
  departments?: DepartmentResponse[];
  loading?: boolean;
}

interface Emits {
  (e: 'submit', data: CreateUserRequest | UpdateUserRequest): void;
  (e: 'cancel'): void;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  departments: () => [],
});

const emit = defineEmits<Emits>();

const formData = ref({
  name: '',
  email: '',
  role: '',
  departmentId: undefined as number | undefined,
  initialPassword: '',
  version: 0,
});

const error = ref<string | null>(null);

const isEdit = computed(() => !!props.user);
const loading = computed(() => props.loading);

onMounted(() => {
  if (props.user) {
    formData.value.name = props.user.name;
    formData.value.email = props.user.email;
    formData.value.role = props.user.role;
    formData.value.departmentId = props.user.department?.id;
    formData.value.version = props.user.version;
  }
});

const handleSubmit = () => {
  error.value = null;
  
  if (isEdit.value) {
    const updateData: UpdateUserRequest = {
      name: formData.value.name,
      role: formData.value.role as any,
      departmentId: formData.value.departmentId,
      version: formData.value.version,
    };
    emit('submit', updateData);
  } else {
    const createData: CreateUserRequest = {
      name: formData.value.name,
      email: formData.value.email,
      role: formData.value.role as any,
      departmentId: formData.value.departmentId,
      initialPassword: formData.value.initialPassword || undefined,
    };
    emit('submit', createData);
  }
};
</script>

<style scoped>
.user-form {
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
