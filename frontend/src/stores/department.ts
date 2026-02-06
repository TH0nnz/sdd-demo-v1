import { defineStore } from 'pinia';
import type { Department } from '../types/department';
import departmentApi from '../api/departmentApi';

interface DepartmentState {
  departments: Department[];
  loading: boolean;
  error: string | null;
}

export const useDepartmentStore = defineStore('department', {
  state: (): DepartmentState => ({
    departments: [],
    loading: false,
    error: null,
  }),

  getters: {
    getDepartmentById: (state) => (id: number) => {
      return state.departments.find((dept) => dept.id === id);
    },
    
    activeDepartments: (state) => {
      return state.departments.filter((dept) => dept.active !== false);
    },
  },

  actions: {
    async fetchDepartments() {
      this.loading = true;
      this.error = null;
      try {
        const response = await departmentApi.getAllDepartments();
        this.departments = response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || 'Failed to fetch departments';
        console.error('Error fetching departments:', error);
      } finally {
        this.loading = false;
      }
    },

    async createDepartment(departmentData: Partial<Department>) {
      this.loading = true;
      this.error = null;
      try {
        const response = await departmentApi.createDepartment(departmentData);
        this.departments.push(response.data);
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || 'Failed to create department';
        console.error('Error creating department:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async updateDepartment(id: number, departmentData: Partial<Department>) {
      this.loading = true;
      this.error = null;
      try {
        const response = await departmentApi.updateDepartment(id, departmentData);
        const index = this.departments.findIndex((dept) => dept.id === id);
        if (index !== -1) {
          this.departments[index] = response.data;
        }
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || 'Failed to update department';
        console.error('Error updating department:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async deleteDepartment(id: number) {
      this.loading = true;
      this.error = null;
      try {
        await departmentApi.deleteDepartment(id);
        this.departments = this.departments.filter((dept) => dept.id !== id);
      } catch (error: any) {
        this.error = error.response?.data?.message || 'Failed to delete department';
        console.error('Error deleting department:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
  },
});
