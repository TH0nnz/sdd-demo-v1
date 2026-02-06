/**
 * Authentication-related types and interfaces.
 */

/**
 * User Role Enum
 */
export enum UserRole {
  MANAGER = 'MANAGER',
  PM = 'PM',
  DEPT_HEAD = 'DEPT_HEAD',
  EXECUTIVE = 'EXECUTIVE',
  HR = 'HR',
}

/**
 * User Role Display Names (Chinese)
 */
export const UserRoleDisplayNames: Record<UserRole, string> = {
  [UserRole.MANAGER]: '管理層',
  [UserRole.PM]: '專案經理',
  [UserRole.DEPT_HEAD]: '部門主管',
  [UserRole.EXECUTIVE]: '執行人員',
  [UserRole.HR]: '人力資源',
};

/**
 * User Interface
 */
export interface User {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  departmentId?: number;
  departmentName?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
  version?: number;
}

/**
 * Login Request
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Login Response
 */
export interface LoginResponse {
  token: string;
  user: User;
}

/**
 * Register Request
 */
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: UserRole;
  departmentId?: number;
}

/**
 * Change Password Request
 */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * Auth State
 */
export interface AuthState {
  user: User | null;
  token: string | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
}

/**
 * Permission Checker Function Type
 */
export type PermissionChecker = (user: User | null) => boolean;

/**
 * Route Meta with Auth Info
 */
export interface RouteMeta {
  requiresAuth?: boolean;
  allowedRoles?: UserRole[];
  title?: string;
  icon?: string;
}
