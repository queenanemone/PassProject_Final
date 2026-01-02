<template>
  <div class="relative flex h-auto min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <!-- Hero Section with Background Image -->
    <div class="relative w-full h-screen flex flex-col snap-start snap-always">
      <div class="absolute inset-0 w-full h-full overflow-hidden bg-white dark:bg-black">
        <!-- 저녁 해변 야경 배경 이미지 (Unsplash) -->
        <img 
          alt="Evening Beach Nightscape Background" 
          class="w-full h-full object-cover" 
          style="object-position: center;"
          src="https://images.unsplash.com/photo-1506905925346-21bda4d32df4?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80"
        />
        <div class="absolute inset-0 bg-black/30"></div>
      </div>
      <div class="relative z-10 w-full">
        <Header :transparent="true" />
      </div>
      <div class="relative z-10 flex flex-1 items-center justify-center px-4">
        <div class="flex flex-col gap-6 text-center max-w-[960px] w-full">
          <h1 class="text-4xl font-black leading-tight tracking-tight text-white drop-shadow-lg md:text-5xl lg:text-6xl">
            AI와 함께 만드는<br/>당신만의 완벽한 여행
          </h1>
          <h2 class="text-base font-normal leading-normal text-gray-200 drop-shadow-md md:text-lg">
            당신의 관심사, 예산, 스타일에 맞춰 AI가 특별한 여행 일정을 만들어 드립니다.<br/>
            진정한 개인화 여행 플래닝을 경험해 보세요.
          </h2>
          <div class="flex justify-center mt-4">
            <RouterLink 
              to="/new-plan" 
              class="flex min-w-[200px] items-center justify-center rounded-lg h-12 px-8 bg-primary text-white text-base font-bold hover:bg-blue-500 transition-colors shadow-lg shadow-blue-500/30"
            >
              내 트립보드 만들기
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Features Section -->
    <div id="features" class="relative w-full h-screen flex flex-col items-center justify-center py-5 bg-background-light dark:bg-background-dark snap-start snap-always">
      <div class="flex flex-col max-w-[960px] flex-1 px-4 sm:px-10 justify-center">
        <div class="flex flex-col gap-10 px-4 py-16 bg-white dark:bg-card-dark rounded-xl">
          <div class="flex flex-col gap-4 text-center">
            <h1 class="text-3xl font-bold leading-tight text-gray-900 dark:text-white md:text-4xl md:font-black max-w-[720px] mx-auto">
              완벽한 여행을 위한 모든 것
            </h1>
            <p class="text-base font-normal leading-normal max-w-[720px] mx-auto text-gray-600 dark:text-text-dark-secondary">
              지능적인 추천부터 원활한 협업까지, 여행 계획을 쉽고 즐겁게 만들어주는 기능들을 만나보세요.
            </p>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4 justify-items-center">
            <div class="flex flex-1 gap-3 rounded-xl border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark p-6 flex-col hover:border-primary/50 transition-colors">
              <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-2">
                <span class="material-symbols-outlined text-primary text-2xl">auto_awesome</span>
              </div>
              <div class="flex flex-col gap-2">
                <h2 class="text-lg font-bold text-gray-900 dark:text-white">AI 추천</h2>
                <p class="text-sm font-normal text-gray-600 dark:text-gray-400">출발지와 도착지만 입력하면 AI가 최적의 여행 경로와 관광지를 자동으로 추천해드립니다.</p>
              </div>
            </div>
            <div class="flex flex-1 gap-3 rounded-xl border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark p-6 flex-col hover:border-primary/50 transition-colors">
              <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-2">
                <span class="material-symbols-outlined text-primary text-2xl">list_alt</span>
              </div>
              <div class="flex flex-col gap-2">
                <h2 class="text-lg font-bold text-gray-900 dark:text-white">개인화된 일정</h2>
                <p class="text-sm font-normal text-gray-600 dark:text-gray-400">날짜별로 일정을 관리하고 드래그 앤 드롭으로 쉽게 조정할 수 있는 직관적인 타임라인을 제공합니다.</p>
              </div>
            </div>
            <div class="flex flex-1 gap-3 rounded-xl border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark p-6 flex-col hover:border-primary/50 transition-colors">
              <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-2">
                <span class="material-symbols-outlined text-primary text-2xl">group</span>
              </div>
              <div class="flex flex-col gap-2">
                <h2 class="text-lg font-bold text-gray-900 dark:text-white">간편한 협업</h2>
                <p class="text-sm font-normal text-gray-600 dark:text-gray-400">여행 계획을 커뮤니티에 공유하고 다른 사용자들의 여행 계획을 참고하여 더 풍부한 여행을 계획하세요.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- How It Works Section -->
    <div class="relative w-full h-screen flex flex-col items-center justify-between py-5 bg-background-light dark:bg-background-dark snap-start snap-always">
      <div class="flex flex-col max-w-[960px] flex-1 px-4 sm:px-10">
        <div class="px-4 py-16">
          <h2 class="text-center text-3xl font-bold leading-tight pb-12 text-gray-900 dark:text-white">서비스 이용 방법</h2>
          <div class="grid grid-cols-[40px_1fr] gap-x-6 max-w-lg mx-auto">
            <div class="flex flex-col items-center gap-1 pt-1">
              <div class="flex items-center justify-center w-10 h-10 rounded-full bg-primary text-white font-bold text-lg z-10">1</div>
              <div class="w-[2px] bg-gray-300 dark:bg-border-dark h-full grow my-2"></div>
            </div>
            <div class="flex flex-1 flex-col pb-10 pt-1 pl-2">
              <p class="text-primary text-sm font-bold leading-normal mb-1">STEP 01</p>
              <p class="text-gray-900 dark:text-white text-xl font-bold leading-normal mb-2">여행 계획 생성하기</p>
              <p class="text-gray-600 dark:text-gray-400 text-base font-normal leading-relaxed">
                출발지, 도착지, 여행 기간, 인원을 입력하여 새로운 여행 계획을 생성합니다. AI 추천을 받거나 빈 계획으로 시작할 수 있습니다.
              </p>
            </div>
            <div class="flex flex-col items-center gap-1">
              <div class="w-[2px] bg-gray-300 dark:bg-border-dark h-full my-2"></div>
              <div class="flex items-center justify-center w-10 h-10 rounded-full bg-white dark:bg-card-dark border-2 border-primary text-primary font-bold text-lg z-10">2</div>
              <div class="w-[2px] bg-gray-300 dark:bg-border-dark h-full grow my-2"></div>
            </div>
            <div class="flex flex-1 flex-col pb-10 pt-1 pl-2">
              <p class="text-primary text-sm font-bold leading-normal mb-1">STEP 02</p>
              <p class="text-gray-900 dark:text-white text-xl font-bold leading-normal mb-2">AI 추천 받기</p>
              <p class="text-gray-600 dark:text-gray-400 text-base font-normal leading-relaxed">
                AI가 여행지 정보를 분석하여 관광지, 숙소, 교통편을 자동으로 추천합니다. 추천된 항목을 원하는 날짜에 추가할 수 있습니다.
              </p>
            </div>
            <div class="flex flex-col items-center gap-1 pb-1">
              <div class="w-[2px] bg-gray-300 dark:bg-border-dark h-full my-2"></div>
              <div class="flex items-center justify-center w-10 h-10 rounded-full bg-white dark:bg-card-dark border-2 border-gray-300 dark:border-border-dark text-gray-600 dark:text-gray-400 font-bold text-lg z-10">3</div>
            </div>
            <div class="flex flex-1 flex-col pb-10 pt-1 pl-2">
              <p class="text-gray-600 dark:text-gray-400 text-sm font-bold leading-normal mb-1">STEP 03</p>
              <p class="text-gray-900 dark:text-white text-xl font-bold leading-normal mb-2">일정 조정 후 여행 시작</p>
              <p class="text-gray-600 dark:text-gray-400 text-base font-normal leading-relaxed">
                드래그 앤 드롭으로 일정을 자유롭게 조정하고, 지도에서 장소를 확인하며 최종 계획을 완성한 후 여행을 시작하세요.
              </p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Footer Section -->
      <footer class="border-t border-gray-200 dark:border-border-dark w-full mt-auto">
        <div class="flex items-center justify-center px-4 sm:px-10 py-6 max-w-[960px] mx-auto">
          <p class="text-sm text-gray-600 dark:text-text-dark-secondary">© 2025 Trip-Board. All rights reserved.</p>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import Header from '@/components/Header.vue'
</script>

<style scoped>
/* 다크 모드는 index.html의 html 태그에 class="dark"를 추가하여 설정 */
</style>

<style>
/* 스크롤 스냅 효과 - 전역 스타일로 적용 */
html {
  scroll-snap-type: y mandatory;
  scroll-behavior: smooth;
}

body {
  scroll-snap-type: y mandatory;
  scroll-behavior: smooth;
}
</style>

