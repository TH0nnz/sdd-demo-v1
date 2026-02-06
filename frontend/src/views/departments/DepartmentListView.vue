<template>
  <div class="department-list-view">
    <div class="view-header">
      <h1>部門管理</h1>
      <button v-if="canCreateDepartment" @click="openCreateForm" class="btn-primary">
        + 新增部門
      </button>
    </div>

    <div class="departments-content">
      <LoadingSpinner v-if="loading" />

      <div v-else-if="departments.length > 0" class="departments-grid">
        <div v-for="dept in departments" :key="dept.id" class="department-card">
          <div class="department-header">
            <h3>{{ dept.name }}</h3>
            <span class="member-count">{{ dept.memberCount }} 人</span>
          </div>

          <div class="department-info">
            <div class="info-item">
              <span class="label">部門代碼</span>
              <span class="value">{{ dept.code }}</span>
            </div>
            <div class="info-item">
              <span class="label">主管</span>
              <span class="value">{{ dept.headName }}</span>
            </div>
            <div class="info-item">
              <span class="label">描述</span>
              <span class="value">{{ dept.description }}</span>
            </div>
          </div>

          <div class="department-actions">
            <button @click="viewMembers(dept.id)" class="btn-secondary">查看成員</button>
            <button v-if="canEditDepartment" @click="editDepartment(dept)" class="btn-secondary">
              編輯
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>暫無部門資料</p>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ editingDept ? '編輯部門' : '新增部門' }}</h2>
          <button @click="closeModal" class="btn-close">✕</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>部門名稱 *</label>
            <input
              v-model="form.name"
              type="text"
              placeholder="輸入部門名稱"
              @keyup.enter="saveDepartment"
            />
          </div>

          <div class="form-group">
            <label>部門代碼 *</label>
            <input
              v-model="form.code"
              type="text"
              placeholder="輸入部門代碼"
              @keyup.enter="saveDepartment"
            />
          </div>

          <div class="form-group">
            <label>描述</label>
            <textarea
              v-model="form.description"
              placeholder="輸入部門描述"
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeModal" class="btn-secondary">取消</button>
          <button @click="saveDepartment" class="btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { UserRole } from '@/types/auth'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const authStore = useAuthStore()

const departments = ref<any[]>([])
const loading = ref(true)
const showModal = ref(false)
const editingDept = ref<any>(null)
const saving = ref(false)

const form = ref({
  name: '',
  code: '',
  description: '',
})

const canCreateDepartment = computed(
  () => authStore.user?.role === UserRole.HR || authStore.user?.role === UserRole.MANAGER
)

const canEditDepartment = computed(() => authStore.user?.role === UserRole.HR)

const openCreateForm = () => {
  editingDept.value = null
  form.value = { name: '', code: '', description: '' }
  showModal.value = true
}

const editDepartment = (dept: any) => {
  editingDept.value = dept
  form.value = {
    name: dept.name,
    code: dept.code,
    description: dept.description,
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingDept.value = null
  form.value = { name: '', code: '', description: '' }
}

const saveDepartment = async () => {
  if (!form.value.name || !form.value.code) {
    console.error('部門名稱和代碼為必填')
    return
  }

  saving.value = true
  try {
    // In a real app, this would call the API
    if (editingDept.value) {
      const index = departments.value.findIndex((d) => d.id === editingDept.value.id)
      if (index >= 0) {
        departments.value[index] = { ...editingDept.value, ...form.value }
      }
    } else {
      departments.value.push({
        id: Date.now().toString(),
        ...form.value,
        memberCount: 0,
        headName: '-',
      })
    }
    closeModal()
  } finally {
    saving.value = false
  }
}

const viewMembers = (departmentId: string) => {
  // Navigate to members view (in real app)
  console.log('View members of department:', departmentId)
}

onMounted(() => {
  // Load departments
  setTimeout(() => {
    departments.value = [
      {
        id: '1',
        name: '工程部',
        code: 'ENG',
        description: '負責軟體開發和系統維護',
        memberCount: 12,
        headName: '王主任',
      },
      {
        id: '2',
        name: '產品部',
        code: 'PRD',
        description: '負責產品規劃和設計',
        memberCount: 8,
        headName: '李主任',
      },
      {
        id: '3',
        name: '運營部',
        code: 'OPS',
        description: '負責系統運營和支持',
        memberCount: 5,
        headName: '張主任',
      },
    ]
    loading.value = false
  }, 500)
})
</script>

<style scoped lang="scss">
.department-list-view {
  padding: 2rem;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;

  h1 {
    font-size: 2rem;
    color: #333;
    margin: 0;
  }
}

.departments-content {
  min-height: 400px;
}

.departments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.department-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }
}

.department-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;

  h3 {
    margin: 0;
    color: #333;
    font-size: 1.2rem;
  }

  .member-count {
    background: #e3f2fd;
    color: #1976d2;
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 500;
  }
}

.department-info {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  font-size: 0.9rem;

  .label {
    color: #999;
    font-weight: 500;
  }

  .value {
    color: #333;
  }
}

.department-actions {
  display: flex;
  gap: 0.75rem;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  padding: 0.6rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #1976d2;
  color: white;

  &:hover {
    background: #1565c0;
  }
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;

  &:hover {
    background: #e0e0e0;
  }
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #999;
}

// Modal Styles
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;

  h2 {
    margin: 0;
    color: #333;
  }

  .btn-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #999;
  }
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;

  label {
    display: block;
    margin-bottom: 0.5rem;
    color: #333;
    font-weight: 500;
  }

  input,
  textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    font-family: inherit;

    &:focus {
      outline: none;
      border-color: #1976d2;
    }
  }
}

.modal-footer {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #f0f0f0;

  button {
    flex: 1;
    padding: 0.75rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 500;
  }
}

@media (max-width: 768px) {
  .department-list-view {
    padding: 1rem;
  }

  .view-header {
    flex-direction: column;
    gap: 1rem;
  }

  .departments-grid {
    grid-template-columns: 1fr;
  }
}
</style>
