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
