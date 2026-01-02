<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />
    <main class="flex-grow bg-background-light dark:bg-background-dark">
      <div class="container mx-auto px-4 py-8 sm:px-6 lg:px-8 max-w-4xl">
        <div v-if="loading" class="text-center py-20">
          <div class="animate-spin h-10 w-10 text-primary mx-auto mb-4"></div>
          <p class="text-gray-600 dark:text-text-secondary-dark">여행 기록을 불러오는 중...</p>
        </div>
        <div v-else-if="record">
          <article class="bg-white dark:bg-card-dark rounded-xl p-6 sm:p-8 border border-gray-200 dark:border-border-dark">
            <h1 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-text-dark mb-4">{{ record.title }}</h1>
            <p class="text-gray-600 dark:text-text-secondary-dark mb-6">{{ formatDate(record.createdAt) }}</p>
            
            <div v-if="record.images && record.images.length > 0" class="mb-6 grid grid-cols-2 gap-4">
              <img v-for="(img, index) in record.images" :key="index" :src="img.imageUrl" class="w-full rounded-lg" />
            </div>
            
            <div class="prose prose-invert max-w-none" v-html="record.content"></div>
            
            <div v-if="isMyRecord" class="mt-6 flex items-center gap-2">
              <button 
                @click="shareToBoard" 
                class="inline-flex items-center justify-center gap-2 rounded-md bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-400 transition hover:bg-blue-500/30"
              >
                <span class="material-symbols-outlined text-sm">share</span>
                커뮤니티에 공유하기
              </button>
              <button 
                @click="editRecord" 
                class="inline-flex items-center justify-center gap-2 rounded-md bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-400 transition hover:bg-blue-500/30"
              >
                <span class="material-symbols-outlined text-sm">edit</span>
                수정
              </button>
              <button 
                @click="deleteRecord" 
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
          이 여행 기록을 커뮤니티에 공유하시겠습니까?
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import recordApi from '@/services/api/record'
import boardApi from '@/services/api/board'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const record = ref(null)
const showShareModal = ref(false)

const isMyRecord = computed(() => {
  return authStore.user && record.value && authStore.user.userId === record.value.userId
})

const loadRecord = async () => {
  loading.value = true
  try {
    const recordId = route.params.id
    const [recordResult, imagesResult] = await Promise.all([
      recordApi.getRecord(recordId),
      recordApi.getRecordImages(recordId)
    ])
    
    if (recordResult.success) {
      record.value = recordResult.data
      if (imagesResult.success) {
        record.value.images = imagesResult.data || []
      }
    }
  } catch (error) {
    console.error('여행 기록 로드 오류:', error)
  } finally {
    loading.value = false
  }
}

const shareToBoard = () => {
  showShareModal.value = true
}

const confirmShare = async () => {
  showShareModal.value = false
  
  try {
    const recordId = route.params.id
    
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
    const contentWithImages = record.value.content + (imagesHtml || '')
    
    const result = await boardApi.createPost({
      title: record.value.title,
      content: contentWithImages,
      category: 'TRAVEL_RECORD'
    })
    
    if (result.success) {
      alert('커뮤니티에 공유되었습니다')
      // 등록한 게시글 상세 페이지로 이동
      if (result.data && result.data.postId) {
        router.push(`/post/${result.data.postId}`)
      } else {
        router.push('/board')
      }
    }
  } catch (error) {
    console.error('공유 오류:', error)
    alert('공유 중 오류가 발생했습니다')
  }
}

const editRecord = () => {
  router.push(`/record?edit=${record.value.recordId}`)
}

const deleteRecord = async () => {
  if (!(await confirm('정말로 이 여행 기록을 삭제하시겠습니까?'))) return
  
  try {
    const result = await recordApi.deleteRecord(record.value.recordId)
    if (result.success) {
      alert('여행 기록이 삭제되었습니다')
      router.push('/my-records')
    } else {
      alert(result.message || '삭제에 실패했습니다')
    }
  } catch (error) {
    console.error('삭제 오류:', error)
    alert('삭제 중 오류가 발생했습니다')
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

onMounted(() => {
  loadRecord()
})
</script>

