<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />
    <main class="flex-grow bg-background-light dark:bg-background-dark">
      <div class="container mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8 lg:py-12">
        <div class="mb-6">
          <button @click="goBack" class="inline-flex items-center gap-2 text-sm text-gray-600 dark:text-slate-400 hover:text-primary dark:hover:text-primary cursor-pointer">
            <span class="material-symbols-outlined">arrow_back</span>
            목록으로 돌아가기
          </button>
        </div>
        
        <div v-if="loading" class="text-center py-20">
          <div class="animate-spin h-10 w-10 text-primary mx-auto mb-4"></div>
          <p class="text-gray-600 dark:text-text-secondary-dark">게시글을 불러오는 중...</p>
        </div>
        
        <div v-else-if="post">
          <!-- 게시글 내용 -->
          <div class="rounded-xl border border-gray-200 dark:border-border-dark bg-white dark:bg-card-dark p-6 sm:p-8">
            <div class="mb-4 flex flex-col items-start gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div v-if="!editMode" id="postTitleDisplay" class="flex-1">
                <h1 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white">{{ post.title || '제목 없음' }}</h1>
              </div>
              <div v-else id="postTitleDisplay" class="flex-1 w-full">
                <input
                  v-model="editTitle"
                  type="text"
                  class="w-full rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark px-4 py-2 text-2xl font-bold text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
              <div v-if="isMyPost && !editMode" class="flex items-center gap-2">
                <button
                  @click="startEdit"
                  class="inline-flex items-center justify-center gap-2 rounded-md bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-400 transition hover:bg-blue-500/30"
                >
                  <span class="material-symbols-outlined text-sm">edit</span>
                  수정
                </button>
                <button
                  @click="handleDelete"
                  class="inline-flex items-center justify-center gap-2 rounded-md bg-red-500/20 px-3 py-2 text-xs font-medium text-red-400 transition hover:bg-red-500/30"
                >
                  <span class="material-symbols-outlined text-sm">delete</span>
                  삭제
                </button>
              </div>
            </div>
            
            <div class="mb-6 flex items-center gap-4 text-sm text-gray-600 dark:text-slate-400">
              <div class="flex items-center gap-2">
                <div
                  v-if="post.authorProfileImage"
                  class="size-6 rounded-full bg-cover bg-center bg-no-repeat"
                  :style="{ backgroundImage: `url('${post.authorProfileImage}')` }"
                ></div>
                <div
                  v-else
                  class="size-6 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center"
                >
                  <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                </div>
                <span class="font-medium text-gray-900 dark:text-white">{{ post.authorNickname || post.authorName || '작성자' }}</span>
              </div>
              <span class="text-gray-400 dark:text-slate-600">·</span>
              <div>조회수 {{ formatCount(post.viewCount || 0) }}</div>
              <span class="text-gray-400 dark:text-slate-600">·</span>
              <div>{{ formatDate(post.createdAt) }}</div>
            </div>
            
            <!-- 태그 표시 -->
            <div v-if="getPostTags(post).length > 0" class="mb-6 flex flex-wrap gap-2">
              <span 
                v-for="(tag, index) in getPostTags(post)" 
                :key="index"
                class="rounded-full bg-purple-600/20 px-3 py-1 text-xs font-semibold text-purple-400"
              >
                {{ tag }}
              </span>
            </div>
            
            <div class="border-t border-gray-200 dark:border-border-dark pt-6">
              <!-- 핫플레이스 뷰 (hotplaceId가 있고 카테고리가 HOTPLACE일 때) -->
              <div v-if="post.category === 'HOTPLACE' && post.hotplaceId && hotPlaceDetails" class="hotplace-view">
                <!-- 게시글 내용 -->
                <div v-if="!editMode" class="mb-8 prose dark:prose-invert min-w-full text-base leading-relaxed text-gray-900 dark:text-slate-300 post-content whitespace-pre-wrap">
                  <div v-html="getCleanContent(post)"></div>
                </div>
                <div v-else class="mb-8">
                  <textarea
                    v-model="editContent"
                    class="w-full resize-y rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark px-4 py-2 text-base leading-relaxed text-gray-900 dark:text-slate-300 focus:outline-none focus:ring-2 focus:ring-primary"
                    rows="10"
                  ></textarea>
                  <div class="mt-4 flex justify-end gap-2">
                    <button
                      @click="cancelEdit"
                      class="rounded-lg px-4 py-2 text-sm font-medium text-slate-400 hover:text-white transition-colors"
                    >
                      취소
                    </button>
                    <button
                      @click="savePost"
                      class="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-500"
                    >
                      저장
                    </button>
                  </div>
                </div>
                
                <!-- 핫플레이스 지도 -->
                <div v-if="hotPlaceDetails.latitude && hotPlaceDetails.longitude" class="mb-8">
                  <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-3">위치</h2>
                  <div class="w-full h-64 rounded-lg overflow-hidden border border-gray-200 dark:border-border-dark">
                    <div ref="hotPlaceMapContainer" class="w-full h-full"></div>
                  </div>
                </div>
                
                <!-- 좋아요/댓글 버튼 -->
                <div class="mt-8 flex items-center gap-6 text-gray-600 dark:text-slate-400">
                  <button
                    @click="toggleLike"
                    :class="['flex items-center gap-2 transition-colors hover:text-primary', post.liked ? 'text-red-500' : '']"
                  >
                    <span :class="['material-symbols-outlined', post.liked ? 'text-red-500 fill-current' : '']">{{ post.liked ? 'favorite' : 'favorite_border' }}</span>
                    <span class="text-sm font-medium">{{ formatCount(post.likeCount || 0) }}</span>
                  </button>
                  <div class="flex items-center gap-2">
                    <span class="material-symbols-outlined">chat_bubble</span>
                    <span class="text-sm font-medium">댓글 {{ totalCommentCount }}</span>
                  </div>
                </div>
              </div>
              
              <!-- 여행 계획 뷰 (planId가 있을 때) -->
              <div v-else-if="post.planId && planDetails" class="plan-view">
                <!-- 게시글 내용 (여행 계획 위에 표시) -->
                <div v-if="!editMode" class="mb-8 prose dark:prose-invert min-w-full text-base leading-relaxed text-gray-900 dark:text-slate-300 post-content whitespace-pre-wrap">
                  <div v-html="getCleanContent(post)"></div>
                </div>
                <div v-else class="mb-8">
                  <textarea
                    v-model="editContent"
                    class="w-full resize-y rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark px-4 py-2 text-base leading-relaxed text-gray-900 dark:text-slate-300 focus:outline-none focus:ring-2 focus:ring-primary"
                    rows="10"
                  ></textarea>
                  <div class="mt-4 flex justify-end gap-2">
                    <button
                      @click="cancelEdit"
                      class="rounded-lg px-4 py-2 text-sm font-medium text-slate-400 hover:text-white transition-colors"
                    >
                      취소
                    </button>
                    <button
                      @click="savePost"
                      class="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-500"
                    >
                      저장
                    </button>
                  </div>
                </div>
                
                <!-- 계획 헤더 -->
                <div class="mb-8 flex items-center justify-between bg-white dark:bg-card-dark p-6 rounded-2xl border border-gray-200 dark:border-white/5">
                  <div class="flex items-center gap-3">
                    <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ planDetails.plan.title }}</h1>
                  </div>
                  <div class="text-right">
                    <p class="text-sm text-gray-600 dark:text-gray-400">{{ planDetails.plan.departureDate }} ~ {{ planDetails.plan.arrivalDate }}</p>
                    <p class="text-xs text-gray-500 dark:text-gray-500 mt-1">성인 {{ planDetails.plan.adultCount }}, 아동 {{ planDetails.plan.childCount || 0 }}</p>
                  </div>
                </div>

                <!-- Day별 일정 -->
                <div class="flex flex-col gap-10">
                  <div v-for="day in dailyItinerary" :key="day.dateStr" class="w-full">
                    <div class="flex items-center justify-between p-4 mb-4 rounded-xl bg-white dark:bg-card-dark border border-gray-200 dark:border-border-dark shadow-xl">
                      <h2 class="text-2xl font-bold text-gray-900 dark:text-white">Day {{ day.dayNum }}: {{ day.dateStr }} ({{ getDayOfWeek(day.dateStr) }})</h2>
                    </div>
                    
                    <!-- Day별 지도 (관광지가 있을 때만 표시) -->
                    <div v-if="day.items.filter(item => item.type === 'attraction').length > 0" class="mb-6 h-80 rounded-xl overflow-hidden shadow-lg border border-white/10 relative">
                      <NaverMap 
                        :items="day.items.filter(item => item.type === 'attraction')" 
                        :read-only="true"
                      />
                      <div class="absolute top-3 left-3 bg-black/60 backdrop-blur-md px-3 py-1 rounded-lg text-xs text-white border border-white/10 pointer-events-none z-0">
                        Day {{ day.dayNum }} 경로
                      </div>
                    </div>

                    <!-- 일정 아이템들 -->
                    <div class="grid grid-cols-1 gap-4">
                      <div v-for="item in day.items" :key="item.uniqueId" class="group relative">
                        <!-- 교통편 -->
                        <div v-if="item.type === 'transport'" class="relative flex flex-col gap-3 rounded-xl bg-white dark:bg-card-dark p-4 transition-all h-full border border-gray-200 dark:border-transparent">
                          <div class="flex items-center gap-4 p-4 bg-gradient-to-r from-blue-900/40 to-blue-700/40 rounded-lg border border-blue-400/50 h-full">
                            <span class="material-symbols-outlined text-4xl text-blue-300 self-start mt-1">train</span>
                            <div class="flex-1 flex flex-col gap-1">
                              <div class="flex justify-between items-center">
                                <span class="text-base font-bold text-gray-900 dark:text-text-dark-primary">{{ item.data.transportType || '교통수단' }}</span>
                                <span v-if="item.data.price && item.data.price > 0" class="text-green-400 font-bold text-sm">{{ formatPrice(item.data.price) }}원</span>
                                <span v-else class="text-gray-500 text-xs">가격 정보 없음</span>
                              </div>
                              <div class="flex items-center gap-2 my-1">
                                <span class="text-xl font-bold text-white tracking-wide">{{ formatTime(item.data.departureTime) }}</span>
                                <span class="material-symbols-outlined text-gray-400 text-sm">arrow_forward</span>
                                <span class="text-xl font-bold text-white tracking-wide">{{ formatTime(item.data.arrivalTime) }}</span>
                              </div>
                              <div class="flex items-center gap-2 text-xs text-gray-600 dark:text-text-dark-secondary">
                                <span>{{ item.data.departureLocation || '출발' }}</span>
                                <span class="w-1 h-1 rounded-full bg-gray-600"></span>
                                <span>{{ item.data.arrivalLocation || '도착' }}</span>
                              </div>
                            </div>
                          </div>
                        </div>

                        <!-- 관광지/숙소 -->
                        <div v-else class="flex gap-4 rounded-xl bg-white dark:bg-card-dark p-4 transition-all overflow-hidden relative h-full border border-gray-200 dark:border-transparent">
                          <div class="aspect-square w-24 h-24 overflow-hidden rounded-lg bg-gray-700 relative shrink-0">
                            <img v-if="getImageUrl(item.data)" :src="getImageUrl(item.data)" :alt="getTitle(item.data)" class="w-full h-full object-cover" />
                            <div v-else class="flex items-center justify-center h-full bg-gray-700">
                              <span class="material-symbols-outlined text-3xl text-gray-500">image</span>
                            </div>
                          </div>
                          <div class="flex flex-col gap-1 flex-1 py-1">
                            <p class="text-lg font-bold text-gray-900 dark:text-text-dark-primary line-clamp-1">{{ getTitle(item.data) }}</p>
                            <p class="text-sm text-gray-600 dark:text-text-dark-secondary line-clamp-2">{{ getAddress(item.data) }}</p>
                            <span v-if="item.type === 'hotel'" class="text-xs text-green-400 font-medium mt-auto">🏨 숙소 체크인 예정</span>
                          </div>
                        </div>
                      </div>
                      <div v-if="day.items.length === 0" class="text-gray-500 text-sm text-center py-8">
                        일정이 없습니다
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- 좋아요/댓글 버튼 -->
                <div class="mt-8 flex items-center gap-6 text-gray-600 dark:text-slate-400">
                  <button
                    @click="toggleLike"
                    :class="['flex items-center gap-2 transition-colors hover:text-primary', post.liked ? 'text-red-500' : '']"
                  >
                    <span :class="['material-symbols-outlined', post.liked ? 'text-red-500 fill-current' : '']">{{ post.liked ? 'favorite' : 'favorite_border' }}</span>
                    <span class="text-sm font-medium">{{ formatCount(post.likeCount || 0) }}</span>
                  </button>
                  <div class="flex items-center gap-2">
                    <span class="material-symbols-outlined">chat_bubble</span>
                    <span class="text-sm font-medium">댓글 {{ totalCommentCount }}</span>
                  </div>
                </div>
              </div>
              
              <!-- 일반 게시글 내용 -->
              <div v-else>
                <div v-if="!editMode" id="postContentDisplay" class="prose dark:prose-invert min-w-full text-base leading-relaxed text-gray-900 dark:text-slate-300 post-content whitespace-pre-wrap">
                  <div v-html="getCleanContent(post)"></div>
                </div>
                <div v-else id="postContentDisplay" class="w-full">
                  <textarea
                    v-model="editContent"
                    class="w-full resize-y rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark px-4 py-2 text-base leading-relaxed text-gray-900 dark:text-slate-300 focus:outline-none focus:ring-2 focus:ring-primary"
                    rows="10"
                  ></textarea>
                  <div class="mt-4 flex justify-end gap-2">
                    <button
                      @click="cancelEdit"
                      class="rounded-lg px-4 py-2 text-sm font-medium text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white transition-colors"
                    >
                      취소
                    </button>
                    <button
                      @click="savePost"
                      class="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-500"
                    >
                      저장
                    </button>
                  </div>
                </div>
                
                <div class="mt-8 flex items-center gap-6 text-gray-600 dark:text-slate-400">
                  <button
                    @click="toggleLike"
                    :class="['flex items-center gap-2 transition-colors hover:text-primary', post.liked ? 'text-red-500' : '']"
                  >
                    <span :class="['material-symbols-outlined', post.liked ? 'text-red-500 fill-current' : '']">{{ post.liked ? 'favorite' : 'favorite_border' }}</span>
                    <span class="text-sm font-medium">{{ formatCount(post.likeCount || 0) }}</span>
                  </button>
                  <div class="flex items-center gap-2">
                    <span class="material-symbols-outlined">chat_bubble</span>
                    <span class="text-sm font-medium">댓글 {{ totalCommentCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 댓글 섹션 -->
          <div class="mt-8 rounded-xl border border-gray-200 dark:border-border-dark bg-white dark:bg-card-dark p-6 sm:p-8">
            <h2 class="mb-6 text-xl font-bold text-gray-900 dark:text-white">댓글</h2>
            <div v-if="comments.length === 0" class="text-gray-600 dark:text-slate-400 text-center py-4">
              댓글이 없습니다
            </div>
            <div v-else class="space-y-6">
              <div
                v-for="comment in comments"
                :key="comment.commentId"
                :id="`comment-${comment.commentId}`"
                class="comment-item"
              >
                <CommentItem
                  :comment="comment"
                  :current-user-id="authStore.user?.userId"
                  :post-id="post.postId"
                  @reload="loadPost"
                />
              </div>
            </div>
            
            <!-- 댓글 작성 -->
            <div class="mt-8 border-t border-gray-200 dark:border-border-dark pt-6">
              <div class="flex items-start gap-4">
                <div
                  v-if="authStore.user?.profileImage"
                  class="size-9 flex-shrink-0 rounded-full bg-cover bg-center bg-no-repeat"
                  :style="{ backgroundImage: `url('${authStore.user.profileImage}')` }"
                ></div>
                <div
                  v-else
                  class="size-9 flex-shrink-0 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center"
                >
                  <span class="material-symbols-outlined text-sm text-slate-500">person</span>
                </div>
                <div class="flex-grow">
                  <textarea
                    v-model="newComment"
                    class="form-textarea w-full resize-none rounded-lg border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark text-gray-900 dark:text-slate-300 placeholder:text-gray-400 dark:placeholder:text-slate-500 focus:border-primary focus:ring-primary/50 px-4 py-3"
                    placeholder="댓글을 입력하세요..."
                    rows="3"
                  ></textarea>
                  <div class="mt-2 flex justify-end">
                    <button
                      @click="addComment"
                      class="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-500"
                    >
                      작성
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Header from '@/components/Header.vue'
import boardApi from '@/services/api/board'
import planApi from '@/services/api/plan'
import hotplaceApi from '@/services/api/hotplace'
import CommentItem from '@/components/CommentItem.vue'
import NaverMap from '@/components/common/NaverMap.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const post = ref(null)
const comments = ref([])
const newComment = ref('')
const editMode = ref(false)
const editTitle = ref('')
const editContent = ref('')
const planDetails = ref(null)
const dailyItinerary = ref([])
const hotPlaceDetails = ref(null)
const hotPlaceMapContainer = ref(null)
let hotPlaceMap = null

const isMyPost = computed(() => {
  return authStore.user && post.value && authStore.user.userId === post.value.userId
})

const totalCommentCount = computed(() => {
  let count = comments.value.length
  comments.value.forEach(comment => {
    if (comment.replies) {
      count += comment.replies.length
    }
  })
  return count
})

const loadPost = async () => {
  loading.value = true
  try {
    const postId = route.params.id
    const [postResult, commentsResult] = await Promise.all([
      boardApi.getPost(postId),
      boardApi.getComments(postId)
    ])
    
    if (postResult.success) {
      post.value = postResult.data
      
      // 여행 계획이 연결된 경우 계획 정보 로드
      if (post.value.planId) {
        await loadPlanDetails(post.value.planId)
      }
      
      // 핫플레이스가 연결된 경우 핫플레이스 정보 로드
      if (post.value.category === 'HOTPLACE' && post.value.hotplaceId) {
        await loadHotPlaceDetails(post.value.hotplaceId)
      }
    }
    if (commentsResult.success) {
      comments.value = commentsResult.data || []
    }
  } catch (error) {
    console.error('게시글 로드 오류:', error)
  } finally {
    loading.value = false
  }
}

// 여행 계획 상세 로드
const loadPlanDetails = async (planId) => {
  try {
    const result = await planApi.getPlan(planId)
    if (result.success) {
      planDetails.value = result.data
      dailyItinerary.value = organizeItemsByDate(result.data)
    }
  } catch (error) {
    console.error('여행 계획 로드 오류:', error)
  }
}

// 핫플레이스 상세 로드
const loadHotPlaceDetails = async (hotplaceId) => {
  try {
    const result = await hotplaceApi.getHotPlace(hotplaceId)
    if (result.success) {
      hotPlaceDetails.value = result.data
    }
  } catch (error) {
    console.error('핫플레이스 로드 오류:', error)
  }
}

// 핫플레이스 지도 초기화를 위한 watch
watch([() => hotPlaceDetails.value, () => hotPlaceMapContainer.value], async ([hotPlaceData, container]) => {
  if (hotPlaceData && container && hotPlaceData.latitude && hotPlaceData.longitude) {
    await nextTick()
    setTimeout(() => {
      initHotPlaceMap()
    }, 100)
  }
}, { immediate: true })

// 핫플레이스 지도 초기화
const initHotPlaceMap = async () => {
  // 이미 지도가 초기화되어 있으면 중복 초기화 방지
  if (hotPlaceMap) {
    console.log('핫플레이스 지도가 이미 초기화되어 있습니다')
    return
  }
  
  if (!hotPlaceMapContainer.value || !hotPlaceDetails.value) {
    console.warn('핫플레이스 지도 컨테이너 또는 데이터가 없습니다')
    return
  }
  
  // Naver Maps API 로드 대기
  if (!window.naver || !window.naver.maps) {
    await new Promise((resolve) => {
      const interval = setInterval(() => {
        if (window.naver && window.naver.maps) {
          clearInterval(interval)
          resolve()
        }
      }, 100)
    })
  }
  
  try {
    const lat = parseFloat(hotPlaceDetails.value.latitude)
    const lng = parseFloat(hotPlaceDetails.value.longitude)
    
    if (isNaN(lat) || isNaN(lng)) {
      console.warn('유효하지 않은 좌표입니다:', { lat, lng })
      return
    }
    
    const mapOptions = {
      center: new window.naver.maps.LatLng(lat, lng),
      zoom: 15
    }
    
    hotPlaceMap = new window.naver.maps.Map(hotPlaceMapContainer.value, mapOptions)
    
    // 마커 추가
    const marker = new window.naver.maps.Marker({
      position: new window.naver.maps.LatLng(lat, lng),
      map: hotPlaceMap
    })
    
    // 인포윈도우 추가
    const infoWindowContent = `
      <div style="
        padding: 10px; 
        font-weight: bold; 
        color: #1f2937; 
        background-color: #ffffff;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
      ">
        ${hotPlaceDetails.value.title}
      </div>
    `
    const infoWindow = new window.naver.maps.InfoWindow({
      content: infoWindowContent,
      backgroundColor: '#ffffff',
      borderColor: '#e5e7eb',
      borderWidth: 1
    })
    
    infoWindow.open(hotPlaceMap, marker)
  } catch (error) {
    console.error('핫플레이스 지도 초기화 오류:', error)
  }
}

// 날짜별로 항목 그룹화 (DashboardView와 동일한 로직)
const organizeItemsByDate = (data) => {
  const plan = data.plan
  if (!plan.departureDate || !plan.arrivalDate) return []

  const startDate = new Date(plan.departureDate)
  const endDate = new Date(plan.arrivalDate)
  const dayMap = new Map()
  let currentDate = new Date(startDate)
  let dayNum = 1

  while (currentDate <= endDate) {
    const dateStr = currentDate.toISOString().split('T')[0]
    dayMap.set(dateStr, {
      dayNum: dayNum++,
      dateStr: dateStr,
      items: []
    })
    currentDate.setDate(currentDate.getDate() + 1)
  }

  const addItem = (dateStr, type, data, sortKey) => {
    const day = dayMap.get(dateStr)
    if (day) {
      let dbId = ''
      if (type === 'attraction') dbId = data.planDestination?.planDestinationId
      else if (type === 'hotel') dbId = data.planAccommodationId
      else if (type === 'transport') dbId = data.transportationId
      
      if (!dbId) dbId = Math.random().toString(36).substr(2, 9)

      const uniqueId = `${type}-${dbId}`
      day.items.push({ type, data, sortKey, uniqueId })
    }
  }

  // 관광지
  ;(data.destinationDetails || []).forEach(d => {
    const visitDateTimeStr = d.planDestination.visitDate
    if (visitDateTimeStr) {
      const datePart = visitDateTimeStr.toString().split('T')[0]
      const sortKey = visitDateTimeStr.toString().includes('T')
        ? visitDateTimeStr
        : `${datePart}T10:00:00`
      addItem(datePart, 'attraction', d, sortKey)
    }
  })

  // 숙소
  ;(data.accommodations || []).forEach(a => {
    const checkInDateStr = a.checkInDate
    if (checkInDateStr) {
      const datePart = checkInDateStr.toString().split('T')[0]
      const sortKey = `${datePart}T15:00:00`
      addItem(datePart, 'hotel', a, sortKey)
    }
  })

  // 교통
  ;(data.transportations || []).forEach(t => {
    const departureTimeStr = t.departureTime
    if (departureTimeStr) {
      const datePart = departureTimeStr.toString().split('T')[0]
      const sortKey = departureTimeStr
      addItem(datePart, 'transport', t, sortKey)
    }
  })

  const finalItinerary = Array.from(dayMap.values())
  finalItinerary.forEach(day => {
    day.items.sort((a, b) => {
      if (a.sortKey < b.sortKey) return -1
      if (a.sortKey > b.sortKey) return 1
      if (a.type < b.type) return -1
      if (a.type > b.type) return 1
      return 0
    })
  })

  return finalItinerary
}

// 유틸리티 함수들
const getDayOfWeek = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR', { weekday: 'short' })
}

const formatTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  return dateTimeStr.toString().split('T')[1]?.substring(0, 5) || ''
}

const formatPrice = (price) => {
  return Number(price).toLocaleString()
}

const getTitle = (itemData) => {
  return itemData.tourInfo?.title || '정보 없음'
}

const getAddress = (itemData) => {
  return itemData.tourInfo?.addr1 || ''
}

const getImageUrl = (itemData) => {
  return itemData.tourInfo?.firstImage || itemData.tourInfo?.firstImage2 || null
}

const goBack = () => {
  // 게시글의 카테고리에 따라 해당 카테고리로 이동
  if (post.value && post.value.category) {
    router.push(`/board?category=${post.value.category}`)
  } else {
    router.push('/board')
  }
}

const startEdit = () => {
  editMode.value = true
  editTitle.value = post.value.title
  editContent.value = post.value.content
}

const cancelEdit = () => {
  editMode.value = false
  editTitle.value = ''
  editContent.value = ''
}

const savePost = async () => {
  if (!editTitle.value.trim()) {
    alert('제목을 입력해주세요')
    return
  }
  
  if (!editContent.value.trim()) {
    alert('내용을 입력해주세요')
    return
  }
  
  try {
    // 백엔드가 Map<String, String>을 기대하므로 형식 맞춤
    const result = await boardApi.updatePost(route.params.id, {
      title: editTitle.value.trim(),
      content: editContent.value.trim()
    })
    
    if (result.success) {
      alert('게시글이 수정되었습니다')
      editMode.value = false
      loadPost()
    } else {
      alert(result.message || '게시글 수정에 실패했습니다')
    }
  } catch (error) {
    console.error('게시글 수정 오류:', error)
    alert('게시글 수정 중 오류가 발생했습니다')
  }
}

const handleDelete = async () => {
  if (!(await confirm('정말 이 게시글을 삭제하시겠습니까?'))) return
  
  try {
    const result = await boardApi.deletePost(route.params.id)
    if (result.success) {
      alert('게시글이 삭제되었습니다')
      router.push('/board')
    } else {
      alert(result.message || '게시글 삭제에 실패했습니다')
    }
  } catch (error) {
    console.error('게시글 삭제 오류:', error)
    alert('게시글 삭제 중 오류가 발생했습니다')
  }
}

const toggleLike = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다')
    router.push('/login')
    return
  }
  
  try {
    const result = await boardApi.likePost(route.params.id)
    if (result.success && result.data) {
      // 새로고침 없이 좋아요 수만 업데이트
      if (post.value) {
        post.value.likeCount = result.data.likeCount ?? post.value.likeCount ?? 0
        post.value.liked = result.data.liked !== undefined ? result.data.liked : (post.value.liked ?? false)
      }
    }
  } catch (error) {
    console.error('좋아요 오류:', error)
  }
}

const addComment = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다')
    router.push('/login')
    return
  }
  
  if (!newComment.value.trim()) {
    alert('댓글을 입력해주세요')
    return
  }
  
  try {
    const commentData = {
      content: newComment.value.trim()
    }
    console.log('댓글 작성 요청:', {
      postId: route.params.id,
      data: commentData
    })
    const result = await boardApi.addComment(route.params.id, commentData)
    if (result.success) {
      newComment.value = ''
      loadPost()
    } else {
      alert(result.message || '댓글 작성에 실패했습니다')
    }
  } catch (error) {
    console.error('댓글 작성 오류:', error)
    console.error('에러 상세:', {
      status: error.response?.status,
      data: error.response?.data,
      message: error.response?.data?.message
    })
    const errorMessage = error.response?.data?.message || error.message || '댓글 작성 중 오류가 발생했습니다'
    alert(`댓글 작성 실패: ${errorMessage}`)
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}

const formatCount = (count) => {
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + '천'
  }
  return count.toString()
}

// content에서 태그 추출
const getPostTags = (post) => {
  if (!post || !post.content) return []
  // HTML 주석에서 태그 추출: <!-- TAGS: #태그1 #태그2 -->
  const tagMatch = post.content.match(/<!--\s*TAGS:\s*([^>]+)\s*-->/)
  if (tagMatch && tagMatch[1]) {
    // 태그 문자열을 배열로 변환 (# 제거하지 않고 그대로 유지)
    return tagMatch[1].trim().split(/\s+/).filter(tag => tag.trim() !== '')
  }
  return []
}

// content에서 태그 주석 제거 및 줄바꿈 처리
const getCleanContent = (post) => {
  if (!post || !post.content) return ''
  let content = post.content
  // HTML 주석 제거: <!-- TAGS: ... -->
  content = content.replace(/<!--\s*TAGS:\s*[^>]+\s*-->/g, '')
  // 줄바꿈 문자를 <br> 태그로 변환
  content = content.replace(/\n/g, '<br>')
  return content
}

onMounted(() => {
  loadPost()
})
</script>

<style scoped>
.post-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 20px 0;
  display: block;
}

.post-content :deep(div) {
  margin: 20px 0;
}

.post-content {
  color: #111827;
}

html.dark .post-content {
  color: #E5E7EB;
}

/* prose 스타일 오버라이드 - 라이트 모드에서 텍스트가 검은색으로 보이도록 */
.post-content :deep(p),
.post-content :deep(span),
.post-content :deep(div),
.post-content :deep(li),
.post-content :deep(h1),
.post-content :deep(h2),
.post-content :deep(h3),
.post-content :deep(h4),
.post-content :deep(h5),
.post-content :deep(h6) {
  color: #111827;
}

html.dark .post-content :deep(p),
html.dark .post-content :deep(span),
html.dark .post-content :deep(div),
html.dark .post-content :deep(li),
html.dark .post-content :deep(h1),
html.dark .post-content :deep(h2),
html.dark .post-content :deep(h3),
html.dark .post-content :deep(h4),
html.dark .post-content :deep(h5),
html.dark .post-content :deep(h6) {
  color: #E5E7EB;
}
</style>
