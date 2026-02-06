import { client } from './client';
import type { 
  ProjectResponse, 
  ProjectPageResponse, 
  CreateProjectRequest, 
  UpdateProjectRequest 
} from '../types/project';

/**
 * Project API endpoints for management layer
 */
export const projectAPI = {
  /**
   * Get all active projects with optional filtering and pagination
   */
  getProjects: (params?: {
    status?: string;
    managerId?: number;
    page?: number;
    size?: number;
  }) => {
    return client.get<ProjectPageResponse>('/api/projects', { params });
  },

  /**
   * Get project by ID
   */
  getProjectById: (projectId: number) => {
    return client.get<ProjectResponse>(`/api/projects/${projectId}`);
  },

  /**
   * Create a new project
   */
  createProject: (data: CreateProjectRequest) => {
    return client.post<ProjectResponse>('/api/projects', data);
  },

  /**
   * Update project information
   */
  updateProject: (projectId: number, data: UpdateProjectRequest) => {
    return client.put<ProjectResponse>(`/api/projects/${projectId}`, data);
  },

  /**
   * Close a project
   */
  closeProject: (projectId: number) => {
    return client.post<ProjectResponse>(`/api/projects/${projectId}/close`);
  },
};
