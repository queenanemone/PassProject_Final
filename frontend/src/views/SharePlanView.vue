<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />
    <main class="flex-grow bg-background-light dark:bg-background-dark">
      <div class="container mx-auto px-4 py-8 sm:px-6 lg:px-8 max-w-4xl">
        <h1 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-text-dark mb-6">여행 계획 공유</h1>
        <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8">
          <p class="text-gray-600 dark:text-text-secondary-dark">여행 계획 공유 기능은 추후 구현 예정입니다.</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue'
</script>

