/**
 * Task management types and interfaces
 */

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED';

export interface Task {
  id: number;
  name: string;
  description: string;
  estimatedHours: number;
  usedHours: number;
  status: TaskStatus;
  projectId: number;
  projectName: string;
  assigneeId: number;
  assigneeName: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  version: number;
}

export interface TaskResponse {
  id: number;
  name: string;
  description: string;
  estimatedHours: number;
  usedHours: number;
  status: TaskStatus;
  projectId: number;
  projectName: string;
  assigneeId: number;
  assigneeName: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  version: number;
}

export interface TaskPageResponse {
  content: TaskResponse[];
  pageInfo: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export interface CreateTaskRequest {
  name: string;
  description: string;
  estimatedHours: number;
  projectId: number;
  assigneeId: number;
  status?: TaskStatus;
}

export interface UpdateTaskRequest {
  name?: string;
  description?: string;
  estimatedHours?: number;
  usedHours?: number;
  status?: TaskStatus;
  assigneeId?: number;
  version?: number;
}
