import { client } from './client';
import type { LoginRequest, LoginResponse, ChangePasswordRequest } from '../types/auth';
import type { UserResponse } from '../types/user';

/**
 * Authentication API endpoints (T153)
 * Handles communication with backend authentication APIs
 */
export const authAPI = {
  /**
   * User login with email and password
   */
  login: (data: LoginRequest) => {
    return client.post<LoginResponse>('/auth/login', data);
  },

  /**
   * Get current authenticated user information
   */
  getCurrentUser: () => {
    return client.get<UserResponse>('/auth/me');
  },

  /**
   * Change user password
   */
  changePassword: (data: ChangePasswordRequest) => {
    return client.post('/auth/change-password', data);
  },

  /**
   * Logout user (clear token on client)
   */
  logout: () => {
    return client.post('/auth/logout');
  },
};
