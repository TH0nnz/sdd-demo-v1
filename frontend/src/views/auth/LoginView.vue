<template>
  <div class="login-view">
    <div class="login-container">
      <div class="login-card">
        <h1 class="login-title">Timesheet Management</h1>
        <p class="login-subtitle">Sign in to your account</p>

        <!-- Error Message -->
        <div v-if="error" class="error-alert">
          <p class="error-message">{{ error }}</p>
        </div>

        <!-- Login Form -->
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="email">Email Address</label>
            <input 
              id="email"
              v-model="credentials.email"
              type="email"
              placeholder="Enter your email"
              required
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <input 
              id="password"
              v-model="credentials.password"
              type="password"
              placeholder="Enter your password"
              required
              class="form-input"
            />
          </div>

          <button 
            type="submit"
            class="btn btn-primary btn-block"
            :disabled="isLoading"
          >
            <span v-if="!isLoading">Sign In</span>
            <span v-else>Signing in...</span>
          </button>
        </form>

        <!-- Demo Credentials -->
        <div class="demo-info">
          <p class="demo-title">Demo Credentials:</p>
          <ul class="demo-list">
            <li>EMPLOYEE: employee@example.com / password123</li>
            <li>MANAGER: manager@example.com / password123</li>
            <li>EXECUTIVE: executive@example.com / password123</li>
            <li>PM: pm@example.com / password123</li>
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
import type { LoginRequest } from '../../types/auth';

const router = useRouter();
const authStore = useAuthStore();

const credentials = ref<LoginRequest>({
  email: '',
  password: '',
});

const error = ref('');
const isLoading = ref(false);

const handleLogin = async () => {
  error.value = '';
  isLoading.value = true;

  try {
    await authStore.login(credentials.value);
    router.push('/');
  } catch (err: any) {
    error.value = err.message || 'Login failed. Please check your credentials.';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.login-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-card {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-align: center;
  color: #333;
}

.login-subtitle {
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

.login-form {
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

.btn {
  padding: 12px 16px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
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

.btn-block {
  width: 100%;
}

.demo-info {
  background-color: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 12px;
  margin-top: 16px;
}

.demo-title {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  color: #666;
  margin: 0 0 8px 0;
}

.demo-list {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 12px;
  color: #555;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
