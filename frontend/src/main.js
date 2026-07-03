import './assets/main.css'
import { createApp } from 'vue'
import App from './App.vue'
import Particles from 'particles.vue3'
import 'animate.css'
import router from './router'

const app = createApp(App)
app.use(Particles)
app.use(router)
app.mount('#app')