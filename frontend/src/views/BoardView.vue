<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark board-view-container">
    <Header />
    
    <main class="flex-grow bg-background-light dark:bg-background-dark">
      <div class="container mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 gap-8 lg:grid-cols-5">
          <!-- 사이드바 필터 -->
          <aside class="lg:col-span-1">
            <div class="sticky top-24 rounded-xl border border-gray-200 dark:border-border-dark bg-white dark:bg-card-dark p-4">
              <div class="flex flex-col gap-6">
                <div>
                  <label class="flex h-12 w-full flex-col">
                    <div class="flex h-full w-full flex-1 items-stretch rounded-lg">
                      <div class="flex items-center justify-center rounded-l-lg border-r-0 border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark pl-4 text-gray-500 dark:text-slate-400">
                        <span class="material-symbols-outlined">search</span>
                      </div>
                      <input
                        v-model="searchQuery"
                        @input="handleSearch"
                        class="form-input h-full min-w-0 flex-1 resize-none overflow-hidden rounded-r-lg border-none border-l-0 bg-gray-50 dark:bg-background-dark px-4 pl-2 text-base font-normal leading-normal text-gray-900 placeholder:text-gray-400 dark:placeholder:text-slate-400 focus:outline-0 focus:ring-0 dark:text-white"
                        placeholder="제목, 내용으로 검색..."
                      />
                    </div>
                  </label>
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-slate-400 mb-2" for="sort-by">정렬 기준</label>
                  <select
                    v-model="filters.sortBy"
                    @change="loadPosts"
                    id="sort-by"
                    class="w-full rounded-lg border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark text-gray-900 dark:text-white focus:border-primary focus:ring-primary px-4 py-3 text-sm"
                  >
                    <option value="popular">인기순</option>
                    <option value="latest">최신순</option>
                    <option value="views">조회순</option>
                  </select>
                </div>
                
                <!-- 필터 섹션들 -->
                <div class="flex flex-col">
                  <details class="flex flex-col border-t border-t-gray-200 dark:border-t-border-dark py-2 group" open>
                    <summary class="flex cursor-pointer items-center justify-between gap-6 py-2">
                      <p class="text-sm font-medium text-gray-900 dark:text-white">지역</p>
                      <span class="material-symbols-outlined text-gray-600 dark:text-slate-400 group-open:rotate-180 transition-transform">expand_more</span>
                    </summary>
                    <div class="flex flex-col gap-2 pt-2 text-sm text-gray-700 dark:text-slate-400">
                      <label v-for="region in regions" :key="region.value" class="flex items-center gap-2">
                        <input
                          v-model="selectedRegions"
                          :value="region.value"
                          @change="handleFilterChange"
                          class="region-filter form-checkbox rounded text-primary focus:ring-primary/50"
                          type="checkbox"
                        />
                        {{ region.label }}
                      </label>
                    </div>
                  </details>
                  
                  <details class="flex flex-col border-t border-t-gray-200 dark:border-t-border-dark py-2 group" open>
                    <summary class="flex cursor-pointer items-center justify-between gap-6 py-2">
                      <p class="text-sm font-medium text-gray-900 dark:text-white">여행 타입</p>
                      <span class="material-symbols-outlined text-gray-600 dark:text-slate-400 group-open:rotate-180 transition-transform">expand_more</span>
                    </summary>
                    <div class="flex flex-col gap-2 pt-2 text-sm text-gray-700 dark:text-slate-400">
                      <label v-for="type in tripTypes" :key="type" class="flex items-center gap-2">
                        <input
                          v-model="selectedTripTypes"
                          :value="type"
                          @change="handleFilterChange"
                          class="trip-type-filter form-checkbox rounded text-primary focus:ring-primary/50"
                          type="checkbox"
                        />
                        {{ type }}
                      </label>
                    </div>
                  </details>
                  
                  <details class="flex flex-col border-t border-b border-t-gray-200 border-b-gray-200 dark:border-t-border-dark dark:border-b-border-dark py-2 group">
                    <summary class="flex cursor-pointer items-center justify-between gap-6 py-2">
                      <p class="text-sm font-medium text-gray-900 dark:text-white">계절</p>
                      <span class="material-symbols-outlined text-gray-600 dark:text-slate-400 group-open:rotate-180 transition-transform">expand_more</span>
                    </summary>
                    <div class="flex flex-col gap-2 pt-2 text-sm text-gray-700 dark:text-slate-400">
                      <label v-for="season in seasons" :key="season" class="flex items-center gap-2">
                        <input
                          v-model="selectedSeasons"
                          :value="season"
                          @change="handleFilterChange"
                          class="season-filter form-checkbox rounded text-primary focus:ring-primary/50"
                          type="checkbox"
                        />
                        {{ season }}
                      </label>
                    </div>
                  </details>
                </div>
              </div>
            </div>
          </aside>
          
          <!-- 메인 컨텐츠 -->
          <div class="lg:col-span-4">
            <div class="mb-8">
              <div class="flex gap-4 border-b border-gray-200 dark:border-border-dark mb-6">
                <button
                  @click="switchTab('TRAVEL_PLAN')"
                  :class="[
                    'tab-button px-4 py-2 text-base font-semibold border-b-2 transition-colors',
                    currentCategory === 'TRAVEL_PLAN'
                      ? 'border-primary text-primary dark:text-primary'
                      : 'border-transparent text-gray-700 dark:text-slate-400 hover:text-primary dark:hover:text-primary'
                  ]"
                >
                  국내 여행 공유
                </button>
                <button
                  @click="switchTab('TRAVEL_RECORD')"
                  :class="[
                    'tab-button px-4 py-2 text-base font-semibold border-b-2 transition-colors',
                    currentCategory === 'TRAVEL_RECORD'
                      ? 'border-primary text-primary dark:text-primary'
                      : 'border-transparent text-gray-700 dark:text-slate-400 hover:text-primary dark:hover:text-primary'
                  ]"
                >
                  여행 기록
                </button>
                <button
                  @click="switchTab('HOTPLACE')"
                  :class="[
                    'tab-button px-4 py-2 text-base font-semibold border-b-2 transition-colors',
                    currentCategory === 'HOTPLACE'
                      ? 'border-primary text-primary dark:text-primary'
                      : 'border-transparent text-gray-700 dark:text-slate-400 hover:text-primary dark:hover:text-primary'
                  ]"
                >
                  HotPlace
                </button>
              </div>
              <div>
                <h1 class="text-4xl font-black tracking-tighter text-gray-900 dark:text-white">
                  {{ currentCategory === 'TRAVEL_PLAN' ? '국내 여행 계획 공유' : currentCategory === 'TRAVEL_RECORD' ? '여행 기록' : 'HotPlace' }}
                </h1>
                <p class="mt-2 text-base font-normal text-gray-600 dark:text-slate-400">
                  {{ currentCategory === 'TRAVEL_PLAN' 
                    ? '다른 사용자들의 여행 계획을 둘러보고 새로운 영감을 얻어보세요.' 
                    : currentCategory === 'TRAVEL_RECORD'
                    ? '다른 사용자들의 여행 기록을 둘러보고 함께 여행의 추억을 나눠보세요.'
                    : '다른 사용자들이 등록한 핫플레이스를 둘러보고 새로운 장소를 발견해보세요.' }}
                </p>
              </div>
            </div>
            
            <!-- 게시글 목록 -->
            <div v-if="loading" class="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
              <div v-for="i in 6" :key="i" class="animate-pulse rounded-xl border border-gray-200 dark:border-border-dark bg-white dark:bg-card-dark h-64"></div>
            </div>
            
            <div v-else-if="posts.length === 0" class="col-span-full group relative flex flex-col items-center justify-center gap-4 overflow-hidden rounded-xl border-2 border-dashed border-gray-200 dark:border-border-dark bg-transparent p-8 text-center">
              <span class="material-symbols-outlined text-4xl text-gray-400 dark:text-slate-500">travel_explore</span>
              <p class="text-sm font-medium text-gray-600 dark:text-slate-500">여행 계획을 찾을 수 없습니다. <br/>필터를 조정해보세요!</p>
            </div>
            
            <div v-else class="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
              <!-- 태그 필터 표시 -->
              <div v-if="selectedTag" class="col-span-full flex items-center gap-2 mb-4">
                <span class="text-sm text-gray-600 dark:text-slate-400">필터:</span>
                <span class="rounded-full bg-purple-600/20 px-3 py-1 text-xs font-semibold text-purple-400 flex items-center gap-2">
                  {{ selectedTag }}
                  <button @click="clearTagFilter" class="hover:text-purple-300">
                    <span class="material-symbols-outlined text-sm">close</span>
                  </button>
                </span>
              </div>
              <div
                v-for="post in filteredPosts"
                :key="post.postId"
                class="group relative flex flex-col overflow-hidden rounded-xl border border-gray-200 dark:border-border-dark bg-white dark:bg-card-dark shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg dark:hover:shadow-black/20"
              >
                <div class="aspect-video w-full bg-cover bg-center rounded-t-xl cursor-pointer" :style="{ backgroundImage: `url('${getPostImage(post)}')` }" @click="viewPost(post.postId)"></div>
                <div class="flex flex-1 flex-col p-4">
                  <div class="flex items-start justify-between gap-2 mb-2">
                    <h3
                      class="text-lg font-bold text-gray-900 dark:text-white cursor-pointer flex-1"
                      @click="viewPost(post.postId)"
                    >
                      {{ post.title || '제목 없음' }}
                    </h3>
                    <button
                      v-if="isMyPost(post)"
                      @click.stop="deletePost(post.postId)"
                      class="flex items-center justify-center rounded-lg p-2 text-red-500 hover:bg-red-500/10 transition-colors"
                      title="삭제"
                    >
                      <span class="material-symbols-outlined text-sm">delete</span>
                    </button>
                  </div>
                  <div class="mt-2 flex flex-wrap gap-2">
                    <!-- 여행 기록 태그 -->
                    <span 
                      v-for="(tag, index) in getPostTags(post)" 
                      :key="index"
                      @click.stop="filterByTag(tag)"
                      :class="[
                        'rounded-full bg-purple-600/20 px-3 py-1 text-xs font-semibold text-purple-400 cursor-pointer hover:bg-purple-600/30 transition-colors',
                        selectedTag === tag ? 'ring-2 ring-purple-400' : ''
                      ]"
                    >
                      {{ tag }}
                    </span>
                    <!-- 국내여행공유 태그 -->
                    <span v-if="getRegionName(post.regionCode)" class="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
                      {{ getRegionName(post.regionCode) }}
                    </span>
                    <span v-if="post.tripType" class="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
                      {{ post.tripType }}
                    </span>
                    <span v-if="post.season" class="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
                      {{ post.season }}
                    </span>
                  </div>
                  <div class="mt-auto flex items-center justify-between pt-4">
                    <div class="flex items-center gap-2">
                      <div
                        v-if="post.authorProfileImage"
                        class="size-6 rounded-full bg-cover bg-center bg-no-repeat"
                        :style="{ backgroundImage: `url('${post.authorProfileImage}')` }"
                      ></div>
                      <div v-else class="size-6 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                        <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                      </div>
                      <span class="text-xs font-medium text-gray-600 dark:text-slate-400">
                        {{ post.authorNickname || post.authorName || '작성자' }}
                      </span>
                    </div>
                    <div class="flex items-center gap-4 text-gray-600 dark:text-slate-400">
                      <div class="flex items-center gap-1 cursor-pointer" @click.stop="toggleLike(post.postId)">
                        <span :class="['material-symbols-outlined text-base', post.liked ? 'text-red-500 fill-current' : '']">{{ post.liked ? 'favorite' : 'favorite_border' }}</span>
                        <span class="text-xs">{{ formatCount(post.likeCount || 0) }}</span>
                      </div>
                      <div class="flex items-center gap-1 cursor-pointer" @click.stop="viewPost(post.postId)">
                        <span class="material-symbols-outlined text-base">chat_bubble_outline</span>
                        <span class="text-xs">{{ post.commentCount || 0 }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 공유 버튼 -->
    <div class="fixed bottom-8 right-8">
      <button
        @click="handleShareClick"
        class="flex items-center gap-2 rounded-full bg-primary px-6 py-3 text-white font-bold shadow-lg transition-transform hover:scale-105"
      >
        <span class="material-symbols-outlined">share</span>
        <span>{{ currentCategory === 'TRAVEL_PLAN' ? '내 여행 계획 공유하기' : currentCategory === 'TRAVEL_RECORD' ? '내 여행 기록 공유하기' : 'HotPlace 등록하기' }}</span>
      </button>
    </div>
    
    <!-- 여행 기록 선택 모달 -->
    <div
      v-if="showRecordModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      @click.self="showRecordModal = false"
    >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-900 dark:text-white">여행 기록 선택</h3>
          <button @click="showRecordModal = false" class="text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <p class="text-gray-600 dark:text-slate-400 mb-4">게시판에 공유할 여행 기록을 선택해주세요.</p>
        <div class="space-y-3">
          <div
            v-for="record in userRecords"
            :key="record.recordId"
            @click="shareRecord(record.recordId)"
            class="p-4 rounded-lg border border-gray-200 dark:border-border-dark hover:bg-gray-50 dark:hover:bg-card-dark/50 cursor-pointer transition-colors"
          >
            <h4 class="text-gray-900 dark:text-white font-semibold mb-2">{{ record.title }}</h4>
            <p class="text-gray-600 dark:text-slate-400 text-sm line-clamp-2">{{ getRecordSummary(record.content) }}</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 여행 계획 공유 모달 -->
    <div
      v-if="showPlanModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      @click.self="showPlanModal = false"
    >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-900 dark:text-white">여행 계획 공유하기</h3>
          <button @click="showPlanModal = false" class="text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <div v-if="!selectedPlanForShare" class="space-y-4">
          <p class="text-gray-600 dark:text-slate-400 mb-4">게시판에 공유할 여행 계획을 선택해주세요.</p>
          <div class="space-y-3">
            <div
              v-for="plan in userPlans"
              :key="plan.planId"
              @click="selectPlanForShare(plan)"
              class="p-4 rounded-lg border border-gray-200 dark:border-border-dark hover:bg-gray-50 dark:hover:bg-card-dark/50 cursor-pointer transition-colors"
            >
              <h4 class="text-gray-900 dark:text-white font-semibold mb-2">{{ plan.title || `여행 계획 ${plan.planId}` }}</h4>
              <p class="text-gray-600 dark:text-slate-400 text-sm">
                {{ plan.departureDate }} ~ {{ plan.arrivalDate }}
              </p>
            </div>
          </div>
        </div>
        
        <form v-else @submit.prevent="sharePlan" class="space-y-6">
          <div>
            <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">제목</label>
            <input
              v-model="sharePlanForm.title"
              type="text"
              class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              placeholder="게시글 제목을 입력하세요"
              required
            />
          </div>
          
          <div>
            <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">내용</label>
            <textarea
              v-model="sharePlanForm.content"
              class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark p-4 min-h-32"
              placeholder="여행 계획에 대한 설명을 작성하세요"
              required
            ></textarea>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">지역</label>
              <select
                v-model="sharePlanForm.regionCode"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="1">서울/경기</option>
                <option value="2">부산</option>
                <option value="3">대구</option>
                <option value="4">인천</option>
                <option value="5">대전</option>
                <option value="32">강원</option>
                <option value="33">충청</option>
                <option value="35">경상</option>
                <option value="37">전라</option>
                <option value="39">제주</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">여행 타입</label>
              <select
                v-model="sharePlanForm.tripType"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="가족여행">가족여행</option>
                <option value="커플여행">커플여행</option>
                <option value="혼자여행">혼자여행</option>
                <option value="우정여행">우정여행</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">계절</label>
              <select
                v-model="sharePlanForm.season"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="봄">봄</option>
                <option value="여름">여름</option>
                <option value="가을">가을</option>
                <option value="겨울">겨울</option>
              </select>
            </div>
          </div>
          
          <div class="flex gap-3 pt-4">
            <button
              type="button"
              @click="cancelPlanShare"
              class="flex-1 flex items-center justify-center rounded-lg h-12 px-6 bg-gray-200 dark:bg-secondary-button-dark text-gray-900 dark:text-text-dark font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
            >
              취소
            </button>
            <button
              type="submit"
              :disabled="sharingPlan"
              class="flex-1 flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-primary text-white font-bold hover:bg-blue-400 transition-colors disabled:opacity-50"
            >
              <span v-if="sharingPlan" class="material-symbols-outlined animate-spin">sync</span>
              <span v-else class="material-symbols-outlined">share</span>
              <span>{{ sharingPlan ? '공유 중...' : '공유하기' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Header from '@/components/Header.vue'
import boardApi from '@/services/api/board'
import recordApi from '@/services/api/record'
import planApi from '@/services/api/plan'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentCategory = ref('TRAVEL_PLAN')
const loading = ref(false)
const posts = ref([])
const searchQuery = ref('')
const showRecordModal = ref(false)
const userRecords = ref([])
const showPlanModal = ref(false)
const userPlans = ref([])
const selectedPlanForShare = ref(null)
const sharePlanForm = ref({
  title: '',
  content: '',
  regionCode: '',
  tripType: '',
  season: ''
})
const sharingPlan = ref(false)

const filters = ref({
  regionCode: null,
  tripType: null,
  season: null,
  sortBy: 'popular'
})

const selectedRegions = ref([])
const selectedTripTypes = ref([])
const selectedSeasons = ref([])
const selectedTag = ref(null) // 선택된 태그

const regions = [
  { label: '서울/경기', value: '1' },
  { label: '강원', value: '32' },
  { label: '충청', value: '33,34' },
  { label: '전라', value: '37,38' },
  { label: '경상', value: '35,36' },
  { label: '제주', value: '39' }
]

const tripTypes = ['가족여행', '커플여행', '혼자여행', '우정여행', '반려동물 동반']
const seasons = ['봄', '여름', '가을', '겨울']

/**
 * 게시글 목록 로드
 */
const loadPosts = async () => {
  loading.value = true
  try {
    const params = {
      category: currentCategory.value,
      sortBy: filters.value.sortBy
    }
    
    if (selectedRegions.value.length > 0) {
      params.regionCode = selectedRegions.value.join(',')
    }
    if (selectedTripTypes.value.length > 0) {
      params.tripType = selectedTripTypes.value.join(',')
    }
    if (selectedSeasons.value.length > 0) {
      params.season = selectedSeasons.value.join(',')
    }
    
    const result = await boardApi.getPosts(params)
    if (result.success) {
      posts.value = result.data || []
      // 국내 여행 공유 및 여행 기록, 핫플레이스 카테고리인 경우 이미지 로드
      if (currentCategory.value === 'TRAVEL_PLAN') {
        await loadPostImages()
      } else if (currentCategory.value === 'TRAVEL_RECORD' || currentCategory.value === 'HOTPLACE') {
        // 여행 기록 및 핫플레이스의 경우 content에서 이미지 URL 추출하여 firstImage 설정
        posts.value.forEach(post => {
          if (post.content) {
            const imgRegex = /<img[^>]+src=["']([^"']+)["'][^>]*>/i
            const match = post.content.match(imgRegex)
            if (match && match[1]) {
              post.firstImage = match[1]
            }
          }
        })
      }
    }
  } catch (error) {
    console.error('게시글 로드 오류:', error)
    posts.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 각 게시글의 첫 번째 관광지 이미지 로드 (병렬 처리)
 */
const loadPostImages = async () => {
  const imagePromises = posts.value.map(async (post) => {
    // TRAVEL_PLAN 카테고리이고 planId가 있는 게시글만 처리
    if (post.category === 'TRAVEL_PLAN' && post.planId) {
      try {
        const planDetailResult = await planApi.getPlan(post.planId)
        if (planDetailResult.success && planDetailResult.data) {
          const destinationDetails = planDetailResult.data.destinationDetails || []
          // 첫 번째 관광지 이미지 찾기
          for (const dest of destinationDetails) {
            if (dest.tourInfo) {
              const imageUrl = dest.tourInfo.firstImage || dest.tourInfo.firstImage2
              if (imageUrl) {
                post.firstImage = imageUrl
                return
              }
            }
          }
          // 관광지에 이미지가 없으면 숙소 이미지 확인
          if (!post.firstImage) {
            const accommodations = planDetailResult.data.accommodations || []
            for (const acc of accommodations) {
              if (acc.tourInfo) {
                const imageUrl = acc.tourInfo.firstImage || acc.tourInfo.firstImage2
                if (imageUrl) {
                  post.firstImage = imageUrl
                  return
                }
              }
            }
          }
        }
      } catch (error) {
        console.error(`게시글 ${post.postId} 이미지 로드 오류:`, error)
      }
    }
  })
  
  await Promise.all(imagePromises)
}

/**
 * 탭 전환 (계획 공유 / 여행 기록)
 */
const switchTab = (category) => {
  // 스크롤 위치를 맨 위로 초기화
  window.scrollTo({ top: 0, behavior: 'instant' })
  
  currentCategory.value = category
  selectedRegions.value = []
  selectedTripTypes.value = []
  selectedSeasons.value = []
  filters.value = {
    regionCode: null,
    tripType: null,
    season: null,
    sortBy: 'popular'
  }
  loadPosts()
}

const handleFilterChange = () => {
  loadPosts()
}

const handleSearch = () => {
  // 검색어가 변경되면 필터링만 수행 (서버 요청 없이 클라이언트에서 필터링)
  // filteredPosts computed가 자동으로 업데이트됨
}

/**
 * 게시글 상세 보기
 */
const viewPost = (postId) => {
  router.push(`/post/${postId}`)
}

/**
 * 게시글 삭제
 */
const deletePost = async (postId) => {
  if (!(await confirm('정말 이 게시글을 삭제하시겠습니까?'))) {
    return
  }
  
  try {
    const result = await boardApi.deletePost(postId)
    if (result.success) {
      alert('게시글이 삭제되었습니다')
      loadPosts()
    } else {
      alert(result.message || '게시글 삭제에 실패했습니다')
    }
  } catch (error) {
    console.error('게시글 삭제 오류:', error)
    alert('게시글 삭제 중 오류가 발생했습니다')
  }
}

/**
 * 좋아요 토글
 */
const toggleLike = async (postId) => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다')
    return
  }
  
  try {
    const result = await boardApi.likePost(postId)
    if (result.success && result.data) {
      // 새로고침 없이 해당 게시글의 좋아요 수만 업데이트
      const post = posts.value.find(p => p.postId === postId)
      if (post) {
        post.likeCount = result.data.likeCount ?? post.likeCount ?? 0
        post.liked = result.data.liked !== undefined ? result.data.liked : (post.liked ?? false)
      }
    }
  } catch (error) {
    console.error('좋아요 오류:', error)
  }
}

/**
 * 공유 버튼 클릭 처리
 */
const handleShareClick = () => {
  if (currentCategory.value === 'TRAVEL_PLAN') {
    openPlanSelectModal()
  } else if (currentCategory.value === 'TRAVEL_RECORD') {
    openRecordSelectModal()
  } else if (currentCategory.value === 'HOTPLACE') {
    router.push('/hotplace')
  }
}

const openRecordSelectModal = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다')
    router.push('/login')
    return
  }
  
  try {
    const result = await recordApi.getUserRecords()
    if (result.success) {
      userRecords.value = result.data || []
      showRecordModal.value = true
    }
  } catch (error) {
    console.error('여행 기록 로드 오류:', error)
  }
}

/**
 * 여행 계획 선택 모달 열기
 */
const openPlanSelectModal = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다')
    router.push('/login')
    return
  }
  
  try {
    const result = await planApi.getPlans()
    if (result.success) {
      userPlans.value = result.data || []
      selectedPlanForShare.value = null
      showPlanModal.value = true
    }
  } catch (error) {
    console.error('여행 계획 로드 오류:', error)
  }
}

/**
 * 공유할 여행 계획 선택
 */
const selectPlanForShare = async (plan) => {
  selectedPlanForShare.value = plan
  
  // 계획 상세 정보 가져오기
  try {
    const result = await planApi.getPlan(plan.planId)
    if (result.success && result.data.plan) {
      const planDetail = result.data.plan
      sharePlanForm.value = {
        title: planDetail.title || '',
        content: `${planDetail.title} 여행 계획을 공유합니다.\n\n기간: ${planDetail.departureDate} ~ ${planDetail.arrivalDate}\n인원: 성인 ${planDetail.adultCount}명, 아동 ${planDetail.childCount || 0}명`,
        regionCode: planDetail.arrivalRegionCode || '',
        tripType: '',
        season: ''
      }
    }
  } catch (error) {
    console.error('여행 계획 상세 로드 오류:', error)
    sharePlanForm.value = {
      title: plan.title || '',
      content: `${plan.title} 여행 계획을 공유합니다.`,
      regionCode: '',
      tripType: '',
      season: ''
    }
  }
}

/**
 * 여행 계획 공유 취소
 */
const cancelPlanShare = () => {
  selectedPlanForShare.value = null
  sharePlanForm.value = {
    title: '',
    content: '',
    regionCode: '',
    tripType: '',
    season: ''
  }
  showPlanModal.value = false
}

/**
 * 여행 계획 공유 실행
 */
const sharePlan = async () => {
  if (!sharePlanForm.value.title || !sharePlanForm.value.content) {
    alert('제목과 내용을 입력해주세요')
    return
  }
  
  if (!selectedPlanForShare.value) {
    alert('여행 계획을 선택해주세요')
    return
  }
  
  sharingPlan.value = true
  try {
    const result = await boardApi.createPost({
      planId: selectedPlanForShare.value.planId,
      title: sharePlanForm.value.title,
      content: sharePlanForm.value.content,
      regionCode: sharePlanForm.value.regionCode || null,
      tripType: sharePlanForm.value.tripType || null,
      season: sharePlanForm.value.season || null,
      category: 'TRAVEL_PLAN'
    })
    
    if (result.success) {
      alert('여행 계획이 게시판에 공유되었습니다!')
      showPlanModal.value = false
      selectedPlanForShare.value = null
      // 등록한 게시글 상세 페이지로 이동
      if (result.data && result.data.postId) {
        router.push(`/post/${result.data.postId}`)
      } else {
        loadPosts()
      }
    } else {
      alert(result.message || '공유에 실패했습니다')
    }
  } catch (error) {
    console.error('여행 계획 공유 오류:', error)
    alert('공유 중 오류가 발생했습니다')
  } finally {
    sharingPlan.value = false
  }
}

/**
 * 여행 기록 공유 실행
 */
const shareRecord = async (recordId) => {
  try {
    const recordResult = await recordApi.getRecord(recordId)
    if (recordResult.success) {
      const record = recordResult.data
      
      // 기록의 이미지 가져오기 (재시도 로직 포함)
      let imagesHtml = ''
      let retryCount = 0
      const maxRetries = 3
      
      while (retryCount < maxRetries && !imagesHtml) {
        try {
          console.log(`게시판 공유: 이미지 로드 시도 ${retryCount + 1}/${maxRetries}, recordId:`, recordId)
          const imagesResult = await recordApi.getRecordImages(recordId)
          console.log('게시판 공유: 이미지 로드 결과:', imagesResult)
          
          if (imagesResult.success && imagesResult.data && imagesResult.data.length > 0) {
            console.log('게시판 공유: 이미지 개수:', imagesResult.data.length)
            // 이미지들을 HTML img 태그로 변환
            imagesHtml = '<div style="margin: 20px 0;">' + 
              imagesResult.data.map((img, index) => {
                const imageUrl = img.imageUrl || ''
                if (!imageUrl) {
                  console.warn(`게시판 공유: 이미지 ${index + 1} URL이 없습니다`)
                  return ''
                }
                console.log(`게시판 공유: 이미지 ${index + 1} URL 길이:`, imageUrl.length)
                return `<img src="${imageUrl}" alt="여행 기록 이미지" style="max-width: 100%; height: auto; margin: 10px 0; border-radius: 8px;" />`
              }).filter(img => img !== '').join('') + 
              '</div>'
            console.log('게시판 공유: 생성된 이미지 HTML 길이:', imagesHtml.length)
            break
          } else {
            console.warn('게시판 공유: 이미지가 없거나 로드 실패')
            if (retryCount < maxRetries - 1) {
              await new Promise(resolve => setTimeout(resolve, 500))
            }
          }
        } catch (error) {
          console.error(`게시판 공유: 이미지 로드 오류 (시도 ${retryCount + 1}):`, error)
          if (retryCount < maxRetries - 1) {
            await new Promise(resolve => setTimeout(resolve, 500))
          }
        }
        retryCount++
      }
      
      // content에 이미지 추가
      const contentWithImages = record.content + (imagesHtml || '')
      
      const result = await boardApi.createPost({
        title: record.title,
        content: contentWithImages,
        category: 'TRAVEL_RECORD',
        regionCode: record.regionCode,
        tripType: record.tripType,
        season: record.season
      })
      
      if (result.success) {
        alert('여행 기록이 게시판에 공유되었습니다')
        showRecordModal.value = false
        // 등록한 게시글 상세 페이지로 이동
        if (result.data && result.data.postId) {
          router.push(`/post/${result.data.postId}`)
        } else {
          loadPosts()
        }
      }
    }
  } catch (error) {
    console.error('여행 기록 공유 오류:', error)
    alert('여행 기록 공유 중 오류가 발생했습니다')
  }
}

/**
 * HTML 컨텐츠에서 텍스트 요약 추출
 */
const getRecordSummary = (content) => {
  if (!content) return ''
  const div = document.createElement('div')
  div.innerHTML = content
  const text = div.textContent || div.innerText || ''
  return text.substring(0, 200) + (text.length > 200 ? '...' : '')
}

const isMyPost = (post) => {
  return authStore.user && authStore.user.userId === post.userId
}

const getRegionName = (regionCode) => {
  const regionMap = {
    '1': '서울/경기', '2': '부산', '3': '대구', '4': '인천', '5': '대전',
    '31': '서울/경기', '32': '강원', '33': '충청', '34': '충청',
    '35': '경상', '36': '경상', '37': '전라', '38': '전라', '39': '제주'
  }
  return regionMap[regionCode] || ''
}

const DEFAULT_IMAGE_URL = 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800'

const getPostImage = (post) => {
  // 국내 여행 공유 카테고리의 경우 planId로 가져온 firstImage 우선 사용
  if (post.firstImage) {
    return post.firstImage
  }
  // content에서 첫 번째 이미지 URL 추출
  if (post.content) {
    const imgRegex = /<img[^>]+src=["']([^"']+)["'][^>]*>/i
    const match = post.content.match(imgRegex)
    if (match && match[1]) {
      return match[1]
    }
  }
  // 이미지가 없으면 기본 이미지
  return DEFAULT_IMAGE_URL
}

const formatCount = (count) => {
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + '천'
  }
  return count.toString()
}

// content에서 태그 추출
const getPostTags = (post) => {
  if (!post.content) return []
  // HTML 주석에서 태그 추출: <!-- TAGS: #태그1 #태그2 -->
  const tagMatch = post.content.match(/<!--\s*TAGS:\s*([^>]+)\s*-->/)
  if (tagMatch && tagMatch[1]) {
    // 태그 문자열을 배열로 변환 (# 제거하지 않고 그대로 유지)
    return tagMatch[1].trim().split(/\s+/).filter(tag => tag.trim() !== '')
  }
  return []
}

// 필터링된 게시글 계산 (검색어 + 태그 필터)
const filteredPosts = computed(() => {
  let filtered = posts.value

  // 검색어 필터링 (제목 또는 내용에 포함)
  if (searchQuery.value && searchQuery.value.trim() !== '') {
    const query = searchQuery.value.trim().toLowerCase()
    filtered = filtered.filter(post => {
      // 제목 검색
      const titleMatch = post.title && post.title.toLowerCase().includes(query)
      
      // 내용 검색 (HTML 태그 제거)
      let contentText = ''
      if (post.content) {
        const div = document.createElement('div')
        div.innerHTML = post.content
        contentText = (div.textContent || div.innerText || '').toLowerCase()
      }
      const contentMatch = contentText.includes(query)
      
      return titleMatch || contentMatch
    })
  }

  // 태그 필터링
  if (selectedTag.value) {
    filtered = filtered.filter(post => {
      const postTags = getPostTags(post)
      return postTags.includes(selectedTag.value)
    })
  }

  return filtered
})

// 태그로 필터링
const filterByTag = (tag) => {
  selectedTag.value = tag
}

// 태그 필터 초기화
const clearTagFilter = () => {
  selectedTag.value = null
}

onMounted(() => {
  // URL 쿼리 파라미터에서 카테고리 확인
  if (route.query.category) {
    const category = route.query.category.toString()
    if (category === 'TRAVEL_RECORD' || category === 'TRAVEL_PLAN' || category === 'HOTPLACE') {
      currentCategory.value = category
    }
  }
  loadPosts()
})
</script>

<style scoped>
.tab-button.active {
  border-bottom-color: #60A5FA;
  color: #60A5FA;
}
</style>

<style>
/* 스크롤바로 인한 레이아웃 흔들림 방지 */
.board-view-container {
  scrollbar-gutter: stable;
}

/* 스크롤바를 항상 표시하여 레이아웃 흔들림 방지 */
html {
  overflow-y: scroll;
}
</style>

<style>
/* 스크롤바로 인한 레이아웃 흔들림 방지 */
.board-view-container {
  scrollbar-gutter: stable;
}

/* 스크롤바를 항상 표시하여 레이아웃 흔들림 방지 */
html {
  overflow-y: scroll;
}
</style>

