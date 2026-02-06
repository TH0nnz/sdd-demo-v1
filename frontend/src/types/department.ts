import type { UserSimple } from './user';

export interface DepartmentResponse {
  id: number;
  name: string;
  manager?: UserSimple;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface DepartmentDetailResponse {
  id: number;
  name: string;
  manager?: UserSimple;
  memberCount: number;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface DepartmentSimpleResponse {
  id: number;
  name: string;
}
