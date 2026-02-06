<template>
  <div class="change-password-view">
    <div class="password-container">
      <div class="password-card">
        <h1 class="password-title">Change Password</h1>
        <p class="password-subtitle">Update your account password</p>

        <!-- Error Message -->
        <div v-if="error" class="error-alert">
          <p class="error-message">{{ error }}</p>
        </div>

        <!-- Success Message -->
        <div v-if="success" class="success-alert">
          <p class="success-message">{{ success }}</p>
        </div>

        <!-- Change Password Form -->
        <form @submit.prevent="handleChangePassword" class="password-form">
          <div class="form-group">
            <label for="current-password">Current Password</label>
            <input 
              id="current-password"
              v-model="passwordData.currentPassword"
              type="password"
              placeholder="Enter your current password"
              required
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="new-password">New Password</label>
            <input 
              id="new-password"
              v-model="passwordData.newPassword"
              type="password"
              placeholder="Enter your new password"
              required
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="confirm-password">Confirm Password</label>
            <input 
              id="confirm-password"
              v-model="passwordData.confirmPassword"
              type="password"
              placeholder="Confirm your new password"
              required
              class="form-input"
            />
          </div>

          <div class="form-actions">
            <button 
              type="submit"
              class="btn btn-primary"
              :disabled="isLoading"
            >
              <span v-if="!isLoading">Change Password</span>
              <span v-else>Changing...</span>
            </button>
            <button 
              type="button"
              class="btn btn-secondary"
              @click="handleCancel"
            >
              Cancel
            </button>
          </div>
        </form>

        <!-- Password Requirements -->
        <div class="password-requirements">
          <p class="requirements-title">Password Requirements:</p>
          <ul class="requirements-list">
            <li>Minimum 8 characters</li>
            <li>At least one uppercase letter</li>
            <li>At least one number</li>
            <li>At least one special character</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import type { ChangePasswordRequest } from '../../types/auth';

const router = useRouter();
const authStore = useAuthStore();

const passwordData = ref<ChangePasswordRequest>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const error = ref('');
const success = ref('');
const isLoading = ref(false);

const handleChangePassword = async () => {
  error.value = '';
  success.value = '';

  // Validate form
  if (passwordData.value.newPassword !== passwordData.value.confirmPassword) {
    error.value = 'New password and confirmation do not match';
    return;
  }

  isLoading.value = true;

  try {
    await authStore.changePassword(passwordData.value);
    success.value = 'Password changed successfully!';
    
    // Redirect to profile after 2 seconds
    setTimeout(() => {
      router.push('/profile');
    }, 2000);
  } catch (err: any) {
    error.value = err.message || 'Failed to change password';
  } finally {
    isLoading.value = false;
  }
};

const handleCancel = () => {
  router.back();
};
</script>

<style scoped>
.change-password-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.password-container {
  width: 100%;
  max-width: 450px;
}

.password-card {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.password-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-align: center;
  color: #333;
}

.password-subtitle {
  font-size: 14px;
  color: #666;
  text-align: center;
  margin: 0 0 24px 0;
}

.error-alert {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 20px;
}

.error-message {
  color: #721c24;
  margin: 0;
  font-size: 14px;
}

.success-alert {
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 20px;
}

.success-message {
  color: #155724;
  margin: 0;
  font-size: 14px;
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.form-input {
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-actions {
  display: flex;
  gap: 10px;
}

.btn {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: inherit;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.password-requirements {
  background-color: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 12px;
  margin-top: 16px;
}

.requirements-title {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  color: #666;
  margin: 0 0 8px 0;
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 12px;
  color: #555;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.requirements-list li::before {
  content: '✓ ';
  color: #28a745;
  font-weight: bold;
}
</style>
