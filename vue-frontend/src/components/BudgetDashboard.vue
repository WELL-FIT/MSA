<template>
  <section class="budget-dashboard">
    <div class="section-head">
      <h3 class="section-title">복지 예산 대시보드</h3>
      <span class="section-subtitle">전체 복지 프로그램의 예산과 이용 현황을 확인할 수 있습니다.</span>
    </div>

    <p v-if="loading" class="empty-text">대시보드를 준비하고 있습니다...</p>

    <template v-else-if="courses.length">
      <div class="summary-cards budget-summary">
        <div class="summary-card">
          <div class="summary-label">총 예산</div>
          <div class="summary-value">{{ formatWon(totalBudget) }}</div>
        </div>
        <div class="summary-card">
          <div class="summary-label">총 이용자 수</div>
          <div class="summary-value">{{ totalUsers.toLocaleString() }}명</div>
        </div>
        <div class="summary-card">
          <div class="summary-label">복지 프로그램 수</div>
          <div class="summary-value">{{ courses.length }}</div>
        </div>
      </div>

      <div class="chart-grid">
        <!-- 유형별 예산 vs 이용률 -->
        <div class="chart-card">
          <div class="chart-card-head">
            <div>
              <h4 class="chart-title">유형별 예산 vs 이용률</h4>
              <p class="chart-caption">카테고리별 누적 예산(단가 × 이용자수)과 전체 이용자 중 비중입니다.</p>
            </div>
            <button class="table-toggle-btn" @click="showCategoryTable = !showCategoryTable">
              {{ showCategoryTable ? '차트로 보기' : '표로 보기' }}
            </button>
          </div>

          <div v-if="!showCategoryTable" class="dual-bar-panels">
            <div class="bar-panel">
              <div class="bar-panel-title">예산</div>
              <svg
                :viewBox="`0 0 320 ${categoryStats.length * ROW_H}`"
                class="bar-svg"
                role="img"
                aria-label="유형별 예산 막대 차트"
              >
                <g v-for="(row, i) in categoryStats" :key="row.key" :transform="`translate(0, ${i * ROW_H})`">
                  <rect
                    class="hit-rect"
                    x="0" y="0" width="320" :height="ROW_H"
                    tabindex="0"
                    @mouseenter="showTip($event, row, 'budget')"
                    @mouseleave="hideTip"
                    @focus="showTip($event, row, 'budget')"
                    @blur="hideTip"
                  />
                  <text x="86" :y="ROW_H / 2" class="row-label" text-anchor="end" dominant-baseline="middle">{{ row.label }}</text>
                  <rect
                    x="94" :y="(ROW_H - 20) / 2" :width="scaledWidth(row.budget, maxBudget, BAR_MAX_W)" height="20" rx="4"
                    :fill="row.color"
                    :class="{ 'row-dim': hovered && hovered.key !== row.key }"
                  />
                  <text
                    :x="94 + scaledWidth(row.budget, maxBudget, BAR_MAX_W) + 8" :y="ROW_H / 2"
                    class="value-label" dominant-baseline="middle"
                  >{{ formatWonShort(row.budget) }}</text>
                </g>
              </svg>
            </div>

            <div class="bar-panel">
              <div class="bar-panel-title">이용률</div>
              <svg
                :viewBox="`0 0 320 ${categoryStats.length * ROW_H}`"
                class="bar-svg"
                role="img"
                aria-label="유형별 이용률 막대 차트"
              >
                <g v-for="(row, i) in categoryStats" :key="row.key" :transform="`translate(0, ${i * ROW_H})`">
                  <rect
                    class="hit-rect"
                    x="0" y="0" width="320" :height="ROW_H"
                    tabindex="0"
                    @mouseenter="showTip($event, row, 'usage')"
                    @mouseleave="hideTip"
                    @focus="showTip($event, row, 'usage')"
                    @blur="hideTip"
                  />
                  <text x="86" :y="ROW_H / 2" class="row-label" text-anchor="end" dominant-baseline="middle">{{ row.label }}</text>
                  <rect
                    x="94" :y="(ROW_H - 20) / 2" :width="scaledWidth(row.usagePct, maxUsagePct, BAR_MAX_W)" height="20" rx="4"
                    :fill="row.color"
                    :class="{ 'row-dim': hovered && hovered.key !== row.key }"
                  />
                  <text
                    :x="94 + scaledWidth(row.usagePct, maxUsagePct, BAR_MAX_W) + 8" :y="ROW_H / 2"
                    class="value-label" dominant-baseline="middle"
                  >{{ row.usagePct.toFixed(1) }}%</text>
                </g>
              </svg>
            </div>
          </div>

          <table v-else class="data-table">
            <thead>
              <tr>
                <th>카테고리</th>
                <th>예산</th>
                <th>이용자 수</th>
                <th>이용률</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in categoryStats" :key="row.key">
                <td><span class="legend-dot" :style="{ background: row.color }"></span>{{ row.label }}</td>
                <td>{{ formatWon(row.budget) }}</td>
                <td>{{ row.users.toLocaleString() }}명</td>
                <td>{{ row.usagePct.toFixed(1) }}%</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 단가 vs 이용자 산점도 -->
        <div class="chart-card">
          <div class="chart-card-head">
            <div>
              <h4 class="chart-title">단가 vs 이용자 산점도</h4>
              <p class="chart-caption">프로그램별 단가와 이용자 수의 관계입니다.</p>
            </div>
            <button class="table-toggle-btn" @click="showScatterTable = !showScatterTable">
              {{ showScatterTable ? '차트로 보기' : '표로 보기' }}
            </button>
          </div>

          <svg v-if="!showScatterTable" viewBox="0 0 460 300" class="scatter-svg" role="img" aria-label="단가 대비 이용자 수 산점도">
            <line :x1="M.left" :y1="M.top" :x2="M.left" :y2="300 - M.bottom" class="axis-line" />
            <line :x1="M.left" :y1="300 - M.bottom" :x2="460 - M.right" :y2="300 - M.bottom" class="axis-line" />

            <g v-for="t in xTicks" :key="'x' + t">
              <line :x1="xScale(t)" :y1="300 - M.bottom" :x2="xScale(t)" :y2="300 - M.bottom + 4" class="tick-line" />
              <text :x="xScale(t)" :y="300 - M.bottom + 16" class="tick-label" text-anchor="middle">{{ formatWonShort(t) }}</text>
            </g>
            <g v-for="t in yTicks" :key="'y' + t">
              <line :x1="M.left - 4" :y1="yScale(t)" :x2="M.left" :y2="yScale(t)" class="tick-line" />
              <text :x="M.left - 8" :y="yScale(t)" class="tick-label" text-anchor="end" dominant-baseline="middle">{{ t.toLocaleString() }}</text>
            </g>

            <text :x="(M.left + 460 - M.right) / 2" y="298" class="axis-caption" text-anchor="middle">단가(원)</text>
            <text
              x="12" :y="(M.top + 300 - M.bottom) / 2" class="axis-caption" text-anchor="middle"
              :transform="`rotate(-90, 12, ${(M.top + 300 - M.bottom) / 2})`"
            >이용자 수(명)</text>

            <g v-for="pt in scatterPoints" :key="pt.id">
              <circle
                class="hit-circle" :cx="xScale(pt.price)" :cy="yScale(pt.users)" r="13"
                tabindex="0"
                @mouseenter="showScatterTip($event, pt)"
                @mouseleave="hideTip"
                @focus="showScatterTip($event, pt)"
                @blur="hideTip"
              />
              <circle
                :cx="xScale(pt.price)" :cy="yScale(pt.users)" r="5" class="scatter-dot"
                :class="{ 'row-dim': hoveredPoint && hoveredPoint.id !== pt.id }"
              />
            </g>
          </svg>

          <table v-else class="data-table">
            <thead>
              <tr>
                <th>프로그램</th>
                <th>카테고리</th>
                <th>단가</th>
                <th>이용자 수</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pt in scatterPoints" :key="pt.id">
                <td>{{ pt.title }}</td>
                <td>{{ pt.categoryLabel }}</td>
                <td>{{ formatWon(pt.price) }}</td>
                <td>{{ pt.users.toLocaleString() }}명</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <p v-else class="empty-text">집계할 복지 프로그램 데이터가 없습니다.</p>

    <div v-if="tooltip.visible" class="chart-tooltip" :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }">
      <div class="tooltip-value">{{ tooltip.value }}</div>
      <div class="tooltip-label">{{ tooltip.label }}</div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  courses: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

const ROW_H = 34
const BAR_MAX_W = 166
const M = { left: 50, right: 12, top: 10, bottom: 30 }

const CATEGORY_META = [
  { key: 'HEALTH', label: '건강·의료', color: '#2a78d6' },
  { key: 'SELF_DEVELOPMENT', label: '자기계발', color: '#eb6834' },
  { key: 'LEISURE', label: '여가·휴식', color: '#1baf7a' },
  { key: 'PSYCHOLOGICAL', label: '심리·마음건강', color: '#eda100' },
  { key: 'FAMILY', label: '가족·생활', color: '#e87ba4' },
  { key: 'FINANCIAL', label: '재정·상담', color: '#008300' },
  { key: 'CULTURE', label: '문화', color: '#4a3aa7' },
  { key: 'OTHER', label: '기타', color: '#e34948' }
]
const CATEGORY_LABEL = Object.fromEntries(CATEGORY_META.map(c => [c.key, c.label]))

const showCategoryTable = ref(false)
const showScatterTable = ref(false)
const hovered = ref(null)
const hoveredPoint = ref(null)
const tooltip = ref({ visible: false, x: 0, y: 0, value: '', label: '' })

function getPrice(course) {
  const value = Number(course.price ?? 0)
  return Number.isNaN(value) ? 0 : value
}

function getUsers(course) {
  const value = Number(course.enrollment_count ?? course.enrollmentCount ?? 0)
  return Number.isNaN(value) ? 0 : value
}

const totalUsers = computed(() => props.courses.reduce((sum, c) => sum + getUsers(c), 0))
const totalBudget = computed(() => props.courses.reduce((sum, c) => sum + getPrice(c) * getUsers(c), 0))

const categoryStats = computed(() => {
  return CATEGORY_META.map(meta => {
    const rows = props.courses.filter(c => c.category === meta.key)
    const users = rows.reduce((sum, c) => sum + getUsers(c), 0)
    const budget = rows.reduce((sum, c) => sum + getPrice(c) * getUsers(c), 0)
    const usagePct = totalUsers.value > 0 ? (users / totalUsers.value) * 100 : 0
    return { ...meta, programCount: rows.length, users, budget, usagePct }
  }).filter(row => row.programCount > 0)
})

const maxBudget = computed(() => Math.max(1, ...categoryStats.value.map(r => r.budget)))
const maxUsagePct = computed(() => Math.max(1, ...categoryStats.value.map(r => r.usagePct)))

const scatterPoints = computed(() =>
  props.courses.map(c => ({
    id: c.id,
    title: c.title || '(제목 없음)',
    category: c.category,
    categoryLabel: CATEGORY_LABEL[c.category] || c.category || '기타',
    price: getPrice(c),
    users: getUsers(c)
  }))
)

function scaledWidth(value, maxValue, maxWidth) {
  if (!maxValue) return 0
  return Math.max(0, (value / maxValue) * maxWidth)
}

function niceCeil(max) {
  if (max <= 0) return 1
  const magnitude = Math.pow(10, Math.floor(Math.log10(max)))
  const residual = max / magnitude
  let niceResidual
  if (residual > 5) niceResidual = 10
  else if (residual > 2) niceResidual = 5
  else if (residual > 1) niceResidual = 2
  else niceResidual = 1
  return niceResidual * magnitude
}

function makeTicks(niceMax, count = 4) {
  const step = niceMax / count
  return Array.from({ length: count + 1 }, (_, i) => Math.round(step * i))
}

const niceMaxPrice = computed(() => niceCeil(Math.max(1, ...scatterPoints.value.map(p => p.price)) * 1.05))
const niceMaxUsers = computed(() => niceCeil(Math.max(1, ...scatterPoints.value.map(p => p.users)) * 1.15))

const xTicks = computed(() => makeTicks(niceMaxPrice.value))
const yTicks = computed(() => makeTicks(niceMaxUsers.value))

function xScale(v) {
  const w = 460 - M.left - M.right
  return M.left + (v / niceMaxPrice.value) * w
}
function yScale(v) {
  const h = 300 - M.top - M.bottom
  return (300 - M.bottom) - (v / niceMaxUsers.value) * h
}

function formatWon(v) {
  return `${Math.round(v).toLocaleString()}원`
}

function formatWonShort(v) {
  const n = Math.round(v)
  if (n >= 100000000) return `${(n / 100000000).toFixed(1)}억원`
  if (n >= 10000) return `${(n / 10000).toFixed(0)}만원`
  return `${n.toLocaleString()}원`
}

function positionFromEvent(e) {
  if (e.type !== 'focus' && typeof e.clientX === 'number') {
    return { x: e.clientX + 14, y: e.clientY + 14 }
  }
  const rect = e.target.getBoundingClientRect()
  return { x: rect.right + 8, y: rect.top }
}

function showTip(e, row, metric) {
  hovered.value = row
  const pos = positionFromEvent(e)
  tooltip.value = {
    visible: true,
    x: pos.x,
    y: pos.y,
    value: metric === 'budget' ? formatWon(row.budget) : `${row.usagePct.toFixed(1)}%`,
    label: `${row.label} · ${metric === 'budget' ? '예산' : '이용률'}`
  }
}

function showScatterTip(e, pt) {
  hoveredPoint.value = pt
  const pos = positionFromEvent(e)
  tooltip.value = {
    visible: true,
    x: pos.x,
    y: pos.y,
    value: `${formatWon(pt.price)} · ${pt.users.toLocaleString()}명`,
    label: `${pt.title} (${pt.categoryLabel})`
  }
}

function hideTip() {
  tooltip.value.visible = false
  hovered.value = null
  hoveredPoint.value = null
}
</script>

<style scoped>
.budget-dashboard {
  display: flex;
  flex-direction: column;
}

.section-head {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
}

.section-subtitle {
  font-size: 13px;
  color: var(--color-text-muted);
}

.summary-cards {
  display: grid;
  gap: 16px;
}

.summary-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 18px 20px;
  box-shadow: var(--shadow-sm);
}

.summary-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.budget-summary {
  grid-template-columns: repeat(3, minmax(160px, 1fr));
  margin-bottom: 20px;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.chart-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  min-width: 0;
}

.chart-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.chart-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 4px;
}

.chart-caption {
  font-size: 12px;
  color: var(--color-text-muted);
}

.table-toggle-btn {
  flex-shrink: 0;
  background: transparent;
  border: 1.5px solid var(--color-border);
  color: var(--color-text-secondary);
  border-radius: var(--radius-md);
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  transition: var(--transition);
}

.table-toggle-btn:hover {
  background: var(--color-bg-tertiary);
  border-color: var(--color-border-hover);
}

.dual-bar-panels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.bar-panel-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.bar-svg,
.scatter-svg {
  width: 100%;
  height: auto;
  display: block;
  overflow: visible;
}

.hit-rect,
.hit-circle {
  fill: transparent;
  cursor: pointer;
}

.hit-rect:focus,
.hit-circle:focus {
  outline: none;
}

.row-label {
  font-size: 11px;
  fill: var(--color-text-secondary);
  pointer-events: none;
}

.value-label {
  font-size: 11px;
  font-weight: 600;
  fill: var(--color-text-primary);
  pointer-events: none;
}

.row-dim {
  opacity: 0.35;
  transition: opacity 0.15s ease;
}

/* bars/dots are decorative on top of the hit layer; let hover pass through to it */
.dual-bar-panels rect:not(.hit-rect) {
  pointer-events: none;
}

.scatter-dot {
  fill: var(--color-primary);
  stroke: var(--color-bg-primary);
  stroke-width: 2;
  transition: opacity 0.15s ease;
  pointer-events: none;
}

.axis-line {
  stroke: var(--color-border-hover);
  stroke-width: 1;
}

.tick-line {
  stroke: var(--color-border);
  stroke-width: 1;
}

.tick-label {
  font-size: 9px;
  fill: var(--color-text-muted);
  pointer-events: none;
}

.axis-caption {
  font-size: 10px;
  fill: var(--color-text-muted);
  pointer-events: none;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  text-align: left;
  font-size: 11px;
  color: var(--color-text-muted);
  font-weight: 600;
  padding: 8px 10px;
  border-bottom: 1px solid var(--color-border);
}

.data-table td {
  padding: 9px 10px;
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-primary);
}

.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.chart-tooltip {
  position: fixed;
  z-index: 60;
  pointer-events: none;
  background: var(--color-text-primary);
  color: #fff;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-md);
  max-width: 220px;
}

.tooltip-value {
  font-size: 13px;
  font-weight: 700;
}

.tooltip-label {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 2px;
}

.empty-text {
  color: var(--color-text-muted);
  font-size: 14px;
}

@media (max-width: 992px) {
  .chart-grid,
  .dual-bar-panels {
    grid-template-columns: 1fr;
  }

  .budget-summary {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .budget-summary {
    grid-template-columns: 1fr;
  }
}
</style>
