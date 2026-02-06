<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-container">
      <div class="error-icon">⚠️</div>
      <h2>發生錯誤</h2>
      <p class="error-message">{{ errorMessage }}</p>
      <div class="error-details" v-if="showDetails">
        <pre>{{ errorStack }}</pre>
      </div>
      <div class="action-buttons">
        <button @click="resetError" class="btn-primary">重新嘗試</button>
        <button @click="goHome" class="btn-secondary">返回首頁</button>
        <button @click="toggleDetails" class="btn-tertiary">
          {{ showDetails ? '隱藏' : '顯示' }}詳細資訊
        </button>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const hasError = ref(false)
const errorMessage = ref('應用程式發生異常')
const errorStack = ref('')
const showDetails = ref(false)
const router = useRouter()

const resetError = () => {
  hasError.value = false
  errorMessage.value = '應用程式發生異常'
  errorStack.value = ''
  showDetails.value = false
}

const goHome = () => {
  resetError()
  router.push('/')
}

const toggleDetails = () => {
  showDetails.value = !showDetails.value
}

// Handle errors from child components
const handleError = (error: Error, info: string) => {
  hasError.value = true
  errorMessage.value = error.message || '應用程式發生異常'
  errorStack.value = `${error.stack}\n\nComponent: ${info}`
  console.error('ErrorBoundary caught error:', error, info)
}

// Expose error handler for parent components
defineExpose({
  handleError,
  resetError
})
</script>

<style scoped lang="scss">
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 2rem;
}

.error-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  max-width: 600px;
  width: 100%;
  text-align: center;
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

h2 {
  color: #d32f2f;
  margin-bottom: 1rem;
  font-size: 1.8rem;
}

.error-message {
  color: #666;
  margin-bottom: 1.5rem;
  font-size: 1.1rem;
  line-height: 1.6;
}

.error-details {
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1rem;
  margin: 1.5rem 0;
  text-align: left;
  max-height: 200px;
  overflow-y: auto;

  pre {
    margin: 0;
    font-size: 0.85rem;
    color: #333;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

button {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;

  &:hover {
    transform: translateY(-2px);
  }
}

.btn-primary {
  background: #1976d2;
  color: white;

  &:hover {
    background: #1565c0;
  }
}

.btn-secondary {
  background: #757575;
  color: white;

  &:hover {
    background: #616161;
  }
}

.btn-tertiary {
  background: #e0e0e0;
  color: #333;

  &:hover {
    background: #bdbdbd;
  }
}

@media (max-width: 600px) {
  .error-container {
    padding: 2rem;
  }

  h2 {
    font-size: 1.5rem;
  }

  .action-buttons {
    flex-direction: column;

    button {
      width: 100%;
    }
  }
}
</style>
