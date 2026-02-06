<template>
  <div class="profile-view">
    <div class="profile-header">
      <h1>個人資料</h1>
    </div>

    <div class="profile-container">
      <!-- User Info Card -->
      <div class="profile-card">
        <h2>基本資訊</h2>
        <div class="form-group">
          <label>姓名</label>
          <input type="text" v-model="profile.name" readonly />
        </div>

        <div class="form-group">
          <label>電子郵件</label>
          <input type="email" v-model="profile.email" readonly />
        </div>

        <div class="form-group">
          <label>職位</label>
          <input type="text" v-model="profile.role" readonly />
        </div>

        <div class="form-group">
          <label>部門</label>
          <input type="text" v-model="profile.department" readonly />
        </div>

        <div class="form-group">
          <label>員工編號</label>
          <input type="text" v-model="profile.employeeId" readonly />
        </div>

        <div class="form-group">
          <label>加入日期</label>
          <input type="text" v-model="profile.joinDate" readonly />
        </div>
      </div>

      <!-- Account Settings -->
      <div class="profile-card">
        <h2>帳號設定</h2>

        <div class="settings-section">
          <div class="settings-item">
            <div class="settings-info">
              <h3>變更密碼</h3>
              <p>更新您的帳號密碼以保持帳號安全</p>
            </div>
            <router-link to="/change-password" class="btn-link">變更</router-link>
          </div>

          <div class="settings-item">
            <div class="settings-info">
              <h3>登出</h3>
              <p>登出您的帳號</p>
            </div>
            <button @click="logout" class="btn-danger">登出</button>
          </div>
        </div>
      </div>

      <!-- Preferences -->
      <div class="profile-card">
        <h2>偏好設定</h2>

        <div class="preferences-section">
          <div class="preference-item">
            <label for="theme">主題</label>
            <select id="theme" v-model="preferences.theme">
              <option value="light">淺色</option>
              <option value="dark">深色</option>
              <option value="auto">自動</option>
            </select>
          </div>

          <div class="preference-item">
            <label for="language">語言</label>
            <select id="language" v-model="preferences.language">
              <option value="zh-TW">繁體中文</option>
              <option value="en">English</option>
            </select>
          </div>

          <div class="preference-item">
            <label for="notifications">通知</label>
            <input type="checkbox" id="notifications" v-model="preferences.notifications" />
            <span>接收系統通知</span>
          </div>
        </div>

        <button @click="savePreferences" class="btn-primary" :disabled="saving">
          {{ saving ? '保存中...' : '保存設定' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const profile = ref({
  name: '',
  email: '',
  role: '',
  department: '',
  employeeId: '',
  joinDate: '',
})

const preferences = ref({
  theme: 'light',
  language: 'zh-TW',
  notifications: true,
})

const saving = ref(false)

const savePreferences = async () => {
  saving.value = true
  try {
    // In a real app, this would save to the backend
    localStorage.setItem('preferences', JSON.stringify(preferences.value))
    // Show success message (would use a toast in real app)
    console.log('Preferences saved')
  } finally {
    saving.value = false
  }
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  // Load user profile
  if (authStore.user) {
    profile.value.name = authStore.user.name
    profile.value.email = authStore.user.email
    profile.value.role = authStore.user.role
    profile.value.department = authStore.user.department || '未分配'
    profile.value.employeeId = authStore.user.id
    profile.value.joinDate = new Date().toLocaleDateString('zh-TW')
  }

  // Load preferences
  const saved = localStorage.getItem('preferences')
  if (saved) {
    preferences.value = JSON.parse(saved)
  }
})
</script>

<style scoped lang="scss">
.profile-view {
  padding: 2rem;
  max-width: 900px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 2rem;

  h1 {
    font-size: 2rem;
    color: #333;
    margin: 0;
  }
}

.profile-container {
  display: grid;
  gap: 2rem;
}

.profile-card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  h2 {
    margin-top: 0;
    margin-bottom: 1.5rem;
    color: #333;
    font-size: 1.3rem;
    border-bottom: 2px solid #f0f0f0;
    padding-bottom: 0.75rem;
  }
}

// Form Groups
.form-group {
  margin-bottom: 1.5rem;

  label {
    display: block;
    margin-bottom: 0.5rem;
    color: #666;
    font-weight: 500;
  }

  input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    background: #f9f9f9;
    color: #666;

    &:focus {
      outline: none;
      border-color: #1976d2;
      background: white;
    }
  }
}

// Settings
.settings-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 6px;

  .settings-info {
    h3 {
      margin: 0 0 0.25rem 0;
      color: #333;
    }

    p {
      margin: 0;
      color: #999;
      font-size: 0.9rem;
    }
  }
}

.btn-link {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: none;
  padding: 0;

  &:hover {
    text-decoration: underline;
  }
}

.btn-danger {
  background: #d32f2f;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    background: #c62828;
  }
}

// Preferences
.preferences-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.preference-item {
  display: flex;
  flex-direction: column;

  label {
    margin-bottom: 0.5rem;
    color: #666;
    font-weight: 500;
  }

  select {
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;

    &:focus {
      outline: none;
      border-color: #1976d2;
    }
  }

  input[type='checkbox'] {
    margin-right: 0.5rem;
  }

  span {
    color: #666;
  }
}

// Buttons
.btn-primary {
  background: #1976d2;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover:not(:disabled) {
    background: #1565c0;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

@media (max-width: 768px) {
  .profile-view {
    padding: 1rem;
  }

  .settings-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
