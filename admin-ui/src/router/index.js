import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/admin-login', name: 'AdminLogin', component: () => import('../views/AdminLogin.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'routes', name: 'Routes', component: () => import('../views/Routes.vue') },
      { path: 'api-keys', name: 'ApiKeys', component: () => import('../views/ApiKeys.vue') },
      { path: 'models', name: 'Models', component: () => import('../views/Models.vue') },
      { path: 'logs', name: 'Logs', component: () => import('../views/Logs.vue') },
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue') }
    ]
  },
  { path: '/user', name: 'UserDashboard', component: () => import('../views/UserDashboard.vue') }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const adminAuth = !!localStorage.getItem('admin_auth')
  const userAuth = !!localStorage.getItem('user_auth')

  // Public pages that anyone can access
  if (to.path === '/login' || to.path === '/admin-login') {
    if (adminAuth) {
      next('/dashboard')
    } else if (userAuth) {
      next('/user')
    } else {
      next()
    }
    return
  }

  if (to.path === '/register') {
    if (adminAuth) {
      next('/dashboard')
    } else if (userAuth) {
      next('/user')
    } else {
      next()
    }
    return
  }

  // Admin pages (under /)
  if (!to.path.startsWith('/user')) {
    if (!adminAuth) {
      next('/login')
      return
    }
  }

  // User dashboard - no auth required, handled by component itself
  if (to.path === '/user') {
    next()
    return
  }

  next()
})

export default router
