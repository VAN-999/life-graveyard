<template>
  <div class="cemetery-box">
    <!-- 夜空 -->
    <div class="sky"></div>
    <!-- 草地 -->
    <div class="grass"></div>
    <!-- 月亮 -->
    <div class="moon" :style="moonStyle"></div>
    <!-- 鬼火 -->
    <div class="will-o-wisp" style="left:40%;top:45%;animation-delay:0s;"></div>
    <div class="will-o-wisp" style="left:65%;top:38%;animation-delay:1.2s;"></div>
    <div class="will-o-wisp" style="left:22%;top:52%;animation-delay:2.4s;"></div>

    <!-- 墓碑 -->
    <div class="tomb-wrap">
      <svg width="160" height="220" id="tombSvg">
        <path class="tomb" :d="tombPath" stroke="#666a88" stroke-width="2" fill="#3a3d4f" />
        <text x="80" y="110" class="tomb-text" text-anchor="middle" fill="#c9cde0" font-size="14">
          {{ username }}
        </text>
      </svg>
    </div>

    <!-- 装饰品 -->
    <div v-for="(deco, index) in equippedDecorations" :key="deco.id"
         class="decorate-item"
         :style="getDecorationStyle(index)">
      {{ deco.icon }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  username: { type: String, default: '无名逝者' },
  epitaph: { type: String, default: '此人安息于此' },
  equippedDecorations: { type: Array, default: () => [] },
  tombStyle: { type: String, default: 'point' } // point, round, square
})

// 墓碑形状
const tombPath = computed(() => {
  switch (props.tombStyle) {
    case 'point':
      return 'M20 210 L20 70 L80 10 L140 70 L140 210 Z'
    case 'round':
      return 'M20 210 L20 80 Q80 20 140 80 L140 210 Z'
    case 'square':
      return 'M20 210 L20 30 L140 30 L140 210 Z'
    default:
      return 'M20 210 L20 70 L80 10 L140 70 L140 210 Z'
  }
})

// 月亮尺寸（可配置）
const moonSize = ref(60)
const moonStyle = computed(() => ({
  width: moonSize.value + 'px',
  height: moonSize.value + 'px'
}))

// 装饰品位置（围绕墓碑摆放）
const getDecorationStyle = (index) => {
  const positions = [
    { left: '70%', top: '75%' },
    { left: '78%', top: '72%' },
    { left: '65%', top: '78%' },
    { left: '82%', top: '80%' }
  ]
  const pos = positions[index % positions.length]
  return {
    position: 'absolute',
    left: pos.left,
    top: pos.top,
    fontSize: '28px',
    cursor: 'pointer',
    filter: 'drop-shadow(0 0 4px rgba(255,255,255,0.3))'
  }
}
</script>

<style scoped>
.cemetery-box {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
}

/* 夜空 */
.sky {
  width: 100%;
  height: 70%;
  background: linear-gradient(#060814, #10132b);
  position: absolute;
  top: 0;
  left: 0;
}

/* 草地 */
.grass {
  width: 100%;
  height: 30%;
  background: #121a16;
  position: absolute;
  bottom: 0;
  left: 0;
}

/* 月亮 */
.moon {
  position: absolute;
  background: #eef2ff;
  border-radius: 50%;
  box-shadow: 0 0 60px #cdd6ff, 0 0 100px #8899dd66;
  left: 12%;
  top: 15%;
  transition: all 0.5s ease;
}

/* 墓碑容器 */
.tomb-wrap {
  position: absolute;
  right: 12%;
  bottom: 10%;
}

/* 墓碑文字 */
.tomb-text {
  fill: #c9cde0;
  text-anchor: middle;
  font-size: 14px;
  font-family: 'Times New Roman', serif;
}

/* 鬼火浮动 */
@keyframes float {
  0% { transform: translateY(0); opacity: 0.7; }
  50% { transform: translateY(-12px); opacity: 1; }
  100% { transform: translateY(0); opacity: 0.7; }
}

.will-o-wisp {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #aaffcc;
  border-radius: 50%;
  box-shadow: 0 0 12px #88ffbb;
  animation: float 4s infinite ease-in-out;
}
</style>