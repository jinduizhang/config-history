import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/config'
  },
  {
    path: '/config',
    name: 'Config',
    component: () => import('@/views/ConfigList.vue'),
    meta: { title: 'Config Management' }
  },
  {
    path: '/config/:id/history',
    name: 'ConfigHistory',
    component: () => import('@/views/HistoryList.vue'),
    meta: { title: 'History' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || 'Config History Management'
  next()
})

export default router