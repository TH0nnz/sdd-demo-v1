# Role Migration Guide - Before & After Reference

## Quick Reference Table

| Aspect | Before (OLD) | After (NEW) |
|--------|------|-----|
| **Management Layer** | `UserRole.MANAGER` | `UserRole.EXECUTIVE` |
| **Department Head** | `UserRole.DEPT_HEAD` | `UserRole.MANAGER` |
| **Workers/Staff** | `UserRole.EXECUTIVE` | `UserRole.EMPLOYEE` |
| **Project Manager** | `UserRole.PM` | `UserRole.PM` ✓ |
| **HR Staff** | `UserRole.HR` | `UserRole.HR` ✓ |

## Enum Definition Changes

### BEFORE (Old Enum)
```typescript
export enum UserRole {
  MANAGER = 'MANAGER',        // Management layer
  PM = 'PM',                  // Project Manager
  DEPT_HEAD = 'DEPT_HEAD',    // Department head
  EXECUTIVE = 'EXECUTIVE',    // Workers/Staff
  HR = 'HR',                  // HR
}
```

### AFTER (New Enum)
```typescript
export enum UserRole {
  EXECUTIVE = 'EXECUTIVE',    // Management layer (了management layer)
  PM = 'PM',                  // Project Manager
  MANAGER = 'MANAGER',        // Department head
  EMPLOYEE = 'EMPLOYEE',      // Workers/Staff
  HR = 'HR',                  // HR
}
```

## Computed Properties Migration

### Permission Checks

#### canManageProjects
```diff
- BEFORE: UserRole.MANAGER || UserRole.PM
+ AFTER:  UserRole.EXECUTIVE || UserRole.PM
```

**Who can now create/manage projects?**
- ✅ EXECUTIVE (was MANAGER)
- ✅ PM (unchanged)

#### canManageTasks
```diff
- BEFORE: UserRole.PM || UserRole.EXECUTIVE
+ AFTER:  UserRole.PM || UserRole.MANAGER
```

**Who can now create/manage tasks?**
- ✅ PM (unchanged)
- ✅ MANAGER (was DEPT_HEAD)

#### canLogTimesheets
```diff
- BEFORE: UserRole.EXECUTIVE
+ AFTER:  UserRole.EMPLOYEE
```

**Who can now submit timesheets?**
- ✅ EMPLOYEE (was EXECUTIVE)

#### canViewReports
```diff
- BEFORE: UserRole.MANAGER || UserRole.DEPT_HEAD || UserRole.PM
+ AFTER:  UserRole.EXECUTIVE || UserRole.MANAGER || UserRole.PM
```

**Who can now view reports?**
- ✅ EXECUTIVE (was MANAGER)
- ✅ MANAGER (was DEPT_HEAD)
- ✅ PM (unchanged)

#### canManageUsers
```diff
- BEFORE: UserRole.HR
+ AFTER:  UserRole.HR
```

**Who can manage users?**
- ✅ HR (unchanged)

## Common Code Patterns

### Old Pattern → New Pattern

#### Single Role Check
```typescript
// OLD
if (user.role === UserRole.MANAGER) { }

// NEW
if (user.role === UserRole.EXECUTIVE) { }
```

#### Multiple Role Check - Projects
```typescript
// OLD
const allowedRoles = [UserRole.MANAGER, UserRole.PM];
if (allowedRoles.includes(userRole)) { }

// NEW
const allowedRoles = [UserRole.EXECUTIVE, UserRole.PM];
if (allowedRoles.includes(userRole)) { }
```

#### Multiple Role Check - Tasks
```typescript
// OLD
const allowedRoles = [UserRole.PM, UserRole.EXECUTIVE];
if (allowedRoles.includes(userRole)) { }

// NEW
const allowedRoles = [UserRole.PM, UserRole.MANAGER];
if (allowedRoles.includes(userRole)) { }
```

#### Multiple Role Check - Reports
```typescript
// OLD
const allowedRoles = [UserRole.MANAGER, UserRole.DEPT_HEAD, UserRole.PM];
if (allowedRoles.includes(userRole)) { }

// NEW
const allowedRoles = [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM];
if (allowedRoles.includes(userRole)) { }
```

## Route Access Changes

### Timesheets Routes
```diff
- OLD: [UserRole.EXECUTIVE, UserRole.PM, UserRole.DEPT_HEAD]
+ NEW: [UserRole.EMPLOYEE, UserRole.PM, UserRole.MANAGER]
```

### Projects Routes
```diff
- OLD: [UserRole.MANAGER, UserRole.PM, UserRole.EXECUTIVE]
+ NEW: [UserRole.EXECUTIVE, UserRole.PM, UserRole.EMPLOYEE]
```

### Projects Create/Edit Routes
```diff
- OLD: [UserRole.MANAGER]
+ NEW: [UserRole.EXECUTIVE]
```

### Tasks Routes
```diff
- OLD: [UserRole.PM, UserRole.EXECUTIVE]
+ NEW: [UserRole.PM, UserRole.EMPLOYEE]
```

### Reports Routes
```diff
- OLD: [UserRole.MANAGER, UserRole.DEPT_HEAD, UserRole.PM]
+ NEW: [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM]
```

### Department Routes
```diff
- OLD: [UserRole.HR, UserRole.DEPT_HEAD]
+ NEW: [UserRole.HR, UserRole.MANAGER]
```

## Navigation Visibility Changes

### Sidebar - Work Hours Section
```diff
- OLD: [UserRole.EXECUTIVE, UserRole.PM, UserRole.DEPT_HEAD]
+ NEW: [UserRole.EMPLOYEE, UserRole.PM, UserRole.MANAGER]
```

### Sidebar - Management Section
```diff
- OLD: [UserRole.MANAGER, UserRole.PM]
+ NEW: [UserRole.EXECUTIVE, UserRole.PM]
```

### Sidebar - Reports Section
```diff
- OLD: [UserRole.MANAGER, UserRole.DEPT_HEAD, UserRole.PM]
+ NEW: [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM]
```

### Header - Reports Link
```diff
- OLD: [UserRole.MANAGER, UserRole.DEPT_HEAD, UserRole.PM]
+ NEW: [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM]
```

### Header - Projects Link
```diff
- OLD: [UserRole.MANAGER, UserRole.PM, UserRole.EXECUTIVE]
+ NEW: [UserRole.EXECUTIVE, UserRole.PM, UserRole.EMPLOYEE]
```

### Header - Timesheets Link
```diff
- OLD: [UserRole.EXECUTIVE, UserRole.PM, UserRole.DEPT_HEAD]
+ NEW: [UserRole.EMPLOYEE, UserRole.PM, UserRole.MANAGER]
```

## Display Name Mappings

### Role Display Names (Chinese)
```typescript
// OLD
{
  'MANAGER': '管理層',
  'PM': 'PM',
  'DEPT_HEAD': '部門主管',
  'EXECUTIVE': '執行人員',
  'HR': 'HR',
}

// NEW
{
  'EXECUTIVE': '管理層',
  'PM': 'PM',
  'MANAGER': '部門主管',
  'EMPLOYEE': '執行人員',
  'HR': 'HR',
}
```

## Form Select Options

### User Role Selection Form
```html
<!-- OLD -->
<option value="MANAGER">管理層</option>
<option value="PM">PM</option>
<option value="DEPT_HEAD">部門主管</option>
<option value="EXECUTIVE">執行人員</option>
<option value="HR">HR</option>

<!-- NEW -->
<option value="EXECUTIVE">管理層</option>
<option value="PM">PM</option>
<option value="MANAGER">部門主管</option>
<option value="EMPLOYEE">執行人員</option>
<option value="HR">HR</option>
```

## Demo Credentials

### Login Page Demo Credentials
```diff
- OLD:
  - EXECUTIVE: executive@example.com / password123
  - DEPT_HEAD: depthead@example.com / password123
  - MANAGER: manager@example.com / password123
  - PM: pm@example.com / password123

+ NEW:
  - EMPLOYEE: employee@example.com / password123
  - MANAGER: manager@example.com / password123
  - EXECUTIVE: executive@example.com / password123
  - PM: pm@example.com / password123
```

## Permission Model Comparison

### Authority Hierarchy

#### OLD Model
```
MANAGER (highest)
├── DEPT_HEAD
├── PM
├── EXECUTIVE (lowest for staff)
└── HR (separate)
```

#### NEW Model
```
EXECUTIVE (highest)
├── MANAGER
├── PM
├── EMPLOYEE (lowest for staff)
└── HR (separate)
```

The hierarchy remains logically consistent - only the names have changed to better reflect roles.

## Migration Checklist for Developers

### If You're Updating Code
- [ ] Replace `UserRole.MANAGER` → `UserRole.EXECUTIVE`
- [ ] Replace `UserRole.DEPT_HEAD` → `UserRole.MANAGER`
- [ ] Replace `UserRole.EXECUTIVE` → `UserRole.EMPLOYEE`
- [ ] Update route `allowedRoles` arrays
- [ ] Update form role options
- [ ] Update role display names
- [ ] Update computed property references
- [ ] Test with all 5 roles
- [ ] Verify route access control
- [ ] Check UI visibility per role

### If You're Testing
- [ ] Log in as EMPLOYEE (was EXECUTIVE)
- [ ] Log in as MANAGER (was DEPT_HEAD)
- [ ] Log in as EXECUTIVE (was MANAGER)
- [ ] Log in as PM (unchanged)
- [ ] Log in as HR (unchanged)
- [ ] Verify each role sees correct navigation
- [ ] Verify route access control
- [ ] Check button visibility per role
- [ ] Test form role selection
- [ ] Verify report/timesheet access

---

**Last Updated**: 2024
**Status**: Reference Guide for Role Migration
