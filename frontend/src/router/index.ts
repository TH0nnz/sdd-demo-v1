import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { UserRole, type RouteMeta } from '@/types/auth';

/**
 * Vue Router configuration with authentication guards.
 * 
 * Features:
 * - Route-level authentication
 * - Role-based access control
 * - Automatic redirect to login
 * - Route meta information
 */

const routes: RouteRecordRaw[] = [
  // Public routes
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: {
      requiresAuth: false,
      title: '登入',
    } as RouteMeta,
  },
  
  // Dashboard (default after login)
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: {
      requiresAuth: true,
      title: '儀表板',
    } as RouteMeta,
  },
  
  // Executive routes (執行人員)
  {
    path: '/timesheets',
    name: 'Timesheets',
    component: () => import('@/views/timesheets/TimesheetListView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EMPLOYEE, UserRole.PM, UserRole.MANAGER],
      title: '工時填報',
    } as RouteMeta,
  },
  {
    path: '/timesheets/form',
    name: 'TimesheetForm',
    component: () => import('@/views/timesheets/TimesheetFormView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EMPLOYEE],
      title: '填報工時',
    } as RouteMeta,
  },
  {
    path: '/timesheets/calendar',
    name: 'TimesheetCalendar',
    component: () => import('@/views/timesheets/TimesheetCalendarView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EMPLOYEE],
      title: '工時日曆',
    } as RouteMeta,
  },
  {
    path: '/timesheets/new',
    name: 'TimesheetNew',
    component: () => import('@/views/timesheets/TimesheetFormView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EMPLOYEE],
      title: '新增工時',
    } as RouteMeta,
  },
  
  // PM routes (專案經理)
  {
    path: '/projects',
    name: 'Projects',
    component: () => import('@/views/projects/ProjectListView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE, UserRole.PM, UserRole.EMPLOYEE],
      title: '專案管理',
    } as RouteMeta,
  },
  {
    path: '/projects/new',
    name: 'ProjectNew',
    component: () => import('@/views/projects/ProjectFormView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE],
      title: '新增專案',
    } as RouteMeta,
  },
  {
    path: '/projects/:id/edit',
    name: 'ProjectEdit',
    component: () => import('@/views/projects/ProjectFormView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE],
      title: '編輯專案',
    } as RouteMeta,
  },
  {
    path: '/projects/:id',
    name: 'ProjectDetail',
    component: () => import('@/views/projects/ProjectDetailView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE, UserRole.PM, UserRole.EMPLOYEE],
      title: '專案詳情',
    } as RouteMeta,
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: () => import('@/views/tasks/TaskListView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.PM, UserRole.EMPLOYEE],
      title: '任務管理',
    } as RouteMeta,
  },
  {
    path: '/tasks/create',
    name: 'TaskCreate',
    component: () => import('@/views/tasks/TaskFormView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.PM],
      title: '建立任務',
    } as RouteMeta,
  },
  {
    path: '/tasks/:taskId/edit',
    name: 'TaskEdit',
    component: () => import('@/views/tasks/TaskFormView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.PM],
      title: '編輯任務',
    } as RouteMeta,
  },
  {
    path: '/projects/:projectId/dashboard',
    name: 'ProjectDashboard',
    component: () => import('@/views/projects/ProjectDashboardView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.PM, UserRole.EMPLOYEE],
      title: '專案儀表板',
    } as RouteMeta,
  },
  
  // Manager routes (管理層)
  {
    path: '/time-requests',
    name: 'TimeRequests',
    component: () => import('@/views/time-requests/TimeRequestListView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE, UserRole.PM],
      title: '時數申請',
    } as RouteMeta,
  },
  
  // Department Head routes (部門主管)
  {
    path: '/reports',
    name: 'Reports',
    component: () => import('@/views/reports/ReportListView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM],
      title: '報表查詢',
    } as RouteMeta,
  },
  {
    path: '/reports/timesheets',
    name: 'TimesheetReport',
    component: () => import('@/views/reports/TimesheetReportView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM],
      title: '工時報表',
    } as RouteMeta,
  },
  {
    path: '/reports/projects',
    name: 'ProjectReport',
    component: () => import('@/views/reports/ProjectReportView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM],
      title: '專案報表',
    } as RouteMeta,
  },
  {
    path: '/reports/users',
    name: 'UserReport',
    component: () => import('@/views/reports/ProjectReportView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM],
      title: '人員報表',
    } as RouteMeta,
  },
  
  // HR routes (人力資源)
  {
    path: '/users',
    name: 'Users',
    component: () => import('@/views/users/UserListView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.HR],
      title: '人員管理',
    } as RouteMeta,
  },
  {
    path: '/users/new',
    name: 'UserNew',
    component: () => import('@/views/users/UserFormView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.HR],
      title: '新增人員',
    } as RouteMeta,
  },
  {
    path: '/users/:id/edit',
    name: 'UserEdit',
    component: () => import('@/views/users/UserFormView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.HR],
      title: '編輯人員',
    } as RouteMeta,
  },
  {
    path: '/departments',
    name: 'Departments',
    component: () => import('@/views/departments/DepartmentListView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: [UserRole.HR, UserRole.MANAGER],
      title: '部門管理',
    } as RouteMeta,
  },
  
  // Profile & Settings
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/ProfileView.vue'),
    meta: {
      requiresAuth: true,
      title: '個人資料',
    } as RouteMeta,
  },
  
  // 404 Not Found
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: {
      requiresAuth: false,
      title: '頁面不存在',
    } as RouteMeta,
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

/**
 * Global navigation guard for authentication and authorization.
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const meta = to.meta as RouteMeta;
  
  // Set page title
  if (meta.title) {
    document.title = `${meta.title} - 工時管理系統`;
  }
  
  // Check if route requires authentication
  if (meta.requiresAuth && !authStore.isAuthenticated) {
    // Redirect to login
    next({
      name: 'Login',
      query: { redirect: to.fullPath },
    });
    return;
  }
  
  // Check if user has required role
  if (meta.allowedRoles && authStore.user) {
    const hasRole = meta.allowedRoles.includes(authStore.user.role);
    
    if (!hasRole) {
      // User doesn't have permission
      console.error('Access denied: insufficient permissions');
      next({ name: 'Dashboard' }); // Redirect to dashboard
      return;
    }
  }
  
  // If already authenticated and trying to access login page
  if (to.name === 'Login' && authStore.isAuthenticated) {
    next({ name: 'Dashboard' });
    return;
  }
  
  // Allow navigation
  next();
});

export default router;
