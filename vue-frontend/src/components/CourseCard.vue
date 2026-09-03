<template>
  <router-link :to="`/courses/${course.id}`" class="course-card">
    <!-- 썸네일 -->
    <div class="card-thumb" :class="thumbBg">
      <img v-if="thumbSrc" :src="thumbSrc" :alt="course.title" class="thumb-img" />
      <div v-else class="thumb-placeholder">{{ course.category?.charAt(0) }}</div>
    </div>

    <!-- 내용 -->
    <div class="card-body">
      <span class="badge" :class="badgeClass">{{ course.category }}</span>
      <h3 class="card-title">{{ course.title }}</h3>
      <div class="card-meta">
        <span class="price">₩{{ Number(course.price).toLocaleString() }}</span>
      </div>
      <div class="card-footer">
        <span class="enrolled">누적 이용 {{ displayEnrollmentCount }}건</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  course: { type: Object, required: true }
})

const categoryConfig = {
  '건강·의료': { bg: 'thumb-teal', badge: 'badge-teal', thumb: 'spring_boot' },
  '자기계발': { bg: 'thumb-purple', badge: 'badge-purple', thumb: 'python' },
  '여가·휴식': { bg: 'thumb-blue', badge: 'badge-blue', thumb: 'vue_js' },
  '심리·마음건강': { bg: 'thumb-pink', badge: 'badge-pink', thumb: 'generative_ai' },
  '가족·생활': { bg: 'thumb-amber', badge: 'badge-amber', thumb: 'vue_js' },
  '재정·상담': { bg: 'thumb-purple', badge: 'badge-purple', thumb: 'python' },
  '문화': { bg: 'thumb-blue', badge: 'badge-blue', thumb: 'vue_js' },
  '기타': { bg: 'thumb-gray', badge: 'badge-gray', thumb: 'docker' },
}

const config = computed(() => categoryConfig[props.course.category] || { bg: 'thumb-gray', badge: 'badge-gray' })
const thumbBg = computed(() => config.value.bg)
const badgeClass = computed(() => config.value.badge)
const displayEnrollmentCount = computed(() => {
  const value = Number(props.course.enrollmentCount ?? props.course.enrollment_count ?? 0)
  return Number.isNaN(value) ? '0' : value.toLocaleString()
})

// 썸네일 이미지 동적 import
const thumbSrc = computed(() => {
  const key = props.course.thumbnail || config.value.thumb
  if (!key) return null
  try {
    return new URL(`../assets/images/courses/${key}.png`, import.meta.url).href
  } catch {
    return null
  }
})
</script>

<style scoped>
.course-card {
  display: flex;
  flex-direction: column;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: var(--transition);
  cursor: pointer;
}
.course-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-border-hover);
}
.card-thumb {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.thumb-teal   { background: var(--color-success-light); }
.thumb-blue   { background: var(--color-primary-light); }
.thumb-amber  { background: var(--color-warning-light); }
.thumb-purple { background: var(--color-bg-tertiary); }
.thumb-pink   { background: var(--color-bg-tertiary); }
.thumb-gray   { background: var(--color-bg-tertiary); }
.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 16px;
}
.thumb-placeholder {
  font-size: 36px;
  font-weight: 700;
  color: var(--color-text-muted);
}
.card-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
}
.card-footer {
  margin-top: 2px;
}
.enrolled {
  font-size: 11px;
  color: var(--color-text-muted);
}
</style>
