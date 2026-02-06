<template>
  <div class="project-form-view">
    <div class="page-header">
      <h1>{{ isEdit ? '編輯專案' : '新增專案' }}</h1>
    </div>

    <ProjectForm
      :project="currentProject"
      :loading="projectStore.loading"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useProjectStore } from '../../stores/project';
import ProjectForm from '../../components/projects/ProjectForm.vue';
import type { CreateProjectRequest, UpdateProjectRequest } from '../../types/project';

const router = useRouter();
const route = useRoute();
const projectStore = useProjectStore();

const currentProject = ref(projectStore.currentProject);
const isEdit = ref(false);

onMounted(async () => {
  const projectId = route.params.id;
  if (projectId) {
    isEdit.value = true;
    try {
      const project = await projectStore.getProjectById(Number(projectId));
      currentProject.value = project;
    } catch (err) {
      console.error('Failed to load project:', err);
      alert('無法載入專案信息');
      router.push('/projects');
    }
  }
});

const handleSubmit = async (data: CreateProjectRequest | UpdateProjectRequest) => {
  try {
    if (isEdit.value && route.params.id) {
      await projectStore.updateProject(Number(route.params.id), data as UpdateProjectRequest);
      alert('專案已成功更新');
    } else {
      await projectStore.createProject(data as CreateProjectRequest);
      alert('專案已成功建立');
    }
    router.push('/projects');
  } catch (err) {
    console.error('Failed to submit form:', err);
    alert('操作失敗，請重試');
  }
};

const handleCancel = () => {
  router.push('/projects');
};
</script>

<style scoped>
.project-form-view {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 1.75rem;
  color: #333;
  margin: 0;
}
</style>
