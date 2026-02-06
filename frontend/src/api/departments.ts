import { client } from './client';
import type { DepartmentResponse, DepartmentDetailResponse } from '../types/department';

/**
 * Department API endpoints
 */
export const departmentAPI = {
  /**
   * Get all departments with pagination
   */
  getDepartments: (params?: {
    page?: number;
    size?: number;
  }) => {
    return client.get<any>('/api/departments', { params });
  },

  /**
   * Get department by ID
   */
  getDepartmentById: (departmentId: number) => {
    return client.get<DepartmentResponse>(`/api/departments/${departmentId}`);
  },

  /**
   * Get department detail with member count
   */
  getDepartmentDetail: (departmentId: number) => {
    return client.get<DepartmentDetailResponse>(`/api/departments/${departmentId}/detail`);
  },
};
