import { client } from './client';
import type { 
  TaskResponse, 
  TaskPageResponse, 
  CreateTaskRequest, 
  UpdateTaskRequest 
} from '../types/task';

/**
 * Task API endpoints for management layer
 */
export const taskAPI = {
  /**
   * Get all tasks with optional filtering and pagination
   */
  listTasks: (params?: {
    projectId?: number;
    assigneeId?: number;
    status?: string;
    page?: number;
    size?: number;
  }) => {
    return client.get<TaskPageResponse>('/api/tasks', { params });
  },

  /**
   * Get task by ID
   */
  getTask: (taskId: number) => {
    return client.get<TaskResponse>(`/api/tasks/${taskId}`);
  },

  /**
   * Create a new task
   */
  createTask: (data: CreateTaskRequest) => {
    return client.post<TaskResponse>('/api/tasks', data);
  },

  /**
   * Update task information
   */
  updateTask: (taskId: number, data: UpdateTaskRequest) => {
    return client.put<TaskResponse>(`/api/tasks/${taskId}`, data);
  },

  /**
   * Delete a task
   */
  deleteTask: (taskId: number) => {
    return client.delete(`/api/tasks/${taskId}`);
  },

  /**
   * Complete a task
   */
  completeTask: (taskId: number) => {
    return client.post<TaskResponse>(`/api/tasks/${taskId}/complete`);
  },
};
