<template>
  <div class="report-filter-panel">
    <div class="filter-section">
      <h3 class="filter-title">Filter Report</h3>
      
      <!-- Date Range -->
      <div class="filter-group">
        <label for="start-date">Start Date</label>
        <input 
          id="start-date"
          v-model="localFilter.startDate"
          type="date"
          @change="emitFilterChange"
        />
      </div>

      <div class="filter-group">
        <label for="end-date">End Date</label>
        <input 
          id="end-date"
          v-model="localFilter.endDate"
          type="date"
          @change="emitFilterChange"
        />
      </div>

      <!-- User Filter -->
      <div class="filter-group">
        <label for="user-select">Employee</label>
        <select 
          id="user-select"
          v-model.number="localFilter.userId"
          @change="emitFilterChange"
        >
          <option :value="null">All Employees</option>
          <option 
            v-for="user in availableUsers" 
            :key="user.id"
            :value="user.id"
          >
            {{ user.name }}
          </option>
        </select>
      </div>

      <!-- Project Filter -->
      <div class="filter-group">
        <label for="project-select">Project</label>
        <select 
          id="project-select"
          v-model.number="localFilter.projectId"
          @change="emitFilterChange"
        >
          <option :value="null">All Projects</option>
          <option 
            v-for="project in availableProjects" 
            :key="project.id"
            :value="project.id"
          >
            {{ project.name }}
          </option>
        </select>
      </div>

      <!-- Action Buttons -->
      <div class="filter-actions">
        <button 
          class="btn btn-primary"
          @click="emitFilterChange"
          :disabled="isLoading"
        >
          Apply Filters
        </button>
        <button 
          class="btn btn-secondary"
          @click="handleClearFilters"
        >
          Clear
        </button>
        <button 
          class="btn btn-success"
          @click="emitExport"
          :disabled="isLoading"
        >
          Export CSV
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { ReportFilterState } from '../types/report';
import { useUserStore } from '../stores/user';
import { useProjectStore } from '../stores/project';

interface Props {
  filterState: ReportFilterState;
  isLoading?: boolean;
  availableUsers?: Array<{ id: number; name: string }>;
  availableProjects?: Array<{ id: number; name: string }>;
}

interface Emits {
  (e: 'filter-change', value: any): void;
  (e: 'export', value: any): void;
  (e: 'clear-filters'): void;
}

const props = withDefaults(defineProps<Props>(), {
  isLoading: false,
  availableUsers: () => [],
  availableProjects: () => [],
});

const emit = defineEmits<Emits>();

const userStore = useUserStore();
const projectStore = useProjectStore();

const localFilter = ref<ReportFilterState>({ ...props.filterState });

watch(() => props.filterState, (newValue) => {
  localFilter.value = { ...newValue };
}, { deep: true });

const emitFilterChange = () => {
  emit('filter-change', {
    startDate: localFilter.value.startDate,
    endDate: localFilter.value.endDate,
    userId: localFilter.value.userId,
    projectId: localFilter.value.projectId,
  });
};

const handleClearFilters = () => {
  localFilter.value.userId = null;
  localFilter.value.projectId = null;
  emit('clear-filters');
};

const emitExport = () => {
  emit('export', {
    startDate: localFilter.value.startDate,
    endDate: localFilter.value.endDate,
    userId: localFilter.value.userId,
    projectId: localFilter.value.projectId,
  });
};
</script>

<style scoped>
.report-filter-panel {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.filter-group input,
.filter-group select {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.filter-group input:focus,
.filter-group select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.filter-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #545b62;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background-color: #1e7e34;
}
</style>
