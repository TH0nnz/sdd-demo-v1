<template>
  <div class="user-form-view">
    <div class="form-container">
      <h1>{{ isEdit ? '編輯用戶' : '新增用戶' }}</h1>
      <UserForm
        :user="currentUser"
        :departments="departments"
        :loading="userStore.loading"
        @submit="handleSubmit"
        @cancel="goBack"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '../stores/user';
import UserForm from '../components/users/UserForm.vue';
import type { DepartmentResponse } from '../types/department';
import type { UserResponse, CreateUserRequest, UpdateUserRequest } from '../types/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const currentUser = ref<UserResponse | null>(null);
const departments = ref<DepartmentResponse[]>([]);

const isEdit = computed(() => !!route.params.userId);

const handleSubmit = async (data: CreateUserRequest | UpdateUserRequest) => {
  try {
    if (isEdit.value && route.params.userId) {
      await userStore.updateUser(Number(route.params.userId), data as UpdateUserRequest);
    } else {
      await userStore.createUser(data as CreateUserRequest);
    }
    router.push('/users');
  } catch (err) {
    console.error('Failed to submit form:', err);
  }
};

const goBack = () => {
  router.push('/users');
};

onMounted(async () => {
  // Load departments (simplified - would load from API)
  departments.value = [
    { id: 1, name: '研發部' },
    { id: 2, name: '市場部' },
    { id: 3, name: '人力資源部' },
  ];

  if (isEdit.value && route.params.userId) {
    await userStore.getUserById(Number(route.params.userId));
    currentUser.value = userStore.currentUser;
  }
});
</script>

<style scoped>
.user-form-view {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.form-container {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 600px;
}

.form-container h1 {
  margin-top: 0;
  margin-bottom: 2rem;
  font-size: 1.5rem;
  color: #333;
}
</style>
