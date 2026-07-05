<template>
  <div class="cemetery-canvas">
    <img :src="bgImage" class="bg-image" alt="墓园背景" />
    <div class="star-container" ref="starContainer"></div>

    <div class="tomb-wrap">
      <div class="tomb-group">
        <div class="shadow-wrapper">
          <img :src="shadowImage" class="tomb-shadow-img" alt="阴影" />
        </div>
        <img :src="tombstoneImage" class="tomb-png" alt="墓碑" />
      </div>
    </div>

    <div class="decor-container">
      <div
          v-for="(deco, index) in equippedDecorations"
          :key="deco.id"
          class="decor-item"
          :style="getDecorationStyle(index)"
      >
        <span class="deco-emoji">{{ deco.icon }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import bgImage from '../assets/cemetery-bg.jpg'
import tombstoneImage from '../assets/tombstone.png'
import shadowImage from '../assets/tombstone-shadow.png'

const props = defineProps({
  username: { type: String, default: '安息于此' },
  equippedDecorations: { type: Array, default: () => [] }
})

const starContainer = ref(null)

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

const getDecorationStyle = (index) => {
  const positions = [
    { left: '55%', top: '72%' },
    { left: '70%', top: '68%' },
    { left: '48%', top: '78%' },
    { left: '78%', top: '75%' }
  ]
  const pos = positions[index % positions.length]
  return {
    position: 'absolute',
    left: pos.left,
    top: pos.top,
    transform: 'translate(-50%, -50%)',
    zIndex: 10,
    fontSize: '28px',
    cursor: 'pointer'
  }
}

onMounted(() => {
  generateStars()
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

.decor-container {
  position: absolute;
  inset: 0;
  z-index: 4;
  pointer-events: none;
}
.decor-item {
  pointer-events: auto;
  cursor: pointer;
  transition: transform 0.2s ease;
  filter: drop-shadow(0 0 6px rgba(255,255,255,0.15));
}
.decor-item:hover {
  transform: scale(1.2) rotate(8deg);
}
.deco-emoji {
  font-size: 28px;
  display: block;
}
</style>