import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import apiClient from '@/api/client';

/**
 * Authentication store using Pinia.
 * 
 * Manages:
 * - User authentication state
 * - JWT token storage
 * - Login/logout operations
 * - Role-based access control
 */

export interface User {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  departmentId?: number;
  departmentName?: string;
  active: boolean;
}

export enum UserRole {
  EXECUTIVE = 'EXECUTIVE',
  PM = 'PM',
  MANAGER = 'MANAGER',
  EMPLOYEE = 'EMPLOYEE',
  HR = 'HR',
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null);
  const token = ref<string | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Getters (computed)
  const isAuthenticated = computed(() => !!token.value && !!user.value);
  const userRole = computed(() => user.value?.role);
  const userName = computed(() => user.value?.name);
  const userEmail = computed(() => user.value?.email);

  // Role-based permissions
  const isExecutive = computed(() => userRole.value === UserRole.EXECUTIVE);
  const isPM = computed(() => userRole.value === UserRole.PM);
  const isManager = computed(() => userRole.value === UserRole.MANAGER);
  const isEmployee = computed(() => userRole.value === UserRole.EMPLOYEE);
  const isHR = computed(() => userRole.value === UserRole.HR);

  const canManageProjects = computed(() => 
    userRole.value === UserRole.EXECUTIVE || userRole.value === UserRole.PM
  );

  const canManageTasks = computed(() => 
    userRole.value === UserRole.PM || userRole.value === UserRole.MANAGER
  );

  const canLogTimesheets = computed(() => 
    userRole.value === UserRole.EMPLOYEE
  );

  const canViewReports = computed(() => 
    userRole.value === UserRole.EXECUTIVE || 
    userRole.value === UserRole.MANAGER || 
    userRole.value === UserRole.PM
  );

  const canManageUsers = computed(() => 
    userRole.value === UserRole.HR
  );

  // Actions
  async function login(credentials: LoginRequest): Promise<void> {
    loading.value = true;
    error.value = null;

    try {
      const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
      const { token: accessToken, user: userData } = response.data;

      // Store token and user
      token.value = accessToken;
      user.value = userData;

      // Persist to localStorage
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (err: any) {
      error.value = err.response?.data?.message || '登入失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function logout(): void {
    // Clear state
    user.value = null;
    token.value = null;
    error.value = null;

    // Clear localStorage
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');

    // Redirect to login
    window.location.href = '/login';
  }

  function initializeAuth(): void {
    // Restore from localStorage on app start
    const storedToken = localStorage.getItem('accessToken');
    const storedUser = localStorage.getItem('user');

    if (storedToken && storedUser) {
      try {
        token.value = storedToken;
        user.value = JSON.parse(storedUser);
      } catch (err) {
        console.error('Failed to parse stored user data', err);
        logout();
      }
    }
  }

  async function refreshUser(): Promise<void> {
    if (!isAuthenticated.value) return;

    try {
      const response = await apiClient.get<User>('/auth/me');
      user.value = response.data;
      localStorage.setItem('user', JSON.stringify(response.data));
    } catch (err) {
      console.error('Failed to refresh user data', err);
      logout();
    }
  }

  // Initialize on store creation
  initializeAuth();

  return {
    // State
    user,
    token,
    loading,
    error,

    // Getters
    isAuthenticated,
    userRole,
    userName,
    userEmail,
    isExecutive,
    isPM,
    isManager,
    isEmployee,
    isHR,
    canManageProjects,
    canManageTasks,
    canLogTimesheets,
    canViewReports,
    canManageUsers,

    // Actions
    login,
    logout,
    refreshUser,
    initializeAuth,
  };
});
