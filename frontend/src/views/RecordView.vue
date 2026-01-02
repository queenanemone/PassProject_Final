<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />

    <main class="flex-grow bg-background-light dark:bg-background-dark">
      <div class="max-w-3xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <!-- 메인 타이틀 -->
        <div class="mb-8">
          <h1 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-text-dark mb-2">당신의 여행을 기록하세요</h1>
          <p class="text-gray-600 dark:text-text-secondary-dark text-base">여행의 특별한 순간들을 공유해보세요.</p>
        </div>

        <!-- 폼 컨테이너 -->
        <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 flex flex-col gap-6">
          <!-- 제목 -->
          <div>
            <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block" for="title">제목</label>
            <input
              id="recordTitle"
              v-model="form.title"
              class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 placeholder:text-gray-400 dark:placeholder:text-text-secondary-dark px-4 text-base"
              placeholder="여행의 제목을 입력해주세요"
              type="text"
            />
          </div>

          <!-- 본문 -->
          <div>
            <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block" for="content">본문</label>
            <textarea
              id="recordContent"
              v-model="form.content"
              class="w-full resize-y rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark min-h-48 placeholder:text-gray-400 dark:placeholder:text-text-secondary-dark p-4 text-base"
              placeholder="어떤 특별한 경험을 하셨나요? 자유롭게 이야기를 들려주세요."
            ></textarea>
          </div>

          <!-- 사진/동영상 -->
          <div>
            <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block">사진/동영상</label>
            <div
              id="imageUploadArea"
              :class="[
                'flex flex-col items-center justify-center gap-4 rounded-lg border-2 border-dashed p-8 min-h-[200px] transition-colors',
                isDragging ? 'border-primary bg-primary/10' : 'border-gray-200 dark:border-border-dark'
              ]"
              @dragover.prevent="handleDragOver"
              @dragleave.prevent="handleDragLeave"
              @drop.prevent="handleDrop"
            >
              <div v-if="images.length === 0" class="flex flex-col items-center gap-3 text-center">
                <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-text-secondary-dark">upload_file</span>
                <p class="text-gray-900 dark:text-text-dark text-base font-semibold">사진/동영상 추가</p>
                <p class="text-gray-600 dark:text-text-secondary-dark text-sm">이곳에 파일을 드래그하세요</p>
                <label class="mt-2 px-4 py-2 rounded-lg bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark text-sm font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors cursor-pointer">
                  컴퓨터에서 선택
                  <input type="file" id="fileInput" accept="image/*,video/*" multiple class="hidden" @change="handleFileSelect" />
                </label>
              </div>
              <div v-else class="w-full">
                <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3 mb-4">
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
                  <input type="file" id="fileInputAdd" accept="image/*,video/*" multiple class="hidden" @change="handleFileSelect" />
                </label>
              </div>
            </div>
          </div>

          <!-- 태그 -->
          <div>
            <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block">태그</label>
            <div class="flex flex-wrap gap-2 p-3 rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark min-h-[48px]">
              <span
                v-for="(tag, index) in tags"
                :key="index"
                class="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-purple-600/20 text-purple-400 text-sm"
              >
                {{ tag }}
                <button @click="removeTag(index)" class="hover:text-purple-300">
                  <span class="material-symbols-outlined text-sm">close</span>
                </button>
              </span>
              <input
                v-model="tagInput"
                @input="handleTagInput"
                @keydown.backspace="handleTagBackspace"
                class="flex-1 min-w-[200px] bg-transparent text-gray-900 dark:text-text-dark placeholder:text-gray-400 dark:placeholder:text-text-secondary-dark focus:outline-none text-sm px-4 py-3"
                placeholder="#태그1, #태그2 형식으로 입력하세요"
                type="text"
              />
            </div>
          </div>

          <!-- 여행 계획 선택 (숨김 처리하거나 하단으로 이동) -->
          <div class="border-t border-gray-200 dark:border-border-dark pt-4">
            <label class="text-gray-900 dark:text-text-dark text-sm font-medium mb-2 block" for="planSelect">연결된 여행 계획 (선택사항)</label>
            <select
              id="planSelect"
              v-model="form.planId"
              class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4 text-base"
            >
              <option :value="null">선택 안함</option>
              <option v-for="plan in plans" :key="plan.planId" :value="plan.planId">
                {{ plan.title || `여행 계획 ${plan.planId}` }}
              </option>
            </select>
          </div>
          
          <!-- 게시하기 버튼 -->
          <div class="flex justify-end pt-4 border-t border-gray-200 dark:border-border-dark">
            <button
              @click="publish"
              :disabled="publishing"
              class="inline-flex items-center justify-center gap-2 rounded-md bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-400 transition hover:bg-blue-500/30 disabled:opacity-50"
            >
              <span class="material-symbols-outlined text-sm">check</span>
              {{ publishing ? '등록 중...' : '등록하기' }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '@/components/Header.vue'
import recordApi from '@/services/api/record'
import planApi from '@/services/api/plan'

const router = useRouter()
const route = useRoute()

const form = ref({
  title: '',
  content: '',
  planId: null
})

const plans = ref([])
const images = ref([])
const publishing = ref(false)
const isDragging = ref(false)
const tags = ref([])
const tagInput = ref('')

// 이미지 URL 길이 제한 (약 5MB, base64 인코딩 고려)
const MAX_IMAGE_URL_LENGTH = 6500000

// URL에서 planId 가져오기
onMounted(() => {
  const planIdFromUrl = route.query.planId
  if (planIdFromUrl) {
    form.value.planId = parseInt(planIdFromUrl)
  }
  loadPlans()
})

// 여행 계획 목록 로드
const loadPlans = async () => {
  try {
    const result = await planApi.getPlans()
    if (result.success) {
      plans.value = result.data || []
      // URL에서 가져온 planId가 있으면 선택
      if (route.query.planId) {
        const planIdFromUrl = parseInt(route.query.planId)
        form.value.planId = planIdFromUrl
      }
    }
  } catch (error) {
    console.error('여행 계획 로드 오류:', error)
  }
}

// 파일 선택 처리
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  if (files.length === 0) return
  processFiles(files)
  // 같은 파일을 다시 선택할 수 있도록 input 초기화
  event.target.value = ''
}

// 파일 처리 공통 함수
const processFiles = (files) => {
  files.forEach(file => {
    if (file.type.startsWith('image/')) {
      // 파일 크기 미리 체크 (원본 파일 크기)
      const maxFileSize = 5000000 // 5MB
      if (file.size > maxFileSize) {
        alert(`이미지 "${file.name}"의 크기가 너무 큽니다 (${(file.size / 1024 / 1024).toFixed(2)}MB).\n\n5MB 이하의 이미지만 업로드할 수 있습니다.`)
        return
      }
      
      const reader = new FileReader()
      reader.onload = (e) => {
        const imageUrl = e.target.result
        const urlLength = imageUrl.length
        
        // base64 URL 길이 체크
        const sizeWarning = urlLength > MAX_IMAGE_URL_LENGTH
        const sizeInfo = (urlLength / 1024 / 1024).toFixed(2)
        
        if (urlLength > MAX_IMAGE_URL_LENGTH) {
          alert(`이미지 "${file.name}"의 URL이 너무 길어서 등록할 수 없습니다 (${sizeInfo}MB).\n\n더 작은 크기의 이미지를 선택해주세요.`)
          return
        }
        
        images.value.push({
          file: file,
          url: imageUrl,
          order: images.value.length,
          urlLength: urlLength,
          sizeWarning: sizeWarning,
          sizeInfo: sizeInfo
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

// 태그 관련 함수
const handleTagInput = () => {
  const input = tagInput.value
  // 쉼표가 있으면 태그 등록
  if (input.includes(',')) {
    // 쉼표로 구분된 태그들을 추출
    const tagParts = input.split(',').map(part => part.trim()).filter(part => part !== '')
    
    tagParts.forEach(part => {
      // #로 시작하지 않으면 추가
      let tag = part.startsWith('#') ? part : `#${part}`
      // 중복 체크
      if (tag && !tags.value.includes(tag)) {
        tags.value.push(tag)
      }
    })
    
    // 입력칸 초기화
    tagInput.value = ''
  }
}

const removeTag = (index) => {
  tags.value.splice(index, 1)
}

const handleTagBackspace = (event) => {
  if (tagInput.value === '' && tags.value.length > 0) {
    tags.value.pop()
  }
}


const publish = async () => {
  if (!form.value.title || !form.value.content) {
    alert('제목과 내용을 입력해주세요')
    return
  }
  
  // 이미지 크기 사전 체크
  if (images.value.length > 0) {
    const oversizedImages = images.value.filter(img => {
      const urlLength = img.urlLength || img.url.length
      return urlLength > MAX_IMAGE_URL_LENGTH
    })
    
    if (oversizedImages.length > 0) {
      const oversizedList = oversizedImages.map((img, idx) => {
        const fileName = img.file ? img.file.name : `이미지 ${idx + 1}`
        const size = ((img.urlLength || img.url.length) / 1024 / 1024).toFixed(2)
        return `  • ${fileName} (${size}MB)`
      }).join('\n')
      
      const proceed = confirm(
        `경고: ${oversizedImages.length}개의 이미지가 너무 커서 등록되지 않을 수 있습니다:\n\n${oversizedList}\n\n\n계속하시겠습니까?`
      )
      
      if (!proceed) {
        return
      }
    }
  }
  
  publishing.value = true
  try {
    // 1. 여행 기록 생성
    const recordResult = await recordApi.createRecord({
      title: form.value.title,
      content: form.value.content,
      planId: form.value.planId && form.value.planId !== '' ? parseInt(form.value.planId) : null
    })
    
    if (!recordResult.success) {
      alert(recordResult.message || '여행 기록 작성에 실패했습니다')
      return
    }
    
    const recordId = recordResult.data.recordId
    
    // 2. 이미지 업로드
    let allImagesUploaded = true
    let uploadedCount = 0
    if (images.value.length > 0) {
      const uploadResult = await uploadImages(recordId)
      allImagesUploaded = uploadResult.allSuccess
      uploadedCount = uploadResult.uploadedCount
      
      // 모든 이미지가 실패한 경우 - 게시물 등록 취소
      if (uploadedCount === 0 && images.value.length > 0) {
        // 생성된 여행 기록 삭제
        try {
          await recordApi.deleteRecord(recordId)
        } catch (error) {
          console.error('여행 기록 삭제 오류:', error)
        }
        
        // 이미지 업로드 실패 경고만 표시 (uploadImages 함수에서 이미 표시됨)
        // 작성 화면에 그대로 머물기
        return
      }
    }
    
    // 3. 여행 기록 저장 완료
    alert('여행 기록이 저장되었습니다!')
    
    // 작성한 여행기록 상세 페이지로 이동
    router.push(`/record/${recordId}`)
  } catch (error) {
    console.error('여행 기록 작성 오류:', error)
    alert('여행 기록 작성 중 오류가 발생했습니다')
  } finally {
    publishing.value = false
  }
}

// 이미지 업로드
const uploadImages = async (recordId) => {
  const failedImages = []
  let uploadedCount = 0
  
  for (let i = 0; i < images.value.length; i++) {
    const img = images.value[i]
    const urlLength = img.urlLength || img.url.length
    const fileName = img.file ? img.file.name : `이미지 ${i + 1}`
    
    // 이미지 URL 길이 재확인
    if (urlLength > MAX_IMAGE_URL_LENGTH) {
      failedImages.push({
        name: fileName,
        reason: `URL이 너무 깁니다 (${(urlLength / 1024 / 1024).toFixed(2)}MB). 최대 ${(MAX_IMAGE_URL_LENGTH / 1024 / 1024).toFixed(0)}MB까지 가능합니다.`
      })
      continue
    }
    
    try {
      const response = await fetch('/api/records/images', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          recordId: recordId,
          imageUrl: img.url,
          imageOrder: i
        })
      })
      
      const responseText = await response.text()
      if (!responseText || responseText.trim() === '') {
        failedImages.push({
          name: fileName,
          reason: '서버 응답 오류: 응답이 비어있습니다'
        })
        continue
      }
      
      let result
      try {
        result = JSON.parse(responseText)
      } catch (e) {
        failedImages.push({
          name: fileName,
          reason: '서버 응답을 파싱할 수 없습니다'
        })
        continue
      }
      
      if (!result.success) {
        const errorMessage = result.message || '알 수 없는 오류'
        failedImages.push({
          name: fileName,
          reason: errorMessage.includes('too long') || errorMessage.includes('너무')
            ? 'URL이 너무 길어서 등록할 수 없습니다. 더 작은 크기의 이미지를 선택해주세요.'
            : errorMessage
        })
      } else {
        uploadedCount++
      }
    } catch (error) {
      failedImages.push({
        name: fileName,
        reason: `네트워크 오류: ${error.message}`
      })
    }
  }
  
  // 실패한 이미지가 있으면 경고 표시
  if (failedImages.length > 0) {
    const failedList = failedImages.map((img, idx) =>
      `${idx + 1}. ${img.name}: ${img.reason}`
    ).join('\n')
    
    if (uploadedCount === 0) {
      // 모든 이미지가 실패한 경우
      alert(`이미지 업로드에 실패했습니다:\n\n${failedList}\n\n\n이미지 크기가 너무 크거나 네트워크 오류가 발생했을 수 있습니다.\n다른 이미지를 선택하여 다시 시도해주세요.`)
    } else {
      // 일부만 실패한 경우
      alert(`일부 이미지 업로드에 실패했습니다:\n\n${failedList}\n\n\n성공: ${uploadedCount}장 / 전체: ${images.value.length}장`)
    }
  }
  
  return {
    allSuccess: failedImages.length === 0,
    uploadedCount: uploadedCount,
    failedImages: failedImages
  }
}

// 게시판에 공유
const shareToBoard = async (recordId, planId, title, content, tagsArray = []) => {
  try {
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
    
    // 태그를 HTML 주석으로 content 앞부분에 추가
    let tagsHtml = ''
    if (tagsArray && tagsArray.length > 0) {
      tagsHtml = `<!-- TAGS: ${tagsArray.join(' ')} -->`
    }
    
    // content에 태그와 이미지 추가
    const contentWithTagsAndImages = tagsHtml + content + (imagesHtml || '')
    console.log('게시판 공유: 최종 content 길이:', contentWithTagsAndImages.length)
    if (imagesHtml) {
      console.log('게시판 공유: 이미지가 content에 포함되었습니다')
    } else {
      console.warn('게시판 공유: 이미지가 content에 포함되지 않았습니다')
    }
    if (tagsArray && tagsArray.length > 0) {
      console.log('게시판 공유: 태그가 content에 포함되었습니다:', tagsArray)
    }
    
    const response = await fetch('/api/board/posts', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        planId: planId,
        title: title,
        content: contentWithTagsAndImages,
        regionCode: null,
        tripType: null,
        season: null,
        category: 'TRAVEL_RECORD'
      })
    })
    
    const responseText = await response.text()
    console.log('게시판 공유: 응답 상태:', response.status, response.statusText)
    console.log('게시판 공유: 응답 텍스트:', responseText.substring(0, 500)) // 처음 500자만 로깅
    
    if (!response.ok) {
      console.error('게시판 공유: HTTP 오류:', response.status, response.statusText)
      console.error('게시판 공유: 전체 응답:', responseText)
      try {
        const errorResult = JSON.parse(responseText)
        const errorMsg = errorResult.message || '알 수 없는 오류'
        console.error('게시판 공유: 오류 메시지:', errorMsg)
        alert(`게시판 공유 실패: ${errorMsg}`)
      } catch (e) {
        console.error('게시판 공유: JSON 파싱 실패:', e)
        alert(`게시판 공유 실패: ${response.status} ${response.statusText}\n\n응답: ${responseText.substring(0, 200)}`)
      }
      return false
    }
    
    if (!responseText || responseText.trim() === '') {
      console.error('게시판 공유: 응답이 비어있습니다')
      alert('게시판 공유 실패: 서버 응답이 비어있습니다')
      return false
    }
    
    let result
    try {
      result = JSON.parse(responseText)
    } catch (e) {
      console.error('게시판 공유: JSON 파싱 오류:', e, responseText)
      alert('게시판 공유 실패: 서버 응답을 파싱할 수 없습니다')
      return false
    }
    
    if (!result.success) {
      console.error('게시판 공유: 실패:', result.message)
      alert(`게시판 공유 실패: ${result.message || '알 수 없는 오류'}`)
    }
    
    return result.success
  } catch (error) {
    console.error('게시판 공유 오류:', error)
    return false
  }
}
</script>
