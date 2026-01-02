<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />
    <main class="flex-grow bg-background-light dark:bg-background-dark">
      <div class="container mx-auto px-4 py-8 sm:px-6 lg:px-8 max-w-4xl">
        <div class="mb-6">
          <button @click="goBack" class="inline-flex items-center gap-2 text-sm text-gray-600 dark:text-slate-400 hover:text-primary dark:hover:text-primary cursor-pointer">
            <span class="material-symbols-outlined">arrow_back</span>
            목록으로 돌아가기
          </button>
        </div>
        
        <div v-if="loading" class="text-center py-20">
          <div class="animate-spin h-10 w-10 text-primary mx-auto mb-4"></div>
          <p class="text-gray-600 dark:text-text-secondary-dark">핫플레이스를 불러오는 중...</p>
        </div>
        
        <div v-else-if="hotPlace">
          <article class="bg-white dark:bg-card-dark rounded-xl p-6 sm:p-8 border border-gray-200 dark:border-border-dark">
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-text-dark mb-4">{{ hotPlace.title }}</h1>
            <p class="text-gray-600 dark:text-text-secondary-dark mb-2">{{ hotPlace.address || '주소 없음' }}</p>
            <p class="text-gray-500 dark:text-text-secondary-dark text-sm mb-6">{{ formatDate(hotPlace.createdAt) }}</p>
            
            <!-- 이미지 표시 -->
            <div v-if="hotPlace.images && hotPlace.images.length > 0" class="mb-6 grid grid-cols-2 gap-4">
              <img 
                v-for="(img, index) in hotPlace.images" 
                :key="index" 
                :src="img.imageUrl" 
                :alt="hotPlace.title"
                class="w-full rounded-lg object-cover"
              />
            </div>
            
            <!-- 설명 -->
            <div v-if="hotPlace.description" class="mb-6">
              <h2 class="text-xl font-bold text-gray-900 dark:text-text-dark mb-3">설명</h2>
              <div class="prose prose-invert max-w-none text-gray-700 dark:text-text-secondary-dark whitespace-pre-wrap">{{ hotPlace.description }}</div>
            </div>
            
            <!-- 지도 (좌표가 있는 경우) -->
            <div v-if="hotPlace.latitude && hotPlace.longitude" class="mb-6">
              <h2 class="text-xl font-bold text-gray-900 dark:text-text-dark mb-3">위치</h2>
              <div class="w-full h-64 rounded-lg overflow-hidden border border-gray-200 dark:border-border-dark">
                <div ref="mapContainer" class="w-full h-full"></div>
              </div>
            </div>
            
            <!-- 액션 버튼 -->
            <div v-if="isMyHotPlace" class="mt-6 flex items-center gap-2">
              <button 
                @click="shareToBoard" 
                class="inline-flex items-center justify-center gap-2 rounded-md bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-400 transition hover:bg-blue-500/30"
              >
                <span class="material-symbols-outlined text-sm">share</span>
                커뮤니티에 공유하기
              </button>
              <button 
                @click="editHotPlace" 
                class="inline-flex items-center justify-center gap-2 rounded-md bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-400 transition hover:bg-blue-500/30"
              >
                <span class="material-symbols-outlined text-sm">edit</span>
                수정
              </button>
              <button 
                @click="deleteHotPlace" 
                class="inline-flex items-center justify-center gap-2 rounded-md bg-red-500/20 px-3 py-2 text-xs font-medium text-red-400 transition hover:bg-red-500/30"
              >
                <span class="material-symbols-outlined text-sm">delete</span>
                삭제
              </button>
            </div>
          </article>
        </div>
      </div>
    </main>
    
    <!-- 공유 확인 모달 -->
    <div
      v-if="showShareModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      @click.self="showShareModal = false"
    >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-md w-full mx-4">
        <div class="flex items-center justify-center mb-4">
          <div class="rounded-full bg-blue-100 dark:bg-blue-900/30 p-3">
            <span class="material-symbols-outlined text-3xl text-blue-600 dark:text-blue-400">share</span>
          </div>
        </div>
        <h3 class="text-xl font-bold text-gray-900 dark:text-text-dark text-center mb-2">커뮤니티에 공유하기</h3>
        <p class="text-gray-600 dark:text-text-secondary-dark text-center mb-6">
          <span class="font-semibold text-gray-900 dark:text-text-dark">"{{ hotPlace?.title }}"</span>을(를) 커뮤니티에 공유하시겠습니까?
        </p>
        <div class="flex gap-3">
          <button
            @click="showShareModal = false"
            class="flex-1 flex items-center justify-center rounded-lg h-11 px-4 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark text-sm font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
          >
            취소
          </button>
          <button
            @click="confirmShare"
            class="flex-1 flex items-center justify-center rounded-lg h-11 px-4 bg-blue-600 text-white text-sm font-bold hover:bg-blue-700 transition-colors"
          >
            공유하기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import hotplaceApi from '@/services/api/hotplace'
import boardApi from '@/services/api/board'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const hotPlace = ref(null)
const mapContainer = ref(null)
const showShareModal = ref(false)
let map = null

const isMyHotPlace = computed(() => {
  return authStore.user && hotPlace.value && authStore.user.userId === hotPlace.value.userId
})

const loadHotPlace = async () => {
  loading.value = true
  try {
    const hotplaceId = route.params.id
    const [hotPlaceResult, imagesResult] = await Promise.all([
      hotplaceApi.getHotPlace(hotplaceId),
      hotplaceApi.getImages(hotplaceId)
    ])
    
    if (hotPlaceResult.success) {
      hotPlace.value = hotPlaceResult.data
      if (imagesResult.success) {
        hotPlace.value.images = imagesResult.data || []
      }
    }
  } catch (error) {
    console.error('핫플레이스 로드 오류:', error)
    alert('핫플레이스를 불러오는 중 오류가 발생했습니다')
  } finally {
    loading.value = false
  }
}

const initMap = async () => {
  // 이미 지도가 초기화되어 있으면 중복 초기화 방지
  if (map) {
    console.log('지도가 이미 초기화되어 있습니다')
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
  
  if (!mapContainer.value || !hotPlace.value) {
    console.warn('지도 컨테이너 또는 핫플레이스 데이터가 없습니다')
    return
  }
  
  try {
    const lat = parseFloat(hotPlace.value.latitude)
    const lng = parseFloat(hotPlace.value.longitude)
    
    if (isNaN(lat) || isNaN(lng)) {
      console.warn('유효하지 않은 좌표입니다:', { lat, lng })
      return
    }
    
    const mapOptions = {
      center: new window.naver.maps.LatLng(lat, lng),
      zoom: 15
    }
    
    map = new window.naver.maps.Map(mapContainer.value, mapOptions)
    
    // 마커 추가
    const marker = new window.naver.maps.Marker({
      position: new window.naver.maps.LatLng(lat, lng),
      map: map
    })
    
    // 인포윈도우 추가 (항상 흰색 배경, 검은색 글자)
    const infoWindowContent = `
      <div style="
        padding: 10px; 
        font-weight: bold; 
        color: #1f2937; 
        background-color: #ffffff;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
      ">
        ${hotPlace.value.title}
      </div>
    `
    const infoWindow = new window.naver.maps.InfoWindow({
      content: infoWindowContent,
      backgroundColor: '#ffffff',
      borderColor: '#e5e7eb',
      borderWidth: 1
    })
    
    infoWindow.open(map, marker)
  } catch (error) {
    console.error('지도 초기화 오류:', error)
  }
}

const shareToBoard = () => {
  showShareModal.value = true
}

const confirmShare = async () => {
  showShareModal.value = false
  
  try {
    let content = hotPlace.value.description || ''
    if (hotPlace.value.images && hotPlace.value.images.length > 0) {
      const imagesHtml = hotPlace.value.images.map(img => {
        return `<img src="${img.imageUrl}" alt="HotPlace 이미지" style="max-width: 100%; height: auto; margin: 10px 0;" />`
      }).join('')
      content = content + imagesHtml
    }
    
    const result = await boardApi.createPost({
      planId: null,
      hotplaceId: hotPlace.value.hotplaceId,
      title: hotPlace.value.title,
      content: content,
      regionCode: null,
      tripType: null,
      season: null,
      category: 'HOTPLACE'
    })
    
    if (result.success) {
      alert('커뮤니티에 공유되었습니다!')
      // 등록한 게시글 상세 페이지로 이동
      if (result.data && result.data.postId) {
        router.push(`/post/${result.data.postId}`)
      } else {
        router.push('/board')
      }
    } else {
      alert(result.message || '공유에 실패했습니다')
    }
  } catch (error) {
    console.error('공유 오류:', error)
    alert('공유 중 오류가 발생했습니다')
  }
}

const editHotPlace = () => {
  router.push(`/hotplace?edit=${hotPlace.value.hotplaceId}`)
}

const deleteHotPlace = async () => {
  if (!(await confirm('정말로 이 핫플레이스를 삭제하시겠습니까?'))) return
  
  try {
    const result = await hotplaceApi.deleteHotPlace(hotPlace.value.hotplaceId)
    if (result.success) {
      alert('핫플레이스가 삭제되었습니다')
      router.push('/my-hotplaces')
    } else {
      alert(result.message || '삭제에 실패했습니다')
    }
  } catch (error) {
    console.error('삭제 오류:', error)
    alert('삭제 중 오류가 발생했습니다')
  }
}

const goBack = () => {
  router.push('/my-hotplaces')
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

// 지도 초기화를 위한 watch
watch([() => hotPlace.value, () => mapContainer.value], async ([hotPlaceData, container]) => {
  if (hotPlaceData && container && hotPlaceData.latitude && hotPlaceData.longitude) {
    await nextTick()
    // DOM 렌더링을 위한 추가 대기
    setTimeout(() => {
      initMap()
    }, 100)
  }
}, { immediate: true })

onMounted(() => {
  loadHotPlace()
})
</script>

