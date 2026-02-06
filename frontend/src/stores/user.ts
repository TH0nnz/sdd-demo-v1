import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { userAPI } from '../api/users';
import type { UserResponse, UserPageResponse, CreateUserRequest, UpdateUserRequest } from '../types/user';

export const useUserStore = defineStore('user', () => {
  const users = ref<UserResponse[]>([]);
  const currentUser = ref<UserResponse | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const pagination = ref({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });

  /**
   * Fetch users with optional filtering
   */
  const fetchUsers = async (params?: {
    role?: string;
    departmentId?: number;
    active?: boolean;
    page?: number;
    size?: number;
  }) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await userAPI.getUsers(params);
      users.value = response.data.content;
      pagination.value = response.data.pageInfo;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch users';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Get user by ID
   */
  const getUserById = async (userId: number) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await userAPI.getUserById(userId);
      currentUser.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch user';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Create a new user
   */
  const createUser = async (data: CreateUserRequest) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await userAPI.createUser(data);
      users.value.push(response.data);
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to create user';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Update user information
   */
  const updateUser = async (userId: number, data: UpdateUserRequest) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await userAPI.updateUser(userId, data);
      const index = users.value.findIndex((u) => u.id === userId);
      if (index > -1) {
        users.value[index] = response.data;
      }
      if (currentUser.value?.id === userId) {
        currentUser.value = response.data;
      }
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to update user';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Deactivate user
   */
  const deactivateUser = async (userId: number) => {
    loading.value = true;
    error.value = null;
    try {
      await userAPI.deactivateUser(userId);
      const user = users.value.find((u) => u.id === userId);
      if (user) {
        user.active = false;
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to deactivate user';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Activate user
   */
  const activateUser = async (userId: number) => {
    loading.value = true;
    error.value = null;
    try {
      await userAPI.activateUser(userId);
      const user = users.value.find((u) => u.id === userId);
      if (user) {
        user.active = true;
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to activate user';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const isLoading = computed(() => loading.value);
  const hasError = computed(() => error.value !== null);

  return {
    users,
    currentUser,
    loading,
    error,
    pagination,
    isLoading,
    hasError,
    fetchUsers,
    getUserById,
    createUser,
    updateUser,
    deactivateUser,
    activateUser,
  };
});
