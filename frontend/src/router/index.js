import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/board',
    name: 'Board',
    component: () => import('@/views/BoardView.vue')
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetailView.vue')
  },
  {
    path: '/new-plan',
    name: 'NewPlan',
    component: () => import('@/views/NewPlanView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/share-plan',
    name: 'SharePlan',
    component: () => import('@/views/SharePlanView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-records',
    name: 'MyRecords',
    component: () => import('@/views/MyRecordsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/record/:id',
    name: 'RecordDetail',
    component: () => import('@/views/RecordDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/record',
    name: 'Record',
    component: () => import('@/views/RecordView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tour-info/:contentId',
    name: 'TourDetail',
    component: () => import('@/views/TourDetailView.vue')
  },
  {
    path: '/mypage',
    name: 'MyPage',
    component: () => import('@/views/MyPageView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/hotplace',
    name: 'HotPlace',
    component: () => import('@/views/HotPlaceView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/hotplace/:id',
    name: 'HotPlaceDetail',
    component: () => import('@/views/HotPlaceDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-hotplaces',
    name: 'MyHotPlaces',
    component: () => import('@/views/MyHotPlacesView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router

