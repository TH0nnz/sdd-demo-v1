<template>
  <div class="task-form-view">
    <div class="form-container">
      <div class="form-header">
        <h1>{{ isEditMode ? '編輯任務' : '建立任務' }}</h1>
      </div>

      <task-form
        :initialData="initialData"
        :projects="projects"
        :users="users"
        :isSubmitting="isSubmitting"
        :submitError="submitError"
        @submit="handleFormSubmit"
        @cancel="navigateBack"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useTaskStore } from '../../stores/task';
import { useProjectStore } from '../../stores/project';
import { useUserStore } from '../../stores/user';
import TaskForm from '../../components/tasks/TaskForm.vue';
import type { CreateTaskRequest, UpdateTaskRequest } from '../../types/task';

const router = useRouter();
const route = useRoute();
const taskStore = useTaskStore();
const projectStore = useProjectStore();
const userStore = useUserStore();

const taskId = computed(() => {
  const id = route.params.taskId as string;
  return id ? parseInt(id, 10) : null;
});

const isEditMode = computed(() => !!taskId.value);

const isSubmitting = ref(false);
const submitError = ref<string | null>(null);
const initialData = ref<any>(null);
const projects = ref<any[]>([]);
const users = ref<any[]>([]);

const fetchProjects = async () => {
  try {
    await projectStore.fetchProjects({ page: 0, size: 100 });
    projects.value = projectStore.projects;
  } catch (err) {
    console.error('Failed to fetch projects:', err);
  }
};

const fetchUsers = async () => {
  try {
    await userStore.fetchUsers({ active: true, page: 0, size: 100 });
    users.value = userStore.users;
  } catch (err) {
    console.error('Failed to fetch users:', err);
  }
};

const fetchTask = async () => {
  if (taskId.value) {
    try {
      const task = await taskStore.fetchTaskById(taskId.value);
      initialData.value = {
        name: task.name,
        description: task.description,
        estimatedHours: task.estimatedHours,
        projectId: task.projectId,
        assigneeId: task.assigneeId,
        status: task.status,
      };
    } catch (err) {
      console.error('Failed to fetch task:', err);
      alert('無法加載任務資料');
      router.push('/tasks');
    }
  }
};

const handleFormSubmit = async (data: CreateTaskRequest | UpdateTaskRequest) => {
  isSubmitting.value = true;
  submitError.value = null;

  try {
    if (isEditMode.value && taskId.value) {
      await taskStore.updateTask(taskId.value, data as UpdateTaskRequest);
      alert('任務已更新');
    } else {
      await taskStore.createTask(data as CreateTaskRequest);
      alert('任務已建立');
    }
    router.push('/tasks');
  } catch (err: any) {
    submitError.value = err.message || '提交失敗，請稍後重試';
    console.error('Form submission error:', err);
  } finally {
    isSubmitting.value = false;
  }
};

const navigateBack = () => {
  router.push('/tasks');
};

onMounted(async () => {
  await Promise.all([fetchProjects(), fetchUsers()]);
  if (isEditMode.value) {
    await fetchTask();
  }
});
</script>

<style scoped>
.task-form-view {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
}

.form-header {
  margin-bottom: 2rem;
}

.form-header h1 {
  font-size: 1.75rem;
  color: #333;
  margin: 0;
}

@media (max-width: 768px) {
  .task-form-view {
    padding: 1rem;
  }

  .form-header h1 {
    font-size: 1.5rem;
  }
}
</style>
