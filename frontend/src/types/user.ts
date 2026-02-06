/**
 * User types and interfaces
 */

export enum UserRole {
  MANAGER = 'MANAGER',
  PM = 'PM',
  DEPT_HEAD = 'DEPT_HEAD',
  EXECUTIVE = 'EXECUTIVE',
  HR = 'HR',
}

export interface DepartmentSimple {
  id: number;
  name: string;
}

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  department?: DepartmentSimple;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface UserSimple {
  id: number;
  name: string;
  email: string;
  role: UserRole;
}

export interface UserPageResponse {
  content: UserResponse[];
  pageInfo: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export interface CreateUserRequest {
  name: string;
  email: string;
  role: UserRole;
  departmentId?: number;
  initialPassword?: string;
}

export interface UpdateUserRequest {
  name?: string;
  role?: UserRole;
  departmentId?: number;
  version?: number;
}
