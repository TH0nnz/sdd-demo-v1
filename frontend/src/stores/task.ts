import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { taskAPI } from '../api/tasks';
import type { 
  TaskResponse, 
  TaskPageResponse, 
  CreateTaskRequest, 
  UpdateTaskRequest,
  Task
} from '../types/task';

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<TaskResponse[]>([]);
  const selectedTask = ref<TaskResponse | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const filters = ref({
    projectId: undefined as number | undefined,
    assigneeId: undefined as number | undefined,
    status: undefined as string | undefined,
  });
  const pagination = ref({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });

  /**
   * Fetch tasks with optional filtering
   */
  const fetchTasks = async (params?: {
    projectId?: number;
    assigneeId?: number;
    status?: string;
    page?: number;
    size?: number;
  }) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskAPI.listTasks(params);
      tasks.value = response.data.content;
      pagination.value = response.data.pageInfo;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch tasks';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Get task by ID
   */
  const fetchTaskById = async (taskId: number) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskAPI.getTask(taskId);
      selectedTask.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch task';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Create a new task
   */
  const createTask = async (data: CreateTaskRequest) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskAPI.createTask(data);
      tasks.value.push(response.data);
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to create task';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Update task information
   */
  const updateTask = async (taskId: number, data: UpdateTaskRequest) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskAPI.updateTask(taskId, data);
      const index = tasks.value.findIndex((t) => t.id === taskId);
      if (index > -1) {
        tasks.value[index] = response.data;
      }
      if (selectedTask.value?.id === taskId) {
        selectedTask.value = response.data;
      }
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to update task';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Delete a task
   */
  const deleteTask = async (taskId: number) => {
    loading.value = true;
    error.value = null;
    try {
      await taskAPI.deleteTask(taskId);
      tasks.value = tasks.value.filter((t) => t.id !== taskId);
      if (selectedTask.value?.id === taskId) {
        selectedTask.value = null;
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to delete task';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Complete a task
   */
  const completeTask = async (taskId: number) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskAPI.completeTask(taskId);
      const index = tasks.value.findIndex((t) => t.id === taskId);
      if (index > -1) {
        tasks.value[index] = response.data;
      }
      if (selectedTask.value?.id === taskId) {
        selectedTask.value = response.data;
      }
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to complete task';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Getter: filtered tasks
   */
  const filteredTasks = computed(() => {
    return tasks.value;
  });

  /**
   * Getter: tasks by project
   */
  const tasksByProject = (projectId: number) => {
    return tasks.value.filter((t) => t.projectId === projectId);
  };

  /**
   * Getter: completed tasks
   */
  const completedTasks = computed(() => {
    return tasks.value.filter((t) => t.status === 'COMPLETED');
  });

  const isLoading = computed(() => loading.value);
  const hasError = computed(() => error.value !== null);

  return {
    // State
    tasks,
    selectedTask,
    loading,
    error,
    filters,
    pagination,
    
    // Computed
    filteredTasks,
    completedTasks,
    isLoading,
    hasError,
    
    // Actions
    fetchTasks,
    fetchTaskById,
    createTask,
    updateTask,
    deleteTask,
    completeTask,
    tasksByProject,
  };
});
