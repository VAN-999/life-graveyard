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

## 2026-07-05 Day 3

### 今天做了什么

**部署上线**
- 前端部署到 Railway：`https://ravishing-rebirth-production-acfe.up.railway.app`
- 后端部署到 Railway：`https://life-graveyard-production.up.railway.app`
- 配置 Railway MySQL 数据库，连接生产环境
- 解决跨域问题，前后端联调成功

**装饰系统重构（核心）**
- 彻底重构了装饰品存储逻辑：从 `quantity` 合并存储改为每条记录独立存储
- 每次购买都生成独立的 `user_decorations` 记录，每个装饰品有自己的 ID
- 解决了“拖拽一个另一个跟着乱跑”的问题（用 `userDecorationId` 作为唯一 key）
- 解决了“装备第二个第一个消失”的问题（移除同分类互斥逻辑）
- 解决了“删除功能没用”的问题（后端删除方法加了 `@Transactional`）
- 解决了“红框残留”的问题（`selectedId` 通过 props 传递到 `Graveyard` 组件）

**后端改动**
- `DecorationController.java`：购买、装备、删除逻辑全部重构
- `UserDecoration.java`：去掉 `quantity` 字段
- `DecorationState.java`：添加 `@JsonProperty("zIndex")` 解决字段映射问题

**前端改动**
- `Home.vue`：背包独立显示每个装饰品，添加删除按钮
- `Graveyard.vue`：接收 `selectedId` prop，红框状态由父组件控制

**数据库**
- 清理了旧的 `decoration_states` 和 `user_decorations` 数据

### 遇到的问题

1. 删除装饰品报 `No EntityManager with actual transaction available`
   → 删除方法加 `@Transactional` 解决

2. 装备第二个装饰品，第一个自动消失
   → 移除 `equip` 方法里的同分类互斥逻辑

3. 红框点击 ✕ 后不消失
   → `selectedId` 通过 props 传递到子组件，由父组件统一控制

4. 部署后前端图片 404
   → 装饰品图片移到 `public/assets/decor/`，数据库路径改为 `/assets/decor/xxx.png`

### 项目当前状态

- 购买装饰品：✅ 每次购买独立记录
- 背包显示：✅ 每个装饰品独立显示
- 装备/卸下：✅ 互不干扰
- 拖拽移动：✅ 独立操作
- 删除装饰品：✅ 只删选中的那一个
- 位置保存：✅ 保存到数据库
- 红框状态：✅ 正确显示和清除
- 公网访问：✅ 前端 + 后端已部署

### 明天计划

- 移动端适配（手机打开能正常用）
- 墓碑换皮肤功能
- 环境特效（雾、雪、萤火虫）
- 整体稳定性测试

### 今日荒诞总结

“今天把装饰系统拆了重做，从‘一捆蜡烛’变成了‘一根一根的蜡烛’。虽然过程很痛苦，但至少现在拖拽一个不会让另一个跟着跑了。而且这堆代码现在全世界都能看到了。”
