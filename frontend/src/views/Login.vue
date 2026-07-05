<template>
  <div class="min-h-screen bg-[#0a0a0a] flex items-center justify-center px-4 relative overflow-hidden">
    <canvas ref="canvasRef" class="fixed inset-0 w-full h-full z-0"></canvas>

    <div class="w-full max-w-md relative z-10 animate__animated animate__fadeIn animate__slow">
      <div class="text-center mb-8 animate__animated animate__bounceIn">
        <div class="text-6xl mb-4">⚰️</div>
        <h1 class="title-font text-5xl text-white tracking-widest">
          人生数据坟场
        </h1>
        <p class="sub-font text-gray-300 text-sm mt-2 tracking-widest">
          — 把你的每一天，活成一座墓园 —
        </p>
      </div>

      <div class="bg-gray-900/80 backdrop-blur-sm border border-gray-700 rounded-2xl p-8 shadow-2xl shadow-black/50 animate__animated animate__fadeInUp animate__delay-1s">
        <h2 class="text-2xl font-semibold text-white text-center mb-6">👻 进入墓园</h2>

        <div v-if="errorMsg" class="mb-4 p-3 bg-red-900/50 border border-red-700 rounded-lg text-red-300 text-sm text-center">
          {{ errorMsg }}
        </div>

        <div class="mb-4">
          <label class="block text-gray-400 text-sm mb-2">用户名</label>
          <input type="text" v-model="username"
                 class="w-full bg-black/50 border border-gray-700 rounded-lg px-4 py-3 text-white placeholder-gray-600 focus:border-gray-500 focus:outline-none transition"
                 placeholder="你的墓碑名">
        </div>

        <div class="mb-6">
          <label class="block text-gray-400 text-sm mb-2">密码</label>
          <input type="password" v-model="password"
                 class="w-full bg-black/50 border border-gray-700 rounded-lg px-4 py-3 text-white placeholder-gray-600 focus:border-gray-500 focus:outline-none transition"
                 placeholder="••••••••" @keyup.enter="handleLogin">
        </div>

        <button @click="handleLogin" :disabled="loading"
                class="w-full bg-white/10 hover:bg-white/20 border border-gray-600 text-white font-medium py-3 rounded-lg transition duration-200 hover:shadow-lg hover:shadow-white/5 disabled:opacity-50 disabled:cursor-not-allowed">
          {{ loading ? '⏳ 验尸中...' : '进入墓场 ⚰️' }}
        </button>

        <p class="text-center text-gray-500 text-sm mt-4">
          还没有墓位？
          <a href="#" @click.prevent="showRegister = true" class="text-gray-300 hover:text-white transition">立即安葬</a>
        </p>
      </div>

      <div class="text-center mt-8 text-gray-700 text-xs tracking-widest animate__animated animate__fadeIn animate__delay-2s">
        <span>🪦</span> 已安葬 <span class="text-gray-500">1,337</span> 位逝者 <span>🪦</span>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <div v-if="showRegister" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
      <div class="bg-gray-900 border border-gray-700 rounded-2xl p-6 max-w-md w-full mx-4">
        <h3 class="text-xl font-bold text-white mb-4">⚰️ 安葬新逝者</h3>
        <div v-if="registerError" class="mb-4 p-3 bg-red-900/50 border border-red-700 rounded-lg text-red-300 text-sm text-center">
          {{ registerError }}
        </div>
        <div v-if="registerSuccess" class="mb-4 p-3 bg-green-900/50 border border-green-700 rounded-lg text-green-300 text-sm text-center">
          {{ registerSuccess }}
        </div>
        <div class="mb-4">
          <label class="block text-gray-400 text-sm mb-2">用户名</label>
          <input type="text" v-model="registerForm.username"
                 class="w-full bg-black/50 border border-gray-700 rounded-lg px-4 py-3 text-white placeholder-gray-600 focus:border-gray-500 focus:outline-none transition"
                 placeholder="你的墓碑名">
        </div>
        <div class="mb-4">
          <label class="block text-gray-400 text-sm mb-2">密码</label>
          <input type="password" v-model="registerForm.password"
                 class="w-full bg-black/50 border border-gray-700 rounded-lg px-4 py-3 text-white placeholder-gray-600 focus:border-gray-500 focus:outline-none transition"
                 placeholder="••••••••">
        </div>
        <div class="mb-4">
          <label class="block text-gray-400 text-sm mb-2">邮箱</label>
          <input type="email" v-model="registerForm.email"
                 class="w-full bg-black/50 border border-gray-700 rounded-lg px-4 py-3 text-white placeholder-gray-600 focus:border-gray-500 focus:outline-none transition"
                 placeholder="your@email.com">
        </div>
        <div class="flex gap-2">
          <button @click="handleRegister"
                  :disabled="registerLoading"
                  class="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-2 rounded-lg transition disabled:opacity-50">
            {{ registerLoading ? '⏳ 安葬中...' : '安葬 💀' }}
          </button>
          <button @click="closeRegister"
                  class="flex-1 bg-gray-700 hover:bg-gray-600 text-white font-bold py-2 rounded-lg transition">
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const BACKEND_URL = 'https://life-graveyard-production.up.railway.app'

const username = ref('')
const password = ref('')
const canvasRef = ref(null)
const loading = ref(false)
const errorMsg = ref('')

const showRegister = ref(false)
const registerLoading = ref(false)
const registerError = ref('')
const registerSuccess = ref('')
const registerForm = ref({
  username: '',
  password: '',
  email: ''
})

const handleLogin = async () => {
  if (!username.value || !password.value) {
    errorMsg.value = '用户名和密码不能为空 ⚰️'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const response = await axios.post(`${BACKEND_URL}/api/v1/user/login`, {
      username: username.value,
      password: password.value
    })
    if (response.data.success) {
      localStorage.setItem('user', JSON.stringify(response.data))
      window.location.href = '/home'
    } else {
      errorMsg.value = response.data.message
    }
  } catch (error) {
    errorMsg.value = '服务器连接失败，墓场暂时关闭 ☠️'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  const { username, password, email } = registerForm.value
  if (!username || !password || !email) {
    registerError.value = '所有字段都不能为空 ⚰️'
    return
  }
  registerLoading.value = true
  registerError.value = ''
  registerSuccess.value = ''
  try {
    const response = await axios.post(`${BACKEND_URL}/api/v1/user/register`, {
      username,
      password,
      email
    })
    if (response.data.success) {
      registerSuccess.value = '注册成功！欢迎来到人生数据坟场 ⚰️'
      setTimeout(() => {
        closeRegister()
        username.value = username
        password.value = password
      }, 1500)
    } else {
      registerError.value = response.data.message
    }
  } catch (error) {
    registerError.value = '注册失败，墓场暂时罢工 ☠️'
  } finally {
    registerLoading.value = false
  }
}

const closeRegister = () => {
  showRegister.value = false
  registerError.value = ''
  registerSuccess.value = ''
  registerForm.value = { username: '', password: '', email: '' }
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  let width = window.innerWidth
  let height = window.innerHeight
  canvas.width = width
  canvas.height = height

  const particles = []
  for (let i = 0; i < 60; i++) {
    particles.push({
      x: Math.random() * width,
      y: Math.random() * height,
      size: Math.random() * 3 + 1,
      speedX: (Math.random() - 0.5) * 0.5,
      speedY: (Math.random() - 0.5) * 0.5,
      opacity: Math.random() * 0.5 + 0.2
    })
  }

  function draw() {
    ctx.fillStyle = 'rgba(10, 10, 10, 0.1)'
    ctx.fillRect(0, 0, width, height)

    particles.forEach(p => {
      p.x += p.speedX
      p.y += p.speedY
      if (p.x < 0 || p.x > width) p.speedX *= -1
      if (p.y < 0 || p.y > height) p.speedY *= -1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(180, 180, 200, ${p.opacity})`
      ctx.fill()
    })

    requestAnimationFrame(draw)
  }

  draw()

  window.addEventListener('resize', () => {
    width = window.innerWidth
    height = window.innerHeight
    canvas.width = width
    canvas.height = height
  })
})
</script>

<style>
.title-font {
  font-family: 'Times New Roman', 'Georgia', serif !important;
  font-weight: bold !important;
  font-style: italic !important;
  text-shadow: 0 0 20px red !important;
  letter-spacing: 4px !important;
}
.sub-font {
  font-family: 'Georgia', serif !important;
  font-style: italic !important;
  text-shadow: 0 0 10px rgba(255, 100, 100, 0.3) !important;
  letter-spacing: 2px !important;
}
</style>