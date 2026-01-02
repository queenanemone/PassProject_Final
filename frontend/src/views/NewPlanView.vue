<template>
  <div class="relative flex h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <!-- 배경 이미지 -->
    <div class="absolute inset-0 w-full h-full bg-center bg-no-repeat bg-cover filter blur-sm brightness-75 dark:brightness-50"
         style='background-image: url("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1920");'></div>
    
    <!-- 로딩 오버레이 -->
    <div v-if="isLoading"
         class="fixed inset-0 z-50 flex flex-col items-center justify-center bg-black/80 backdrop-blur-sm transition-opacity">
      <div class="flex flex-col items-center gap-4 p-8 rounded-2xl bg-white dark:bg-[#1a2035] border border-gray-200 dark:border-white/10 shadow-2xl">
        <svg class="animate-spin h-12 w-12 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
          </path>
        </svg>
        <div class="text-center">
          <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-1">여행 계획 생성 중...</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400">{{ loadingMessage }}</p>
        </div>
      </div>
    </div>

    <!-- 토스트 알림 -->
    <div
      v-if="toastMessage"
      class="fixed top-4 right-4 z-50 bg-white dark:bg-card-dark rounded-lg border border-gray-200 dark:border-border-dark shadow-lg p-4 min-w-[300px] max-w-md animate-slide-in"
      :class="toastType === 'error' ? 'border-red-500' : 'border-primary'"
    >
      <div class="flex items-center gap-3">
        <span
          class="material-symbols-outlined text-2xl"
          :class="toastType === 'error' ? 'text-red-500' : 'text-primary'"
        >
          {{ toastType === 'error' ? 'error' : 'check_circle' }}
        </span>
        <p class="flex-1 text-gray-900 dark:text-white">{{ toastMessage }}</p>
        <button @click="toastMessage = ''" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </div>
    
    <!-- 메인 컨텐츠 -->
    <div class="relative flex h-full grow flex-col items-center justify-start p-4 overflow-y-auto">
      <div class="flex flex-col w-full max-w-xl flex-1 pt-20 pb-10">
        <div class="flex flex-col items-center gap-6 rounded-xl bg-white/75 dark:bg-background-dark/90 backdrop-blur-xl p-6 sm:p-10 shadow-2xl border border-gray-200/50 dark:border-white/10">
          <div class="flex flex-col items-center gap-2">
            <h1 class="text-gray-900 dark:text-white tracking-tight text-3xl font-bold leading-tight text-center">새로운 국내 여행</h1>
            <p class="text-gray-600 dark:text-white/80 text-base font-normal leading-normal text-center max-w-md">
              여행 정보를 입력하여 AI 추천으로 채워진 여행 계획을 만들거나, 빈 계획으로 시작할 수 있습니다.
            </p>
          </div>
          
          <form @submit.prevent="handleCreatePlan" class="w-full flex flex-col gap-6">
            <div class="w-full">
              <label class="text-gray-900 dark:text-white text-base font-medium leading-normal pb-2 block">여행 제목</label>
              <input
                v-model="formData.title"
                type="text"
                placeholder="예: 부산 힐링 여행 (비워두면 '새 여행 계획'으로 저장됩니다)"
                class="w-full h-12 rounded-lg bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 px-4 text-gray-900 dark:text-white placeholder:text-gray-500 dark:placeholder:text-gray-500 focus:border-primary focus:ring-2 focus:ring-primary/50 outline-none transition-all"
              />
            </div>
            
            <div class="flex flex-col sm:flex-row w-full gap-4">
              <label class="flex flex-col flex-1">
                <p class="text-gray-900 dark:text-white text-base font-medium leading-normal pb-2">출발지 (기차역)</p>
                <select
                  v-model="formData.departureRegionCode"
                  class="form-select flex w-full rounded-lg text-gray-900 dark:text-white focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-white/10 bg-gray-50 dark:bg-white/5 focus:border-primary h-12 px-4 text-base font-normal appearance-none"
                >
                  <option value="">출발지 선택</option>
                  <option value="1">서울</option>
                  <option value="6">부산</option>
                  <option value="4">대구</option>
                  <option value="2">인천</option>
                  <option value="5">광주</option>
                  <option value="3">대전</option>
                  <option value="31">경기 (수원)</option>
                  <option value="32">강원 (강릉)</option>
                  <option value="33">충북 (청주)</option>
                  <option value="34">충남 (천안아산)</option>
                  <option value="35">경북</option>
                  <option value="36">경남</option>
                  <option value="37">전북 (전주)</option>
                  <option value="38">전남</option>
                </select>
              </label>
              
              <label class="flex flex-col flex-1">
                <p class="text-gray-900 dark:text-white text-base font-medium leading-normal pb-2">도착지 (여행지)</p>
                <select
                  v-model="formData.arrivalRegionCode"
                  class="form-select flex w-full rounded-lg text-gray-900 dark:text-white focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-white/10 bg-gray-50 dark:bg-white/5 focus:border-primary h-12 px-4 text-base font-normal appearance-none"
                >
                  <option value="">도착지 선택</option>
                  <option value="1">서울</option>
                  <option value="6">부산</option>
                  <option value="39">제주</option>
                  <option value="32">강원</option>
                  <option value="4">대구</option>
                  <option value="2">인천</option>
                  <option value="3">대전</option>
                  <option value="35">경북 (경주/포항)</option>
                  <option value="36">경남</option>
                  <option value="37">전북</option>
                  <option value="38">전남 (여수/목포)</option>
                </select>
              </label>
            </div>
            
            <div>
              <p class="text-gray-900 dark:text-white text-base font-medium leading-normal pb-2">여행 기간</p>
              <div class="flex items-center gap-4">
                <input
                  v-model="formData.departureDate"
                  class="form-input flex w-full rounded-lg text-gray-900 dark:text-white focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-white/10 bg-gray-50 dark:bg-white/5 focus:border-primary h-12 px-4 text-base font-normal"
                  type="date"
                />
                <span class="text-gray-600 dark:text-white/60">-</span>
                <input
                  v-model="formData.arrivalDate"
                  class="form-input flex w-full rounded-lg text-gray-900 dark:text-white focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-white/10 bg-gray-50 dark:bg-white/5 focus:border-primary h-12 px-4 text-base font-normal"
                  type="date"
                />
              </div>
            </div>
            
            <div class="flex items-end gap-4">
              <label class="flex flex-col flex-1">
                <p class="text-gray-900 dark:text-white text-base font-medium leading-normal pb-2">인원</p>
                <div class="flex items-center h-12 rounded-lg border border-gray-200 dark:border-white/10 bg-gray-50 dark:bg-white/5">
                  <button
                    type="button"
                    @click="decreaseAdult"
                    class="flex items-center justify-center h-full w-12 text-gray-900 dark:text-white hover:bg-gray-100 dark:hover:bg-white/10 rounded-l-lg transition-colors"
                  >
                    -
                  </button>
                  <input
                    :value="formData.adultCount"
                    class="w-full h-full text-center bg-transparent border-0 p-0 text-gray-900 dark:text-white focus:ring-0"
                    type="text"
                    readonly
                  />
                  <button
                    type="button"
                    @click="increaseAdult"
                    class="flex items-center justify-center h-full w-12 text-gray-900 dark:text-white hover:bg-gray-100 dark:hover:bg-white/10 rounded-r-lg transition-colors"
                  >
                    +
                  </button>
                </div>
              </label>
              <div class="flex items-center pb-3">
                <input
                  v-model="formData.hasPet"
                  id="hasPet"
                  class="form-checkbox h-5 w-5 rounded bg-gray-100 dark:bg-white/10 border-gray-300 dark:border-white/20 text-primary focus:ring-primary"
                  type="checkbox"
                />
                <label class="ml-2 text-gray-900 dark:text-white text-base font-medium" for="hasPet">반려동물 동반</label>
              </div>
            </div>
            
            <div class="flex flex-col w-full gap-3 pt-4">
              <button
                type="button"
                @click="handleCreatePlan"
                class="flex items-center justify-center h-12 px-6 rounded-lg w-full bg-primary text-white text-base font-bold hover:bg-primary/90 transition-colors"
              >
                AI 추천으로 계획 생성
              </button>
              <button
                type="button"
                @click="handleSkip"
                class="flex items-center justify-center h-12 px-6 rounded-lg w-full bg-gray-100 dark:bg-white/10 text-gray-900 dark:text-white text-base font-medium hover:bg-gray-200 dark:hover:bg-white/20 transition-colors"
              >
                건너뛰고 빈 계획 만들기
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import planApi from '@/services/api/plan'

const router = useRouter()

const isLoading = ref(false)
const loadingMessage = ref('AI가 최적의 코스를 분석하고 있습니다')

// 토스트 알림 상태
const toastMessage = ref('')
const toastType = ref('success') // 'success' | 'error'

const formData = ref({
  title: '',
  departureRegionCode: '',
  arrivalRegionCode: '',
  departureDate: '',
  arrivalDate: '',
  adultCount: 2,
  hasPet: false
})

const increaseAdult = () => {
  formData.value.adultCount++
}

const decreaseAdult = () => {
  if (formData.value.adultCount > 1) {
    formData.value.adultCount--
  }
}

/**
 * 로딩 애니메이션 및 메시지 표시
 */
const showLoading = () => {
  isLoading.value = true
  
  const messages = [
    "🚅 기차표 정보를 조회하고 있습니다...",
    "🏨 추천 숙소를 검색 중입니다...",
    "📸 인기 관광지 데이터를 분석합니다...",
    "✨ AI가 최적의 코스를 만드는 중입니다..."
  ]
  
  let i = 0
  const interval = setInterval(() => {
    if (i < messages.length) {
      loadingMessage.value = messages[i++]
    } else {
      clearInterval(interval)
    }
  }, 1500)
  
  return interval
}

/**
 * 토스트 메시지 표시
 */
const showToast = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  setTimeout(() => {
    toastMessage.value = ''
  }, 3000)
}

/**
 * 여행 계획 생성 요청 처리
 */
const handleCreatePlan = async () => {
  if (!formData.value.arrivalRegionCode || !formData.value.departureRegionCode || 
      !formData.value.departureDate || !formData.value.arrivalDate) {
    showToast('출발지, 도착지, 여행 기간을 모두 입력해주세요', 'error')
    return
  }
  
  if (formData.value.arrivalRegionCode === formData.value.departureRegionCode) {
    showToast('출발지와 도착지는 같을 수 없습니다.', 'error')
    return
  }
  
  const planData = {
    title: formData.value.title.trim() || "새 여행 계획",
    arrivalRegionCode: formData.value.arrivalRegionCode,
    departureRegionCode: formData.value.departureRegionCode,
    departureDate: formData.value.departureDate,
    arrivalDate: formData.value.arrivalDate,
    adultCount: formData.value.adultCount,
    hasPet: formData.value.hasPet
  }
  
  try {
    const interval = showLoading()
    
    const result = await planApi.createPlan(planData)
    
    if (result.success) {
      router.push(`/dashboard?planId=${result.data.planId}`)
    } else {
      throw new Error(result.message || '생성 실패')
    }
    
    clearInterval(interval)
  } catch (error) {
    console.error('생성 오류:', error)
    showToast('오류가 발생했습니다: ' + (error.message || '알 수 없는 오류'), 'error')
    isLoading.value = false
  }
}

/**
 * 빈 계획으로 건너뛰기
 */
const handleSkip = async () => {
  try {
    isLoading.value = true
    
    const result = await planApi.createPlan({})
    
    if (result.success) {
      router.push(`/dashboard?planId=${result.data.planId}`)
    } else {
      showToast('빈 계획 생성 실패', 'error')
    }
  } catch (error) {
    console.error('빈 계획 생성 오류:', error)
    showToast('빈 계획 생성 실패', 'error')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
/* 드롭다운 메뉴(Option)의 색상 강제 지정 */
select option {
  background-color: white;
  color: #1f2937;
  padding: 10px;
}

html.dark select option {
  background-color: #1a2035;
  color: white;
}

@keyframes slide-in {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.animate-slide-in {
  animation: slide-in 0.3s ease-out;
}
</style>
