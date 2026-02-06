<template>
  <ErrorBoundary ref="errorBoundary">
    <div class="main-layout">
      <AppHeader />
      <div class="layout-body">
        <AppSidebar />
        <main class="main-content">
          <RouterView v-slot="{ Component }">
            <Transition name="fade" mode="out-in">
              <component :is="Component" :key="$route.fullPath" />
            </Transition>
          </RouterView>
        </main>
      </div>
    </div>
  </ErrorBoundary>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import AppSidebar from '@/components/common/AppSidebar.vue'
import ErrorBoundary from '@/components/common/ErrorBoundary.vue'

const route = useRoute()
const errorBoundary = ref<InstanceType<typeof ErrorBoundary>>()

// Handle uncaught errors
const handleError = (error: Error) => {
  console.error('Unhandled error:', error)
  if (errorBoundary.value) {
    errorBoundary.value.handleError(error, 'MainLayout')
  }
}

// Global error handler setup would typically be done in main.ts
// This is just a ref to the ErrorBoundary for programmatic access
defineExpose({
  errorBoundary
})
</script>

<style scoped lang="scss">
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
}

.layout-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 1.5rem;
  background: #f5f5f5;

  // Scrollbar styling
  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
  }

  &::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;

    &:hover {
      background: #555;
    }
  }
}

// Transition animation
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .layout-body {
    flex-direction: column;
  }

  .main-content {
    padding: 1rem;
  }
}
</style>
