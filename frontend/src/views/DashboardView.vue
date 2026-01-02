<template>
  <div class="bg-background-light dark:bg-background-dark text-gray-900 dark:text-text-dark-primary font-pretendard h-screen overflow-hidden flex flex-col">
    <Header />

    <!-- 메인 컨텐츠 -->
    <main class="flex-1 overflow-y-auto bg-background-light dark:bg-background-dark relative">
      <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8 pb-24">
        <div class="grid grid-cols-1 gap-6 lg:grid-cols-4" id="dashboardContent">
          <!-- 로딩 상태 -->
          <div v-if="loading" class="col-span-full text-center py-10">
            <svg class="animate-spin h-10 w-10 text-primary mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <p class="text-gray-600 dark:text-text-dark-secondary">{{ loadingMessage }}</p>
          </div>

          <!-- 계획 목록 뷰 -->
          <template v-else-if="!currentPlanId">
            <div class="col-span-full mb-8 flex items-center justify-between">
              <div>
                <h1 class="text-3xl md:text-4xl font-extrabold text-gray-900 dark:text-text-dark-primary mb-2">내 트립보드</h1>
                <p class="text-gray-600 dark:text-text-secondary-dark">여행 계획을 관리하고 일정을 세워보세요.</p>
              </div>
              <RouterLink to="/new-plan" class="flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-white text-sm font-bold hover:bg-blue-400 transition-colors">
                <span class="material-symbols-outlined text-base">add</span>
                <span class="hidden sm:inline">새 계획 만들기</span>
                <span class="sm:hidden">새 계획</span>
              </RouterLink>
            </div>
            <div v-if="plans.length === 0" class="col-span-full flex flex-col items-center justify-center py-16 text-center">
              <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-text-secondary-dark mb-4">travel_explore</span>
              <p class="text-xl font-bold text-gray-900 dark:text-text-dark-primary mb-2">아직 작성한 여행 계획이 없습니다</p>
              <p class="text-gray-600 dark:text-text-secondary-dark mb-6">첫 번째 여행 계획을 만들어보세요!</p>
              <RouterLink to="/new-plan" class="flex items-center gap-2 rounded-lg bg-primary px-6 py-3 text-white font-bold hover:bg-blue-400 transition-colors">
                <span class="material-symbols-outlined">add</span>
                <span>새 계획 만들기</span>
              </RouterLink>
            </div>
            <div v-else class="col-span-full grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
              <div
                v-for="plan in plans"
                :key="plan.planId"
                class="relative group flex flex-col overflow-hidden rounded-xl border border-gray-200 dark:border-border-dark bg-white dark:bg-card-dark shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg hover:border-primary/50 cursor-pointer"
                @click="viewPlan(plan.planId)"
              >
                <div v-if="plan.firstImage" class="aspect-video w-full bg-cover bg-center rounded-t-xl" :style="{ backgroundImage: `url('${plan.firstImage}')` }"></div>
                <div v-else class="aspect-video w-full bg-gray-700 rounded-t-xl flex items-center justify-center">
                  <span class="material-symbols-outlined text-4xl text-gray-500">image</span>
                </div>
                <div class="flex flex-1 flex-col p-6">
                  <button @click.stop="deletePlan(plan.planId)" class="absolute top-4 right-4 p-2 rounded-lg text-gray-600 dark:text-text-secondary-dark hover:text-red-500 hover:bg-red-500/10 transition-colors z-10 opacity-0 group-hover:opacity-100" title="계획 삭제">
                    <span class="material-symbols-outlined text-lg">delete</span>
                  </button>
                  <h3 class="text-xl font-bold text-gray-900 dark:text-text-dark-primary mb-2 pr-8 line-clamp-2">{{ plan.title || '제목 없음' }}</h3>
                  <p class="text-sm text-gray-600 dark:text-text-dark-secondary mb-1">출발: {{ plan.departureDate || '미정' }}</p>
                  <p class="text-sm text-gray-600 dark:text-text-dark-secondary">도착: {{ plan.arrivalDate || '미정' }}</p>
                </div>
              </div>
            </div>
          </template>

          <!-- 계획 상세 뷰 -->
          <template v-else-if="planDetails">
            <!-- 뒤로 가기 버튼 -->
            <div class="col-span-full mb-4">
              <button @click="navigateToPlanList" class="flex items-center gap-2 text-gray-600 dark:text-text-dark-secondary hover:text-gray-900 dark:hover:text-text-dark-primary transition-colors">
                <span class="material-symbols-outlined">arrow_back</span>
                <span>여행 계획 목록으로</span>
              </button>
            </div>

            <!-- 계획 헤더 -->
            <div class="col-span-full flex items-center justify-between bg-white dark:bg-card-dark p-6 rounded-2xl border border-gray-200 dark:border-white/5 mb-8">
              <div class="flex items-center gap-3">
                <h1 id="planTitleText" class="text-3xl font-bold text-gray-900 dark:text-white">{{ planDetails.plan.title }}</h1>
                <button @click="editTitle" class="text-gray-600 dark:text-gray-400 hover:text-primary transition-colors p-1 rounded-full hover:bg-gray-100 dark:hover:bg-white/5" title="제목 수정">
                  <span class="material-symbols-outlined text-xl">edit</span>
                </button>
              </div>
              <div class="flex items-center gap-4">
                 <button 
                  @click="openDateRangeModal" 
                  class="flex items-center gap-1 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors bg-gray-100 dark:bg-white/5 px-3 py-1.5 rounded-lg text-sm"
                >
                  <span class="material-symbols-outlined text-sm">calendar_month</span>
                  <span v-if="!planDetails.plan.departureDate || !planDetails.plan.arrivalDate">날짜 추가</span>
                  <span v-else>일정 변경</span>
                </button>
                <div class="text-right">
                  <p class="text-sm text-gray-600 dark:text-gray-400">{{ planDetails.plan.departureDate }} ~ {{ planDetails.plan.arrivalDate }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-500 mt-1">성인 {{ planDetails.plan.adultCount }}, 아동 {{ planDetails.plan.childCount || 0 }}</p>
                </div>
                <button
                  @click="openSharePlanModal"
                  class="flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-white text-sm font-bold hover:bg-blue-400 transition-colors"
                >
                  <span class="material-symbols-outlined text-base">share</span>
                  <span>게시판에 공유</span>
                </button>
              </div>
            </div>

            <!-- Day별 일정 및 AI 추천 + 지도 -->
            <div class="col-span-full flex flex-col gap-6">
              
               <!-- 날짜가 없을 때 표시할 빈 상태 UI -->
              <div v-if="dailyItinerary.length === 0" class="col-span-full py-20 flex flex-col items-center justify-center text-center bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-white/5">
                 <div class="w-16 h-16 rounded-full bg-gray-100 dark:bg-white/5 flex items-center justify-center mb-6">
                   <span class="material-symbols-outlined text-3xl text-gray-500 dark:text-gray-500">calendar_month</span>
                 </div>
                 <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">여행 날짜가 정해지지 않았습니다</h3>
                 <p class="text-gray-600 dark:text-gray-400 mb-8 max-w-md">날짜를 설정하면 일자별 카드가 생성되어 일정을 추가할 수 있습니다.</p>
                 <button 
                   @click="openDateRangeModal" 
                   class="px-8 py-4 bg-primary text-white rounded-xl font-bold text-lg hover:bg-blue-600 transition-all shadow-lg shadow-blue-500/30 hover:shadow-blue-500/50 hover:-translate-y-1"
                 >
                   여행 날짜 정하기
                 </button>
              </div>

              <!-- 일정 리스트 (전체 너비 사용) -->
              <div v-else class="w-full flex flex-col gap-10">
                 <div v-for="day in dailyItinerary" :key="day.dateStr" class="w-full">
                  <div class="flex items-center justify-between p-4 mb-4 rounded-xl bg-white dark:bg-card-dark border border-gray-200 dark:border-border-dark shadow-xl">
                    <h2 class="text-2xl font-bold text-gray-900 dark:text-white">Day {{ day.dayNum }}: {{ day.dateStr }} ({{ getDayOfWeek(day.dateStr) }})</h2>
                    <button @click="openSearchModalWithDate(day.dateStr)" class="flex items-center gap-1 bg-gray-100 dark:bg-white/10 hover:bg-primary hover:text-white px-3 py-1.5 rounded-lg text-sm text-gray-700 dark:text-gray-300 transition-colors">
                      <span class="material-symbols-outlined text-lg">add_circle</span>
                      일정 추가
                    </button>
                  </div>
                  
                  <!-- Day별 지도 (관광지가 있을 때만 표시) -->
                  <div class="mb-6 h-80 rounded-xl overflow-hidden shadow-lg border border-white/10 relative">
                     <NaverMap 
                        :items="day.items.filter(item => item.type === 'attraction' || item.type === 'hotel')" 
                        @add-item="(item) => handleAddFromMap(item, day.dateStr)"
                     />
                     <div class="absolute top-3 left-3 bg-black/60 backdrop-blur-md px-3 py-1 rounded-lg text-xs text-white border border-white/10 pointer-events-none z-0">
                        Day {{ day.dayNum }} 경로
                     </div>
                  </div>

                  <draggable 
                    v-model="day.items" 
                    group="planItems" 
                    item-key="uniqueId"
                    class="grid grid-cols-1 gap-4 min-h-[120px] rounded-lg transition-colors p-2"
                    :class="day.items.length === 0 ? 'border-2 border-dashed border-gray-700 dark:border-border-dark bg-gray-800/20 flex items-center justify-center' : ''"
                    ghost-class="opacity-50"
                    :scroll="true"
                    :scroll-sensitivity="150"
                    :scroll-speed="20"
                    :force-fallback="true"
                    :fallback-tolerance="5"
                    @change="(e) => onDragChange(e, day.dateStr)"
                  >
                    <template #item="{ element: item }">
                      <div class="group relative col-span-1 h-full cursor-grab active:cursor-grabbing select-none">
                        <!-- 교통편 -->
                        <div v-if="item.type === 'transport'" class="relative flex flex-col gap-3 rounded-xl bg-white dark:bg-card-dark p-4 transition-all hover:ring-2 hover:ring-primary/80 h-full border border-gray-200 dark:border-transparent">
                          <button @click.stop="handleDeleteItem(item)" class="absolute top-2 right-2 p-1.5 rounded-full bg-black/50 text-white/70 hover:bg-red-500 hover:text-white transition-all opacity-0 group-hover:opacity-100 z-10">
                            <span class="material-symbols-outlined text-sm">close</span>
                          </button>
                          <div class="flex items-center gap-4 p-4 bg-gradient-to-r from-blue-900/40 to-blue-700/40 rounded-lg border border-blue-400/50 h-full">
                            <span class="material-symbols-outlined text-4xl text-blue-300 self-start mt-1">train</span>
                            <div class="flex-1 flex flex-col gap-1">
                              <div class="flex justify-between items-center">
                                <span class="text-base font-bold text-gray-900 dark:text-text-dark-primary">{{ item.data.transportType || '교통수단' }}</span>
                                <span v-if="item.data.price && item.data.price > 0" class="text-green-400 font-bold text-sm">{{ formatPrice(item.data.price) }}원</span>
                                <span v-else class="text-gray-500 text-xs">가격 정보 없음</span>
                              </div>
                              <div class="flex items-center gap-2 my-1">
                                <span class="text-xl font-bold text-gray-900 dark:text-white tracking-wide">{{ formatTime(item.data.departureTime) }}</span>
                                <span class="material-symbols-outlined text-gray-600 dark:text-gray-400 text-sm">arrow_forward</span>
                                <span class="text-xl font-bold text-gray-900 dark:text-white tracking-wide">{{ formatTime(item.data.arrivalTime) }}</span>
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
                        <div v-else class="flex gap-4 rounded-xl bg-white dark:bg-card-dark p-4 transition-all hover:ring-2 hover:ring-primary/80 overflow-hidden relative h-full border border-gray-200 dark:border-transparent">
                          <button @click.stop="handleDeleteItem(item)" class="absolute top-2 right-2 p-1.5 rounded-full bg-black/50 text-white/70 hover:bg-red-500 hover:text-white transition-all opacity-0 group-hover:opacity-100 z-10">
                            <span class="material-symbols-outlined text-sm">close</span>
                          </button>
                          <div class="aspect-square w-24 h-24 overflow-hidden rounded-lg bg-gray-700 relative shrink-0">
                            <img v-if="getImageUrl(item.data)" :src="getImageUrl(item.data)" :alt="getTitle(item.data)" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
                            <div v-else class="flex items-center justify-center h-full bg-gray-700">
                              <span class="material-symbols-outlined text-3xl text-gray-500">image</span>
                            </div>
                          </div>
                          <div class="flex flex-col gap-1 flex-1 py-1">
                            <p class="text-lg font-bold text-gray-900 dark:text-text-dark-primary line-clamp-1 group-hover:text-primary transition-colors">{{ getTitle(item.data) }}</p>
                            <p class="text-sm text-gray-600 dark:text-text-dark-secondary line-clamp-2">{{ getAddress(item.data) }}</p>
                            <span v-if="item.type === 'hotel'" class="text-xs text-green-400 font-medium mt-auto">🏨 숙소 체크인 예정</span>
                          </div>
                        </div>
                      </div>
                    </template>
                    <template #footer>
                       <div v-if="day.items.length === 0" class="text-gray-500 text-sm pointer-events-none">
                         일정을 이곳으로 드래그하세요
                       </div>
                    </template>
                  </draggable>
                </div>

                 <!-- AI 추천 섹션 (리스트 아래에 배치) -->
                <div class="border-t border-gray-200 dark:border-border-dark pt-10">
                  <div class="flex-1 flex flex-col gap-4 w-full min-w-[300px]">
                    <h2 class="text-xl font-bold text-gray-900 dark:text-text-dark-primary px-2 flex items-center gap-2">
                      AI 추천 <span class="text-xs font-normal text-purple-400 bg-purple-900/30 px-2 py-0.5 rounded-full">AI</span>
                    </h2>
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full">
                      <div v-if="aiRecommendations.length === 0" class="col-span-full text-gray-600 dark:text-text-dark-secondary">AI 추천 내역이 없습니다</div>
                      <div v-for="item in aiRecommendations" :key="item.recommendationId" class="flex flex-col gap-3 rounded-xl bg-gradient-to-br from-purple-900/20 to-blue-900/20 p-4 border border-purple-500/30 group hover:border-purple-500/60 transition-all">
                        <div class="flex items-start gap-3">
                          <span class="material-symbols-outlined text-2xl text-purple-400 mt-1">auto_awesome</span>
                          <div class="flex-1">
                            <div class="flex justify-between items-start">
                              <p class="text-base font-bold text-gray-900 dark:text-text-dark-primary mb-1">{{ item.title || '추천 장소' }}</p>
                              <button @click="openAiDateSelectModal(item)" class="text-xs bg-purple-500/20 text-purple-300 px-2 py-1 rounded hover:bg-purple-500 hover:text-white transition-colors flex items-center gap-1 shrink-0">
                                <span class="material-symbols-outlined text-sm">add</span> 담기
                              </button>
                            </div>
                            <p class="text-sm text-gray-600 dark:text-text-dark-secondary mb-2 line-clamp-2">{{ item.description || '' }}</p>
                            <p class="text-xs text-gray-500 mb-1 flex items-center gap-1">
                              <span class="material-symbols-outlined text-[10px]">location_on</span>
                              {{ item.address || 'AI 추천' }}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </main>

    <!-- AI로 전체 채우기 버튼 -->
    <div v-if="currentPlanId && planDetails" id="aiRecommendContainer" class="fixed bottom-8 left-0 right-0 z-30 flex justify-center pointer-events-none">
      <button @click="handleAiFillClick" class="pointer-events-auto flex items-center gap-2 px-8 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-full shadow-2xl shadow-blue-500/30 hover:shadow-blue-500/50 hover:-translate-y-1 hover:scale-105 transition-all duration-300 border border-white/10 backdrop-blur-md">
        <span class="material-symbols-outlined animate-pulse">auto_awesome</span>
        <span class="font-bold text-lg">AI로 전체 채우기</span>
      </button>
    </div>

    <!-- 통합 검색 모달 -->
    <div v-if="showSearchModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4" @click.self="closeSearchModal">
      <div class="bg-white dark:bg-[#1a2035] w-full max-w-2xl rounded-2xl border border-gray-200 dark:border-white/10 shadow-2xl flex flex-col max-h-[85vh]">
        <div class="p-6 border-b border-gray-200 dark:border-white/10">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-xl font-bold text-gray-900 dark:text-white">항목 추가하기</h3>
            <button @click="closeSearchModal" class="text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
          <div class="flex gap-2 bg-gray-100 dark:bg-black/20 p-1 rounded-lg">
            <button @click="switchSearchTab('tour')" :class="['flex-1 py-2 rounded-md text-sm font-medium transition-colors', currentSearchTab === 'tour' ? 'bg-primary text-white' : 'text-gray-700 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white']">관광지</button>
            <button @click="switchSearchTab('accom')" :class="['flex-1 py-2 rounded-md text-sm font-medium transition-colors', currentSearchTab === 'accom' ? 'bg-primary text-white' : 'text-gray-700 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white']">숙소</button>
            <button @click="switchSearchTab('train')" :class="['flex-1 py-2 rounded-md text-sm font-medium transition-colors', currentSearchTab === 'train' ? 'bg-primary text-white' : 'text-gray-700 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white']">교통(기차)</button>
          </div>
        </div>
        <div class="p-6 bg-gray-50 dark:bg-[#151b2d]">
          <!-- 관광지/숙소 검색 폼 -->
          <form v-if="currentSearchTab !== 'train'" class="flex flex-col gap-3" @submit.prevent="executeSearch">
            <div class="flex gap-2">
              <select v-model="searchForm.areaCode" class="w-1/3 bg-white dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-lg text-gray-900 dark:text-white px-3 py-2 outline-none focus:border-primary">
                <option value="">전체 지역</option>
                <option value="1">서울</option>
                <option value="6">부산</option>
                <option value="2">인천</option>
                <option value="3">대전</option>
                <option value="4">대구</option>
                <option value="5">광주</option>
                <option value="32">강원</option>
                <option value="35">경북</option>
                <option value="38">전남</option>
                <option value="39">제주</option>
              </select>
              <input v-model="searchForm.keyword" type="text" :placeholder="currentSearchTab === 'tour' ? '장소명 검색 (예: 해운대)' : '숙소명 검색 (예: 신라스테이)'" class="flex-1 bg-white dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-lg text-gray-900 dark:text-white px-4 focus:ring-2 focus:ring-primary outline-none placeholder:text-gray-500 dark:placeholder:text-gray-400" />
            </div>
            <button type="submit" :disabled="searchLoading" class="w-full bg-primary py-2 rounded-lg text-white font-bold hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
              <span v-if="searchLoading">검색 중...</span>
              <span v-else>검색</span>
            </button>
          </form>
          <!-- 기차 검색 폼 -->
          <form v-else class="flex flex-col gap-3" @submit.prevent="executeSearch">
            <div class="flex gap-2">
              <input v-model="searchForm.trainDep" type="text" placeholder="출발역 (예: 서울)" class="flex-1 bg-white dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-lg text-gray-900 dark:text-white px-3 py-2 outline-none focus:border-primary placeholder:text-gray-500 dark:placeholder:text-gray-400" />
              <span class="text-gray-700 dark:text-white self-center">→</span>
              <input v-model="searchForm.trainArr" type="text" placeholder="도착역 (예: 부산)" class="flex-1 bg-white dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-lg text-gray-900 dark:text-white px-3 py-2 outline-none focus:border-primary placeholder:text-gray-500 dark:placeholder:text-gray-400" />
            </div>
            <div class="flex gap-2">
              <select v-model="searchForm.trainType" class="flex-1 bg-white dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-lg text-gray-900 dark:text-white px-3 py-2 outline-none focus:border-primary">
                <option value="">전체 차종</option>
                <option value="KTX">KTX</option>
                <option value="ITX">ITX</option>
                <option value="무궁화">무궁화</option>
              </select>
              <input v-model="searchForm.trainTime" type="time" class="flex-1 bg-white dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-lg text-gray-900 dark:text-white px-3 py-2 outline-none focus:border-primary" />
            </div>
            <button type="submit" :disabled="searchLoading" class="w-full bg-primary py-2 rounded-lg text-white font-bold hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
              <span v-if="searchLoading">조회 중...</span>
              <span v-else>열차 조회</span>
            </button>
          </form>
        </div>
        <div class="flex-1 overflow-y-auto p-6 space-y-3 min-h-[200px]">
          <div v-if="searchResults.length === 0" class="text-center text-gray-500 dark:text-gray-400 mt-10">검색 조건을 입력하고 검색하세요.</div>
          <div v-else class="space-y-3">
            <div v-for="(item, index) in searchResults" :key="index" class="flex items-center gap-4 bg-gray-50 dark:bg-white/5 p-3 rounded-lg border border-gray-200 dark:border-white/5 hover:border-primary transition-colors">
              <img v-if="item.type !== 'train' && item.image" :src="item.image" class="w-16 h-16 rounded object-cover bg-gray-200 dark:bg-gray-700" />
              <div class="flex-1">
                <div v-if="item.type === 'train'" class="flex items-center gap-2 mb-1">
                  <span class="font-bold text-primary">{{ item.trainType }}</span>
                  <span class="text-gray-900 dark:text-white text-sm">{{ item.trainNo }}</span>
                </div>
                <h4 v-else class="font-bold text-gray-900 dark:text-white text-sm line-clamp-1">{{ item.title }}</h4>
                <p v-if="item.type === 'train'" class="text-sm text-gray-600 dark:text-gray-300">
                  {{ item.departureTime }} {{ item.departureStation }} → {{ item.arrivalTime }} {{ item.arrivalStation }}
                </p>
                <p v-else class="text-xs text-gray-600 dark:text-gray-400 line-clamp-2">{{ item.addr || '' }}</p>
                <div v-if="item.type === 'train' && item.fare" class="text-sm text-green-600 dark:text-green-400 mt-1">{{ formatPrice(item.fare) }}원</div>
              </div>
              <button @click="addItemToPlan(item)" class="p-2 bg-primary/20 text-primary rounded hover:bg-primary hover:text-white transition-colors">
                <span class="material-symbols-outlined text-lg">add</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI 추천 모달 -->
    <div v-if="showAiModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 transition-opacity duration-300" @click.self="closeAiModal">
      <div class="relative w-full max-w-4xl bg-[#1a2035] border border-white/10 rounded-2xl shadow-2xl flex flex-col max-h-[90vh]">
        <div class="flex items-center justify-between p-6 border-b border-white/10">
          <div>
            <h2 class="text-2xl font-bold text-white flex items-center gap-2">
              <span class="text-primary">📍</span> 어디로 떠날까요?
            </h2>
            <p class="text-gray-400 text-sm mt-1">현재 위치를 기반으로 AI가 추천하는 여행지입니다.</p>
          </div>
          <button @click="closeAiModal" class="flex h-8 w-8 items-center justify-center rounded-full bg-white/5 text-gray-400 hover:bg-white/10 hover:text-white transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <div class="overflow-y-auto p-6">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div v-for="rec in locationRecommendations" :key="rec.regionCode" @click="selectRecommendation(rec)" class="flex flex-col gap-2 p-4 rounded-lg bg-background-dark border border-white/10 hover:border-primary cursor-pointer transition-colors">
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-bold text-white">{{ rec.regionName }}</h3>
                <span class="text-xs bg-primary/20 text-primary px-2 py-1 rounded">추천</span>
              </div>
              <p class="text-sm text-gray-400">{{ rec.reason }}</p>
              <button class="mt-2 w-full py-2 rounded bg-white/5 hover:bg-primary hover:text-white text-sm transition-colors text-text-dark-secondary">
                이곳으로 결정하기
              </button>
            </div>
          </div>
        </div>
        <div class="p-4 border-t border-white/10 text-center bg-[#151b2d] rounded-b-2xl">
          <p class="text-xs text-gray-500">마음에 드는 곳을 선택하면 AI가 자동으로 여행 계획을 완성해드립니다.</p>
        </div>
      </div>
    </div>

    <!-- 날짜 선택 모달 (AI 추가용) -->
    <div v-if="showDateSelectModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4" @click.self="closeDateSelectModal">
      <div class="bg-[#1a2035] w-full max-w-sm rounded-2xl border border-white/10 shadow-2xl overflow-hidden">
        <div class="p-6 border-b border-white/10">
          <h3 class="text-xl font-bold text-white mb-1">📅 날짜 선택</h3>
          <p class="text-gray-400 text-sm">어느 날짜에 추가할까요?</p>
        </div>
        <div class="p-4 flex flex-col gap-2 max-h-[60vh] overflow-y-auto">
          <button 
            v-for="day in dailyItinerary" 
            :key="day.dateStr"
            @click="confirmAddAiItem(day.dateStr)"
            class="w-full flex items-center justify-between p-4 rounded-xl bg-white/5 hover:bg-primary/20 hover:border-primary border border-transparent transition-all group"
          >
            <div class="flex flex-col items-start gap-1">
              <span class="text-primary font-bold text-xs uppercase tracking-wider">Day {{ day.dayNum }}</span>
              <span class="text-white font-bold text-lg">{{ day.dateStr }}</span>
            </div>
            <span class="text-gray-400 group-hover:text-white">{{ getDayOfWeek(day.dateStr) }}</span>
          </button>
        </div>
        <div class="p-4 border-t border-white/10 bg-[#151b2d]">
          <button @click="closeDateSelectModal" class="w-full py-3 rounded-xl bg-white/5 text-gray-400 hover:text-white hover:bg-white/10 transition-colors">
            취소
          </button>
        </div>
      </div>
    </div>
    
    <!-- 여행 계획 공유 모달 -->
    <div
      v-if="showSharePlanModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      @click.self="showSharePlanModal = false"
    >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-900 dark:text-white">여행 계획 공유하기</h3>
          <button @click="showSharePlanModal = false" class="text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <form @submit.prevent="sharePlan" class="space-y-6">
          <div>
            <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">제목</label>
            <input
              v-model="sharePlanForm.title"
              type="text"
              class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              placeholder="게시글 제목을 입력하세요"
              required
            />
          </div>
          
          <div>
            <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">내용</label>
            <textarea
              v-model="sharePlanForm.content"
              class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark p-4 min-h-32"
              placeholder="여행 계획에 대한 설명을 작성하세요"
              required
            ></textarea>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">지역</label>
              <select
                v-model="sharePlanForm.regionCode"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="1">서울/경기</option>
                <option value="2">부산</option>
                <option value="3">대구</option>
                <option value="4">인천</option>
                <option value="5">대전</option>
                <option value="32">강원</option>
                <option value="33">충청</option>
                <option value="35">경상</option>
                <option value="37">전라</option>
                <option value="39">제주</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">여행 타입</label>
              <select
                v-model="sharePlanForm.tripType"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="가족여행">가족여행</option>
                <option value="커플여행">커플여행</option>
                <option value="혼자여행">혼자여행</option>
                <option value="우정여행">우정여행</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">계절</label>
              <select
                v-model="sharePlanForm.season"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="봄">봄</option>
                <option value="여름">여름</option>
                <option value="가을">가을</option>
                <option value="겨울">겨울</option>
              </select>
            </div>
          </div>
          
          <div class="flex gap-3 pt-4">
            <button
              type="button"
              @click="showSharePlanModal = false"
              class="flex-1 flex items-center justify-center rounded-lg h-12 px-6 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
            >
              취소
            </button>
            <button
              type="submit"
              :disabled="sharingPlan"
              class="flex-1 flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-primary text-white font-bold hover:bg-blue-400 transition-colors disabled:opacity-50"
            >
              <span v-if="sharingPlan" class="material-symbols-outlined animate-spin">sync</span>
              <span v-else class="material-symbols-outlined">share</span>
              <span>{{ sharingPlan ? '공유 중...' : '공유하기' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 확인 모달 -->
    <div v-if="showConfirmModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4" @click.self="cancelConfirm">
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-md w-full mx-4">
        <div class="flex items-center gap-4 mb-4">
          <div class="flex-shrink-0 w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
            <span class="material-symbols-outlined text-primary text-2xl">help</span>
          </div>
          <h3 class="text-xl font-bold text-gray-900 dark:text-white">{{ confirmModal.title || '확인' }}</h3>
        </div>
        <p class="text-gray-600 dark:text-text-secondary-dark mb-6 whitespace-pre-line">{{ confirmModal.message }}</p>
        <div class="flex gap-3">
          <button
            @click="cancelConfirm"
            class="flex-1 px-4 py-2 rounded-lg bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
          >
            취소
          </button>
          <button
            @click="confirmAction"
            class="flex-1 px-4 py-2 rounded-lg bg-primary text-white font-bold hover:bg-blue-400 transition-colors"
          >
            확인
          </button>
        </div>
      </div>
    </div>

    <!-- 토스트 알림 -->
    <div
      v-if="toastMessage"
      class="fixed top-4 right-4 z-50 bg-white dark:bg-card-dark rounded-lg border border-gray-200 dark:border-border-dark shadow-lg p-4 min-w-[300px] max-w-md animate-slide-in"
      :class="toastType === 'error' ? 'border-red-500' : 'border-primary'"
    >
      <div class="flex items-center gap-3">
        <span
          class="material-symbols-outlined text-2xl"
          :class="toastType === 'error' ? 'text-red-500' : 'text-primary'"
        >
          {{ toastType === 'error' ? 'error' : 'check_circle' }}
        </span>
        <p class="flex-1 text-gray-900 dark:text-white">{{ toastMessage }}</p>
        <button @click="toastMessage = ''" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </div>

    <!-- 날짜 설정 모달 -->
    <div v-if="showDateRangeModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4" @click.self="closeDateRangeModal">
      <div class="bg-[#1a2035] w-full max-w-md rounded-2xl border border-white/10 shadow-2xl overflow-hidden">
        <div class="p-6 border-b border-white/10">
          <h3 class="text-xl font-bold text-white mb-1">📅 여행 날짜 설정</h3>
          <p class="text-gray-400 text-sm">여행의 출발일과 도착일을 설정해주세요.</p>
        </div>
        
        <form @submit.prevent="updatePlanDates" class="p-6 flex flex-col gap-6">
          <div class="flex flex-col gap-2">
            <label class="text-white text-sm font-bold">출발일</label>
            <input 
              v-model="dateRangeForm.departureDate" 
              type="date" 
              class="w-full bg-white/5 border border-white/10 rounded-lg text-white px-4 py-3 outline-none focus:border-primary transition-colors"
              required
            />
          </div>
          
          <div class="flex flex-col gap-2">
            <label class="text-white text-sm font-bold">도착일</label>
            <input 
              v-model="dateRangeForm.arrivalDate" 
              type="date" 
              class="w-full bg-white/5 border border-white/10 rounded-lg text-white px-4 py-3 outline-none focus:border-primary transition-colors"
              required
            />
          </div>

          <p class="text-xs text-orange-400 bg-orange-400/10 p-3 rounded-lg flex gap-2 items-start">
             <span class="material-symbols-outlined text-sm mt-0.5">info</span>
             <span>날짜를 변경하면 기간에 맞춰 일자별 카드가 자동으로 생성되거나 삭제됩니다.</span>
          </p>
          
          <div class="flex gap-3 pt-2">
            <button 
              type="button" 
              @click="closeDateRangeModal" 
              class="flex-1 py-3 rounded-xl bg-white/5 text-gray-400 hover:text-white hover:bg-white/10 transition-colors font-bold"
            >
              취소
            </button>
            <button 
              type="submit" 
              :disabled="updatingDates"
              class="flex-1 py-3 rounded-xl bg-primary text-white hover:bg-blue-600 transition-colors font-bold disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <span v-if="updatingDates" class="material-symbols-outlined animate-spin text-lg">sync</span>
              <span>{{ updatingDates ? '저장 중...' : '저장하기' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Header from '@/components/Header.vue'
import planApi from '@/services/api/plan'
import boardApi from '@/services/api/board'
import draggable from 'vuedraggable'
import NaverMap from '@/components/common/NaverMap.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const loadingMessage = ref('여행 계획을 불러오는 중...')
const plans = ref([])
const currentPlanId = ref(null)
const planDetails = ref(null)
const dailyItinerary = ref([])
const aiRecommendations = ref([])
const showSearchModal = ref(false)
const showAiModal = ref(false)
const currentSearchTab = ref('tour')
const searchResults = ref([])
const locationRecommendations = ref([])
const selectedAiItem = ref(null)
const showDateSelectModal = ref(false)
const showSharePlanModal = ref(false)
const sharingPlan = ref(false)
const sharePlanForm = ref({
  title: '',
  content: '',
  regionCode: '',
  tripType: '',
  season: ''
})

const selectedDateToAdd = ref(null)

const showDateRangeModal = ref(false)
const updatingDates = ref(false)
const dateRangeForm = ref({
  departureDate: '',
  arrivalDate: ''
})

// 확인 모달 상태
const showConfirmModal = ref(false)
const confirmModal = ref({
  title: '',
  message: '',
  onConfirm: null
})

// 토스트 알림 상태
const toastMessage = ref('')
const toastType = ref('success') // 'success' | 'error'

const searchForm = ref({
  areaCode: '',
  keyword: '',
  trainDep: '',
  trainArr: '',
  trainType: '',
  trainTime: ''
})


// 지도 필터링 상태 제거됨 (각 Day별 임베디드 매핑 사용)

/**
 * 계획 목록 로드
 */
const loadPlans = async () => {
  loading.value = true
  loadingMessage.value = '여행 계획 목록을 불러오는 중...'
  try {
    const result = await planApi.getPlans()
    if (result.success) {
      // 공유본(제목에 "(공유본)" 포함)과 공유된 계획(isPublic === true)은 제외하고 필터링
      plans.value = (result.data || []).filter(plan => {
        // 공유본 제외 (제목에 "(공유본)"이 포함된 경우)
        if (plan.title && plan.title.includes('(공유본)')) {
          return false
        }
        // 공유된 계획 제외
        if (plan.isPublic) {
          return false
        }
        return true
      })
      // 각 계획의 첫 번째 관광지 이미지 로드
      await loadPlanImages()
    }
  } catch (error) {
    console.error('계획 목록 로드 오류:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 각 계획의 첫 번째 관광지 이미지 로드 (병렬 처리)
 */
const loadPlanImages = async () => {
  const imagePromises = plans.value.map(async (plan) => {
    try {
      const planDetailResult = await planApi.getPlan(plan.planId)
      if (planDetailResult.success && planDetailResult.data) {
        const destinationDetails = planDetailResult.data.destinationDetails || []
        // 첫 번째 관광지 이미지 찾기
        for (const dest of destinationDetails) {
          if (dest.tourInfo) {
            const imageUrl = dest.tourInfo.firstImage || dest.tourInfo.firstImage2
            if (imageUrl) {
              plan.firstImage = imageUrl
              return
            }
          }
        }
        // 관광지에 이미지가 없으면 숙소 이미지 확인
        if (!plan.firstImage) {
          const accommodations = planDetailResult.data.accommodations || []
          for (const acc of accommodations) {
            if (acc.tourInfo) {
              const imageUrl = acc.tourInfo.firstImage || acc.tourInfo.firstImage2
              if (imageUrl) {
                plan.firstImage = imageUrl
                return
              }
            }
          }
        }
      }
    } catch (error) {
      console.error(`계획 ${plan.planId} 이미지 로드 오류:`, error)
    }
  })
  
  await Promise.all(imagePromises)
}

/**
 * 계획 상세 로드
 */
const loadPlanDetails = async (planId) => {
  loading.value = true
  loadingMessage.value = '여행 계획을 불러오는 중...'
  try {
    const result = await planApi.getPlan(planId)
    if (result.success) {
      planDetails.value = result.data
      dailyItinerary.value = organizeItemsByDate(result.data)
      aiRecommendations.value = result.data.aiRecommendations || []
    }
  } catch (error) {
    console.error('계획 상세 로드 오류:', error)
    showToast('계획을 불러오는데 실패했습니다', 'error')
    navigateToPlanList()
  } finally {
    loading.value = false
  }
}

/**
 * 날짜별로 항목 그룹화
 */
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

/**
 * 유틸리티 함수들
 */
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

const getItemId = (itemData) => {
  return itemData.planDestination?.planDestinationId || itemData.planAccommodationId || itemData.transportationId
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

/**
 * 네비게이션
 */
const viewPlan = (planId) => {
  router.push(`/dashboard?planId=${planId}`)
}

const navigateToPlanList = () => {
  router.push('/dashboard')
  currentPlanId.value = null
  planDetails.value = null
  loadPlans()
}

/**
 * 계획 삭제
 */
const deletePlan = async (planId) => {
  if (!(await customConfirm('정말 삭제하시겠습니까?', '계획 삭제'))) return
  try {
    await planApi.deletePlan(planId)
    await loadPlans()
    showToast('계획이 삭제되었습니다')
  } catch (error) {
    console.error('계획 삭제 오류:', error)
    showToast('삭제 실패', 'error')
  }
}

/**
 * 제목 수정
 */
const editTitle = async () => {
  const currentTitle = planDetails.value.plan.title
  const newTitle = await prompt('수정할 여행 제목을 입력하세요:', currentTitle)
  if (newTitle === null || newTitle === undefined || newTitle.trim() === '') return

  try {
    await planApi.updatePlanTitle(currentPlanId.value, newTitle)
    planDetails.value.plan.title = newTitle
  } catch (error) {
    console.error('제목 수정 오류:', error)
    showToast('제목 수정 실패', 'error')
  }
}

/**
 * 항목 삭제
 */
const handleDeleteItem = async (item) => {
  if (!(await customConfirm('이 항목을 삭제하시겠습니까?', '항목 삭제'))) return
  try {
    await planApi.deleteItem(currentPlanId.value, item)
    await loadPlanDetails(currentPlanId.value)
    showToast('항목이 삭제되었습니다')
  } catch (error) {
    console.error('항목 삭제 오류:', error)
    showToast('삭제 실패: ' + (error.response?.data?.message || error.message), 'error')
  }
}

/**
 * 날짜 설정 관련
 */
const openDateRangeModal = () => {
  if (planDetails.value && planDetails.value.plan) {
    dateRangeForm.value = {
      departureDate: planDetails.value.plan.departureDate || new Date().toISOString().split('T')[0],
      arrivalDate: planDetails.value.plan.arrivalDate || new Date(Date.now() + 86400000).toISOString().split('T')[0]
    }
  } else {
    const today = new Date().toISOString().split('T')[0]
    dateRangeForm.value = {
      departureDate: today,
      arrivalDate: today 
    }
  }
  showDateRangeModal.value = true
}

const closeDateRangeModal = () => {
  showDateRangeModal.value = false
}

const updatePlanDates = async () => {
  if (!dateRangeForm.value.departureDate || !dateRangeForm.value.arrivalDate) {
    showToast('출발일과 도착일을 모두 선택해주세요', 'error')
    return
  }

  if (dateRangeForm.value.departureDate > dateRangeForm.value.arrivalDate) {
    showToast('출발일이 도착일보다 늦을 수 없습니다', 'error')
    return
  }
  
  try {
    updatingDates.value = true
    
    // 이전 날짜 저장 및 해당 날짜의 교통편 항목 미리 확보
    const oldDepartureDate = planDetails.value.plan.departureDate
    const oldArrivalDate = planDetails.value.plan.arrivalDate
    
    // updatePlan 호출 전에 dailyItinerary에서 미리 찾아서 저장해둠
    const oldStartItems = dailyItinerary.value.find(d => d.dateStr === oldDepartureDate)?.items.filter(i => i.type === 'transport') || []
    const oldEndItems = dailyItinerary.value.find(d => d.dateStr === oldArrivalDate)?.items.filter(i => i.type === 'transport') || []

    await planApi.updatePlan(currentPlanId.value, {
      departureDate: dateRangeForm.value.departureDate,
      arrivalDate: dateRangeForm.value.arrivalDate
    })
    
    await loadPlanDetails(currentPlanId.value)
    closeDateRangeModal()

    // 1. 출발일 변경 시 처리
    if (oldDepartureDate && oldDepartureDate !== dateRangeForm.value.departureDate) {
       // 기존 출발일에 기차표가 있는지 확인 (미리 저장해둔 변수 사용)
       if (oldStartItems.length > 0) {
          if (await customConfirm(`출발일이 변경되었습니다.\n기존 출발일(${oldDepartureDate})의 교통편을 삭제하시겠습니까?`, '교통편 삭제')) {
             for (const item of oldStartItems) {
                await planApi.deleteItem(currentPlanId.value, item)
             }
             // 화면 갱신을 위해 다시 로드
             await loadPlanDetails(currentPlanId.value)
          }
       }

       // 새 출발일 기차 검색 여부 묻기
       if (await customConfirm(`새로운 출발일(${dateRangeForm.value.departureDate})의 기차표를 검색하시겠습니까?`, '기차표 검색')) {
          // 검색 모달 열기 (기차 탭, 값 미리 채우기)
          searchForm.value.trainDep = planDetails.value.plan.departureRegionCode ? getStationName(planDetails.value.plan.departureRegionCode) : ''
          searchForm.value.trainArr = planDetails.value.plan.arrivalRegionCode ? getStationName(planDetails.value.plan.arrivalRegionCode) : ''
          // 날짜는 자동 반영됨 (plan.departureDate 기준)
          
          switchSearchTab('train')
          showSearchModal.value = true
       }
    }

    // 2. 도착일 변경 시 처리
    if (oldArrivalDate && oldArrivalDate !== dateRangeForm.value.arrivalDate) {
       // 기존 도착일에 기차표가 있는지 확인
       if (oldEndItems.length > 0) {
          if (await customConfirm(`도착일이 변경되었습니다.\n기존 도착일(${oldArrivalDate})의 교통편을 삭제하시겠습니까?`, '교통편 삭제')) {
             for (const item of oldEndItems) {
                await planApi.deleteItem(currentPlanId.value, item)
             }
             await loadPlanDetails(currentPlanId.value)
          }
       }

       if (await customConfirm(`새로운 도착일(${dateRangeForm.value.arrivalDate})의 기차표를 검색하시겠습니까?`, '기차표 검색')) {
          searchForm.value.trainDep = planDetails.value.plan.arrivalRegionCode ? getStationName(planDetails.value.plan.arrivalRegionCode) : ''
          searchForm.value.trainArr = planDetails.value.plan.departureRegionCode ? getStationName(planDetails.value.plan.departureRegionCode) : ''
          
          switchSearchTab('train')
          showSearchModal.value = true
       }
    }

  } catch (error) {
    console.error('날짜 수정 오류:', error)
    showToast('날짜 수정 중 오류가 발생했습니다: ' + (error.response?.data?.message || error.message), 'error')
  } finally {
    updatingDates.value = false
  }
}

// 지역 코드를 기차역 이름으로 변환하는 간단한 헬퍼
const getStationName = (code) => {
   const map = {
     '1': '서울', '2': '인천', '3': '대전', '4': '대구', '5': '광주', '6': '부산',
     '31': '수원', '32': '강릉', '33': '청주', '34': '천안아산', '35': '포항', '36': '창원',
     '37': '전주', '38': '여수EXPO', '39': '제주'
   }
   return map[code] || ''
}

/**
 * 검색 모달 관련 함수
 */
const switchSearchTab = (tab) => {
  currentSearchTab.value = tab
  searchResults.value = []
}

const closeSearchModal = () => {
  showSearchModal.value = false
  searchResults.value = []
}

const openSearchModalWithDate = (dateStr) => {
  selectedDateToAdd.value = dateStr
  showSearchModal.value = true
  searchResults.value = []
}

const searchLoading = ref(false)

const executeSearch = async () => {
  try {
    searchLoading.value = true
    
    let result

    if (currentSearchTab.value === 'train') {
      const date = planDetails.value?.plan.departureDate || new Date().toISOString().split('T')[0]
      result = await planApi.searchTrain({
        depStation: searchForm.value.trainDep,
        arrStation: searchForm.value.trainArr,
        date: date,
        time: searchForm.value.trainTime,
        trainType: searchForm.value.trainType
      })
    } else {
      const contentTypeId = currentSearchTab.value === 'accom' ? '32' : '12'
      result = await planApi.searchTour({
        keyword: searchForm.value.keyword,
        contentTypeId: contentTypeId,
        areaCode: searchForm.value.areaCode
      })
    }

    if (result.success) {
      searchResults.value = result.data || []
    }
  } catch (error) {
    console.error('검색 오류:', error)
    showToast('검색 중 오류가 발생했습니다', 'error')
  } finally {
    searchLoading.value = false
  }
}

const handleAddFromMap = async (item, dateStr) => {
  try {
    // 1. 데이터 포맷팅 (backend API 기대 포맷)
    const addItemData = {
        planId: currentPlanId.value,
        type: 'attraction',
        data: {
             contentId: item.contentid,
             contentTypeId: item.contenttypeid,
             title: item.title,
             addr1: item.addr1,
             addr2: item.addr2,
             mapx: Number(item.mapx),
             mapy: Number(item.mapy),
             firstImage: item.firstimage,
             firstImage2: item.firstimage2,
             visitDate: `${dateStr}T10:00:00` // 기본 시간
        }
    }
    
    // 2. API 호출
    const response = await planApi.addItem(currentPlanId.value, addItemData);
    if (response.success) {
        // 3. 성공 시 목록 갱신 (로컬 상태만 갱신하거나 전체 다시 로드)
        // 여기서는 전체 다시 로드해서 정렬 등 맞춤
        await loadPlanDetails(currentPlanId.value);
    } else {
        showToast('추가 실패: ' + response.message, 'error');
    }
  } catch (error) {
    console.error('Failed to add item from map:', error);
    showToast('장소 추가 중 오류가 발생했습니다.', 'error');
  }
}    
const onDragChange = async (event, newDateStr) => {
  if (event.added) {
    const item = event.added.element
        
    try {
      let idData = {}
      if (item.type === 'attraction' || item.type === 'destination') {
        idData = { spotOrder: item.data.planDestination?.spotOrder || item.data.planSpotId } 
      } else if (item.type === 'hotel' || item.type === 'accommodation') {
        idData = { 
          checkInDate: item.data.checkInDate,
          accommodationOrder: item.data.accommodationOrder
        }
      } else {
        // Transport or others -> skip or handle if needed
        return
      }
      
      await planApi.moveItem(currentPlanId.value, item.type, idData, newDateStr);

    } catch (error) {
      console.error('이동 실패:', error)
      showToast('일정 이동에 실패했습니다.', 'error')
      await loadPlanDetails(currentPlanId.value) // Revert
    }
  }
}

const addItemToPlan = async (item) => {
  if (!(await customConfirm('이 항목을 계획에 추가하시겠습니까?', '항목 추가'))) return
  try {
    const payload = { ...item, targetDate: selectedDateToAdd.value }
    await planApi.addItem(currentPlanId.value, payload)
    showToast('추가되었습니다!')
    closeSearchModal()
    await loadPlanDetails(currentPlanId.value)
  } catch (error) {
    console.error('항목 추가 오류:', error)
    showToast('추가 실패', 'error')
  }
}

/**
 * 여행 계획 공유 모달 열기
 */
const openSharePlanModal = () => {
  if (!planDetails.value || !planDetails.value.plan) {
    showToast('여행 계획 정보를 불러올 수 없습니다', 'error')
    return
  }
  
  const plan = planDetails.value.plan
  sharePlanForm.value = {
    title: plan.title || '',
    content: `${plan.title} 여행 계획을 공유합니다.\n\n기간: ${plan.departureDate} ~ ${plan.arrivalDate}\n인원: 성인 ${plan.adultCount}명, 아동 ${plan.childCount || 0}명`,
    regionCode: plan.arrivalRegionCode || '',
    tripType: '',
    season: ''
  }
  
  showSharePlanModal.value = true
}

/**
 * 여행 계획 공유
 */
const sharePlan = async () => {
  if (!sharePlanForm.value.title || !sharePlanForm.value.content) {
    showToast('제목과 내용을 입력해주세요', 'error')
    return
  }
  
  if (!planDetails.value || !planDetails.value.plan) {
    showToast('여행 계획 정보를 불러올 수 없습니다', 'error')
    return
  }
  
  sharingPlan.value = true
  try {
    const result = await boardApi.createPost({
      planId: planDetails.value.plan.planId,
      title: sharePlanForm.value.title,
      content: sharePlanForm.value.content,
      regionCode: sharePlanForm.value.regionCode || null,
      tripType: sharePlanForm.value.tripType || null,
      season: sharePlanForm.value.season || null,
      category: 'TRAVEL_PLAN'
    })
    
    if (result.success) {
      showToast('여행 계획이 게시판에 공유되었습니다!')
      showSharePlanModal.value = false
      router.push('/board')
    } else {
      showToast(result.message || '공유에 실패했습니다', 'error')
    }
  } catch (error) {
    console.error('여행 계획 공유 오류:', error)
    showToast('공유 중 오류가 발생했습니다', 'error')
  } finally {
    sharingPlan.value = false
  }
}

/**
 * AI 추천
 */
const handleAiFillClick = async () => {
  if (!currentPlanId.value) return

  if (planDetails.value?.plan.arrivalRegionCode) {
    if (!(await customConfirm('AI가 이 여행 계획을 자동으로 채워줍니다.\n기존 데이터는 유지되거나 업데이트됩니다. 계속하시겠습니까?', 'AI 자동 채우기'))) return
    await executeFillPlan(null)
  } else {
    startLocationBasedRecommendation()
  }
}

const startLocationBasedRecommendation = () => {
  loading.value = true
  loadingMessage.value = '현재 위치를 확인하고 갈만한 곳을 찾는 중입니다...'

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        fetchAndShowRecommendations(latitude, longitude)
      },
      (error) => {
        console.warn('위치 정보 실패/거부:', error)
        fetchAndShowRecommendations(37.5665, 126.9780) // 서울 기준
      }
    )
  } else {
    fetchAndShowRecommendations(37.5665, 126.9780)
  }
}

const fetchAndShowRecommendations = async (lat, lon) => {
  try {
    const result = await planApi.getRecommendDestinations(lat, lon)
    if (result.success) {
      locationRecommendations.value = result.data || []
      showAiModal.value = true
    }
  } catch (error) {
    console.error('추천 정보 가져오기 오류:', error)
    showToast('추천 정보를 가져오는데 실패했습니다.', 'error')
  } finally {
    loading.value = false
  }
}

const selectRecommendation = async (rec) => {
  closeAiModal()
  
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(today.getDate() + 1)
  const dayAfter = new Date(today)
  dayAfter.setDate(today.getDate() + 2)

  const formatDate = (d) => d.toISOString().split('T')[0]

  const updateData = {
    arrivalRegionCode: rec.regionCode,
    departureRegionCode: '1',
    departureDate: formatDate(tomorrow),
    arrivalDate: formatDate(dayAfter),
    adultCount: 1,
    hasPet: false
  }

  await executeFillPlan(updateData)
}

const executeFillPlan = async (updateData) => {
  loading.value = true
  loadingMessage.value = 'AI가 여행 계획을 만들고 있습니다...'

  try {
    await planApi.fillPlan(currentPlanId.value, updateData)
    showToast('여행 계획이 완성되었습니다!')
    await loadPlanDetails(currentPlanId.value)
  } catch (error) {
    console.error('AI 채우기 오류:', error)
    showToast('실패: ' + (error.message || '알 수 없는 오류'), 'error')
  } finally {
    loading.value = false
  }
}

const openAiDateSelectModal = (itemData) => {
  if (!dailyItinerary.value || dailyItinerary.value.length === 0) {
     showToast("일정 정보가 없어 추가할 수 없습니다.", 'error');
     return;
  }
  selectedAiItem.value = itemData
  showDateSelectModal.value = true
}

const confirmAddAiItem = async (dateStr) => {
  if (!selectedAiItem.value) return
  showDateSelectModal.value = false
  
  try {
    const payload = { 
        ...selectedAiItem.value, 
        targetDate: dateStr 
    };
    loading.value = true;
    await planApi.addAiRecommendation(currentPlanId.value, payload)
    showToast('선택한 날짜에 추가되었습니다!')
    await loadPlanDetails(currentPlanId.value)
  } catch (error) {
    console.error('AI 추천 추가 오류:', error)
    showToast('추가 실패: ' + error.message, 'error')
  } finally {
    loading.value = false;
    selectedAiItem.value = null
  }
}

const closeDateSelectModal = () => {
  showDateSelectModal.value = false
  selectedAiItem.value = null
}

const closeAiModal = () => {
  showAiModal.value = false
}

/**
 * 커스텀 confirm 함수
 */
const customConfirm = (message, title = '확인') => {
  return new Promise((resolve) => {
    confirmModal.value = {
      title,
      message,
      onConfirm: () => {
        showConfirmModal.value = false
        resolve(true)
      }
    }
    showConfirmModal.value = true
  })
}

const confirmAction = () => {
  if (confirmModal.value.onConfirm) {
    confirmModal.value.onConfirm()
  }
}

const cancelConfirm = () => {
  showConfirmModal.value = false
  confirmModal.value.onConfirm = null
}

/**
 * 커스텀 alert 함수 (토스트)
 */
const showToast = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  setTimeout(() => {
    toastMessage.value = ''
  }, 3000)
}

/**
 * URL 쿼리 파라미터 감시
 */
watch(() => route.query.planId, (newPlanId) => {
  if (newPlanId) {
    currentPlanId.value = Number(newPlanId)
    loadPlanDetails(Number(newPlanId))
  } else {
    currentPlanId.value = null
    planDetails.value = null
    loadPlans()
  }
}, { immediate: true })

onMounted(() => {
  if (route.query.planId) {
    currentPlanId.value = Number(route.query.planId)
    loadPlanDetails(Number(route.query.planId))
  } else {
    loadPlans()
  }
})
</script>

<style scoped>
@keyframes slide-in {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.animate-slide-in {
  animation: slide-in 0.3s ease-out;
}
</style>

<style scoped>
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #111827;
}

::-webkit-scrollbar-thumb {
  background: #374151;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #4B5563;
}

select option {
  background-color: #1a2035;
  color: white;
  padding: 10px;
}

/* 드래그 중 스크롤 영역 감지 개선 */
.sortable-ghost {
  opacity: 0.5;
  background: #3b82f620;
}
</style>
