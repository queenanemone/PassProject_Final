<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />
    <main class="flex-grow bg-background-light dark:bg-background-dark overflow-y-auto relative">
      <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8 pb-24">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-3xl md:text-4xl font-extrabold text-gray-900 dark:text-text-dark mb-2">나의 여행 기록</h1>
            <p class="text-gray-600 dark:text-text-secondary-dark">여행의 소중한 순간들을 기록으로 남겨보세요.</p>
          </div>
          <button
            v-if="!loading && records.length > 0"
            @click="openPlanSelectModal"
            class="flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-white text-sm font-bold hover:bg-blue-400 transition-colors"
          >
            <span class="material-symbols-outlined text-base">add</span>
            <span class="hidden sm:inline">새 여행 기록</span>
            <span class="sm:hidden">새 기록</span>
          </button>
        </div>
        
        <div v-if="loading" class="text-center py-20">
          <div class="animate-spin h-10 w-10 text-primary mx-auto mb-4"></div>
          <p class="text-gray-600 dark:text-text-secondary-dark">여행 기록을 불러오는 중...</p>
        </div>
        
        <div v-else-if="records.length === 0" class="flex flex-col items-center justify-center py-16 text-center">
          <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-text-secondary-dark mb-4">book</span>
          <p class="text-xl font-bold text-gray-900 dark:text-text-dark mb-2">아직 작성한 여행 기록이 없습니다</p>
          <p class="text-gray-600 dark:text-text-secondary-dark mb-6">첫 번째 여행 기록을 작성해보세요!</p>
          <button
            @click="openPlanSelectModal"
            class="flex items-center gap-2 rounded-lg bg-primary px-6 py-3 text-white font-bold hover:bg-blue-400 transition-colors"
          >
            <span class="material-symbols-outlined">add</span>
            <span>새 여행 기록 작성하기</span>
          </button>
        </div>
        
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
          <div
            v-for="record in records"
            :key="record.recordId"
            @click="viewRecord(record.recordId)"
            class="group relative flex flex-col overflow-hidden rounded-xl border border-gray-200 dark:border-border-dark bg-white dark:bg-card-dark shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg hover:border-primary/50 cursor-pointer"
          >
            <div v-if="record.images && record.images.length > 0" class="aspect-video w-full bg-cover bg-center rounded-t-xl mb-4" :style="{ backgroundImage: `url('${record.images[0].imageUrl}')` }"></div>
            <div v-else class="aspect-video w-full bg-gray-700 rounded-t-xl mb-4 flex items-center justify-center">
              <span class="material-symbols-outlined text-4xl text-gray-500">image</span>
            </div>
            <div class="flex flex-1 flex-col p-4">
              <div class="flex items-start justify-between mb-2">
                <h3 class="text-lg font-bold text-gray-900 dark:text-text-dark line-clamp-2 flex-1">{{ record.title || '제목 없음' }}</h3>
                <button
                  @click.stop="deleteRecord(record.recordId)"
                  class="ml-2 text-gray-600 dark:text-text-secondary-dark hover:text-red-400 transition-colors opacity-0 group-hover:opacity-100"
                >
                  <span class="material-symbols-outlined text-sm">delete</span>
                </button>
              </div>
              <p class="text-gray-600 dark:text-text-secondary-dark text-sm mb-4 line-clamp-2">{{ getTextContent(record.content) }}</p>
              <div class="mt-auto flex items-center justify-between text-xs text-gray-600 dark:text-text-secondary-dark">
                <span>{{ formatDate(record.createdAt) }}</span>
                <span v-if="record.planId" class="px-2 py-1 rounded bg-primary/20 text-primary text-xs">계획 연결됨</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 여행 계획 선택 모달 -->
    <div
      v-if="showPlanModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      @click.self="closePlanSelectModal"
    >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-md w-full mx-4">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-900 dark:text-text-dark">여행 계획 선택</h3>
          <button @click="closePlanSelectModal" class="text-gray-600 dark:text-text-secondary-dark hover:text-gray-900 dark:hover:text-text-dark transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <p class="text-gray-600 dark:text-text-secondary-dark mb-4">여행 기록을 연결할 계획을 선택하거나, 선택 없이 작성할 수 있습니다.</p>
        <div class="mb-4">
          <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block">여행 계획 선택</label>
          <select
            v-model="selectedPlanId"
            class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
          >
            <option :value="null">선택 안함</option>
            <option v-for="plan in plans" :key="plan.planId" :value="plan.planId">
              {{ plan.title || `여행 계획 ${plan.planId}` }}
            </option>
          </select>
        </div>
        <div class="flex gap-3">
          <button
            @click="closePlanSelectModal"
            class="flex-1 flex items-center justify-center rounded-lg h-11 px-4 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark text-sm font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
          >
            취소
          </button>
          <button
            @click="goToWriteRecord"
            class="flex-1 flex items-center justify-center rounded-lg h-11 px-4 bg-primary text-white text-sm font-bold hover:bg-blue-400 transition-colors"
          >
            작성하기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import recordApi from '@/services/api/record'
import planApi from '@/services/api/plan'

const router = useRouter()

const loading = ref(false)
const records = ref([])
const showPlanModal = ref(false)
const plans = ref([])
const selectedPlanId = ref(null)

const loadRecords = async () => {
  loading.value = true
  try {
    const result = await recordApi.getUserRecords()
    if (result.success) {
      records.value = result.data || []
      // 각 기록의 이미지 로드
      for (const record of records.value) {
        try {
          const imageResult = await recordApi.getRecordImages(record.recordId)
          if (imageResult.success && imageResult.data && imageResult.data.length > 0) {
            record.images = imageResult.data
          }
        } catch (error) {
          console.error(`기록 ${record.recordId} 이미지 로드 오류:`, error)
        }
      }
    }
  } catch (error) {
    console.error('여행 기록 로드 오류:', error)
  } finally {
    loading.value = false
  }
}

const viewRecord = (recordId) => {
  router.push(`/record/${recordId}`)
}

const deleteRecord = async (recordId) => {
  if (!(await confirm('정말로 이 여행 기록을 삭제하시겠습니까?'))) {
    return
  }
  
  try {
    const result = await recordApi.deleteRecord(recordId)
    if (result.success) {
      alert('여행 기록이 삭제되었습니다')
      loadRecords()
    } else {
      alert(result.message || '삭제에 실패했습니다')
    }
  } catch (error) {
    console.error('삭제 오류:', error)
    alert('삭제 중 오류가 발생했습니다')
  }
}

const getTextContent = (html) => {
  if (!html) return ''
  const div = document.createElement('div')
  div.innerHTML = html
  const text = div.textContent || div.innerText || ''
  // 연속된 공백과 줄바꿈 정리
  return text.replace(/\s+/g, ' ').trim()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const openPlanSelectModal = async () => {
  showPlanModal.value = true
  selectedPlanId.value = null
  
  // 여행 계획 목록 로드
  try {
    const result = await planApi.getPlans()
    if (result.success) {
      plans.value = result.data || []
    }
  } catch (error) {
    console.error('여행 계획 로드 오류:', error)
  }
}

const closePlanSelectModal = () => {
  showPlanModal.value = false
  selectedPlanId.value = null
}

const goToWriteRecord = () => {
  const planId = selectedPlanId.value
  const query = planId ? { planId: planId } : {}
  closePlanSelectModal()
  router.push({ path: '/record', query })
}

onMounted(() => {
  loadRecords()
})
</script>

