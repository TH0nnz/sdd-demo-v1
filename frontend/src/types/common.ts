/**
 * Common TypeScript types and interfaces for the frontend.
 */

/**
 * API Error Response
 */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: Record<string, string>;
}

/**
 * API Message Response
 */
export interface MessageResponse {
  message: string;
}

/**
 * Pagination Metadata
 */
export interface PageMetadata {
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * Paginated Response
 */
export interface PaginatedResponse<T> {
  data: T[];
  page: PageMetadata;
}

/**
 * Sort Direction
 */
export enum SortDirection {
  ASC = 'asc',
  DESC = 'desc',
}

/**
 * Query Parameters for List Endpoints
 */
export interface QueryParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  direction?: SortDirection;
  search?: string;
  [key: string]: any;
}

/**
 * Form Validation Rule
 */
export interface ValidationRule {
  required?: boolean;
  message: string;
  trigger?: string | string[];
  min?: number;
  max?: number;
  pattern?: RegExp;
  validator?: (rule: any, value: any, callback: any) => void;
}

/**
 * Select Option
 */
export interface SelectOption<T = any> {
  label: string;
  value: T;
  disabled?: boolean;
}

/**
 * Table Column Definition
 */
export interface TableColumn {
  prop: string;
  label: string;
  width?: string | number;
  minWidth?: string | number;
  sortable?: boolean | 'custom';
  formatter?: (row: any, column: any, cellValue: any, index: number) => any;
  align?: 'left' | 'center' | 'right';
  fixed?: boolean | 'left' | 'right';
}

/**
 * Loading State
 */
export interface LoadingState {
  loading: boolean;
  error: string | null;
}

/**
 * Date Range
 */
export interface DateRange {
  startDate: string | Date;
  endDate: string | Date;
}

/**
 * Time Range
 */
export interface TimeRange {
  startTime: string;
  endTime: string;
}
