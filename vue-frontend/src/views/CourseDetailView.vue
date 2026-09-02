<template>
  <div class="page-wrapper">
    <AppHeader />

    <div class="detail-layout" v-if="course">
      <div class="detail-hero">
        <div class="detail-hero-inner">
          <!-- 좌측 상세 정보 -->
          <div class="detail-info fade-in-up">
            <span class="badge" :class="badgeClass">{{ displayCategory }}</span>
            <h1 class="detail-title">{{ course.title }}</h1>
            <p class="detail-desc">
              {{ course.description || '실무 전문가가 직접 설계한 커리큘럼으로 체계적으로 학습하세요.' }}
            </p>

            <div class="detail-meta">
              <span>강사: {{ displayInstructorName }}</span>
              <span>수강생: {{ displayEnrollmentCount }}명</span>
            </div>
          </div>

          <!-- 우측 결제/수강 카드 -->
          <div class="enroll-card fade-in">
            <div class="enroll-thumb" :class="thumbBg">
              <img v-if="thumbSrc" :src="thumbSrc" :alt="course.title" />
            </div>

            <div class="enroll-body">
              <div class="enroll-price">₩{{ displayPrice }}</div>

              <button
                class="btn btn-primary btn-full"
                @click="handlePrimaryAction"
                :disabled="buttonDisabled"
                :class="{ 'btn-disabled': buttonDisabled }"
              >
                <span v-if="enrolling">처리 중...</span>
                <span v-else>{{ buttonLabel }}</span>
              </button>

              <div v-if="enrollError" class="error-msg">{{ enrollError }}</div>

              <p class="helper-text" v-if="helperText">
                {{ helperText }}
              </p>

              <ul class="enroll-info-list">
                <li>✅ 즉시 수강 가능</li>
                <li>✅ 평생 소장</li>
                <li>✅ 수료증 발급</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="loading" class="loading-center">
      <div class="spinner"></div>
    </div>

    <div v-else class="loading-center">
      <p class="empty-text">강의 정보를 불러오지 못했습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { useCourseStore } from '@/store/course.js'
import { enrollmentApi } from '@/api/enrollment.js'
import { useAuthStore } from '@/store/auth.js'

const route = useRoute()
const router = useRouter()
const courseStore = useCourseStore()
const auth = useAuthStore()

const enrolling = ref(false)
const enrollError = ref('')
const enrollmentStatus = ref('NONE') // NONE | PENDING | ACTIVE

const course = computed(() => courseStore.selectedCourse)
const loading = computed(() => courseStore.loading)
const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const categoryConfig = {
  '백엔드': { badge: 'badge-teal', bg: 'thumb-teal', thumb: 'spring_boot' },
  '프론트엔드': { badge: 'badge-teal', bg: 'thumb-teal', thumb: 'vue_js' },
  'DevOps': { badge: 'badge-blue', bg: 'thumb-blue', thumb: 'kubernetes' },
  '데이터': { badge: 'badge-purple', bg: 'thumb-purple', thumb: 'python' },
  'AI': { badge: 'badge-pink', bg: 'thumb-pink', thumb: 'generative_ai' },
}

const config = computed(() => categoryConfig[course.value?.category] || {})
const badgeClass = computed(() => config.value.badge || 'badge-gray')
const thumbBg = computed(() => config.value.bg || 'thumb-gray')

const displayCategory = computed(() => course.value?.category || '-')

const displayInstructorName = computed(() => {
  return (
    course.value?.instructorName ||
    course.value?.teacherName ||
    course.value?.instructor?.name ||
    course.value?.instructor_name ||
    course.value?.ownerName ||
    '강사 정보 없음'
  )
})

const displayEnrollmentCount = computed(() => {
  const value = Number(
    course.value?.enrollmentCount ??
    course.value?.enrollment_count ??
    0
  )
  return Number.isNaN(value) ? 0 : value.toLocaleString()
})

const displayPrice = computed(() => {
  const value = Number(course.value?.price ?? 0)
  return Number.isNaN(value) ? '0' : value.toLocaleString()
})

const thumbSrc = computed(() => {
  const key = course.value?.thumbnail || config.value.thumb
  if (!key) return null

  try {
    return new URL(`../assets/images/courses/${key}.png`, import.meta.url).href
  } catch {
    return null
  }
})

const buttonLabel = computed(() => {
  if (isInstructor.value) return '강사 계정은 신청 불가'
  if (enrollmentStatus.value === 'ACTIVE') return '내 수강 목록으로 이동'
  if (enrollmentStatus.value === 'PENDING') return '신청 완료 · 결제 처리 중'
  return '결제하고 수강하기'
})

const buttonDisabled = computed(() => {
  if (enrolling.value) return true
  if (isInstructor.value) return true
  if (enrollmentStatus.value === 'PENDING') return true
  return false
})

const helperText = computed(() => {
  if (isInstructor.value) {
    return '강사 계정은 본인 강의를 수강 신청할 수 없습니다.'
  }

  if (enrollmentStatus.value === 'ACTIVE') {
    return '이미 수강 중인 강의입니다. 내 수강 목록에서 바로 이어서 학습할 수 있습니다.'
  }

  if (enrollmentStatus.value === 'PENDING') {
    return '수강 신청이 접수되었습니다. 결제/처리 상태가 반영되면 내 수강 목록에서 확인할 수 있습니다.'
  }

  return '결제를 진행하면 수강 신청이 함께 처리됩니다.'
})

async function loadEnrollmentStatus() {
  if (!auth.user?.id || !course.value?.id || isInstructor.value) {
    enrollmentStatus.value = 'NONE'
    return
  }

  try {
    const res = await enrollmentApi.getMyEnrollments()
    console.log('[CourseDetail] my enrollments response =', res.data)

    const enrollments = Array.isArray(res.data?.data)
      ? res.data.data
      : Array.isArray(res.data)
        ? res.data
        : []

    const matched = enrollments.find(item => Number(item.courseId) === Number(course.value.id))

    if (!matched) {
      enrollmentStatus.value = 'NONE'
      return
    }

    enrollmentStatus.value = matched.status === 'ACTIVE' ? 'ACTIVE' : 'PENDING'
  } catch (e) {
    console.error('[CourseDetail] failed to load enrollment status:', e)
    enrollmentStatus.value = 'NONE'
  }
}

async function handlePrimaryAction() {
  enrollError.value = ''

  if (!course.value?.id) {
    enrollError.value = '강의 정보가 올바르지 않습니다.'
    return
  }

  if (isInstructor.value) {
    enrollError.value = '강사 계정은 본인 강의를 수강 신청할 수 없습니다.'
    return
  }

  if (enrollmentStatus.value === 'ACTIVE') {
    router.push('/enrollments')
    return
  }

  if (enrollmentStatus.value === 'PENDING') {
    return
  }

  enrolling.value = true

  try {
    await enrollmentApi.enroll(course.value.id)
    enrollmentStatus.value = 'PENDING'
  } catch (e) {
    console.error('[CourseDetail] enroll failed:', e)
    enrollError.value = e.response?.data?.message || '결제/수강 신청에 실패했습니다.'
  } finally {
    enrolling.value = false
  }
}

onMounted(async () => {
  await courseStore.fetchCourse(route.params.id)
  console.log('[CourseDetail] selectedCourse =', courseStore.selectedCourse)
  await loadEnrollmentStatus()
})

watch(
  () => courseStore.selectedCourse,
  async (value) => {
    console.log('[CourseDetail] selectedCourse changed =', value)
    if (value?.id) {
      await loadEnrollmentStatus()
    }
  },
  { deep: true }
)
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

.detail-hero {
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4fd 100%);
  border-bottom: 1px solid var(--color-border);
  padding: 48px 0;
}

.detail-hero-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 48px;
  align-items: start;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-title {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.3;
}

.detail-desc {
  font-size: 15px;
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.detail-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
  flex-wrap: wrap;
}

.enroll-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.enroll-thumb {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.enroll-thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 20px;
}

.thumb-teal { background: #E1F5EE; }
.thumb-blue { background: #E6F1FB; }
.thumb-purple { background: #EEEDFE; }
.thumb-pink { background: #FBEAF0; }
.thumb-gray { background: #F1EFE8; }

.enroll-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.enroll-price {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-primary);
}

.btn-full {
  width: 100%;
  padding: 13px;
  font-size: 15px;
  justify-content: center;
}

.btn-disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.enroll-info-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.enroll-info-list li {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.error-msg {
  font-size: 13px;
  color: #dc2626;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: var(--radius-sm);
}

.helper-text {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.empty-text {
  font-size: 14px;
  color: var(--color-text-muted);
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: 100px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.badge-gray {
  background: #f3f4f6;
  color: #6b7280;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 900px) {
  .detail-hero-inner {
    grid-template-columns: 1fr;
  }
}
</style>