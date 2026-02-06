/**
 * Authentication-related types and interfaces.
 */

/**
 * User Role Enum
 * Roles follow the specification:
 * - EXECUTIVE: 管理層 (Highest authority)
 * - PM: 專案經理 (Project Manager)
 * - MANAGER: 部門主管 (Department Head)
 * - EMPLOYEE: 執行人員 (Worker)
 * - HR: 人力資源 (Human Resources)
 */
export enum UserRole {
  EXECUTIVE = 'EXECUTIVE',
  PM = 'PM',
  MANAGER = 'MANAGER',
  EMPLOYEE = 'EMPLOYEE',
  HR = 'HR',
}

/**
 * User Role Display Names (Chinese)
 */
export const UserRoleDisplayNames: Record<UserRole, string> = {
  [UserRole.EXECUTIVE]: '管理層',
  [UserRole.PM]: '專案經理',
  [UserRole.MANAGER]: '部門主管',
  [UserRole.EMPLOYEE]: '執行人員',
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
