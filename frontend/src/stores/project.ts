import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { projectAPI } from '../api/projects';
import { timeRequestAPI } from '../api/time-requests';
import type { 
  ProjectResponse, 
  ProjectPageResponse, 
  CreateProjectRequest, 
  UpdateProjectRequest,
  TimeRequestResponse,
  TimeRequestPageResponse,
  ApproveTimeRequestRequest
} from '../types/project';

export const useProjectStore = defineStore('project', () => {
  const projects = ref<ProjectResponse[]>([]);
  const currentProject = ref<ProjectResponse | null>(null);
  const timeRequests = ref<TimeRequestResponse[]>([]);
  const currentTimeRequest = ref<TimeRequestResponse | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const projectPagination = ref({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });
  const timeRequestPagination = ref({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });

  /**
   * Fetch projects with optional filtering
   */
  const fetchProjects = async (params?: {
    status?: string;
    managerId?: number;
    page?: number;
    size?: number;
  }) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await projectAPI.getProjects(params);
      projects.value = response.data.content;
      projectPagination.value = response.data.pageInfo;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch projects';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Get project by ID
   */
  const getProjectById = async (projectId: number) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await projectAPI.getProjectById(projectId);
      currentProject.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch project';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Create a new project
   */
  const createProject = async (data: CreateProjectRequest) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await projectAPI.createProject(data);
      projects.value.push(response.data);
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to create project';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Update project information
   */
  const updateProject = async (projectId: number, data: UpdateProjectRequest) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await projectAPI.updateProject(projectId, data);
      const index = projects.value.findIndex((p) => p.id === projectId);
      if (index > -1) {
        projects.value[index] = response.data;
      }
      if (currentProject.value?.id === projectId) {
        currentProject.value = response.data;
      }
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to update project';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Close a project
   */
  const closeProject = async (projectId: number) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await projectAPI.closeProject(projectId);
      const index = projects.value.findIndex((p) => p.id === projectId);
      if (index > -1) {
        projects.value[index] = response.data;
      }
      if (currentProject.value?.id === projectId) {
        currentProject.value = response.data;
      }
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to close project';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Fetch pending time requests for approval
   */
  const fetchPendingTimeRequests = async (params?: {
    page?: number;
    size?: number;
  }) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await timeRequestAPI.getPendingRequests(params);
      timeRequests.value = response.data.content;
      timeRequestPagination.value = response.data.pageInfo;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch time requests';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Get time request by ID
   */
  const getTimeRequestById = async (requestId: number) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await timeRequestAPI.getTimeRequestById(requestId);
      currentTimeRequest.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch time request';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Approve a time request
   */
  const approveTimeRequest = async (requestId: number, data: ApproveTimeRequestRequest) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await timeRequestAPI.approveTimeRequest(requestId, data);
      const index = timeRequests.value.findIndex((tr) => tr.id === requestId);
      if (index > -1) {
        timeRequests.value[index] = response.data;
      }
      if (currentTimeRequest.value?.id === requestId) {
        currentTimeRequest.value = response.data;
      }
      return response.data;
    } catch (err: any) {
      error.value = err.message || 'Failed to approve time request';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const isLoading = computed(() => loading.value);
  const hasError = computed(() => error.value !== null);

  return {
    // State
    projects,
    currentProject,
    timeRequests,
    currentTimeRequest,
    loading,
    error,
    projectPagination,
    timeRequestPagination,
    
    // Computed
    isLoading,
    hasError,
    
    // Actions
    fetchProjects,
    getProjectById,
    createProject,
    updateProject,
    closeProject,
    fetchPendingTimeRequests,
    getTimeRequestById,
    approveTimeRequest,
  };
});
