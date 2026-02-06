import { client } from './client';
import type { UserResponse, UserPageResponse, CreateUserRequest, UpdateUserRequest } from '../types/user';

/**
 * User API endpoints for HR management
 */
export const userAPI = {
  /**
   * Get all users with optional filtering
   */
  getUsers: (params?: {
    role?: string;
    departmentId?: number;
    active?: boolean;
    page?: number;
    size?: number;
  }) => {
    return client.get<UserPageResponse>('/api/users', { params });
  },

  /**
   * Create a new user
   */
  createUser: (data: CreateUserRequest) => {
    return client.post<UserResponse>('/api/users', data);
  },

  /**
   * Get user by ID
   */
  getUserById: (userId: number) => {
    return client.get<UserResponse>(`/api/users/${userId}`);
  },

  /**
   * Update user information
   */
  updateUser: (userId: number, data: UpdateUserRequest) => {
    return client.put<UserResponse>(`/api/users/${userId}`, data);
  },

  /**
   * Deactivate user account
   */
  deactivateUser: (userId: number) => {
    return client.post(`/api/users/${userId}/deactivate`);
  },

  /**
   * Activate user account
   */
  activateUser: (userId: number) => {
    return client.post(`/api/users/${userId}/activate`);
  },
};
