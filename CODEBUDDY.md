# CODEBUDDY.md This file provides guidance to CodeBuddy when working with code in this repository.

## 常用命令

```bash
# 构建项目
./gradlew build

# 运行项目
./gradlew bootRun

# 仅编译
./gradlew compileJava

# 运行测试
./gradlew test

# 运行单个测试类
./gradlew test --tests "com.pys.luntai.service.UserServiceTest"

# 清理构建
./gradlew clean
```

## 项目架构

### 技术栈
- **Spring Boot 3.2.5** + Java 21
- **MyBatis-Plus 3.5.5** (ORM框架)
- **MySQL 8.3** (数据库)
- **JWT (jjwt 0.12.3)** (身份认证)
- **Fastjson2** (JSON处理)
- **Lombok** (简化代码)

### 目录结构
```
src/main/java/com/pys/luntai/
├── LuntaiApplication.java          # Spring Boot 启动类
├── user/                           # 用户模块
│   ├── UserController.java        # REST API 控制器
│   ├── UserService.java            # 业务逻辑
│   ├── UserMapper.java             # 数据库访问
│   ├── User.java                   # 实体类
│   └── UserVO.java / LoginResponse.java  # 响应VO
├── product/                        # 商品模块
├── audit/                          # 审批模块
├── file/                           # 文件上传模块
└── common/
    ├── util/JwtUtil.java           # JWT工具类
    ├── vo/Result.java              # 统一响应封装
    └── config/                     # 配置类
```

### 核心设计

**认证流程**：微信小程序登录 → 后端调用微信API换取openid → 生成JWT Token返回前端 → 前端携带Token访问需认证接口

**Token验证**：通过 `JwtUtil.validateToken()` 解析请求头 `Authorization: Bearer {token}`，使用 `getCurrentUserId()` 获取当前用户ID

**响应格式**：统一使用 `Result<T>` 封装，结构为 `{code, message, data}`

**权限控制**：管理员操作通过 `isAdmin` 字段判断，控制器层直接调用 `getCurrentUserId()` 验证登录状态

**API设计**：RESTful风格，路由格式 `/api/{module}/{action}`

### 数据库连接
- Host: `120.26.19.102`
- Database: `luntai`
- 配置位置：`src/main/resources/application.properties`
