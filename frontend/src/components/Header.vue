<template>
  <header 
    :class="[
      'sticky top-0 z-50 flex items-center justify-between whitespace-nowrap px-4 py-3 sm:px-6 lg:px-8',
      transparent 
        ? 'header-transparent border-b-0 bg-transparent backdrop-blur-md' 
        : 'header-normal border-b border-gray-200/10 dark:border-border-dark bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm'
    ]"
  >
    <RouterLink 
      to="/" 
      :class="[
        'flex items-center gap-4 hover:opacity-80 transition-opacity',
        transparent ? 'text-white drop-shadow-lg' : 'text-slate-800 dark:text-white'
      ]"
    >
      <span :class="['material-symbols-outlined text-2xl', transparent ? 'text-white drop-shadow-lg' : 'text-primary']">map</span>
      <h2 class="text-lg font-bold tracking-tight">Trip-Board</h2>
    </RouterLink>
    
    <div class="hidden items-center gap-8 md:flex ml-auto mr-6">
      <RouterLink 
        to="/board" 
        :class="[
          'text-sm font-medium hover:text-primary dark:hover:text-primary transition-colors',
          transparent ? 'text-white drop-shadow-lg' : 'text-slate-600 dark:text-slate-300'
        ]"
      >
        커뮤니티
      </RouterLink>
      <a
        @click.prevent="handleMenuClick('/my-records')"
        :class="[
          'text-sm font-medium hover:text-primary dark:hover:text-primary cursor-pointer transition-colors',
          transparent ? 'text-white drop-shadow-lg' : 'text-slate-600 dark:text-slate-300'
        ]"
      >
        여행기록
      </a>
      <a
        @click.prevent="handleMenuClick('/my-hotplaces')"
        :class="[
          'text-sm font-medium hover:text-primary dark:hover:text-primary cursor-pointer transition-colors',
          transparent ? 'text-white drop-shadow-lg' : 'text-slate-600 dark:text-slate-300'
        ]"
      >
        핫플레이스
      </a>
      <a
        @click.prevent="handleMenuClick('/dashboard')"
        :class="[
          'text-sm font-medium hover:text-primary dark:hover:text-primary cursor-pointer transition-colors',
          transparent ? 'text-white drop-shadow-lg' : 'text-slate-600 dark:text-slate-300'
        ]"
      >
        내 트립보드
      </a>
    </div>
    
    <div class="flex items-center gap-4">
      <RouterLink 
        v-if="authStore.isAuthenticated"
        to="/mypage" 
        id="mypageIcon"
        class="flex size-10 rounded-full bg-cover bg-center bg-no-repeat bg-slate-300 dark:bg-card-dark hover:opacity-80 transition-opacity cursor-pointer items-center justify-center"
        :style="profileImageStyle"
      >
        <span v-if="!authStore.user?.profileImage" class="material-symbols-outlined text-slate-500 dark:text-slate-400">person</span>
      </RouterLink>
      <div v-else class="flex gap-2">
        <RouterLink 
          to="/login" 
          :class="[
            'px-4 py-2 text-sm font-medium hover:text-primary transition-colors',
            transparent ? 'text-white drop-shadow-lg' : 'text-slate-600 dark:text-slate-300'
          ]"
        >
          로그인
        </RouterLink>
        <RouterLink 
          to="/login?tab=register" 
          :class="[
            'px-4 py-2 text-sm font-bold rounded-lg hover:bg-blue-500',
            transparent 
              ? 'text-white bg-white/10 backdrop-blur-sm border border-white/20 hover:bg-white/20' 
              : 'text-white bg-primary'
          ]"
        >
          회원가입
        </RouterLink>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  transparent: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const authStore = useAuthStore()

const profileImageStyle = computed(() => {
  if (authStore.user?.profileImage) {
    return {
      backgroundImage: `url('${authStore.user.profileImage}')`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat'
    }
  }
  return {}
})

const handleMenuClick = (path) => {
  if (authStore.isAuthenticated) {
    router.push(path)
  } else {
    router.push('/login')
  }
}
</script>

<style scoped>
header.header-normal {
  background-color: rgba(246, 247, 248, 0.85) !important;
  backdrop-filter: blur(8px) !important;
  -webkit-backdrop-filter: blur(8px) !important;
}

html.dark header.header-normal {
  background-color: rgba(23, 31, 45, 0.85) !important;
  backdrop-filter: blur(8px) !important;
  -webkit-backdrop-filter: blur(8px) !important;
}

header.header-transparent {
  background-color: transparent !important;
}
</style>

