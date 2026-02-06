<template>
  <div class="not-found-view">
    <div class="not-found-container">
      <div class="not-found-icon">404</div>
      <h1>頁面不存在</h1>
      <p>抱歉，您要查找的頁面不存在或已被移除。</p>
      <div class="not-found-actions">
        <router-link to="/" class="btn-primary">返回首頁</router-link>
        <button @click="goBack" class="btn-secondary">返回上一頁</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.not-found-view {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.not-found-container {
  text-align: center;
  background: white;
  border-radius: 12px;
  padding: 3rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-width: 500px;
}

.not-found-icon {
  font-size: 5rem;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 1rem;
  line-height: 1;
}

h1 {
  color: #333;
  margin: 1rem 0 0.5rem 0;
  font-size: 2rem;
}

p {
  color: #666;
  margin: 0.5rem 0 2rem 0;
  font-size: 1.1rem;
}

.not-found-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  text-decoration: none;
  display: inline-block;

  &:hover {
    transform: translateY(-2px);
  }
}

.btn-primary {
  background: #667eea;
  color: white;

  &:hover {
    background: #5568d3;
  }
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;

  &:hover {
    background: #e0e0e0;
  }
}

@media (max-width: 600px) {
  .not-found-container {
    padding: 2rem;
    margin: 1rem;
  }

  .not-found-icon {
    font-size: 3rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .not-found-actions {
    flex-direction: column;

    .btn-primary,
    .btn-secondary {
      width: 100%;
    }
  }
}
</style>
