# 仓库协作指南

## 沟通与修改范围

- 默认使用简体中文沟通、说明修改和记录验证结果，代码标识符与命令保留原文。
- 修改前阅读相关源码及配置，以实际实现为准核实 `API.md`、`DEPLOY.md` 和 `CODEBUDDY.md`。
- 围绕用户需求进行修改，沿用相邻代码风格，保留用户已有改动，避免无关重构与格式化。

## 技术栈与结构

本仓库为轮胎业务后端，使用 Java 21、Spring Boot 3.2.5、Gradle Wrapper 8.14、MyBatis-Plus 3.5.5、MySQL JDBC 驱动、Lombok、jjwt 和 Fastjson2。数据库服务版本应以实际环境为准。

主代码位于 `src/main/java/com/pys/luntai/`，按业务模块组织。

- `LuntaiApplication.java` 为 Spring Boot 入口。
- `product/` 管理商品、列表筛选、价格和批量操作。
- `user/` 管理微信登录、用户资料、身份认证与用户审核。
- `audit/` 管理审核记录及相关接口。
- `file/` 处理文件上传。
- `common/config/` 管理 MyBatis-Plus 和上传资源映射。
- `common/util/JwtUtil.java` 提供 JWT 工具。
- `common/vo/Result.java` 是商品控制器等使用的响应封装。另有 `common/Result.java`，其包声明为 `com.pys.luntai.vo`，修改前核对实际 import。
- `src/main/resources/` 保存默认及生产配置，`db/` 保存 SQL 脚本。
- `src/test/java/` 保存 JUnit 5、Mockito 及 Spring Boot 测试。
- `uploads/` 含已纳入版本管理的图片，修改时注意现有文件引用。

## 开发与验证

在本文件所在仓库根目录使用 Java 21 和项目自带 Gradle Wrapper。Windows PowerShell 命令如下。

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
.\gradlew.bat test --tests "com.pys.luntai.service.ProductServiceTest"
.\gradlew.bat build
.\gradlew.bat bootRun
.\gradlew.bat bootJar
```

Linux 或 macOS 使用对应的 `./gradlew` 命令。首次运行可能需要下载 Gradle 及 Maven 依赖，构建产物位于 `build/`。

- 修改业务代码后先编译，再运行相关测试，按改动范围执行完整构建。
- 部分现有测试引用 `dto`、`entity`、`mapper` 等旧包路径，与当前业务模块包结构有差异。测试编译失败时先确认具体原因，报告已有问题与本次修改的影响。
- `LuntaiApplicationTests` 使用 `@SpringBootTest`，验证时需检查所需配置及外部服务条件。
- 启动或联调前核对数据源、微信配置、JWT 配置及 `file.upload.path`。使用独立的本地配置或环境变量提供测试环境参数，避免在日志、文档或提交中复制凭据。
- 数据库脚本执行前先检查内容与目标数据库，写入验证使用明确的测试数据。
- `Dockerfile` 采用多阶段构建，在 Java 21 JDK 阶段通过 Gradle Wrapper 执行 `bootJar`，将生成的 JAR 复制到 Java 21 JRE 运行镜像。上传源码后可直接执行 `docker build -t luntai .`，测试需单独验证。

## 代码与接口约定

- 延续 Controller、Service、Mapper、实体和请求/响应对象的职责划分及现有 Lombok 用法。
- 查询复用 MyBatis-Plus Wrapper，遵循实体字段映射和参数绑定方式。
- API 响应结构为 `{code, message, data}`，成功业务码为 `200`，通用错误封装为 `500`。修改时区分 HTTP 状态码和业务码。
- 涉及认证或管理员操作时，检查对应控制器的 JWT 解析、用户标识和权限验证流程。
- 商品查询关键词当前在品牌、名称和规格字段间使用分组 OR 模糊匹配，再与其他筛选条件组合。修改搜索规则时同步核对前端参数及接口说明。
- 调整商品价格或响应转换时，检查用户审核状态对价格展示的影响，并保持相关接口一致。
- 批量修改、删除和审核变更需考虑失败、重试及事务边界，保留调用方依赖的行为。
- 完成后检查 Git 差异，用中文说明修改内容、实际执行的验证及尚未验证的事项。
