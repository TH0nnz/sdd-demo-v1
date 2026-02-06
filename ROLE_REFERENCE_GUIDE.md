# Frontend Role Names - Quick Reference Guide

## New Role Enum Values

```typescript
export enum UserRole {
  EXECUTIVE = 'EXECUTIVE',    // 管理層 (Management Layer)
  PM = 'PM',                  // 專案經理 (Project Manager)
  MANAGER = 'MANAGER',        // 部門主管 (Department Head)
  EMPLOYEE = 'EMPLOYEE',      // 執行人員 (Workers)
  HR = 'HR',                  // 人力資源 (Human Resources)
}
```

## Role Hierarchy & Permissions

### EXECUTIVE (管理層 - Management Layer)
- **Can**: Manage projects, manage tasks, view reports, access timesheets
- **Routes**: /projects, /tasks, /time-requests, /reports
- **Features**: Create/edit projects

### MANAGER (部門主管 - Department Head)
- **Can**: Manage tasks, view reports, access timesheets, manage departments
- **Routes**: /tasks, /reports, /timesheets, /departments
- **Features**: Create/edit tasks

### EMPLOYEE (執行人員 - Workers)
- **Can**: Log timesheets, access timesheets
- **Routes**: /timesheets, /timesheets/calendar
- **Features**: Submit timesheet entries

### PM (專案經理 - Project Manager)
- **Can**: Manage projects, manage tasks, view reports, access timesheets
- **Routes**: /projects, /tasks, /reports, /timesheets
- **Features**: Create/edit projects and tasks

### HR (人力資源 - Human Resources)
- **Can**: Manage users, manage departments
- **Routes**: /users, /departments
- **Features**: CRUD operations on users

## Common Role Checks in Code

### Single Role Check
```typescript
// Check if current user is EXECUTIVE
if (authStore.user?.role === UserRole.EXECUTIVE) {
  // ...
}

// Using computed property
if (authStore.isExecutive) {
  // ...
}
```

### Multiple Roles Check
```typescript
// Check if user has any of these roles
const allowedRoles = [UserRole.EXECUTIVE, UserRole.PM];
if (allowedRoles.includes(authStore.userRole)) {
  // ...
}
```

### Computed Properties
```typescript
// Available in authStore
authStore.isExecutive      // boolean
authStore.isManager        // boolean
authStore.isPM             // boolean
authStore.isEmployee       // boolean
authStore.isHR             // boolean

// Permission checks
authStore.canManageProjects  // EXECUTIVE || PM
authStore.canManageTasks     // PM || MANAGER
authStore.canLogTimesheets   // EMPLOYEE
authStore.canViewReports     // EXECUTIVE || MANAGER || PM
authStore.canManageUsers     // HR
```

## Form Role Options

When displaying role selection in forms:

```typescript
const roleOptions = [
  { value: UserRole.EXECUTIVE, label: '管理層' },
  { value: UserRole.MANAGER, label: '部門主管' },
  { value: UserRole.EMPLOYEE, label: '執行人員' },
  { value: UserRole.PM, label: 'PM' },
  { value: UserRole.HR, label: 'HR' },
];
```

## String Literals vs Enum

### ❌ DON'T USE STRING LITERALS
```typescript
if (user.role === 'EXECUTIVE') { }  // Wrong - avoid!
```

### ✅ USE ENUM VALUES
```typescript
if (user.role === UserRole.EXECUTIVE) { }  // Correct
```

## Sidebar Visibility Map

```
Dashboard → Always visible to all roles

Work Hours Section:
- Timesheets, Calendar → EMPLOYEE, PM, MANAGER

Management Section:
- Projects, Tasks, Time Requests → EXECUTIVE, PM

Reports Section:
- Reports → EXECUTIVE, MANAGER, PM

Administration Section:
- Users, Departments → HR only
- Departments can also be edited by MANAGER
```

## Route Guard Example

```typescript
// In router/index.ts
{
  path: '/projects',
  name: 'Projects',
  component: () => import('@/views/projects/ProjectListView.vue'),
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.EXECUTIVE, UserRole.PM, UserRole.EMPLOYEE],
  }
}
```

## Template Visibility

In Vue templates:

```vue
<!-- Show only to EXECUTIVE users -->
<div v-if="authStore.user?.role === UserRole.EXECUTIVE">
  Executive content
</div>

<!-- Show to multiple roles -->
<div v-if="[UserRole.EXECUTIVE, UserRole.PM].includes(authStore.user?.role)">
  Executive or PM content
</div>

<!-- Use computed property for complex checks -->
<div v-if="authStore.canManageProjects">
  Project management content
</div>
```

## Migration Checklist

If updating old code:

- [ ] Replace `UserRole.MANAGER` → `UserRole.EXECUTIVE`
- [ ] Replace `UserRole.DEPT_HEAD` → `UserRole.MANAGER`
- [ ] Replace `UserRole.EXECUTIVE` → `UserRole.EMPLOYEE`
- [ ] Replace `isDeptHead` → appropriate combination of `isManager`, `isExecutive`, etc.
- [ ] Update route `allowedRoles` arrays
- [ ] Update form options
- [ ] Test all permission checks
- [ ] Verify navigation and visibility

## Display Names

```typescript
const displayNames = {
  EXECUTIVE: '管理層',
  MANAGER: '部門主管',
  EMPLOYEE: '執行人員',
  PM: 'PM',
  HR: 'HR',
};
```

---

For more information, see `ROLE_UPDATE_COMPLETED.md`
