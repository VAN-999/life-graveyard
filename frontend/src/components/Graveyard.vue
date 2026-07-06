<template>
  <div class="cemetery-canvas" ref="canvasRef">
    <img :src="bgImage" class="bg-image" alt="墓园背景" />
    <div class="star-container" ref="starContainer"></div>

    <div class="tomb-wrap">
      <div class="tomb-group">
        <div class="shadow-wrapper">
          <img :src="shadowImage" class="tomb-shadow-img" alt="阴影" />
        </div>
        <!-- 墓碑图片：从 public 加载 -->
        <img :src="currentTombstoneUrl" class="tomb-png" alt="墓碑" />
      </div>
    </div>

    <div
        v-for="deco in editableDecorations"
        :key="deco.userDecorationId"
        class="decor-item"
        :style="{
        left: deco.x + '%',
        top: deco.y + '%',
        transform: `translate(-50%, -50%) rotate(${deco.rotation || 0}deg) scale(${deco.scale || 1})`,
        zIndex: deco.zIndex || 0,
        cursor: 'grab',
        border: selectedId === deco.id ? '2px solid #ff4444' : 'none',
        borderRadius: '4px',
        padding: '2px'
      }"
        @mousedown="startDrag($event, deco)"
        @click.stop="selectDecoration(deco.id)"
    >
      <img :src="deco.icon" class="w-12 h-12 object-contain" draggable="false" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import bgImage from '../assets/cemetery-bg.jpg'
import shadowImage from '../assets/tombstone-shadow.png'

const props = defineProps({
  username: { type: String, default: '安息于此' },
  equippedDecorations: { type: Array, default: () => [] },
  decorationStates: { type: Array, default: () => [] },
  selectedId: { type: Number, default: null },
  tombstoneStyle: { type: String, default: 'classic' }
})

const emit = defineEmits(['update-state', 'select-decoration'])

const canvasRef = ref(null)
const starContainer = ref(null)

// ====== 墓碑图片：从 public 加载 ======
const getTombstoneUrl = (style) => {
  return `/assets/decor/tombstone-${style}.png`
}

const currentTombstoneUrl = computed(() => {
  return getTombstoneUrl(props.tombstoneStyle)
})

// ====== 装饰品 ======
const editableDecorations = ref([])

watch(
    () => [props.equippedDecorations, props.decorationStates],
    () => {
      mergeDecorations()
    },
    { deep: true }
)

const mergeDecorations = () => {
  const statesMap = {}
  props.decorationStates.forEach(s => {
    statesMap[s.userDecorationId] = s
  })

  const newList = props.equippedDecorations.map(deco => {
    const state = statesMap[deco.userDecorationId]
    const existing = editableDecorations.value.find(d => d.userDecorationId === deco.userDecorationId)
    if (existing) {
      existing.x = state?.x ?? existing.x
      existing.y = state?.y ?? existing.y
      existing.rotation = state?.rotation ?? existing.rotation
      existing.scale = state?.scale ?? existing.scale
      existing.zIndex = state?.zIndex ?? existing.zIndex
      existing.isVisible = state?.isVisible !== false
      return existing
    }
    return {
      ...deco,
      x: state?.x ?? (50 + (Math.random() - 0.5) * 30),
      y: state?.y ?? (50 + (Math.random() - 0.5) * 30),
      rotation: state?.rotation ?? 0,
      scale: state?.scale ?? 1,
      zIndex: state?.zIndex ?? 0,
      isVisible: state?.isVisible !== false
    }
  })

  editableDecorations.value = newList
}

// ====== 拖拽 ======
let dragData = null

const startDrag = (event, deco) => {
  if (event.button !== 0) return
  event.preventDefault()

  const rect = canvasRef.value.getBoundingClientRect()
  dragData = {
    deco: deco,
    offsetX: (event.clientX - rect.left) / rect.width * 100 - deco.x,
    offsetY: (event.clientY - rect.top) / rect.height * 100 - deco.y
  }

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (event) => {
  if (!dragData) return
  const rect = canvasRef.value.getBoundingClientRect()
  let x = (event.clientX - rect.left) / rect.width * 100 - dragData.offsetX
  let y = (event.clientY - rect.top) / rect.height * 100 - dragData.offsetY
  x = Math.max(0, Math.min(100, x))
  y = Math.max(0, Math.min(100, y))

  const deco = dragData.deco
  deco.x = x
  deco.y = y
  emit('update-state', deco)
}

const stopDrag = () => {
  dragData = null
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

const selectDecoration = (id) => {
  emit('select-decoration', id)
}

// ====== 星星 ======
const generateStars = () => {
  if (!starContainer.value) return
  for (let i = 0; i < 60; i++) {
    const star = document.createElement('div')
    star.className = 'star-item'
    const size = Math.random() * 2.5 + 0.5
    star.style.width = size + 'px'
    star.style.height = size + 'px'
    star.style.left = Math.random() * 100 + '%'
    star.style.top = Math.random() * 40 + '%'
    star.style.animationDelay = Math.random() * 3 + 's'
    starContainer.value.appendChild(star)
  }
}

onMounted(() => {
  generateStars()
  mergeDecorations()
})

defineExpose({
  editableDecorations
})
</script>

<style scoped>
.cemetery-canvas {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  background: #0a0c18;
}
.bg-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 30%;
  z-index: 0;
}
.star-container {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
}
.star-item {
  position: absolute;
  background: #ffffff;
  border-radius: 50%;
  animation: starTwinkle 3s infinite ease-in-out;
}
@keyframes starTwinkle {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 1; }
}
.tomb-wrap {
  position: absolute;
  right: 18%;
  bottom: 18%;
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.tomb-group {
  position: relative;
  display: inline-block;
}
.shadow-wrapper {
  position: absolute;
  bottom: -28px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  width: 280px;
  pointer-events: none;
}
.tomb-shadow-img {
  width: 100%;
  height: auto;
  display: block;
  opacity: 0.35;
}
.tomb-png {
  position: relative;
  z-index: 2;
  width: 358px;
  height: auto;
  display: block;
}
.decor-item {
  position: absolute;
  z-index: 4;
  pointer-events: auto;
  user-select: none;
  transition: border 0.1s ease;
}
.decor-item:active {
  cursor: grabbing;
}
</style>