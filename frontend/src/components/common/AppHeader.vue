<template>
  <header class="app-header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="brand">
          <span class="brand-icon">⏱️</span>
          <span class="brand-name">Timesheet</span>
        </router-link>
      </div>

      <nav class="header-nav" v-if="authStore.isAuthenticated">
        <router-link 
          to="/reports" 
          class="nav-link"
          v-if="canAccessReports"
        >
          Reports
        </router-link>
        <router-link 
          to="/projects" 
          class="nav-link"
          v-if="canAccessProjects"
        >
          Projects
        </router-link>
        <router-link 
          to="/timesheets" 
          class="nav-link"
          v-if="canAccessTimesheets"
        >
          Timesheets
        </router-link>
      </nav>

      <div class="header-right" v-if="authStore.isAuthenticated">
        <div class="user-menu">
          <button 
            class="user-button"
            @click="toggleUserMenu"
            :class="{ active: showUserMenu }"
          >
            <span class="user-icon">👤</span>
            <span class="user-name">{{ authStore.user?.name }}</span>
            <span class="dropdown-icon">▼</span>
          </button>

          <div v-if="showUserMenu" class="user-dropdown">
            <router-link 
              to="/profile" 
              class="dropdown-link"
              @click="closeUserMenu"
            >
              Profile
            </router-link>
            <router-link 
              to="/change-password" 
              class="dropdown-link"
              @click="closeUserMenu"
            >
              Change Password
            </router-link>
            <hr class="dropdown-divider" />
            <button 
              class="dropdown-link logout-link"
              @click="handleLogout"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { UserRole } from '../../types/auth';

const router = useRouter();
const authStore = useAuthStore();

const showUserMenu = ref(false);

const canAccessReports = computed(() => {
  return [UserRole.EXECUTIVE, UserRole.MANAGER, UserRole.PM].includes(authStore.userRole);
});

const canAccessProjects = computed(() => {
  return [UserRole.EXECUTIVE, UserRole.PM, UserRole.EMPLOYEE].includes(authStore.userRole);
});

const canAccessTimesheets = computed(() => {
  return [UserRole.EMPLOYEE, UserRole.PM, UserRole.MANAGER].includes(authStore.userRole);
});

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
};

const closeUserMenu = () => {
  showUserMenu.value = false;
};

const handleLogout = async () => {
  await authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.app-header {
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #333;
  font-weight: 700;
  font-size: 16px;
  transition: color 0.3s ease;
}

.brand:hover {
  color: #007bff;
}

.brand-icon {
  font-size: 24px;
}

.brand-name {
  letter-spacing: 0.5px;
}

.header-nav {
  display: flex;
  gap: 24px;
  align-items: center;
}

.nav-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #007bff;
  border-bottom-color: #007bff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-menu {
  position: relative;
}

.user-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  transition: all 0.3s ease;
}

.user-button:hover,
.user-button.active {
  border-color: #007bff;
  background-color: #f0f7ff;
}

.user-icon {
  font-size: 16px;
}

.user-name {
  max-width: 100px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-icon {
  font-size: 10px;
  margin-left: 4px;
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 200px;
  margin-top: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1000;
}

.dropdown-link {
  display: block;
  width: 100%;
  padding: 12px 16px;
  text-align: left;
  text-decoration: none;
  color: #333;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
  font-family: inherit;
}

.dropdown-link:hover {
  background-color: #f5f5f5;
}

.logout-link {
  color: #e74c3c;
  font-weight: 500;
}

.logout-link:hover {
  background-color: #ffe6e0;
}

.dropdown-divider {
  height: 1px;
  background-color: #e0e0e0;
  border: none;
  margin: 4px 0;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .header-nav {
    gap: 12px;
  }

  .nav-link {
    font-size: 12px;
  }

  .user-name {
    display: none;
  }

  .brand-name {
    display: none;
  }
}
</style>
