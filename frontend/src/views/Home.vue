<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white relative overflow-hidden">
    <canvas ref="canvasRef" class="fixed inset-0 w-full h-full z-0"></canvas>

    <header class="relative z-10 flex items-center justify-between px-6 py-4 border-b border-gray-800 bg-black/30 backdrop-blur-sm">
      <div class="flex items-center gap-4">
        <span class="text-2xl">⚰️</span>
        <h1 class="text-2xl font-bold title-font tracking-widest">人生数据坟场</h1>
      </div>
      <div class="flex items-center gap-6 text-sm">
        <span class="text-gray-400">🪙 冥币: <span class="text-yellow-400 font-bold">{{ user?.hellMoney || 0 }}</span></span>
        <span class="text-gray-400">📈 Lv.{{ user?.level || 1 }}</span>
        <span class="text-gray-300">{{ user?.username || '未知' }}</span>
        <button @click="showShop = true" class="text-gray-300 hover:text-white transition text-sm">
          🛒 商店
        </button>
        <button @click="logout" class="text-gray-500 hover:text-red-400 transition text-sm">退出</button>
      </div>
    </header>

    <main class="relative z-10 flex flex-1 h-[calc(100vh-80px)] gap-4 p-4 overflow-hidden">

      <!-- 左侧数据卡片 -->
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

      <!-- 中间墓场 -->
      <section class="flex-1 flex flex-col bg-black/20 border border-gray-800/50 rounded-2xl backdrop-blur-sm relative overflow-hidden">
        <Graveyard
            ref="graveyardRef"
            :username="user?.username || '墓场主'"
            :epitaph="epitaph"
            :equippedDecorations="equippedDecorations"
            :decorationStates="decorationStates"
            @update-state="onUpdateState"
            @select-decoration="onSelectDecoration"
        />

        <!-- 编辑器工具栏（选中装饰品时显示） -->
        <div v-if="selectedDecorationId" class="absolute bottom-4 left-1/2 transform -translate-x-1/2 z-20 flex gap-2 bg-black/70 border border-gray-700 rounded-xl p-2 backdrop-blur-sm">
          <button @click="rotateDecoration(-15)" class="px-3 py-1 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm">↺ -15°</button>
          <button @click="rotateDecoration(15)" class="px-3 py-1 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm">↻ +15°</button>
          <button @click="scaleDecoration(0.9)" class="px-3 py-1 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm">🔍−</button>
          <button @click="scaleDecoration(1.1)" class="px-3 py-1 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm">🔍+</button>
          <button @click="moveLayer('up')" class="px-3 py-1 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm">⬆ 上移</button>
          <button @click="moveLayer('down')" class="px-3 py-1 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm">⬇ 下移</button>
          <button @click="saveAllStates" class="px-3 py-1 bg-green-700 hover:bg-green-600 rounded-lg text-sm">💾 保存</button>
        </div>
      </section>

      <!-- 右侧面板 -->
      <aside class="w-64 flex flex-col gap-3 overflow-y-auto">
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

        <div class="bg-black/40 border border-gray-800 rounded-xl p-4 backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-gray-400 text-xs uppercase tracking-widest">📋 今日任务</span>
            <span class="text-xs text-gray-500">{{ taskProgress }}/{{ taskTotal }}</span>
          </div>
          <div class="w-full h-1.5 bg-gray-700 rounded-full mt-2 overflow-hidden">
            <div class="h-full bg-red-600 rounded-full transition-all" :style="{ width: taskPercent + '%' }"></div>
          </div>

          <div class="mt-3 space-y-3 max-h-60 overflow-y-auto">
            <div v-if="incompleteTasks.length">
              <div class="text-xs text-gray-500 mb-1">⏳ 未完成</div>
              <div
                  v-for="task in incompleteTasks"
                  :key="task.userTaskId"
                  class="flex items-center justify-between p-2 rounded-lg bg-black/30 hover:bg-black/50 transition cursor-pointer"
                  @click="openTaskDetail(task)"
              >
                <span class="text-sm truncate">{{ task.name }}</span>
                <span class="text-xs text-gray-500 flex-shrink-0">+{{ task.rewardMoney }}冥币</span>
              </div>
            </div>

            <div v-if="claimableTasks.length">
              <div class="text-xs text-green-400 mb-1">✅ 可领取</div>
              <div
                  v-for="task in claimableTasks"
                  :key="task.userTaskId"
                  class="flex items-center justify-between p-2 rounded-lg bg-green-900/20 hover:bg-green-900/30 border border-green-700/30 transition cursor-pointer"
                  @click="openTaskDetail(task)"
              >
                <span class="text-sm truncate">{{ task.name }}</span>
                <span class="text-xs text-yellow-400 flex-shrink-0">点击领取 →</span>
              </div>
            </div>

            <div v-if="claimedTasks.length">
              <div class="text-xs text-gray-600 mb-1">💰 已领取</div>
              <div
                  v-for="task in claimedTasks"
                  :key="task.userTaskId"
                  class="flex items-center justify-between p-2 rounded-lg bg-gray-800/30 opacity-60"
              >
                <span class="text-sm truncate text-gray-500">{{ task.name }}</span>
                <span class="text-xs text-gray-600 flex-shrink-0">已领</span>
              </div>
            </div>

            <div v-if="!taskList.length" class="text-center text-gray-500 text-sm py-4">
              今天没有任务，去提交数据试试
            </div>
          </div>

          <button
              @click="showDataModal = true"
              class="mt-3 w-full bg-white/10 hover:bg-white/20 border border-gray-600 text-white font-medium py-2 rounded-lg transition text-sm"
          >
            📊 提交今日数据
          </button>
        </div>
      </aside>
    </main>

    <!-- 任务详情弹窗 -->
    <div v-if="showTaskDetail && selectedTask" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
      <div class="bg-gray-900 border border-gray-700 rounded-2xl p-6 max-w-sm w-full mx-4">
        <h3 class="text-xl font-bold text-white mb-2">{{ selectedTask.name }}</h3>
        <p class="text-gray-400 text-sm mb-4">{{ selectedTask.description }}</p>
        <div class="flex items-center justify-between mb-4">
          <span class="text-gray-500 text-sm">💰 奖励</span>
          <span class="text-yellow-400 font-bold">+{{ selectedTask.rewardMoney }} 冥币</span>
        </div>
        <div class="flex items-center justify-between mb-4">
          <span class="text-gray-500 text-sm">📊 状态</span>
          <span v-if="selectedTask.isCompleted" class="text-green-400">✅ 已完成</span>
          <span v-else-if="selectedTask.isClaimed" class="text-gray-500">💰 已领取</span>
          <span v-else class="text-gray-500">⏳ 未完成</span>
        </div>
        <div class="flex gap-2">
          <button
              v-if="selectedTask.isCompleted && !selectedTask.isClaimed"
              @click="claimReward(selectedTask.userTaskId)"
              class="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-2 rounded-lg transition"
          >
            领取奖励 💀
          </button>
          <button
              @click="closeTaskDetail"
              class="flex-1 bg-gray-700 hover:bg-gray-600 text-white font-bold py-2 rounded-lg transition"
          >
            关闭
          </button>
        </div>
      </div>
    </div>

    <!-- 数据提交弹窗 -->
    <div v-if="showDataModal" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
      <div class="bg-gray-900 border border-gray-700 rounded-2xl p-6 max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto">
        <h3 class="text-xl font-bold text-white mb-4">📊 提交今日数据</h3>

        <div class="space-y-3">
          <div>
            <label class="text-gray-400 text-sm">步数</label>
            <input v-model.number="dataForm.steps" type="number" class="w-full bg-black/50 border border-gray-700 rounded-lg px-3 py-2 text-white" placeholder="0" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">屏幕时间（分钟）</label>
            <input v-model.number="dataForm.screenTimeMinutes" type="number" class="w-full bg-black/50 border border-gray-700 rounded-lg px-3 py-2 text-white" placeholder="0" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">键盘敲击次数</label>
            <input v-model.number="dataForm.keyPresses" type="number" class="w-full bg-black/50 border border-gray-700 rounded-lg px-3 py-2 text-white" placeholder="0" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">睡眠（小时）</label>
            <input v-model.number="dataForm.sleepHours" type="number" step="0.5" class="w-full bg-black/50 border border-gray-700 rounded-lg px-3 py-2 text-white" placeholder="0" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">APP打开次数</label>
            <input v-model.number="dataForm.appOpens" type="number" class="w-full bg-black/50 border border-gray-700 rounded-lg px-3 py-2 text-white" placeholder="0" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">最后活跃时间（如 01:23）</label>
            <input v-model="dataForm.lastActiveAt" type="text" class="w-full bg-black/50 border border-gray-700 rounded-lg px-3 py-2 text-white" placeholder="01:23" />
          </div>
          <div class="flex items-center gap-3">
            <input v-model="dataForm.momentsViewed" type="checkbox" class="w-4 h-4" />
            <label class="text-gray-400 text-sm">今天看了朋友圈</label>
          </div>
        </div>

        <div class="flex gap-2 mt-4">
          <button
              @click="submitData"
              :disabled="submitting"
              class="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-2 rounded-lg transition disabled:opacity-50"
          >
            {{ submitting ? '提交中...' : '提交数据 💀' }}
          </button>
          <button
              @click="showDataModal = false"
              class="flex-1 bg-gray-700 hover:bg-gray-600 text-white font-bold py-2 rounded-lg transition"
          >
            取消
          </button>
        </div>
      </div>
    </div>

    <!-- ====== 商店 + 背包弹窗 ====== -->
    <div v-if="showShop" class="fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4">
      <div class="bg-gray-900 border border-gray-700 rounded-2xl p-6 max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-white">🛒 冥币商店</h3>
          <span class="text-yellow-400 text-sm">💰 {{ user?.hellMoney || 0 }} 冥币</span>
        </div>

        <div class="flex gap-2 mb-4">
          <button
              @click="shopTab = 'store'"
              class="px-4 py-1.5 rounded-lg text-sm transition"
              :class="shopTab === 'store' ? 'bg-red-600 text-white' : 'bg-gray-800 text-gray-400 hover:bg-gray-700'"
          >
            🛒 商店
          </button>
          <button
              @click="shopTab = 'bag'; loadMyDecorations()"
              class="px-4 py-1.5 rounded-lg text-sm transition"
              :class="shopTab === 'bag' ? 'bg-red-600 text-white' : 'bg-gray-800 text-gray-400 hover:bg-gray-700'"
          >
            🎒 背包 ({{ myDecorations.length }})
          </button>
        </div>

        <!-- 商店 -->
        <div v-if="shopTab === 'store'">
          <div class="flex gap-2 mb-4 overflow-x-auto">
            <button
                v-for="cat in categories"
                :key="cat"
                @click="selectedCategory = cat"
                class="px-3 py-1 rounded-lg text-sm transition whitespace-nowrap"
                :class="selectedCategory === cat ? 'bg-red-600 text-white' : 'bg-gray-800 text-gray-400 hover:bg-gray-700'"
            >
              {{ cat }}
            </button>
          </div>

          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
            <div
                v-for="item in filteredDecorations"
                :key="item.id"
                class="bg-black/40 border border-gray-700 rounded-xl p-3 text-center hover:border-gray-500 transition"
            >
              <div class="mb-1">
                <img :src="item.icon" class="w-16 h-16 object-contain mx-auto" />
              </div>
              <div class="text-white font-medium text-sm">{{ item.name }}</div>
              <div class="text-yellow-400 text-sm">💰 {{ item.price }} 冥币</div>
              <button
                  @click="buyDecoration(item.id)"
                  :disabled="(user?.hellMoney || 0) < item.price || buying"
                  class="mt-2 w-full bg-red-600 hover:bg-red-700 text-white text-sm font-medium py-1.5 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {{ (user?.hellMoney || 0) >= item.price ? '购买 💀' : '冥币不足' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 背包 -->
        <div v-if="shopTab === 'bag'">
          <div v-if="myDecorations.length === 0" class="text-center text-gray-500 py-8">
            背包空空如也，去商店买点东西吧 💀
          </div>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
            <div
                v-for="item in myDecorations"
                :key="item.userDecorationId"
                class="bg-black/40 border border-gray-700 rounded-xl p-3 text-center hover:border-gray-500 transition"
            >
              <div class="mb-1">
                <img :src="item.icon" class="w-16 h-16 object-contain mx-auto" />
              </div>
              <div class="text-white font-medium text-sm">{{ item.name }}</div>
              <div class="text-xs mt-1" :class="item.isEquipped ? 'text-green-400' : 'text-gray-500'">
                {{ item.isEquipped ? '✅ 已装备' : '未装备' }}
              </div>
              <button
                  v-if="!item.isEquipped"
                  @click="equipDecoration(item.userDecorationId)"
                  class="mt-2 w-full bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium py-1.5 rounded-lg transition"
              >
                装备 💀
              </button>
              <button
                  v-else
                  @click="unequipDecoration(item.userDecorationId)"
                  class="mt-2 w-full bg-gray-600 hover:bg-gray-700 text-white text-sm font-medium py-1.5 rounded-lg transition"
              >
                卸下
              </button>
            </div>
          </div>
        </div>

        <button @click="closeShop" class="mt-4 w-full bg-gray-700 hover:bg-gray-600 text-white font-medium py-2 rounded-lg transition">
          关闭商店
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import axios from 'axios'
import Graveyard from '../components/Graveyard.vue'

const API_BASE = import.meta.env.VITE_API_URL || 'https://life-graveyard-production.up.railway.app'

// ====== 用户 ======
const user = ref(null)
const todayData = ref(null)
const reportData = ref(null)
const equippedDecorations = ref([])
const decorationStates = ref([])

const graveyardRef = ref(null)
const selectedDecorationId = ref(null)

// ====== 商店 ======
const showShop = ref(false)
const shopTab = ref('store')
const decorations = ref([])
const selectedCategory = ref('全部')
const buying = ref(false)

const myDecorations = ref([])

// ====== 任务 ======
const taskList = ref([])
const taskProgress = ref(0)
const taskTotal = ref(0)

// ====== 其他 ======
const showTaskDetail = ref(false)
const selectedTask = ref(null)
const showDataModal = ref(false)
const submitting = ref(false)
const dataForm = ref({
  steps: 0,
  screenTimeMinutes: 0,
  keyPresses: 0,
  sleepHours: 0,
  appOpens: 0,
  lastActiveAt: '',
  momentsViewed: false
})

const canvasRef = ref(null)

// ====== 计算属性 ======
const epitaph = computed(() => reportData.value?.epitaph || '此人安息于此')
const deathScore = computed(() => reportData.value?.deathScore || 0)
const deathReason = computed(() => reportData.value?.deathReason || '今天还没死')
const taskPercent = computed(() => taskTotal.value ? Math.round((taskProgress.value / taskTotal.value) * 100) : 0)

const incompleteTasks = computed(() => taskList.value.filter(t => !t.isCompleted && !t.isClaimed))
const claimableTasks = computed(() => taskList.value.filter(t => t.isCompleted && !t.isClaimed))
const claimedTasks = computed(() => taskList.value.filter(t => t.isClaimed))

const categories = computed(() => {
  const cats = decorations.value.map(d => d.category)
  return ['全部', ...new Set(cats)]
})

const filteredDecorations = computed(() => {
  if (selectedCategory.value === '全部') return decorations.value
  return decorations.value.filter(d => d.category === selectedCategory.value)
})

// ====== 加载数据 ======
const loadUserInfo = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    window.location.href = '/'
    return
  }
  const data = JSON.parse(userStr)
  // 兼容两种字段名
  if (data.userId === undefined && data.id !== undefined) {
    data.userId = data.id
  }
  if (data.id === undefined && data.userId !== undefined) {
    data.id = data.userId
  }
  user.value = data
}

const fetchUserInfo = async () => {
  if (!user.value) return
  try {
    const res = await axios.get(`${API_BASE}/api/v1/user/info?userId=${user.value.id}`)
    if (res.data.success) {
      user.value = res.data.data
      localStorage.setItem('user', JSON.stringify(res.data.data))
    }
  } catch (error) { console.error('获取用户信息失败:', error) }
}

const loadTasks = async () => {
  if (!user.value) return
  try {
    let res = await axios.get(`${API_BASE}/api/v1/tasks/today?userId=${user.value.id}`)
    if (res.data.success && res.data.data.length === 0) {
      await axios.post(`${API_BASE}/api/v1/tasks/assign?userId=${user.value.id}`)
      res = await axios.get(`${API_BASE}/api/v1/tasks/today?userId=${user.value.id}`)
    }
    if (res.data.success) {
      taskList.value = res.data.data
      taskTotal.value = res.data.totalCount
      taskProgress.value = res.data.completedCount
    }
  } catch (error) { console.error('加载任务失败:', error) }
}

const loadTodayData = async () => {
  if (!user.value) return
  try {
    const res = await axios.get(`${API_BASE}/api/v1/daily-data/today?userId=${user.value.id}`)
    if (res.data.success) todayData.value = res.data.data
  } catch (error) { console.error('加载数据失败:', error) }
}

const loadReport = async () => {
  if (!user.value) return
  try {
    const res = await axios.get(`${API_BASE}/api/v1/report/today?userId=${user.value.id}`)
    if (res.data.success) reportData.value = res.data.report
  } catch (error) { console.error('加载报告失败:', error) }
}

const loadDecorations = async () => {
  if (!user.value) return
  try {
    const res = await axios.get(`${API_BASE}/api/v1/decorations/my?userId=${user.value.id}`)
    if (res.data.success) {
      equippedDecorations.value = res.data.data.filter(d => d.isEquipped)
    }
  } catch (error) { console.error('加载装饰失败:', error) }
}

const loadDecorationStates = async () => {
  if (!user.value) return
  try {
    const res = await axios.get(`${API_BASE}/api/v1/decorations/states?userId=${user.value.id}`)
    if (res.data.success) {
      decorationStates.value = res.data.data
    }
  } catch (error) { console.error('加载装饰状态失败:', error) }
}

const loadDecorationsList = async () => {
  try {
    const res = await axios.get(`${API_BASE}/api/v1/decorations/list`)
    if (res.data.success) {
      const flat = []
      for (const category in res.data.data) {
        flat.push(...res.data.data[category])
      }
      decorations.value = flat
    }
  } catch (error) { console.error('加载装饰品列表失败:', error) }
}

const loadMyDecorations = async () => {
  if (!user.value) return
  try {
    const res = await axios.get(`${API_BASE}/api/v1/decorations/my?userId=${user.value.id}`)
    if (res.data.success) {
      myDecorations.value = res.data.data
    }
  } catch (error) { console.error('加载背包失败:', error) }
}

const loadData = async () => {
  await loadUserInfo()
  await loadTasks()
  await loadTodayData()
  await loadReport()
  await loadDecorations()
  await loadDecorationStates()
  await loadDecorationsList()
  await loadMyDecorations()
}

// ====== 任务操作 ======
const openTaskDetail = (task) => { selectedTask.value = task; showTaskDetail.value = true }
const closeTaskDetail = () => { showTaskDetail.value = false; selectedTask.value = null }

const claimReward = async (userTaskId) => {
  try {
    const res = await axios.post(`${API_BASE}/api/v1/tasks/claim?userTaskId=${userTaskId}`)
    if (res.data.success) {
      alert(`✅ ${res.data.message}`)
      await loadTasks()
      await fetchUserInfo()
      await loadDecorations()
    } else {
      alert(`❌ ${res.data.message}`)
    }
  } catch (error) {
    alert('领取失败，墓场暂时罢工 ☠️')
  }
}

// ====== 商店操作 ======
const buyDecoration = async (decorationId) => {
  if (!user.value) return
  buying.value = true
  try {
    const res = await axios.post(`${API_BASE}/api/v1/decorations/buy?userId=${user.value.id}&decorationId=${decorationId}`)
    if (res.data.success) {
      alert(`✅ ${res.data.message}`)
      await fetchUserInfo()
      await loadDecorationsList()
      await loadMyDecorations()
      await loadDecorations()
      await loadDecorationStates()
    } else {
      alert(`❌ ${res.data.message}`)
    }
  } catch (error) {
    alert('购买失败，墓场暂时罢工 ☠️')
  } finally {
    buying.value = false
  }
}

// ====== 背包操作 ======
const equipDecoration = async (userDecorationId) => {
  try {
    const res = await axios.post(`${API_BASE}/api/v1/decorations/equip?userDecorationId=${userDecorationId}`)
    if (res.data.success) {
      alert(`✅ ${res.data.message}`)
      await loadMyDecorations()
      await loadDecorations()
      await loadDecorationStates()
    } else {
      alert(`❌ ${res.data.message}`)
    }
  } catch (error) {
    alert('装备失败，墓场暂时罢工 ☠️')
  }
}

const unequipDecoration = async (userDecorationId) => {
  try {
    const res = await axios.post(`${API_BASE}/api/v1/decorations/unequip?userDecorationId=${userDecorationId}`)
    if (res.data.success) {
      alert(`✅ ${res.data.message}`)
      await loadMyDecorations()
      await loadDecorations()
      await loadDecorationStates()
    } else {
      alert(`❌ ${res.data.message}`)
    }
  } catch (error) {
    alert('卸下失败，墓场暂时罢工 ☠️')
  }
}

// ====== 编辑器操作 ======
const onSelectDecoration = (id) => {
  selectedDecorationId.value = id
}

const onUpdateState = (deco) => {
  // 实时更新位置（拖拽时）
  const state = decorationStates.value.find(s => s.userDecorationId === deco.userDecorationId)
  if (state) {
    state.x = deco.x
    state.y = deco.y
  }
}

const rotateDecoration = (angle) => {
  const deco = graveyardRef.value?.editableDecorations?.find(d => d.id === selectedDecorationId.value)
  if (!deco) return
  deco.rotation = (deco.rotation || 0) + angle
  updateState(deco)
}

const scaleDecoration = (factor) => {
  const deco = graveyardRef.value?.editableDecorations?.find(d => d.id === selectedDecorationId.value)
  if (!deco) return
  deco.scale = Math.max(0.3, Math.min(3, (deco.scale || 1) * factor))
  updateState(deco)
}

const moveLayer = (direction) => {
  const deco = graveyardRef.value?.editableDecorations?.find(d => d.id === selectedDecorationId.value)
  if (!deco) return
  deco.zIndex = (deco.zIndex || 0) + (direction === 'up' ? 1 : -1)
  updateState(deco)
}

const updateState = (deco) => {
  const state = decorationStates.value.find(s => s.userDecorationId === deco.userDecorationId)
  if (state) {
    state.x = deco.x
    state.y = deco.y
    state.rotation = deco.rotation || 0
    state.scale = deco.scale || 1
    state.zIndex = deco.zIndex || 0
  }
}

const saveAllStates = async () => {
  if (!user.value) return
  try {
    for (const state of decorationStates.value) {
      await axios.post(`${API_BASE}/api/v1/decorations/state/save`, state)
    }
    alert('✅ 所有装饰位置已保存！')
  } catch (error) {
    alert('保存失败，墓场暂时罢工 ☠️')
  }
}

// ====== 提交数据 ======
const submitData = async () => {
  if (!user.value) return
  submitting.value = true
  try {
    const payload = {
      userId: user.value.id,
      steps: dataForm.value.steps,
      screenTimeMinutes: dataForm.value.screenTimeMinutes,
      keyPresses: dataForm.value.keyPresses,
      sleepHours: dataForm.value.sleepHours,
      appOpens: dataForm.value.appOpens,
      lastActiveAt: dataForm.value.lastActiveAt || null,
      momentsViewed: dataForm.value.momentsViewed
    }
    const res = await axios.post(`${API_BASE}/api/v1/daily-data/submit`, payload)
    if (res.data.success) {
      alert('✅ 数据提交成功！')
      showDataModal.value = false
      dataForm.value = { steps: 0, screenTimeMinutes: 0, keyPresses: 0, sleepHours: 0, appOpens: 0, lastActiveAt: '', momentsViewed: false }
      await loadTodayData()
      await loadReport()
      try { await axios.post(`${API_BASE}/api/v1/tasks/check?userId=${user.value.id}`) } catch (e) {}
      await loadTasks()
      await fetchUserInfo()
    } else {
      alert('❌ ' + res.data.message)
    }
  } catch (error) {
    alert('提交失败，墓场暂时罢工 ☠️')
  } finally {
    submitting.value = false
  }
}

const closeShop = () => {
  showShop.value = false
  shopTab.value = 'store'
}

const logout = () => {
  localStorage.removeItem('user')
  window.location.href = '/'
}

// ====== 粒子背景 ======
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