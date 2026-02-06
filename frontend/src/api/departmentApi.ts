import { client } from './client';
import type { DepartmentResponse, DepartmentDetailResponse } from '../types/department';

/**
 * Department API endpoints for store compatibility
 */
export default {
  /**
   * Get all departments
   */
  getAllDepartments: () => {
    return client.get<any>('/api/departments');
  },

  /**
   * Get department by ID
   */
  getDepartmentById: (departmentId: number) => {
    return client.get<DepartmentResponse>(`/api/departments/${departmentId}`);
  },

  /**
   * Create a new department
   */
  createDepartment: (departmentData: any) => {
    return client.post<DepartmentResponse>('/api/departments', departmentData);
  },

  /**
   * Update an existing department
   */
  updateDepartment: (id: number, departmentData: any) => {
    return client.put<DepartmentResponse>(`/api/departments/${id}`, departmentData);
  },

  /**
   * Delete a department
   */
  deleteDepartment: (id: number) => {
    return client.delete(`/api/departments/${id}`);
  },

  /**
   * Get department detail with member count
   */
  getDepartmentDetail: (departmentId: number) => {
    return client.get<DepartmentDetailResponse>(`/api/departments/${departmentId}/detail`);
  },
};
