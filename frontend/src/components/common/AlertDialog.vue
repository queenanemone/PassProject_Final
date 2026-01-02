<template>
  <Teleport to="body">
    <!-- Alert 모달 -->
    <div
      v-if="alertState.show"
      class="fixed inset-0 z-[10000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
      @click.self="closeAlert"
    >
      <div class="bg-card-dark rounded-xl border border-border-dark p-6 sm:p-8 max-w-md w-full mx-4 shadow-2xl">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <span 
              class="material-symbols-outlined text-2xl"
              :class="alertState.type === 'error' ? 'text-red-400' : alertState.type === 'success' ? 'text-green-400' : 'text-primary'"
            >
              {{ alertState.type === 'error' ? 'error' : alertState.type === 'success' ? 'check_circle' : 'info' }}
            </span>
            <h3 v-if="alertState.title" class="text-xl font-bold text-text-dark">{{ alertState.title }}</h3>
          </div>
          <button
            @click="closeAlert"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-text-secondary-dark hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <p class="text-text-secondary-dark mb-6 whitespace-pre-line">{{ alertState.message }}</p>
        
        <div class="flex justify-end gap-3">
          <button
            @click="closeAlert"
            class="px-6 py-2.5 rounded-lg bg-primary text-white font-medium hover:bg-blue-400 transition-colors"
          >
            확인
          </button>
        </div>
      </div>
    </div>

    <!-- Confirm 모달 -->
    <div
      v-if="confirmState.show"
      class="fixed inset-0 z-[10000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
      @click.self="closeConfirm(false)"
    >
      <div class="bg-card-dark rounded-xl border border-border-dark p-6 sm:p-8 max-w-md w-full mx-4 shadow-2xl">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <span class="material-symbols-outlined text-2xl text-yellow-400">warning</span>
            <h3 v-if="confirmState.title" class="text-xl font-bold text-text-dark">{{ confirmState.title }}</h3>
          </div>
          <button
            @click="closeConfirm(false)"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-text-secondary-dark hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <p class="text-text-secondary-dark mb-6 whitespace-pre-line">{{ confirmState.message }}</p>
        
        <div class="flex justify-end gap-3">
          <button
            @click="closeConfirm(false)"
            class="px-6 py-2.5 rounded-lg border border-border-dark bg-background-dark text-text-dark font-medium hover:bg-surface-dark transition-colors"
          >
            취소
          </button>
          <button
            @click="closeConfirm(true)"
            class="px-6 py-2.5 rounded-lg bg-red-500 text-white font-medium hover:bg-red-600 transition-colors"
          >
            확인
          </button>
        </div>
      </div>
    </div>

    <!-- Prompt 모달 -->
    <div
      v-if="promptState.show"
      class="fixed inset-0 z-[10000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
      @click.self="closePrompt(null)"
    >
      <div class="bg-card-dark rounded-xl border border-border-dark p-6 sm:p-8 max-w-md w-full mx-4 shadow-2xl">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <span class="material-symbols-outlined text-2xl text-primary">edit</span>
            <h3 v-if="promptState.title" class="text-xl font-bold text-text-dark">{{ promptState.title }}</h3>
          </div>
          <button
            @click="closePrompt(null)"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-text-secondary-dark hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <p class="text-text-secondary-dark mb-4 whitespace-pre-line">{{ promptState.message }}</p>
        
        <input
          v-model="promptInput"
          @keyup.enter="closePrompt(promptInput)"
          class="w-full rounded-lg border-border-dark bg-background-dark text-text-dark focus:border-primary focus:ring-primary px-4 py-3 mb-6"
          type="text"
          autofocus
        />
        
        <div class="flex justify-end gap-3">
          <button
            @click="closePrompt(null)"
            class="px-6 py-2.5 rounded-lg border border-border-dark bg-background-dark text-text-dark font-medium hover:bg-surface-dark transition-colors"
          >
            취소
          </button>
          <button
            @click="closePrompt(promptInput)"
            class="px-6 py-2.5 rounded-lg bg-primary text-white font-medium hover:bg-blue-400 transition-colors"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 전역 상태 (모든 인스턴스에서 공유)
const alertState = ref({
  show: false,
  message: '',
  title: '',
  type: 'info' // 'info', 'success', 'error'
})

const confirmState = ref({
  show: false,
  message: '',
  title: '',
  resolve: null
})

const promptState = ref({
  show: false,
  message: '',
  title: '',
  defaultValue: '',
  resolve: null
})

const promptInput = ref('')

const showAlert = (message, title = '', type = 'info') => {
  alertState.value.message = message
  alertState.value.title = title
  alertState.value.type = type
  alertState.value.show = true
}

const closeAlert = () => {
  alertState.value.show = false
  alertState.value.message = ''
  alertState.value.title = ''
  alertState.value.type = 'info'
}

const showConfirm = (message, title = '') => {
  return new Promise((resolve) => {
    confirmState.value.message = message
    confirmState.value.title = title
    confirmState.value.show = true
    confirmState.value.resolve = resolve
  })
}

const closeConfirm = (result) => {
  confirmState.value.show = false
  if (confirmState.value.resolve) {
    confirmState.value.resolve(result)
  }
  confirmState.value.message = ''
  confirmState.value.title = ''
  confirmState.value.resolve = null
}

const showPrompt = (message, defaultValue = '', title = '') => {
  return new Promise((resolve) => {
    promptState.value.message = message
    promptState.value.title = title
    promptState.value.defaultValue = defaultValue
    promptInput.value = defaultValue
    promptState.value.show = true
    promptState.value.resolve = resolve
  })
}

const closePrompt = (result) => {
  promptState.value.show = false
  if (promptState.value.resolve) {
    promptState.value.resolve(result)
  }
  promptState.value.message = ''
  promptState.value.title = ''
  promptState.value.defaultValue = ''
  promptInput.value = ''
  promptState.value.resolve = null
}

// 전역 함수로 등록 (컴포넌트가 마운트된 후 사용 가능)
onMounted(() => {
  if (typeof window !== 'undefined') {
    window._customAlert = showAlert
    window._customConfirm = showConfirm
    window._customPrompt = showPrompt
  }
})

// 전역으로 사용할 수 있도록 expose
defineExpose({
  showAlert,
  showConfirm,
  alertState,
  confirmState
})
</script>

