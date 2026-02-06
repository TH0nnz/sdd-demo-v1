<template>
  <div class="work-hours-calculator">
    <div class="calculator-card">
      <h3 class="calculator-title">工時計算預覽</h3>
      
      <div class="time-inputs">
        <div class="time-field">
          <label for="start-time">開始時間</label>
          <input
            id="start-time"
            v-model="calculator.startTime"
            type="time"
            class="time-input"
            @change="handleTimeChange"
          />
        </div>

        <div class="time-separator">→</div>

        <div class="time-field">
          <label for="end-time">結束時間</label>
          <input
            id="end-time"
            v-model="calculator.endTime"
            type="time"
            class="time-input"
            @change="handleTimeChange"
          />
        </div>
      </div>

      <div v-if="!calculator.isValidTimeRange()" class="error-message">
        請輸入有效的時間範圍（結束時間必須晚於開始時間）
      </div>

      <div v-if="calculator.error" class="error-message">
        {{ calculator.error }}
      </div>

      <div class="calculation-result">
        <div class="result-item">
          <span class="label">計算工時：</span>
          <span class="value">{{ calculatedHours.toFixed(1) }} 小時</span>
        </div>

        <div v-if="calculator.localCalculation.lunchDeducted" class="result-item lunch-alert">
          <span class="label">⚠️ 午休扣除：</span>
          <span class="value">1 小時</span>
        </div>

        <div class="result-message">{{ calculator.message }}</div>
      </div>

      <div v-if="preview && preview.valid === false" class="warning-box">
        <p>⚠️ {{ preview.validationMessage }}</p>
      </div>

      <div v-if="preview && preview.valid" class="success-box">
        <p>✓ 可以提交</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useWorkHoursCalculator } from '../../hooks/useWorkHoursCalculator';

defineProps<{
  taskId?: number;
  workDate?: string;
  fetchPreview?: boolean;
}>();

const calculator = useWorkHoursCalculator();

const calculatedHours = computed(() => {
  return calculator.localCalculation.value.hours;
});

const preview = computed(() => {
  return calculator.preview.value;
});

const handleTimeChange = async () => {
  if (
    calculator.isValidTimeRange() &&
    calculator.fetchPreview // Only fetch if flag is true
  ) {
    // Could fetch from API here, but local calculation is sufficient for preview
    // await calculator.fetchPreview(taskId, workDate);
  }
};
</script>

<style scoped>
.work-hours-calculator {
  padding: 1rem;
}

.calculator-card {
  background: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.calculator-title {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.time-inputs {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.time-field {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.time-field label {
  font-size: 0.9rem;
  margin-bottom: 0.4rem;
  color: #666;
  font-weight: 500;
}

.time-input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  font-family: monospace;
}

.time-input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 4px rgba(76, 175, 80, 0.3);
}

.time-separator {
  font-size: 1.5rem;
  color: #999;
  padding-bottom: 0.5rem;
}

.error-message {
  color: #d32f2f;
  font-size: 0.85rem;
  margin-bottom: 1rem;
  padding: 0.5rem;
  background-color: #ffebee;
  border-radius: 4px;
}

.calculation-result {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.result-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  font-size: 0.95rem;
}

.result-item .label {
  font-weight: 500;
  color: #666;
}

.result-item .value {
  font-weight: 600;
  color: #333;
  font-family: monospace;
}

.result-item.lunch-alert {
  color: #f57c00;
  background-color: #fff3e0;
  padding: 0.75rem;
  border-radius: 4px;
  margin-top: 0.5rem;
}

.result-message {
  margin-top: 0.75rem;
  padding: 0.75rem;
  background-color: #e3f2fd;
  border-left: 3px solid #2196F3;
  color: #1565c0;
  font-size: 0.9rem;
  border-radius: 4px;
}

.warning-box {
  background-color: #fff3e0;
  border: 1px solid #ffe0b2;
  border-radius: 4px;
  padding: 0.75rem;
  color: #e65100;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.warning-box p {
  margin: 0;
}

.success-box {
  background-color: #e8f5e9;
  border: 1px solid #c8e6c9;
  border-radius: 4px;
  padding: 0.75rem;
  color: #2e7d32;
  font-size: 0.9rem;
}

.success-box p {
  margin: 0;
}
</style>
