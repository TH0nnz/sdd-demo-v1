<template>
  <aside class="app-sidebar" :class="{ open: isOpen }">
    <nav class="sidebar-nav">
      <div class="sidebar-section">
        <h3 class="section-title">Main</h3>
        <ul class="nav-list">
          <li>
            <router-link 
              to="/" 
              class="nav-item"
              v-if="isVisible('dashboard')"
            >
              <span class="nav-icon">📊</span>
              <span class="nav-text">Dashboard</span>
            </router-link>
          </li>
        </ul>
      </div>

      <div class="sidebar-section" v-if="isVisible('executive')">
        <h3 class="section-title">Work Hours</h3>
        <ul class="nav-list">
          <li>
            <router-link 
              to="/timesheets" 
              class="nav-item"
            >
              <span class="nav-icon">📝</span>
              <span class="nav-text">Timesheets</span>
            </router-link>
          </li>
          <li>
            <router-link 
              to="/timesheets/calendar" 
              class="nav-item"
            >
              <span class="nav-icon">📅</span>
              <span class="nav-text">Calendar</span>
            </router-link>
          </li>
        </ul>
      </div>

      <div class="sidebar-section" v-if="isVisible('manager')">
        <h3 class="section-title">Management</h3>
        <ul class="nav-list">
          <li>
            <router-link 
              to="/projects" 
              class="nav-item"
            >
              <span class="nav-icon">📦</span>
              <span class="nav-text">Projects</span>
            </router-link>
          </li>
          <li>
            <router-link 
              to="/tasks" 
              class="nav-item"
            >
              <span class="nav-icon">✓</span>
              <span class="nav-text">Tasks</span>
            </router-link>
          </li>
          <li>
            <router-link 
              to="/time-requests" 
              class="nav-item"
            >
              <span class="nav-icon">📋</span>
              <span class="nav-text">Time Requests</span>
            </router-link>
          </li>
        </ul>
      </div>

      <div class="sidebar-section" v-if="isVisible('deptHead')">
        <h3 class="section-title">Reports</h3>
        <ul class="nav-list">
          <li>
            <router-link 
              to="/reports" 
              class="nav-item"
            >
              <span class="nav-icon">📈</span>
              <span class="nav-text">Reports</span>
            </router-link>
          </li>
        </ul>
      </div>

      <div class="sidebar-section" v-if="isVisible('hr')">
        <h3 class="section-title">Administration</h3>
        <ul class="nav-list">
          <li>
            <router-link 
              to="/users" 
              class="nav-item"
            >
              <span class="nav-icon">👥</span>
              <span class="nav-text">Users</span>
            </router-link>
          </li>
          <li>
            <router-link 
              to="/departments" 
              class="nav-item"
            >
              <span class="nav-icon">🏢</span>
              <span class="nav-text">Departments</span>
            </router-link>
          </li>
        </ul>
      </div>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useAuthStore } from '../../stores/auth';
import { UserRole } from '../../types/auth';

interface Props {
  isOpen?: boolean;
}

withDefaults(defineProps<Props>(), {
  isOpen: true,
});

const authStore = useAuthStore();

const isVisible = (section: string) => {
  switch (section) {
    case 'dashboard':
      return true;
    case 'executive':
      return [UserRole.EXECUTIVE, UserRole.PM, UserRole.DEPT_HEAD].includes(authStore.userRole);
    case 'manager':
      return [UserRole.MANAGER, UserRole.PM].includes(authStore.userRole);
    case 'deptHead':
      return [UserRole.MANAGER, UserRole.DEPT_HEAD, UserRole.PM].includes(authStore.userRole);
    case 'hr':
      return authStore.userRole === UserRole.HR;
    default:
      return false;
  }
};
</script>

<style scoped>
.app-sidebar {
  width: 240px;
  background-color: #f8f9fa;
  border-right: 1px solid #e0e0e0;
  padding: 20px 0;
  height: calc(100vh - 64px);
  overflow-y: auto;
  position: fixed;
  left: 0;
  top: 64px;
  transition: transform 0.3s ease;
  z-index: 99;
}

.app-sidebar.open {
  transform: translateX(0);
}

@media (max-width: 768px) {
  .app-sidebar {
    width: 100%;
    transform: translateX(-100%);
  }

  .app-sidebar.open {
    transform: translateX(0);
  }
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 0;
}

.sidebar-section {
  padding: 0 12px;
}

.section-title {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  color: #999;
  margin: 0 0 12px 0;
  padding: 0 12px;
  letter-spacing: 0.5px;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  color: #666;
  text-decoration: none;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.nav-item:hover {
  background-color: #e8eaed;
  color: #333;
}

.nav-item.router-link-active {
  background-color: #d4e3ff;
  color: #007bff;
  font-weight: 500;
}

.nav-icon {
  font-size: 16px;
  min-width: 20px;
  text-align: center;
}

.nav-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Scrollbar styling */
.app-sidebar::-webkit-scrollbar {
  width: 6px;
}

.app-sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.app-sidebar::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.app-sidebar::-webkit-scrollbar-thumb:hover {
  background: #999;
}
</style>
