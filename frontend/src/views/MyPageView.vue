<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-light dark:bg-background-dark">
    <Header />
    <main class="flex-1 bg-background-light dark:bg-background-dark">
      <div class="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between mb-8">
          <h1 class="text-3xl md:text-4xl font-extrabold text-gray-900 dark:text-text-dark">마이페이지</h1>
          <button
            @click="showSettingsModal = true"
            class="flex items-center justify-center w-10 h-10 rounded-lg bg-white dark:bg-card-dark text-gray-900 dark:text-text-dark border border-gray-200 dark:border-border-dark hover:bg-gray-50 dark:hover:bg-card-dark/80 transition-colors"
            title="설정"
          >
            <span class="material-symbols-outlined">settings</span>
          </button>
        </div>
        
        <!-- 사용자 정보 섹션 -->
        <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 mb-6">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-text-dark mb-6">내 정보</h2>
          
          <!-- 프로필 이미지 및 이름 -->
          <div class="flex items-center gap-4 mb-4">
            <div class="relative">
              <img
                v-if="authStore.user?.profileImage"
                :src="authStore.user.profileImage"
                alt="프로필"
                class="w-20 h-20 rounded-full object-cover border-2 border-primary"
              />
              <div
                v-else
                class="w-20 h-20 rounded-full bg-primary/20 flex items-center justify-center border-2 border-primary"
              >
                <span class="material-symbols-outlined text-4xl text-primary">person</span>
              </div>
            </div>
            <div class="flex-1">
              <h3 class="text-xl font-bold text-gray-900 dark:text-text-dark">{{ authStore.user?.name || authStore.user?.nickname || '사용자' }}</h3>
              <p v-if="authStore.user?.nickname && authStore.user?.nickname !== authStore.user?.name" class="text-gray-600 dark:text-text-secondary-dark">
                {{ authStore.user.nickname }}
              </p>
              <p v-if="authStore.user?.bio" class="text-gray-600 dark:text-text-secondary-dark text-sm mt-2">{{ authStore.user.bio }}</p>
              <span v-if="isGoogleUser" class="inline-block mt-1 px-2 py-1 rounded bg-blue-500/20 text-blue-400 text-xs">
                Google 로그인
              </span>
            </div>
          </div>
          
          <!-- 사용자 정보 그리드 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">이메일</label>
              <p class="text-gray-900 dark:text-text-dark font-medium">{{ authStore.user?.email || '-' }}</p>
            </div>
            <div>
              <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">이름</label>
              <p class="text-gray-900 dark:text-text-dark font-medium">{{ authStore.user?.name || '-' }}</p>
            </div>
            <div>
              <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">닉네임</label>
              <p class="text-gray-900 dark:text-text-dark font-medium">{{ nickname || '닉네임이 없습니다' }}</p>
            </div>
            <div v-if="authStore.user?.phone">
              <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">전화번호</label>
              <p class="text-gray-900 dark:text-text-dark font-medium">{{ authStore.user.phone }}</p>
            </div>
            <div>
              <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">가입일</label>
              <p class="text-gray-900 dark:text-text-dark font-medium">{{ formatDate(authStore.user?.createdAt) }}</p>
            </div>
          </div>
          
          <!-- 프로필 수정 및 로그아웃 버튼 -->
          <div class="mt-6 pt-6 border-t border-gray-200 dark:border-border-dark flex gap-3">
            <button
              @click="openProfileModal"
              class="flex-1 flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-primary text-white font-medium hover:bg-blue-400 transition-colors"
            >
              <span class="material-symbols-outlined">edit</span>
              <span>프로필 수정</span>
            </button>
            <button
              @click="handleLogout"
              class="flex-1 flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
            >
              <span class="material-symbols-outlined">logout</span>
              <span>로그아웃</span>
            </button>
          </div>
          
          <!-- 내가 쓴 글 / 댓글 단 글 / 좋아요한 글 통계 -->
          <div class="mt-6 pt-6 border-t border-gray-200 dark:border-border-dark">
            <div class="bg-gray-50 dark:bg-background-dark rounded-xl overflow-hidden">
              <div class="flex divide-x divide-gray-200 dark:divide-border-dark">
                <!-- 내가 쓴 글 -->
                <div
                  @click="openMyPostsModal"
                  class="flex-1 flex flex-col items-center justify-center py-8 px-6 cursor-pointer hover:bg-gray-100 dark:hover:bg-background-dark/50 transition-colors group"
                >
                  <div class="text-2xl font-bold text-gray-900 dark:text-text-dark mb-3 group-hover:scale-105 transition-transform">{{ myPostsCount }}</div>
                  <div class="text-gray-600 dark:text-text-secondary-dark text-base font-medium">내가 쓴 글</div>
                </div>
                
                <!-- 댓글 단 글 -->
                <div
                  @click="openCommentedPostsModal"
                  class="flex-1 flex flex-col items-center justify-center py-8 px-6 cursor-pointer hover:bg-gray-100 dark:hover:bg-background-dark/50 transition-colors group"
                >
                  <div class="text-2xl font-bold text-gray-900 dark:text-text-dark mb-3 group-hover:scale-105 transition-transform">{{ commentedPostsCount }}</div>
                  <div class="text-gray-600 dark:text-text-secondary-dark text-base font-medium">댓글 단 글</div>
                </div>
                
                <!-- 좋아요한 글 -->
                <div
                  @click="openLikedPostsModal"
                  class="flex-1 flex flex-col items-center justify-center py-8 px-6 cursor-pointer hover:bg-gray-100 dark:hover:bg-background-dark/50 transition-colors group"
                >
                  <div class="text-2xl font-bold text-gray-900 dark:text-text-dark mb-3 group-hover:scale-105 transition-transform">{{ likedPostsCount }}</div>
                  <div class="text-gray-600 dark:text-text-secondary-dark text-base font-medium">좋아요한 글</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 여행 성향 설정 섹션 -->
        <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-text-dark mb-6">여행 성향 설정</h2>
          <p class="text-gray-600 dark:text-text-secondary-dark mb-6">AI 여행지 추천을 위해 여행 성향을 설정해주세요.</p>
          
          <!-- 읽기 모드 -->
          <div v-if="!editPreferenceMode && hasPreference" id="preferenceViewContainer" class="space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div v-if="preference.preferredTripType">
                <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">선호하는 여행 타입</label>
                <p class="text-gray-900 dark:text-text-dark font-medium">{{ preference.preferredTripType }}</p>
              </div>
              <div v-if="preferredActivitiesList.length > 0">
                <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">선호하는 활동</label>
                <p class="text-gray-900 dark:text-text-dark font-medium">{{ preferredActivitiesList.join(', ') }}</p>
              </div>
              <div v-if="preference.budgetPreference">
                <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">예산 선호도</label>
                <p class="text-gray-900 dark:text-text-dark font-medium">{{ preference.budgetPreference }}</p>
              </div>
              <div v-if="preference.accommodationPreference">
                <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">숙소 선호도</label>
                <p class="text-gray-900 dark:text-text-dark font-medium">{{ preference.accommodationPreference }}</p>
              </div>
              <div v-if="preference.seasonPreference">
                <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">계절 선호도</label>
                <p class="text-gray-900 dark:text-text-dark font-medium">{{ preference.seasonPreference }}</p>
              </div>
              <div v-if="preference.transportationPreference">
                <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">교통 수단 선호도</label>
                <p class="text-gray-900 dark:text-text-dark font-medium">{{ preference.transportationPreference }}</p>
              </div>
              <div v-if="preference.foodPreference">
                <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">음식 선호도</label>
                <p class="text-gray-900 dark:text-text-dark font-medium">{{ preference.foodPreference }}</p>
              </div>
              <div v-if="preference.travelStyle">
                <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">여행 스타일</label>
                <p class="text-gray-900 dark:text-text-dark font-medium">{{ preference.travelStyle }}</p>
              </div>
            </div>
            <div v-if="preference.additionalInfo">
              <label class="text-gray-600 dark:text-text-secondary-dark text-sm mb-1 block">추가 정보</label>
              <p class="text-gray-900 dark:text-text-dark font-medium whitespace-pre-wrap">{{ preference.additionalInfo }}</p>
            </div>
            <div class="flex gap-3 pt-4">
              <button
                @click="editPreferenceMode = true"
                class="flex-1 flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-primary text-white font-bold hover:bg-blue-400 transition-colors"
              >
                <span class="material-symbols-outlined">edit</span>
                <span>수정하기</span>
              </button>
            </div>
          </div>
          
          <!-- 편집 모드 -->
          <form v-else id="preferenceForm" @submit.prevent="savePreference" class="space-y-6">
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">선호하는 여행 타입</label>
              <select
                v-model="preference.preferredTripType"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="가족여행">가족여행</option>
                <option value="커플여행">커플여행</option>
                <option value="혼자여행">혼자여행</option>
                <option value="우정여행">우정여행</option>
                <option value="반려동물 동반">반려동물 동반</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">선호하는 활동 (복수 선택 가능)</label>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <label
                  v-for="activity in activities"
                  :key="activity"
                  class="flex items-center gap-2 cursor-pointer"
                >
                  <input
                    type="checkbox"
                    :value="activity"
                    v-model="selectedActivities"
                    class="form-checkbox rounded text-primary focus:ring-primary/50"
                  />
                  <span class="text-gray-900 dark:text-text-dark text-sm">{{ activity }}</span>
                </label>
              </div>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">예산 선호도</label>
              <select
                v-model="preference.budgetPreference"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="절약형">절약형</option>
                <option value="보통">보통</option>
                <option value="여유형">여유형</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">숙소 선호도</label>
              <select
                v-model="preference.accommodationPreference"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="호텔">호텔</option>
                <option value="펜션">펜션</option>
                <option value="게스트하우스">게스트하우스</option>
                <option value="캠핑">캠핑</option>
                <option value="리조트">리조트</option>
                <option value="상관없음">상관없음</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">계절 선호도</label>
              <select
                v-model="preference.seasonPreference"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="봄">봄</option>
                <option value="여름">여름</option>
                <option value="가을">가을</option>
                <option value="겨울">겨울</option>
                <option value="상관없음">상관없음</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">교통 수단 선호도</label>
              <select
                v-model="preference.transportationPreference"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="자가용">자가용</option>
                <option value="대중교통">대중교통</option>
                <option value="렌트카">렌트카</option>
                <option value="상관없음">상관없음</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">음식 선호도</label>
              <select
                v-model="preference.foodPreference"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="한식">한식</option>
                <option value="양식">양식</option>
                <option value="중식">중식</option>
                <option value="일식">일식</option>
                <option value="다양하게">다양하게</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">여행 스타일</label>
              <select
                v-model="preference.travelStyle"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
              >
                <option value="">선택 안함</option>
                <option value="계획형">계획형</option>
                <option value="즉흥형">즉흥형</option>
                <option value="둘 다">둘 다</option>
              </select>
            </div>
            
            <div>
              <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">추가 정보</label>
              <textarea
                v-model="preference.additionalInfo"
                rows="4"
                class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark p-4"
                placeholder="추가로 알려주고 싶은 여행 성향이 있다면 적어주세요."
              ></textarea>
            </div>
            
            <div class="flex gap-3 pt-4">
              <button
                type="submit"
                :disabled="savingPreference"
                class="flex-1 flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-primary text-white font-bold hover:bg-blue-400 transition-colors disabled:opacity-50"
              >
                <span v-if="savingPreference" class="material-symbols-outlined animate-spin">sync</span>
                <span v-else class="material-symbols-outlined">save</span>
                <span>{{ savingPreference ? '저장 중...' : '저장하기' }}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>

    <!-- 내가 쓴 글 모달 -->
    <Teleport to="body">
      <div
        v-if="showMyPostsModal"
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/50 backdrop-blur-sm"
        @click.self="showMyPostsModal = false"
      >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-text-dark">내가 쓴 글</h2>
          <button
            @click="showMyPostsModal = false"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-gray-600 dark:text-text-secondary-dark hover:bg-gray-100 dark:hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <!-- 게시글 목록 -->
        <div v-if="loadingPosts" class="grid grid-cols-1 gap-4">
          <div v-for="i in 3" :key="i" class="animate-pulse rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-32"></div>
        </div>

        <div v-else-if="posts.length === 0" class="text-center py-12">
          <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-text-secondary-dark mb-4">article</span>
          <p class="text-gray-600 dark:text-text-secondary-dark">작성한 글이 없습니다</p>
        </div>

        <div v-else class="grid grid-cols-1 gap-4">
          <div
            v-for="post in posts"
            :key="post.postId"
            class="group relative flex overflow-hidden rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark hover:border-primary transition-colors cursor-pointer"
            @click="viewPost(post.postId)"
          >
            <div
              class="w-32 h-32 flex-shrink-0 bg-cover bg-center"
              :style="{ backgroundImage: `url('${getPostImage(post)}')` }"
            ></div>
            <div class="flex flex-1 flex-col p-4">
              <div class="flex items-start justify-between gap-2 mb-2">
                <h3 class="text-lg font-bold text-gray-900 dark:text-text-dark flex-1 line-clamp-2">
                  {{ post.title || '제목 없음' }}
                </h3>
                <button
                  @click.stop="deletePost(post.postId)"
                  class="flex items-center justify-center rounded-lg p-2 text-red-500 hover:bg-red-500/10 transition-colors"
                  title="삭제"
                >
                  <span class="material-symbols-outlined text-sm">delete</span>
                </button>
              </div>
              <div class="mt-auto flex items-center justify-between pt-2">
                <div class="flex items-center gap-2">
                  <div
                    v-if="post.authorProfileImage"
                    class="size-5 rounded-full bg-cover bg-center bg-no-repeat"
                    :style="{ backgroundImage: `url('${post.authorProfileImage}')` }"
                  ></div>
                  <div v-else class="size-5 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                    <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                  </div>
                  <span class="text-xs font-medium text-gray-600 dark:text-text-secondary-dark">
                    {{ post.authorNickname || post.authorName || '작성자' }}
                  </span>
                </div>
                <div class="flex items-center gap-4 text-gray-600 dark:text-text-secondary-dark">
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">favorite_border</span>
                    <span class="text-xs">{{ formatCount(post.likeCount || 0) }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">chat_bubble_outline</span>
                    <span class="text-xs">0</span>
                  </div>
                  <span class="text-xs">{{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </Teleport>

    <!-- 댓글 단 글 모달 -->
    <Teleport to="body">
      <div
        v-if="showCommentedPostsModal"
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/50 backdrop-blur-sm"
        @click.self="showCommentedPostsModal = false"
      >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-text-dark">댓글 단 글</h2>
          <button
            @click="showCommentedPostsModal = false"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-gray-600 dark:text-text-secondary-dark hover:bg-gray-100 dark:hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <!-- 게시글 목록 -->
        <div v-if="loadingPosts" class="grid grid-cols-1 gap-4">
          <div v-for="i in 3" :key="i" class="animate-pulse rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-32"></div>
        </div>

        <div v-else-if="posts.length === 0" class="text-center py-12">
          <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-text-secondary-dark mb-4">chat_bubble_outline</span>
          <p class="text-gray-600 dark:text-text-secondary-dark">댓글 단 글이 없습니다</p>
        </div>

        <div v-else class="grid grid-cols-1 gap-4">
          <div
            v-for="post in posts"
            :key="post.postId"
            class="group relative flex overflow-hidden rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark hover:border-primary transition-colors cursor-pointer"
            @click="viewPost(post.postId)"
          >
            <div
              class="w-32 h-32 flex-shrink-0 bg-cover bg-center"
              :style="{ backgroundImage: `url('${getPostImage(post)}')` }"
            ></div>
            <div class="flex flex-1 flex-col p-4">
              <div class="flex items-start justify-between gap-2 mb-2">
                <h3 class="text-lg font-bold text-gray-900 dark:text-text-dark flex-1 line-clamp-2">
                  {{ post.title || '제목 없음' }}
                </h3>
              </div>
              <div class="mt-auto flex items-center justify-between pt-2">
                <div class="flex items-center gap-2">
                  <div
                    v-if="post.authorProfileImage"
                    class="size-5 rounded-full bg-cover bg-center bg-no-repeat"
                    :style="{ backgroundImage: `url('${post.authorProfileImage}')` }"
                  ></div>
                  <div v-else class="size-5 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                    <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                  </div>
                  <span class="text-xs font-medium text-gray-600 dark:text-text-secondary-dark">
                    {{ post.authorNickname || post.authorName || '작성자' }}
                  </span>
                </div>
                <div class="flex items-center gap-4 text-gray-600 dark:text-text-secondary-dark">
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">favorite_border</span>
                    <span class="text-xs">{{ formatCount(post.likeCount || 0) }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">chat_bubble_outline</span>
                    <span class="text-xs">0</span>
                  </div>
                  <span class="text-xs">{{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </Teleport>

    <!-- 좋아요한 글 모달 -->
    <Teleport to="body">
      <div
        v-if="showLikedPostsModal"
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/50 backdrop-blur-sm"
        @click.self="showLikedPostsModal = false"
      >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-text-dark">좋아요한 글</h2>
          <button
            @click="showLikedPostsModal = false"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-gray-600 dark:text-text-secondary-dark hover:bg-gray-100 dark:hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <!-- 게시글 목록 -->
        <div v-if="loadingPosts" class="grid grid-cols-1 gap-4">
          <div v-for="i in 3" :key="i" class="animate-pulse rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-32"></div>
        </div>

        <div v-else-if="posts.length === 0" class="text-center py-12">
          <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-text-secondary-dark mb-4">favorite_border</span>
          <p class="text-gray-600 dark:text-text-secondary-dark">좋아요한 글이 없습니다</p>
        </div>

        <div v-else class="grid grid-cols-1 gap-4">
          <div
            v-for="post in posts"
            :key="post.postId"
            class="group relative flex overflow-hidden rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark hover:border-primary transition-colors cursor-pointer"
            @click="viewPost(post.postId)"
          >
            <div
              class="w-32 h-32 flex-shrink-0 bg-cover bg-center"
              :style="{ backgroundImage: `url('${getPostImage(post)}')` }"
            ></div>
            <div class="flex flex-1 flex-col p-4">
              <div class="flex items-start justify-between gap-2 mb-2">
                <h3 class="text-lg font-bold text-gray-900 dark:text-text-dark flex-1 line-clamp-2">
                  {{ post.title || '제목 없음' }}
                </h3>
              </div>
              <div class="mt-auto flex items-center justify-between pt-2">
                <div class="flex items-center gap-2">
                  <div
                    v-if="post.authorProfileImage"
                    class="size-5 rounded-full bg-cover bg-center bg-no-repeat"
                    :style="{ backgroundImage: `url('${post.authorProfileImage}')` }"
                  ></div>
                  <div v-else class="size-5 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                    <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                  </div>
                  <span class="text-xs font-medium text-gray-600 dark:text-text-secondary-dark">
                    {{ post.authorNickname || post.authorName || '작성자' }}
                  </span>
                </div>
                <div class="flex items-center gap-4 text-gray-600 dark:text-text-secondary-dark">
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">favorite_border</span>
                    <span class="text-xs">{{ formatCount(post.likeCount || 0) }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">chat_bubble_outline</span>
                    <span class="text-xs">0</span>
                  </div>
                  <span class="text-xs">{{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </Teleport>

    <!-- 프로필 수정 모달 -->
    <div
      v-if="showProfileModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      @click.self="showProfileModal = false"
    >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-md w-full mx-4">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-text-dark">프로필 수정</h2>
          <button
            @click="showProfileModal = false"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-gray-600 dark:text-text-secondary-dark hover:bg-gray-100 dark:hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <div class="flex flex-col gap-6">
          <!-- 프로필 이미지 -->
          <div>
            <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-3 block">프로필 이미지</label>
            <div class="flex flex-col items-center gap-4">
              <div class="relative mx-auto">
                <img
                  v-if="profileImagePreview || authStore.user?.profileImage"
                  :src="profileImagePreview || authStore.user.profileImage"
                  alt="프로필 미리보기"
                  class="w-32 h-32 rounded-full object-cover border-2 border-primary mx-auto"
                />
                <div
                  v-else
                  class="w-32 h-32 rounded-full bg-primary/20 flex items-center justify-center border-2 border-primary mx-auto"
                >
                  <span class="material-symbols-outlined text-6xl text-primary">person</span>
                </div>
                <label
                  v-if="!isGoogleUser"
                  for="profileImageInputModal"
                  class="absolute bottom-0 right-0 w-8 h-8 rounded-full bg-primary flex items-center justify-center cursor-pointer hover:bg-blue-400 transition-colors border-2 border-background-dark"
                >
                  <span class="material-symbols-outlined text-sm text-white">camera_alt</span>
                </label>
                <input
                  v-if="!isGoogleUser"
                  type="file"
                  id="profileImageInputModal"
                  accept="image/*"
                  class="hidden"
                  @change="handleProfileImageChangeModal"
                />
                <p v-else class="text-gray-600 dark:text-text-secondary-dark text-sm text-center mt-2">Google 로그인 사용자는 프로필 이미지를 변경할 수 없습니다</p>
              </div>
            </div>
          </div>

          <!-- 닉네임 -->
          <div>
            <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">닉네임</label>
            <input
              v-model="editNickname"
              type="text"
              placeholder="닉네임을 입력하세요"
              class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark h-12 px-4"
            />
          </div>

          <!-- 소개 -->
          <div>
            <label class="text-gray-900 dark:text-text-dark text-base font-medium mb-2 block">소개</label>
            <textarea
              v-model="editBio"
              rows="4"
              placeholder="자신을 소개해주세요"
              class="w-full rounded-lg text-gray-900 dark:text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark px-4 py-3 resize-none"
            ></textarea>
          </div>

          <!-- 저장 버튼 -->
          <div class="flex gap-3 pt-4">
            <button
              @click="saveProfile"
              :disabled="savingProfile"
              class="flex-1 flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-primary text-white font-medium hover:bg-blue-400 transition-colors disabled:opacity-50"
            >
              <span v-if="savingProfile" class="material-symbols-outlined animate-spin">sync</span>
              <span v-else class="material-symbols-outlined">save</span>
              <span>{{ savingProfile ? '저장 중...' : '저장하기' }}</span>
            </button>
            <button
              @click="cancelProfileEdit"
              class="flex items-center justify-center rounded-lg h-12 px-6 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
            >
              취소
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 설정 모달 -->
    <div
      v-if="showSettingsModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      @click.self="showSettingsModal = false"
    >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-md w-full mx-4">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-text-dark">설정</h2>
          <button
            @click="showSettingsModal = false"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-gray-600 dark:text-text-secondary-dark hover:bg-gray-100 dark:hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <div class="flex flex-col gap-4">
          <!-- 다크모드 토글 -->
          <div class="flex items-center justify-between p-4 rounded-lg bg-gray-50 dark:bg-background-dark">
            <div class="flex items-center gap-3">
              <span class="material-symbols-outlined text-gray-900 dark:text-text-dark">dark_mode</span>
              <span class="text-gray-900 dark:text-text-dark font-medium">다크모드</span>
            </div>
            <button
              @click="toggleDarkMode"
              :class="[
                'relative inline-flex h-6 w-11 items-center rounded-full transition-colors',
                isDarkMode ? 'bg-primary' : 'bg-gray-600'
              ]"
            >
              <span
                :class="[
                  'inline-block h-4 w-4 transform rounded-full bg-white transition-transform',
                  isDarkMode ? 'translate-x-6' : 'translate-x-1'
                ]"
              ></span>
            </button>
          </div>

          <!-- 비밀번호 변경 버튼 -->
          <button
            @click="showChangePasswordModal = true"
            class="flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-primary/20 text-primary font-medium hover:bg-primary/30 transition-colors"
          >
            <span class="material-symbols-outlined">lock</span>
            <span>비밀번호 변경</span>
          </button>

          <!-- 회원탈퇴 버튼 -->
          <button
            @click="handleDeleteAccount"
            :disabled="deletingAccount"
            class="flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-red-500/20 text-red-400 font-medium hover:bg-red-500/30 transition-colors disabled:opacity-50"
          >
            <span v-if="deletingAccount" class="material-symbols-outlined animate-spin">sync</span>
            <span v-else class="material-symbols-outlined">delete_forever</span>
            <span>{{ deletingAccount ? '탈퇴 중...' : '회원탈퇴' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 비밀번호 변경 모달 -->
    <div
      v-if="showChangePasswordModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      @click.self="closeChangePasswordModal"
    >
      <div class="bg-white dark:bg-card-dark rounded-xl border border-gray-200 dark:border-border-dark p-6 sm:p-8 max-w-md w-full mx-4">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-text-dark">비밀번호 변경</h2>
          <button
            @click="closeChangePasswordModal"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-gray-600 dark:text-text-secondary-dark hover:bg-gray-100 dark:hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <form @submit.prevent="handleChangePassword" class="flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-gray-900 dark:text-text-dark" for="currentPassword">현재 비밀번호</label>
            <input
              v-model="changePasswordForm.currentPassword"
              type="password"
              id="currentPassword"
              class="block w-full rounded-lg border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark text-gray-900 dark:text-text-dark focus:border-primary focus:ring-primary px-4 py-3"
              placeholder="현재 비밀번호를 입력하세요"
              required
            />
          </div>

          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-gray-900 dark:text-text-dark" for="newPassword">새 비밀번호</label>
            <input
              v-model="changePasswordForm.newPassword"
              type="password"
              id="newPassword"
              class="block w-full rounded-lg border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark text-gray-900 dark:text-text-dark focus:border-primary focus:ring-primary px-4 py-3"
              placeholder="새 비밀번호를 입력하세요 (8자 이상)"
              required
              minlength="8"
            />
          </div>

          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-gray-900 dark:text-text-dark" for="confirmPassword">새 비밀번호 확인</label>
            <input
              v-model="changePasswordForm.confirmPassword"
              type="password"
              id="confirmPassword"
              class="block w-full rounded-lg border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark text-gray-900 dark:text-text-dark focus:border-primary focus:ring-primary px-4 py-3"
              placeholder="새 비밀번호를 다시 입력하세요"
              required
              minlength="8"
            />
          </div>

          <div class="flex gap-3 pt-4">
            <button
              type="button"
              @click="closeChangePasswordModal"
              class="flex-1 flex items-center justify-center rounded-lg h-12 px-6 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-text-dark font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
            >
              취소
            </button>
            <button
              type="submit"
              :disabled="changingPassword"
              class="flex-1 flex items-center justify-center gap-2 rounded-lg h-12 px-6 bg-primary text-white font-bold hover:bg-blue-400 transition-colors disabled:opacity-50"
            >
              <span v-if="changingPassword" class="material-symbols-outlined animate-spin">sync</span>
              <span v-else class="material-symbols-outlined">lock</span>
              <span>{{ changingPassword ? '변경 중...' : '변경하기' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Header from '@/components/Header.vue'
import userApi from '@/services/api/user'
import boardApi from '@/services/api/board'

const router = useRouter()
const authStore = useAuthStore()

const editingNickname = ref(false)
const nickname = ref('')
const savingNickname = ref(false)
const editPreferenceMode = ref(false)
const savingPreference = ref(false)
const deletingAccount = ref(false)
const showSettingsModal = ref(false)
const showProfileModal = ref(false)
const showChangePasswordModal = ref(false)
const changingPassword = ref(false)
const changePasswordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const editBio = ref('')
const editNickname = ref('')
const profileImagePreview = ref(null)
const savingProfile = ref(false)
const activePostTab = ref('my')
const posts = ref([])
const myPosts = ref([])
const likedPosts = ref([])
const loadingPosts = ref(false)
const myPostsCount = ref(0)
const commentedPostsCount = ref(0)
const likedPostsCount = ref(0)
const showMyPostsModal = ref(false)
const showCommentedPostsModal = ref(false)
const showLikedPostsModal = ref(false)
const preference = ref({
  preferredTripType: '',
  preferredActivities: '',
  budgetPreference: '',
  accommodationPreference: '',
  seasonPreference: '',
  transportationPreference: '',
  foodPreference: '',
  travelStyle: '',
  additionalInfo: ''
})
const selectedActivities = ref([])

const activities = ['휴양', '액티비티', '문화', '먹방', '쇼핑', '사진', '자연', '힐링']

const isGoogleUser = computed(() => {
  return !authStore.user?.password || authStore.user?.password === ''
})

const hasPreference = computed(() => {
  return preference.value.preferredTripType ||
    preference.value.preferredActivities ||
    preference.value.budgetPreference ||
    preference.value.accommodationPreference ||
    preference.value.seasonPreference ||
    preference.value.transportationPreference ||
    preference.value.foodPreference ||
    preference.value.travelStyle ||
    preference.value.additionalInfo
})

const preferredActivitiesList = computed(() => {
  if (!preference.value.preferredActivities) return []
  try {
    return JSON.parse(preference.value.preferredActivities)
  } catch (e) {
    return preference.value.preferredActivities.split(',').map(a => a.trim())
  }
})

const loadUserInfo = async () => {
  try {
    const result = await userApi.getUserInfo()
    if (result.success) {
      nickname.value = result.data.nickname || authStore.user?.nickname || ''
    }
  } catch (error) {
    console.error('사용자 정보 로드 오류:', error)
  }
}

const loadPreference = async () => {
  try {
    const result = await userApi.getPreference()
    if (result.success && result.data) {
      preference.value = {
        preferredTripType: result.data.preferredTripType || '',
        preferredActivities: result.data.preferredActivities || '',
        budgetPreference: result.data.budgetPreference || '',
        accommodationPreference: result.data.accommodationPreference || '',
        seasonPreference: result.data.seasonPreference || '',
        transportationPreference: result.data.transportationPreference || '',
        foodPreference: result.data.foodPreference || '',
        travelStyle: result.data.travelStyle || '',
        additionalInfo: result.data.additionalInfo || ''
      }
      
      // 활동 목록 파싱
      if (preference.value.preferredActivities) {
        try {
          selectedActivities.value = JSON.parse(preference.value.preferredActivities)
        } catch (e) {
          selectedActivities.value = preference.value.preferredActivities.split(',').map(a => a.trim())
        }
      }
      
      editPreferenceMode.value = false
    } else {
      editPreferenceMode.value = true
    }
  } catch (error) {
    console.error('여행 성향 로드 오류:', error)
    editPreferenceMode.value = true
  }
}

const saveNickname = async () => {
  if (!nickname.value.trim()) {
    alert('닉네임을 입력해주세요')
    return
  }
  
  savingNickname.value = true
  try {
    const result = await userApi.updateNickname(nickname.value.trim())
    if (result.success) {
      editingNickname.value = false
      const user = { ...authStore.user, nickname: nickname.value.trim() }
      authStore.user = user
      localStorage.setItem('user', JSON.stringify(user))
      showSuccessMessage('닉네임이 변경되었습니다!')
    } else {
      alert(result.message || '닉네임 변경에 실패했습니다')
    }
  } catch (error) {
    console.error('닉네임 업데이트 오류:', error)
    alert('닉네임 변경 중 오류가 발생했습니다')
  } finally {
    savingNickname.value = false
  }
}

const cancelEditNickname = () => {
  nickname.value = authStore.user?.nickname || ''
  editingNickname.value = false
}

// 프로필 수정 모달 관련 함수
const openProfileModal = () => {
  editBio.value = authStore.user?.bio || ''
  editNickname.value = authStore.user?.nickname || ''
  profileImagePreview.value = null
  showProfileModal.value = true
}

const cancelProfileEdit = () => {
  editBio.value = authStore.user?.bio || ''
  editNickname.value = authStore.user?.nickname || ''
  profileImagePreview.value = null
  showProfileModal.value = false
}

const saveProfile = async () => {
  savingProfile.value = true
  try {
    // 닉네임 변경
    if (editNickname.value.trim() !== (authStore.user?.nickname || '')) {
      const result = await userApi.updateNickname(editNickname.value.trim())
      if (result.success) {
        const user = { ...authStore.user, nickname: editNickname.value.trim() }
        authStore.user = user
        localStorage.setItem('user', JSON.stringify(user))
        nickname.value = editNickname.value.trim()
      } else {
        alert(result.message || '닉네임 변경에 실패했습니다')
        savingProfile.value = false
        return
      }
    }

    // 소개 변경
    if (editBio.value.trim() !== (authStore.user?.bio || '')) {
      const result = await userApi.updateBio(editBio.value.trim())
      if (result.success) {
        const user = { ...authStore.user, bio: editBio.value.trim() }
        authStore.user = user
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        alert(result.message || '소개 변경에 실패했습니다')
      }
    }

    // 프로필 이미지 변경 (이미 변경된 경우)
    if (profileImagePreview.value) {
      const result = await userApi.updateProfileImage(profileImagePreview.value)
      if (result.success) {
        const user = { ...authStore.user, profileImage: profileImagePreview.value }
        authStore.user = user
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        alert(result.message || '프로필 이미지 변경에 실패했습니다')
      }
    }

    showSuccessMessage('프로필이 수정되었습니다!')
    showProfileModal.value = false
    profileImagePreview.value = null
  } catch (error) {
    console.error('프로필 수정 오류:', error)
    alert('프로필 수정 중 오류가 발생했습니다')
  } finally {
    savingProfile.value = false
  }
}

const handleProfileImageChangeModal = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 파일 크기 확인 (5MB 제한)
  if (file.size > 5 * 1024 * 1024) {
    alert('이미지 크기는 5MB 이하여야 합니다')
    return
  }
  
  // 이미지 타입 확인
  if (!file.type.startsWith('image/')) {
    alert('이미지 파일만 업로드할 수 있습니다')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    const imageDataUrl = e.target.result
    const MAX_IMAGE_URL_LENGTH = 60000
    
    if (imageDataUrl.length > MAX_IMAGE_URL_LENGTH) {
      alert('이미지 파일이 너무 큽니다. 이미지를 압축하거나 더 작은 크기의 이미지를 선택해주세요.')
      event.target.value = ''
      return
    }
    
    profileImagePreview.value = imageDataUrl
  }
  reader.readAsDataURL(file)
}

const handleProfileImageChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 파일 크기 확인 (5MB 제한)
  if (file.size > 5 * 1024 * 1024) {
    alert('이미지 크기는 5MB 이하여야 합니다')
    return
  }
  
  // 이미지 타입 확인
  if (!file.type.startsWith('image/')) {
    alert('이미지 파일만 업로드할 수 있습니다')
    return
  }
  
  const reader = new FileReader()
  reader.onload = async (e) => {
    const imageDataUrl = e.target.result
    const MAX_IMAGE_URL_LENGTH = 60000
    
    if (imageDataUrl.length > MAX_IMAGE_URL_LENGTH) {
      alert('이미지 파일이 너무 큽니다. 이미지를 압축하거나 더 작은 크기의 이미지를 선택해주세요.')
      event.target.value = ''
      return
    }
    
    try {
      const result = await userApi.updateProfileImage(imageDataUrl)
      if (result.success) {
        const user = { ...authStore.user, profileImage: imageDataUrl }
        authStore.user = user
        localStorage.setItem('user', JSON.stringify(user))
        showSuccessMessage('프로필 이미지가 변경되었습니다!')
      } else {
        alert(result.message || '프로필 이미지 변경에 실패했습니다')
        event.target.value = ''
      }
    } catch (error) {
      console.error('프로필 이미지 업데이트 오류:', error)
      alert('프로필 이미지 변경 중 오류가 발생했습니다')
      event.target.value = ''
    }
  }
  reader.readAsDataURL(file)
}

const savePreference = async () => {
  savingPreference.value = true
  try {
    const preferenceData = {
      preferredTripType: preference.value.preferredTripType || null,
      preferredActivities: selectedActivities.value.length > 0 ? JSON.stringify(selectedActivities.value) : null,
      budgetPreference: preference.value.budgetPreference || null,
      accommodationPreference: preference.value.accommodationPreference || null,
      seasonPreference: preference.value.seasonPreference || null,
      transportationPreference: preference.value.transportationPreference || null,
      foodPreference: preference.value.foodPreference || null,
      travelStyle: preference.value.travelStyle || null,
      additionalInfo: preference.value.additionalInfo || null
    }
    
    const result = await userApi.savePreference(preferenceData)
    if (result.success) {
      if (result.data) {
        preference.value = {
          preferredTripType: result.data.preferredTripType || '',
          preferredActivities: result.data.preferredActivities || '',
          budgetPreference: result.data.budgetPreference || '',
          accommodationPreference: result.data.accommodationPreference || '',
          seasonPreference: result.data.seasonPreference || '',
          transportationPreference: result.data.transportationPreference || '',
          foodPreference: result.data.foodPreference || '',
          travelStyle: result.data.travelStyle || '',
          additionalInfo: result.data.additionalInfo || ''
        }
        
        if (preference.value.preferredActivities) {
          try {
            selectedActivities.value = JSON.parse(preference.value.preferredActivities)
          } catch (e) {
            selectedActivities.value = preference.value.preferredActivities.split(',').map(a => a.trim())
          }
        }
      }
      
      editPreferenceMode.value = false
      showSuccessMessage('여행 성향이 저장되었습니다!')
    } else {
      alert(result.message || '저장에 실패했습니다')
    }
  } catch (error) {
    console.error('여행 성향 저장 오류:', error)
    alert('저장 중 오류가 발생했습니다')
  } finally {
    savingPreference.value = false
  }
}

const handleLogout = async () => {
  if (await confirm('로그아웃하시겠습니까?')) {
    authStore.logout()
    router.push('/')
  }
}

const handleChangePassword = async () => {
  if (!changePasswordForm.value.currentPassword || !changePasswordForm.value.newPassword || !changePasswordForm.value.confirmPassword) {
    alert('모든 필드를 입력해주세요')
    return
  }

  if (changePasswordForm.value.newPassword.length < 8) {
    alert('새 비밀번호는 8자 이상이어야 합니다')
    return
  }

  if (changePasswordForm.value.newPassword !== changePasswordForm.value.confirmPassword) {
    alert('새 비밀번호와 확인 비밀번호가 일치하지 않습니다')
    return
  }

  if (changePasswordForm.value.currentPassword === changePasswordForm.value.newPassword) {
    alert('현재 비밀번호와 새 비밀번호가 같습니다')
    return
  }

  changingPassword.value = true
  try {
    const result = await userApi.changePassword(
      changePasswordForm.value.currentPassword,
      changePasswordForm.value.newPassword
    )
    if (result.success) {
      alert('비밀번호가 변경되었습니다')
      closeChangePasswordModal()
    } else {
      alert(result.message || '비밀번호 변경 중 오류가 발생했습니다')
    }
  } catch (error) {
    console.error('비밀번호 변경 오류:', error)
    alert(error.response?.data?.message || '비밀번호 변경 중 오류가 발생했습니다')
  } finally {
    changingPassword.value = false
  }
}

const closeChangePasswordModal = () => {
  showChangePasswordModal.value = false
  changePasswordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const handleDeleteAccount = async () => {
  const confirmMessage = '정말 회원탈퇴를 하시겠습니까?\n\n탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.'
  if (!(await confirm(confirmMessage))) {
    return
  }

  const doubleConfirm = await confirm('회원탈퇴를 최종 확인합니다.\n\n정말 탈퇴하시겠습니까?')
  if (!doubleConfirm) {
    return
  }

  deletingAccount.value = true
  try {
    const result = await userApi.deleteAccount()
    if (result.success) {
      showSettingsModal.value = false
      alert('회원탈퇴가 완료되었습니다.')
      authStore.logout()
      router.push('/')
    } else {
      alert(result.message || '회원탈퇴 중 오류가 발생했습니다.')
    }
  } catch (error) {
    console.error('회원탈퇴 오류:', error)
    alert('회원탈퇴 중 오류가 발생했습니다.')
  } finally {
    deletingAccount.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

const formatCount = (count) => {
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + '천'
  }
  return count.toString()
}

// 게시글 통계 로드
const loadPostStats = async () => {
  try {
    const [myResult, commentedResult, likedResult] = await Promise.all([
      boardApi.getMyPosts(),
      boardApi.getCommentedPosts(),
      boardApi.getLikedPosts()
    ])
    
    if (myResult.success) {
      myPosts.value = myResult.data || []
      myPostsCount.value = myPosts.value.length
    }
    
    if (commentedResult.success) {
      commentedPostsCount.value = (commentedResult.data || []).length
    }
    
    if (likedResult.success) {
      likedPosts.value = likedResult.data || []
      likedPostsCount.value = likedPosts.value.length
    }
  } catch (error) {
    console.error('게시글 통계 로드 오류:', error)
  }
}

// 게시글 로드 (모달용)
const loadPosts = async (type) => {
  loadingPosts.value = true
  try {
    let result
    if (type === 'my') {
      result = await boardApi.getMyPosts()
    } else if (type === 'commented') {
      result = await boardApi.getCommentedPosts()
    } else {
      result = await boardApi.getLikedPosts()
    }
    
    if (result.success) {
      posts.value = result.data || []
    } else {
      posts.value = []
    }
  } catch (error) {
    console.error('게시글 로드 오류:', error)
    posts.value = []
  } finally {
    loadingPosts.value = false
  }
}

// 게시글 이미지 가져오기
const getPostImage = (post) => {
  if (!post.content) return 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800'
  const imgRegex = /<img[^>]+src=["']([^"']+)["'][^>]*>/i
  const match = post.content.match(imgRegex)
  if (match && match[1]) {
    return match[1]
  }
  return 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800'
}

// 게시글 상세 보기
const viewPost = (postId) => {
  router.push(`/post/${postId}`)
}

// 게시글 삭제
const deletePost = async (postId) => {
  if (!(await confirm('정말 삭제하시겠습니까?'))) {
    return
  }
  
  try {
    const result = await boardApi.deletePost(postId)
    if (result.success) {
      alert('게시글이 삭제되었습니다')
      loadPosts('my')
      loadPostStats() // 통계도 업데이트
    } else {
      alert(result.message || '게시글 삭제에 실패했습니다')
    }
  } catch (error) {
    console.error('게시글 삭제 오류:', error)
    alert('게시글 삭제 중 오류가 발생했습니다')
  }
}

// 모달 열기
const openMyPostsModal = () => {
  console.log('내가 쓴 글 모달 열기')
  showMyPostsModal.value = true
  activePostTab.value = 'my'
  loadPosts('my')
}

const openCommentedPostsModal = () => {
  console.log('댓글 단 글 모달 열기')
  showCommentedPostsModal.value = true
  activePostTab.value = 'commented'
  loadPosts('commented')
}

const openLikedPostsModal = () => {
  console.log('좋아요한 글 모달 열기')
  showLikedPostsModal.value = true
  activePostTab.value = 'liked'
  loadPosts('liked')
}

const showSuccessMessage = (message) => {
  // 성공 메시지 표시 (간단한 alert로 대체, 필요시 토스트 컴포넌트로 교체 가능)
  alert(message)
}

// 다크모드 관련
const isDarkMode = ref(document.documentElement.classList.contains('dark'))

const toggleDarkMode = () => {
  const html = document.documentElement
  if (html.classList.contains('dark')) {
    html.classList.remove('dark')
    localStorage.setItem('theme', 'light')
    isDarkMode.value = false
  } else {
    html.classList.add('dark')
    localStorage.setItem('theme', 'dark')
    isDarkMode.value = true
  }
}

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  
  // 다크모드 상태 동기화 (앱 시작 시 이미 초기화됨)
  isDarkMode.value = document.documentElement.classList.contains('dark')
  
  nickname.value = authStore.user?.nickname || ''
  loadUserInfo()
  loadPreference()
  loadPostStats()
})
</script>
