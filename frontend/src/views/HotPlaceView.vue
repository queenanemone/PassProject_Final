<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />

    <main class="flex-grow bg-background-light dark:bg-background-dark">
      <div class="max-w-6xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <!-- 메인 타이틀 -->
        <div class="mb-8">
          <h1 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-text-dark mb-2">나만의 HotPlace 등록</h1>
          <p class="text-gray-600 dark:text-text-secondary-dark text-base">지도에서 장소를 검색하고 선택해서 나만의 핫플레이스를 등록해보세요.</p>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 왼쪽: 지도 -->
          <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-4">
            <div class="h-[600px]">
              <HotPlaceMap @place-selected="handlePlaceSelected" />
            </div>
          </div>

          <!-- 오른쪽: 폼 -->
          <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 flex flex-col gap-6">
            <!-- 제목 -->
            <div>
              <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block">제목 *</label>
              <input
                v-model="form.title"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 placeholder:text-gray-400 dark:placeholder:text-text-secondary-dark px-4 text-base"
                placeholder="HotPlace 제목을 입력해주세요"
                type="text"
              />
            </div>

            <!-- 설명 -->
            <div>
              <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block">설명</label>
              <textarea
                v-model="form.description"
                class="w-full resize-y rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark min-h-32 placeholder:text-gray-400 dark:placeholder:text-text-secondary-dark p-4 text-base"
                placeholder="이 장소에 대한 설명을 입력해주세요"
              ></textarea>
            </div>

            <!-- 선택된 장소 정보 -->
            <div v-if="selectedPlace" class="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
              <h3 class="text-sm font-semibold text-gray-900 dark:text-white mb-2">선택된 장소</h3>
              <p class="text-sm text-gray-700 dark:text-gray-300 font-medium mb-1">{{ selectedPlace.title }}</p>
              <p class="text-xs text-gray-600 dark:text-gray-400">{{ selectedPlace.addr1 || '주소 없음' }}</p>
            </div>
            <div v-else class="p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg border border-yellow-200 dark:border-yellow-800">
              <p class="text-sm text-yellow-800 dark:text-yellow-300">지도에서 장소를 검색하고 선택해주세요.</p>
            </div>

            <!-- 사진 -->
            <div>
              <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block">사진</label>
              <div
                :class="[
                  'flex flex-col items-center justify-center gap-4 rounded-lg border-2 border-dashed p-6 min-h-[200px] transition-colors',
                  isDragging ? 'border-primary bg-primary/10' : 'border-gray-200 dark:border-border-dark'
                ]"
                @dragover.prevent="handleDragOver"
                @dragleave.prevent="handleDragLeave"
                @drop.prevent="handleDrop"
              >
                <div v-if="images.length === 0" class="flex flex-col items-center gap-3 text-center">
                  <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-text-secondary-dark">upload_file</span>
                  <p class="text-gray-900 dark:text-text-dark text-base font-semibold">사진 추가</p>
                  <p class="text-gray-600 dark:text-text-secondary-dark text-sm">이곳에 파일을 드래그하세요</p>
                  <label class="mt-2 px-4 py-2 rounded-lg bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark text-sm font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors cursor-pointer">
                    컴퓨터에서 선택
                    <input type="file" accept="image/*" multiple class="hidden" @change="handleFileSelect" />
                  </label>
                </div>
                <div v-else class="w-full">
                  <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 mb-4">
                    <div v-for="(img, index) in images" :key="index" class="relative group aspect-square">
                      <img :src="img.url" :alt="`Preview ${index + 1}`" class="w-full h-full object-cover rounded-lg" />
                      <button
                        @click="removeImage(index)"
                        class="absolute top-1 right-1 bg-black/60 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <span class="material-symbols-outlined text-sm">close</span>
                      </button>
                    </div>
                  </div>
                  <label class="flex items-center justify-center gap-2 px-4 py-2 rounded-lg bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark text-sm font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors cursor-pointer w-full">
                    <span class="material-symbols-outlined text-base">add_photo_alternate</span>
                    <span>사진 추가</span>
                    <input type="file" accept="image/*" multiple class="hidden" @change="handleFileSelect" />
                  </label>
                </div>
              </div>
            </div>

            <!-- 게시하기 버튼 -->
            <div class="flex items-center gap-2 pt-4 border-t border-gray-200 dark:border-border-dark">
              <button
                @click="registerOnly"
                :disabled="publishing"
                class="inline-flex items-center justify-center gap-2 rounded-md bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-400 transition hover:bg-blue-500/30 disabled:opacity-50"
              >
                <span class="material-symbols-outlined text-sm">check</span>
                {{ publishing ? '등록 중...' : '등록하기' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import HotPlaceMap from '@/components/common/HotPlaceMap.vue'
import hotplaceApi from '@/services/api/hotplace'

const router = useRouter()

const form = ref({
  title: '',
  description: ''
})

const selectedPlace = ref(null)
const images = ref([])
const publishing = ref(false)
const isDragging = ref(false)

// 이미지 URL 길이 제한 (약 5MB, base64 인코딩 고려)
const MAX_IMAGE_URL_LENGTH = 6500000

const handlePlaceSelected = (place) => {
  selectedPlace.value = place
  if (place) {
    // 자동으로 제목 채우기
    if (!form.value.title && place.title) {
      form.value.title = place.title
    }
  }
}

// 파일 선택 처리
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  if (files.length === 0) return
  processFiles(files)
  event.target.value = ''
}

// 파일 처리 공통 함수
const processFiles = (files) => {
  files.forEach(file => {
    if (file.type.startsWith('image/')) {
      const maxFileSize = 5000000 // 5MB
      if (file.size > maxFileSize) {
        alert(`이미지 "${file.name}"의 크기가 너무 큽니다 (${(file.size / 1024 / 1024).toFixed(2)}MB).\n\n5MB 이하의 이미지만 업로드할 수 있습니다.`)
        return
      }
      
      const reader = new FileReader()
      reader.onload = (e) => {
        const imageUrl = e.target.result
        const urlLength = imageUrl.length
        
        if (urlLength > MAX_IMAGE_URL_LENGTH) {
          alert(`이미지 "${file.name}"의 URL이 너무 길어서 등록할 수 없습니다 (${(urlLength / 1024 / 1024).toFixed(2)}MB).\n\n더 작은 크기의 이미지를 선택해주세요.`)
          return
        }
        
        images.value.push({
          file: file,
          url: imageUrl,
          order: images.value.length,
          urlLength: urlLength
        })
      }
      reader.onerror = () => {
        alert(`이미지 "${file.name}"을 읽는 중 오류가 발생했습니다.`)
      }
      reader.readAsDataURL(file)
    }
  })
}

// 드래그 앤 드롭
const handleDragOver = (e) => {
  e.preventDefault()
  isDragging.value = true
}

const handleDragLeave = () => {
  isDragging.value = false
}

const handleDrop = (e) => {
  e.preventDefault()
  isDragging.value = false
  const files = Array.from(e.dataTransfer.files)
  processFiles(files)
}

const removeImage = (index) => {
  images.value.splice(index, 1)
}

// HotPlace 등록만
const registerOnly = async () => {
  if (!form.value.title || !form.value.title.trim()) {
    alert('제목을 입력해주세요')
    return
  }

  if (!selectedPlace.value) {
    alert('지도에서 장소를 선택해주세요')
    return
  }

  // 좌표 유효성 검사
  const lat = Number(selectedPlace.value.latitude)
  const lng = Number(selectedPlace.value.longitude)
  
  if (isNaN(lat) || isNaN(lng)) {
    alert('유효하지 않은 좌표입니다. 지도에서 장소를 다시 선택해주세요.')
    return
  }

  publishing.value = true
  try {
    // 1. HotPlace 생성
    const requestData = {
      title: form.value.title,
      description: form.value.description || '',
      address: selectedPlace.value.addr1 || '',
      latitude: lat,
      longitude: lng,
      contentId: selectedPlace.value.contentId || null,
      contentTypeId: selectedPlace.value.contentTypeId || null
    }
    
    console.log('HotPlace 등록 요청 데이터:', requestData)
    
    const hotPlaceResult = await hotplaceApi.createHotPlace(requestData)

    if (!hotPlaceResult.success) {
      alert(hotPlaceResult.message || 'HotPlace 등록에 실패했습니다')
      return
    }

    const hotplaceId = hotPlaceResult.data.hotplaceId

    // 2. 이미지 업로드
    if (images.value.length > 0) {
      await uploadImages(hotplaceId)
    }

    alert('HotPlace가 등록되었습니다!')
    // 등록한 핫플레이스 상세 페이지로 이동
    router.push(`/hotplace/${hotplaceId}`)
  } catch (error) {
    console.error('HotPlace 등록 오류:', error)
    const errorMessage = error.response?.data?.message || error.message || '알 수 없는 오류'
    console.error('에러 상세:', {
      status: error.response?.status,
      data: error.response?.data,
      message: errorMessage
    })
    alert(`HotPlace 등록 중 오류가 발생했습니다: ${errorMessage}`)
  } finally {
    publishing.value = false
  }
}

// 이미지 업로드
const uploadImages = async (hotplaceId) => {
  let uploadedCount = 0

  for (let i = 0; i < images.value.length; i++) {
    const img = images.value[i]
    const urlLength = img.urlLength || img.url.length
    const fileName = img.file ? img.file.name : `이미지 ${i + 1}`

    if (urlLength > MAX_IMAGE_URL_LENGTH) {
      console.warn(`이미지 "${fileName}"이 너무 큽니다`)
      continue
    }

    try {
      const result = await hotplaceApi.addImage(hotplaceId, img.url, i)
      if (result.success) {
        uploadedCount++
      }
    } catch (error) {
      console.error(`이미지 업로드 오류 (${fileName}):`, error)
    }
  }

  return uploadedCount
}
</script>

