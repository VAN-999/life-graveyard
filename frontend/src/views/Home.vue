<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white relative overflow-hidden">
    <!-- 粒子背景 -->
    <canvas ref="canvasRef" class="fixed inset-0 w-full h-full z-0"></canvas>

    <!-- ====== 顶部导航栏 ====== -->
    <header class="relative z-10 flex items-center justify-between px-6 py-4 border-b border-gray-800 bg-black/30 backdrop-blur-sm">
      <div class="flex items-center gap-4">
        <span class="text-2xl">⚰️</span>
        <h1 class="text-2xl font-bold title-font tracking-widest">人生数据坟场</h1>
      </div>
      <div class="flex items-center gap-6 text-sm">
        <span class="text-gray-400">🪙 冥币: <span class="text-yellow-400 font-bold">{{ user?.hellMoney || 0 }}</span></span>
        <span class="text-gray-400">📈 Lv.{{ user?.level || 1 }}</span>
        <span class="text-gray-300">{{ user?.username || '未知' }}</span>
        <button @click="logout" class="text-gray-500 hover:text-red-400 transition text-sm">退出</button>
      </div>
    </header>

    <!-- ====== 主区域 ====== -->
    <main class="relative z-10 flex flex-1 h-[calc(100vh-80px)] gap-4 p-4 overflow-hidden">

      <!-- ====== 左侧：数据卡片 ====== -->
      <aside class="w-56 flex flex-col gap-3 overflow-y-auto">
        <div class="bg-black/40 border border-gray-800 rounded-xl p-4 backdrop-blur-sm">
          <div class="text-gray-400 text-xs uppercase tracking-widest mb-2">🏃 今日步数</div>
          <div class="text-2xl font-bold">{{ todayData?.steps || '--' }}</div>
        </div>
        <div class="bg-black/40 border border-gray-800 rounded-xl p-4 backdrop-blur-sm">
          <div class="text-gray-400 text-xs uppercase tracking-widest mb-2">📱 屏幕时间</div>
          <div class="text-2xl font-bold">{{ todayData?.screenTimeMinutes || '--' }}<span class="text-sm text-gray-500 ml-1">分钟</span></div>
        </div>
        <div class="bg-black/40 border border-gray-800 rounded-xl p-4 backdrop-blur-sm">
          <div class="text-gray-400 text-xs uppercase tracking-widest mb-2">⌨️ 键盘敲击</div>
          <div class="text-2xl font-bold">{{ todayData?.keyPresses || '--' }}</div>
        </div>
        <div class="bg-black/40 border border-gray-800 rounded-xl p-4 backdrop-blur-sm">
          <div class="text-gray-400 text-xs uppercase tracking-widest mb-2">💤 睡眠</div>
          <div class="text-2xl font-bold">{{ todayData?.sleepHours || '--' }}<span class="text-sm text-gray-500 ml-1">小时</span></div>
        </div>
      </aside>

      <!-- ====== 中间：墓场 ====== -->
      <section class="flex-1 flex flex-col items-center justify-center bg-black/20 border border-gray-800/50 rounded-2xl backdrop-blur-sm relative overflow-hidden">
        <Graveyard
            :username="user?.username || '墓场主'"
            :epitaph="epitaph"
            :equippedDecorations="equippedDecorations"
            tomb-style="point"
        />
      </section>

      <!-- ====== 右侧：报告 + 任务 ====== -->
      <aside class="w-64 flex flex-col gap-3 overflow-y-auto">
        <!-- 今日死亡报告 -->
        <div class="bg-black/40 border border-gray-800 rounded-xl p-4 backdrop-blur-sm flex-1">
          <div class="text-gray-400 text-xs uppercase tracking-widest mb-2">💀 今日死亡报告</div>
          <div class="flex items-center gap-3 mb-2">
            <span class="text-3xl font-bold" :class="deathScore >= 70 ? 'text-red-500' : deathScore >= 40 ? 'text-yellow-500' : 'text-green-500'">
              {{ deathScore || 0 }}%
            </span>
            <span class="text-xs text-gray-500">死亡指数</span>
          </div>
          <p class="text-sm text-gray-300 line-clamp-3">{{ deathReason || '今天还没死，等晚上吧' }}</p>
        </div>

        <!-- 任务进度 -->
        <div class="bg-black/40 border border-gray-800 rounded-xl p-4 backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-gray-400 text-xs uppercase tracking-widest">📋 今日任务</span>
            <span class="text-xs text-gray-500">{{ taskProgress }}/{{ taskTotal }}</span>
          </div>
          <div class="w-full h-1.5 bg-gray-700 rounded-full mt-2 overflow-hidden">
            <div class="h-full bg-red-600 rounded-full transition-all" :style="{ width: taskPercent + '%' }"></div>
          </div>
          <p class="text-xs text-gray-500 mt-2">{{ taskProgress }} / {{ taskTotal }} 已完成</p>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import Graveyard from '../components/Graveyard.vue'

const user = ref(null)
const todayData = ref(null)
const reportData = ref(null)
const taskData = ref([])

const epitaph = computed(() => reportData.value?.epitaph || '此人安息于此')
const deathScore = computed(() => reportData.value?.deathScore || 0)
const deathReason = computed(() => reportData.value?.deathReason || '今天还没死')
const equippedDecorations = ref([])
const taskProgress = ref(0)
const taskTotal = ref(0)
const taskPercent = computed(() => taskTotal.value ? Math.round((taskProgress.value / taskTotal.value) * 100) : 0)

const canvasRef = ref(null)

const loadData = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    window.location.href = '/'
    return
  }
  user.value = JSON.parse(userStr)
  const userId = user.value.userId

  try {
    const dataRes = await axios.get(`/api/v1/daily-data/today?userId=${userId}`)
    if (dataRes.data.success) todayData.value = dataRes.data.data

    const reportRes = await axios.get(`/api/v1/report/today?userId=${userId}`)
    if (reportRes.data.success) reportData.value = reportRes.data.report

    const taskRes = await axios.get(`/api/v1/tasks/today?userId=${userId}`)
    if (taskRes.data.success) {
      taskData.value = taskRes.data.data
      taskTotal.value = taskRes.data.totalCount
      taskProgress.value = taskRes.data.completedCount
    }

    const decoRes = await axios.get(`/api/v1/decorations/my?userId=${userId}`)
    if (decoRes.data.success) {
      equippedDecorations.value = decoRes.data.data.filter(d => d.isEquipped)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const logout = () => {
  localStorage.removeItem('user')
  window.location.href = '/'
}

onMounted(() => {
  loadData()

  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  let width = window.innerWidth
  let height = window.innerHeight
  canvas.width = width
  canvas.height = height

  const particles = []
  for (let i = 0; i < 80; i++) {
    particles.push({
      x: Math.random() * width,
      y: Math.random() * height,
      size: Math.random() * 3 + 1,
      speedX: (Math.random() - 0.5) * 0.4,
      speedY: (Math.random() - 0.5) * 0.4,
      opacity: Math.random() * 0.5 + 0.15
    })
  }

  function draw() {
    ctx.fillStyle = 'rgba(10, 10, 10, 0.08)'
    ctx.fillRect(0, 0, width, height)

    particles.forEach(p => {
      p.x += p.speedX
      p.y += p.speedY
      if (p.x < 0 || p.x > width) p.speedX *= -1
      if (p.y < 0 || p.y > height) p.speedY *= -1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(200, 200, 220, ${p.opacity})`
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

<style scoped>
.title-font {
  font-family: 'Times New Roman', 'Georgia', serif !important;
  font-weight: bold !important;
  font-style: italic !important;
  text-shadow: 0 0 20px rgba(255, 0, 0, 0.3) !important;
}
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>