# 开发日志 - 人生数据坟场

## 2026-07-02 Day 1

### 今天做了什么

**环境准备：**
- 装了 Java 17、MySQL 8.0、IntelliJ IDEA
- 注册了 GitHub 仓库 life-graveyard
- 本地初始化 git，连上了远程仓库

**项目搭建：**
- 用 Spring Initializr 创建了 Spring Boot 3.2 项目
- 依赖：Spring Web、Spring Data JPA、MySQL Driver、DevTools
- 解决了 IDEA 里 JDK 没识别的问题

**数据库：**
- 装 MySQL 8.0
- 创建数据库 `graveyard`
- 配置 application.yaml 连上数据库

**编码：**
- 写 TestController，访问 `/api/v1/test/hello` 返回 "墓场建设中"![img.png](img.png)
- 写 User 实体类，对应 users 表
- 写 UserRepository 继承 JpaRepository
- 写 UserController，实现两个接口：
    - POST /api/v1/user/register：注册新用户
    - POST /api/v1/user/login：登录验证
    - ![img_1.png](img_1.png)#注册接口


**测试：**
- 装 Postman
- 注册了一个测试用户：测试用户/123456
- 登录成功，返回 userId 和用户信息
- 去 MySQL 查 users 表，数据落库成功

### 遇到的主要问题

1. cmd 里 mysql 命令不识别
   → 环境变量没配，直接用绝对路径连：`"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql" -u root -p`

2. 第一次启动报 Unknown database 'graveyard'
   → 手动进 MySQL 创建数据库

3. TestController 访问 404
   → 之前用 VSCode 写的，换 IDEA 后手动建了 Controller 文件重新编译就好了

### 项目当前状态

- Spring Boot 正常运行在 8080 端口
- MySQL 连上了
- 注册和登录接口能用
- 代码已提交到 GitHub

## 2026-07-03 Day 2

### 今天做了什么

**前端项目搭建：**
- 创建 Vue 3 + Vite 项目
- 安装 Tailwind CSS、Animate.css、Particles、Axios、Vue Router
- 配置 Vite 代理解决跨域问题
- 解决 Node.js 版本太高的问题（从 v24 降到 v22 LTS）
- 解决 Tailwind CSS v4 语法变化问题（锁回 v3）

**登录页：**
- 暗黑墓园风登录页（粒子背景 + 红色发光标题 + 入场动画）
- 接入后端 `/login` 接口
- 登录成功跳转到首页，用户信息存 localStorage

**首页：**
- 三栏布局：左侧数据卡片 + 中间墓场 + 右侧报告/任务
- 展示今日数据（步数、屏幕时间、键盘敲击、睡眠）
- 展示今日死亡报告（死亡指数、死亡原因）
- 展示今日任务进度
- 接入接口：`/daily-data/today`、`/report/today`、`/tasks/today`

**墓园组件（Graveyard.vue）：**
- SVG 绘制：夜空、月亮、星星、草地、墓碑
- 墓碑支持三种款式（尖顶/圆弧/方正）
- 名字和墓志铭刻在墓碑上
- 装饰品围绕墓碑摆放
- 鬼火飘浮动效 + 蜡烛火焰闪烁

**遇到的问题：**
- Google Fonts 国内被墙 → 改用系统字体
- Tailwind v4 语法变化 → 锁回 v3
- 依赖冲突 → 加 `--legacy-peer-deps` 安装
- 前端跨域 → Vite 配置 proxy 代理到 8080
- npm 安装 particles.vue3 找不到版本 → 改用兼容版本

### 项目当前状态

- 登录：✅
- 首页数据展示：✅
- 墓园组件：✅
- 装饰品展示：✅
- 任务进度：✅
- 死亡报告：✅

### 明天计划

- 优化墓园组件视觉（纹理叠加方案）
- 补完注册功能
- 装修墓场弹窗

### 今日荒诞总结

"今天终于看见了墓园长什么样。虽然墓碑还是有点丑，但至少能看见字了。比昨天强。"
