<template>
  <div class="calendar-view">
    <div class="page-header">
      <h1>工時日曆</h1>
      <div class="header-controls">
        <button @click="previousMonth" class="btn btn-small">← 上月</button>
        <span class="current-month">{{ currentMonthYear }}</span>
        <button @click="nextMonth" class="btn btn-small">下月 →</button>
      </div>
    </div>

    <!-- Month Summary -->
    <div class="month-summary">
      <div class="summary-card">
        <span class="label">本月工時</span>
        <span class="value">{{ monthlyHours.toFixed(1) }}</span>
        <span class="unit">小時</span>
      </div>
      <div class="summary-card">
        <span class="label">工作日數</span>
        <span class="value">{{ workDays }}</span>
        <span class="unit">天</span>
      </div>
      <div class="summary-card">
        <span class="label">平均每天</span>
        <span class="value">{{ averageHours.toFixed(1) }}</span>
        <span class="unit">小時</span>
      </div>
    </div>

    <!-- Calendar Grid -->
    <div class="calendar-container">
      <div class="calendar">
        <!-- Day of week headers -->
        <div class="calendar-header">
          <div v-for="day in dayOfWeekHeaders" :key="day" class="day-header">
            {{ day }}
          </div>
        </div>

        <!-- Calendar days -->
        <div class="calendar-body">
          <div
            v-for="day in calendarDays"
            :key="day.date"
            :class="['calendar-day', day.classes]"
          >
            <div class="day-number">{{ day.dayOfMonth }}</div>
            <div v-if="day.timesheets.length > 0" class="timesheets">
              <div
                v-for="ts in day.timesheets"
                :key="ts.id"
                class="timesheet-dot"
                :title="`${ts.taskName}: ${ts.calculatedHours}h`"
              >
                {{ ts.calculatedHours.toFixed(1) }}h
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Day Detail Modal -->
    <div v-if="selectedDay" class="modal-overlay" @click="selectedDay = null">
      <div class="modal-content" @click.stop>
        <button class="modal-close" @click="selectedDay = null">×</button>
        <h2>{{ formatFullDate(selectedDay.date) }}</h2>

        <div v-if="selectedDay.timesheets.length > 0" class="day-timesheets">
          <div
            v-for="ts in selectedDay.timesheets"
            :key="ts.id"
            class="timesheet-card"
          >
            <div class="timesheet-header">
              <span class="task-name">{{ ts.taskName }}</span>
              <span class="hours">{{ ts.calculatedHours.toFixed(1) }}h</span>
            </div>
            <div class="timesheet-details">
              <div>{{ ts.startTime }} ~ {{ ts.endTime }}</div>
              <div v-if="ts.lunchDeducted" class="lunch-note">
                (包含午休扣除 1 小時)
              </div>
            </div>
            <div class="timesheet-actions">
              <button @click="editTimesheet(ts.id)" class="btn-small btn-edit">
                編輯
              </button>
              <button @click="deleteTimesheet(ts.id)" class="btn-small btn-delete">
                刪除
              </button>
            </div>
          </div>
        </div>
        <div v-else class="no-timesheets">
          此日期無工時記錄
          <button @click="addTimesheet(selectedDay.date)" class="btn btn-small">
            + 新增
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useTimesheetStore } from '../../stores/timesheet';
import type { TimesheetResponse } from '../../types/timesheet';

const router = useRouter();
const timesheetStore = useTimesheetStore();

const currentMonth = ref(new Date());
const selectedDay = ref<{
  date: string;
  timesheets: TimesheetResponse[];
} | null>(null);

const dayOfWeekHeaders = ['日', '一', '二', '三', '四', '五', '六'];

const currentMonthYear = computed(() => {
  return currentMonth.value.toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: 'long',
  });
});

const monthlyHours = computed(() => {
  return timesheetStore.timesheets
    .filter((ts) => {
      const date = new Date(ts.workDate);
      return (
        date.getFullYear() === currentMonth.value.getFullYear() &&
        date.getMonth() === currentMonth.value.getMonth()
      );
    })
    .reduce((sum, ts) => sum + ts.calculatedHours, 0);
});

const workDays = computed(() => {
  return timesheetStore.timesheets
    .filter((ts) => {
      const date = new Date(ts.workDate);
      const day = date.getDay();
      return (
        date.getFullYear() === currentMonth.value.getFullYear() &&
        date.getMonth() === currentMonth.value.getMonth() &&
        day !== 0 &&
        day !== 6
      );
    })
    .filter((ts, index, self) => self.findIndex((t) => t.workDate === ts.workDate) === index).length;
});

const averageHours = computed(() => {
  return workDays.value > 0 ? monthlyHours.value / workDays.value : 0;
});

const calendarDays = computed(() => {
  const year = currentMonth.value.getFullYear();
  const month = currentMonth.value.getMonth();
  
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();
  const startingDayOfWeek = firstDay.getDay();

  const days = [];

  // Previous month's days
  for (let i = startingDayOfWeek - 1; i >= 0; i--) {
    const date = new Date(year, month, -i);
    days.push({
      date: date.toISOString().split('T')[0],
      dayOfMonth: date.getDate(),
      classes: 'other-month',
      timesheets: [],
    });
  }

  // Current month's days
  for (let i = 1; i <= daysInMonth; i++) {
    const date = new Date(year, month, i);
    const dateStr = date.toISOString().split('T')[0];
    const isToday = dateStr === new Date().toISOString().split('T')[0];
    const dayOfWeek = date.getDay();
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;

    const timesheets = timesheetStore.timesheets.filter(
      (ts) => ts.workDate === dateStr
    );

    days.push({
      date: dateStr,
      dayOfMonth: i,
      classes: [
        isToday && 'today',
        isWeekend && 'weekend',
        timesheets.length > 0 && 'has-timesheets',
      ]
        .filter(Boolean)
        .join(' '),
      timesheets,
    });
  }

  // Next month's days
  const remainingDays = 42 - days.length;
  for (let i = 1; i <= remainingDays; i++) {
    const date = new Date(year, month + 1, i);
    days.push({
      date: date.toISOString().split('T')[0],
      dayOfMonth: i,
      classes: 'other-month',
      timesheets: [],
    });
  }

  return days;
});

const formatFullDate = (dateStr: string): string => {
  const date = new Date(dateStr + 'T00:00:00');
  return date.toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  });
};

const previousMonth = () => {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() - 1,
    1
  );
};

const nextMonth = () => {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() + 1,
    1
  );
};

const editTimesheet = (id: number) => {
  selectedDay.value = null;
  router.push({
    name: 'TimesheetForm',
    query: { id },
  });
};

const deleteTimesheet = async (id: number) => {
  if (confirm('確認刪除此工時記錄？')) {
    try {
      await timesheetStore.deleteTimesheet(id);
      // Refresh selected day
      if (selectedDay.value) {
        const updatedTimesheets = selectedDay.value.timesheets.filter(
          (ts) => ts.id !== id
        );
        if (updatedTimesheets.length === 0) {
          selectedDay.value = null;
        } else {
          selectedDay.value.timesheets = updatedTimesheets;
        }
      }
    } catch (err) {
      alert('刪除失敗');
    }
  }
};

const addTimesheet = (date: string) => {
  router.push({
    name: 'TimesheetForm',
    query: { date },
  });
};

onMounted(async () => {
  try {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)
      .toISOString()
      .split('T')[0];
    const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0)
      .toISOString()
      .split('T')[0];

    await timesheetStore.fetchTimesheets({
      startDate: firstDay,
      endDate: lastDay,
    });
  } catch (err) {
    console.error('Failed to fetch timesheets:', err);
  }
});
</script>

<style scoped>
.calendar-view {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #333;
}

.header-controls {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.current-month {
  font-size: 1.1rem;
  font-weight: 600;
  color: #666;
  min-width: 150px;
  text-align: center;
}

.btn {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.btn-small {
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
}

.month-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.summary-card {
  background: white;
  padding: 1.5rem;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.summary-card .label {
  display: block;
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.summary-card .value {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  color: #4CAF50;
}

.summary-card .unit {
  display: block;
  color: #999;
  font-size: 0.85rem;
}

.calendar-container {
  background: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.calendar {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-header {
  display: contents;
}

.day-header {
  background-color: #f5f5f5;
  border-bottom: 2px solid #ddd;
  padding: 1rem;
  text-align: center;
  font-weight: 600;
  color: #333;
}

.calendar-body {
  display: contents;
}

.calendar-day {
  border: 1px solid #eee;
  padding: 1rem;
  min-height: 100px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.calendar-day:hover {
  background-color: #f9f9f9;
}

.calendar-day.today {
  background-color: #e8f5e9;
  border: 2px solid #4CAF50;
}

.calendar-day.weekend {
  background-color: #f5f5f5;
}

.calendar-day.other-month {
  background-color: #fafafa;
  color: #ccc;
}

.calendar-day.has-timesheets {
  background-color: #fff9c4;
}

.day-number {
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.timesheets {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.8rem;
}

.timesheet-dot {
  background-color: #4CAF50;
  color: white;
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 6px;
  padding: 2rem;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
}

.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: #999;
}

.modal-content h2 {
  margin-top: 0;
  color: #333;
}

.day-timesheets {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.timesheet-card {
  background: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 1rem;
}

.timesheet-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.task-name {
  font-weight: 600;
  color: #333;
}

.hours {
  font-weight: 600;
  color: #4CAF50;
}

.timesheet-details {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.75rem;
}

.lunch-note {
  font-size: 0.8rem;
  color: #f57c00;
}

.timesheet-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-edit {
  color: #2196F3;
  border-color: #2196F3;
}

.btn-delete {
  color: #f44336;
  border-color: #f44336;
}

.no-timesheets {
  text-align: center;
  padding: 2rem;
  color: #999;
}
</style>
