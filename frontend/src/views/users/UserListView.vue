<template>
  <div class="user-list-view">
    <div class="page-header">
      <h1>用戶管理</h1>
      <button class="btn btn-primary" @click="navigateToCreate">+ 新增用戶</button>
    </div>

    <div class="filters">
      <select v-model="filters.role" @change="fetchUsers" class="filter-input">
        <option value="">所有角色</option>
        <option value="EXECUTIVE">管理層</option>
        <option value="PM">PM</option>
        <option value="MANAGER">部門主管</option>
        <option value="EMPLOYEE">執行人員</option>
        <option value="HR">HR</option>
      </select>

      <select v-model="filters.active" @change="fetchUsers" class="filter-input">
        <option value="">所有狀態</option>
        <option value="true">啟用</option>
        <option value="false">停用</option>
      </select>
    </div>

    <div v-if="userStore.loading" class="loading">載入中...</div>

    <table v-else class="users-table">
      <thead>
        <tr>
          <th>姓名</th>
          <th>電子郵件</th>
          <th>角色</th>
          <th>部門</th>
          <th>狀態</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in userStore.users" :key="user.id">
          <td>{{ user.name }}</td>
          <td>{{ user.email }}</td>
          <td>{{ translateRole(user.role) }}</td>
          <td>{{ user.department?.name || '-' }}</td>
          <td>
            <span :class="['status-badge', user.active ? 'active' : 'inactive']">
              {{ user.active ? '啟用' : '停用' }}
            </span>
          </td>
          <td class="actions">
            <button @click="navigateToEdit(user.id)" class="btn-link">編輯</button>
            <button
              @click="toggleUserStatus(user.id, user.active)"
              :class="['btn-link', user.active ? 'danger' : 'success']"
            >
              {{ user.active ? '停用' : '啟用' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="userStore.users.length === 0 && !userStore.loading" class="empty-state">
      沒有用戶資料
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user';
import type { UserRole } from '../../types/user';

const router = useRouter();
const userStore = useUserStore();

const filters = ref({
  role: '',
  active: '',
});

const roleNames: Record<UserRole, string> = {
  EXECUTIVE: '管理層',
  PM: 'PM',
  MANAGER: '部門主管',
  EMPLOYEE: '執行人員',
  HR: 'HR',
};

const translateRole = (role: UserRole) => roleNames[role] || role;

const fetchUsers = async () => {
  await userStore.fetchUsers({
    role: filters.value.role || undefined,
    active: filters.value.active ? filters.value.active === 'true' : undefined,
    page: 0,
    size: 20,
  });
};

const navigateToCreate = () => {
  router.push('/users/create');
};

const navigateToEdit = (userId: number) => {
  router.push(`/users/${userId}/edit`);
};

const toggleUserStatus = async (userId: number, isActive: boolean) => {
  try {
    if (isActive) {
      await userStore.deactivateUser(userId);
    } else {
      await userStore.activateUser(userId);
    }
  } catch (err) {
    console.error('Failed to toggle user status:', err);
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-list-view {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 1.75rem;
  color: #333;
  margin: 0;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.filter-input {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.users-table thead {
  background-color: #f9f9f9;
  border-bottom: 1px solid #ddd;
}

.users-table th,
.users-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.users-table th {
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.users-table tbody tr:hover {
  background-color: #f9f9f9;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.active {
  background-color: #c8e6c9;
  color: #2e7d32;
}

.status-badge.inactive {
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

.btn-link.danger {
  color: #f56c6c;
}

.btn-link.success {
  color: #67c23a;
}

.loading,
.empty-state {
  padding: 2rem;
  text-align: center;
  background: white;
  border-radius: 8px;
  color: #666;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.95rem;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-primary:hover {
  background-color: #0a6cff;
}
</style>
