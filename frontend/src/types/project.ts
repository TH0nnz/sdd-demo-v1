/**
 * Project and time request types and interfaces
 */

export enum ProjectStatus {
  ACTIVE = 'ACTIVE',
  CLOSED = 'CLOSED',
}

export enum TimeRequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export interface UserSimple {
  id: number;
  name: string;
  email: string;
}

export interface ProjectResponse {
  id: number;
  name: string;
  description: string;
  status: ProjectStatus;
  managerId: number;
  manager: UserSimple;
  startDate: string;
  endDate: string;
  budget: number;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface ProjectPageResponse {
  content: ProjectResponse[];
  pageInfo: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export interface CreateProjectRequest {
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  budget: number;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  budget?: number;
  version?: number;
}

export interface TimeRequestResponse {
  id: number;
  projectId: number;
  requesterId: number;
  requester: UserSimple;
  status: TimeRequestStatus;
  requestedHours: number;
  approvalNotes?: string;
  approvedBy?: UserSimple;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface TimeRequestPageResponse {
  content: TimeRequestResponse[];
  pageInfo: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export interface ApproveTimeRequestRequest {
  approved: boolean;
  approvalNotes?: string;
  version?: number;
}
