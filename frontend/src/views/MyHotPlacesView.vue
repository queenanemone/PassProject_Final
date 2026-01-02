<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />
    <main class="flex-grow bg-background-light dark:bg-background-dark overflow-y-auto relative">
      <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8 pb-24">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-3xl md:text-4xl font-extrabold text-gray-900 dark:text-text-dark mb-2">나의 핫플레이스</h1>
            <p class="text-gray-600 dark:text-text-secondary-dark">등록한 핫플레이스를 확인하고 커뮤니티에 공유해보세요.</p>
          </div>
          <button
            v-if="!loading && hotPlaces.length > 0"
            @click="router.push('/hotplace')"
            class="flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-white text-sm font-bold hover:bg-blue-400 transition-colors"
          >
            <span class="material-symbols-outlined text-base">add</span>
            <span class="hidden sm:inline">새 핫플레이스</span>
            <span class="sm:hidden">새로 등록</span>
          </button>
        </div>
        
        <div v-if="loading" class="text-center py-20">
          <div class="animate-spin h-10 w-10 text-primary mx-auto mb-4"></div>
          <p class="text-gray-600 dark:text-text-secondary-dark">핫플레이스를 불러오는 중...</p>
        </div>
        
        <div v-else-if="hotPlaces.length === 0" class="flex flex-col items-center justify-center py-16 text-center">
          <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-text-secondary-dark mb-4">location_on</span>
          <p class="text-xl font-bold text-gray-900 dark:text-text-dark mb-2">아직 등록한 핫플레이스가 없습니다</p>
          <p class="text-gray-600 dark:text-text-secondary-dark mb-6">첫 번째 핫플레이스를 등록해보세요!</p>
          <button
            @click="router.push('/hotplace')"
            class="flex items-center gap-2 rounded-lg bg-primary px-6 py-3 text-white font-bold hover:bg-blue-400 transition-colors"
          >
            <span class="material-symbols-outlined">add</span>
            <span>새 핫플레이스 등록하기</span>
          </button>
        </div>
        
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
          <div 
            v-for="hotPlace in hotPlaces" 
            :key="hotPlace.hotplaceId"
            @click="viewHotPlace(hotPlace.hotplaceId)"
            class="group relative flex flex-col overflow-hidden rounded-xl border border-gray-200 dark:border-border-dark bg-white dark:bg-card-dark shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg hover:border-primary/50 cursor-pointer"
          >
            <div v-if="hotPlace.images && hotPlace.images.length > 0" class="aspect-video w-full bg-cover bg-center rounded-t-xl mb-4" :style="{ backgroundImage: `url('${hotPlace.images[0].imageUrl}')` }"></div>
            <div v-else class="aspect-video w-full bg-gray-700 rounded-t-xl mb-4 flex items-center justify-center">
              <span class="material-symbols-outlined text-4xl text-gray-500">location_on</span>
            </div>
            <div class="flex flex-1 flex-col p-4">
              <div class="flex items-start justify-between mb-2">
                <h3 class="text-lg font-bold text-gray-900 dark:text-text-dark line-clamp-2 flex-1">{{ hotPlace.title }}</h3>
                <button
                  @click.stop="deleteHotPlace(hotPlace.hotplaceId)"
                  class="ml-2 text-gray-600 dark:text-text-secondary-dark hover:text-red-400 transition-colors opacity-0 group-hover:opacity-100"
                >
                  <span class="material-symbols-outlined text-sm">delete</span>
                </button>
              </div>
              <p class="text-gray-600 dark:text-text-secondary-dark text-sm mb-4 line-clamp-2">{{ hotPlace.description || '설명 없음' }}</p>
              <div class="mt-auto flex items-center justify-between text-xs text-gray-600 dark:text-text-secondary-dark">
                <span>{{ formatDate(hotPlace.createdAt) }}</span>
                <button
                  @click.stop="shareHotPlace(hotPlace)"
                  class="px-2 py-1 rounded bg-blue-500/20 text-blue-400 text-xs hover:bg-blue-500/30 transition-colors"
                >
                  공유하기
                </button>
              </div>
            </div>
          </div>
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
          <div class="rounded-full bg-purple-100 dark:bg-purple-900/30 p-3">
            <span class="material-symbols-outlined text-3xl text-purple-600 dark:text-purple-400">share</span>
          </div>
        </div>
        <h3 class="text-xl font-bold text-gray-900 dark:text-text-dark text-center mb-2">커뮤니티에 공유하기</h3>
        <p class="text-gray-600 dark:text-text-secondary-dark text-center mb-6">
          <span class="font-semibold text-gray-900 dark:text-text-dark">"{{ shareTarget?.title }}"</span>을(를) 커뮤니티에 공유하시겠습니까?
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
            class="flex-1 flex items-center justify-center rounded-lg h-11 px-4 bg-purple-600 text-white text-sm font-bold hover:bg-purple-700 transition-colors"
          >
            공유하기
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
import hotplaceApi from '@/services/api/hotplace'
import boardApi from '@/services/api/board'

const router = useRouter()

const loading = ref(false)
const hotPlaces = ref([])
const showShareModal = ref(false)
const shareTarget = ref(null)

const loadHotPlaces = async () => {
  loading.value = true
  try {
    const result = await hotplaceApi.getMyHotPlaces()
    if (result.success) {
      hotPlaces.value = result.data || []
      // 각 핫플레이스의 이미지 로드
      for (const hotPlace of hotPlaces.value) {
        try {
          const imageResult = await hotplaceApi.getImages(hotPlace.hotplaceId)
          if (imageResult.success && imageResult.data && imageResult.data.length > 0) {
            hotPlace.images = imageResult.data
          }
        } catch (error) {
          console.error(`핫플레이스 ${hotPlace.hotplaceId} 이미지 로드 오류:`, error)
        }
      }
    }
  } catch (error) {
    console.error('핫플레이스 로드 오류:', error)
    alert('핫플레이스를 불러오는 중 오류가 발생했습니다')
  } finally {
    loading.value = false
  }
}

const viewHotPlace = (hotplaceId) => {
  router.push(`/hotplace/${hotplaceId}`)
}

const deleteHotPlace = async (hotplaceId) => {
  if (!(await confirm('정말로 이 핫플레이스를 삭제하시겠습니까?'))) {
    return
  }
  
  try {
    const result = await hotplaceApi.deleteHotPlace(hotplaceId)
    if (result.success) {
      alert('핫플레이스가 삭제되었습니다')
      loadHotPlaces()
    } else {
      alert(result.message || '삭제에 실패했습니다')
    }
  } catch (error) {
    console.error('삭제 오류:', error)
    alert('삭제 중 오류가 발생했습니다')
  }
}

const shareHotPlace = (hotPlace) => {
  shareTarget.value = hotPlace
  showShareModal.value = true
}

const confirmShare = async () => {
  if (!shareTarget.value) return
  
  showShareModal.value = false
  const hotPlace = shareTarget.value
  
  try {
    // 이미지 포함하여 공유
    let content = hotPlace.description || ''
    if (hotPlace.images && hotPlace.images.length > 0) {
      const imagesHtml = hotPlace.images.map(img => {
        return `<img src="${img.imageUrl}" alt="HotPlace 이미지" style="max-width: 100%; height: auto; margin: 10px 0;" />`
      }).join('')
      content = content + imagesHtml
    }
    
    const shareResult = await boardApi.createPost({
      planId: null,
      title: hotPlace.title,
      content: content,
      regionCode: null,
      tripType: null,
      season: null,
      category: 'HOTPLACE'
    })
    
    if (shareResult.success) {
      alert('핫플레이스가 커뮤니티에 공유되었습니다!')
      router.push('/board')
    } else {
      alert(shareResult.message || '공유에 실패했습니다')
    }
  } catch (error) {
    console.error('공유 오류:', error)
    alert('공유 중 오류가 발생했습니다')
  } finally {
    shareTarget.value = null
  }
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

onMounted(() => {
  loadHotPlaces()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

