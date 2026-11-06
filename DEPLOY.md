# 宝塔面板 Docker 部署指南

> 前置条件为已安装宝塔面板和 Docker 管理器。Docker 会使用 Java 21 多阶段构建自动编译源码并打包。服务器需能下载基础镜像、Gradle 分发包及 Maven 依赖。

---

## 一、上传项目文件到服务器

### 上传源码

1. 宝塔面板 → 文件 → 进入 `/www/wwwroot/`
2. 新建目录 `luntai`，进入
3. 上传整个项目文件夹内容（或用 git clone）

```bash
cd /www/wwwroot/luntai

# 如果是 git 仓库
git clone https://github.com/pys876121546/luntaib.git .
```

### 构建所需文件

- `Dockerfile`
- `gradlew`、`build.gradle`、`settings.gradle`
- `gradle/wrapper/` 目录（包含 Wrapper JAR 和配置文件）
- `src/main/` 目录（包含 Java 源码和资源配置）

本地未提交或未推送的修改需要通过上传更新后的文件同步到服务器。

---

## 二、构建并运行 Docker 容器

### 2.1 生产配置说明

`application-prod.properties` 已包含在项目中，关键配置如下：

```properties
# 文件上传路径（Docker 内部路径）
file.upload.path=/app/uploads

# 图片访问域名（支持环境变量覆盖）
file.server.url=${FILE_SERVER_URL:https://tire.shengjupay.com}
```

> 无需手动创建，直接使用项目中的配置文件即可。如需修改域名，通过运行时的环境变量传入。

### 2.2 构建镜像

Dockerfile 的构建阶段运行 `bootJar`，生成可运行 JAR 后复制到 Java 21 JRE 运行镜像。此流程只打包主代码，测试应在部署前单独验证。首次构建需要下载依赖，耗时取决于服务器网络。

**方式 A：宝塔 Docker 管理器**
1. 宝塔面板 → Docker → 镜像 → 构建
2. 选择项目目录：`/www/wwwroot/luntai`
3. 镜像名称：`luntai`
4. 点击构建

**方式 B：命令行 SSH**
```bash
cd /www/wwwroot/luntai
docker build -t luntai .
```

### 2.3 运行容器

```bash
docker run -d \
  --name luntai \
  --restart always \
  -p 8080:8080 \
  -v /data/uploads:/app/uploads \
  -e FILE_SERVER_URL=https://tire.shengjupay.com \
  luntai
```

参数说明：

| 参数 | 说明 |
|------|------|
| `-d` | 后台运行 |
| `--name luntai` | 容器名称 |
| `--restart always` | 自动重启 |
| `-p 8080:8080` | 映射端口 8080 |
| `-v /data/uploads:/app/uploads` | 挂载上传文件目录（服务器路径→容器路径） |
| `-e FILE_SERVER_URL=...` | 图片访问域名，上传图片返回的 URL 前缀 |

---

## 三、配置域名和反向代理（Nginx）

### 3.1 添加网站

1. 宝塔面板 → 网站 → 添加站点
2. 填写你的域名，如 `tires.example.com`
3. PHP 版本选「纯静态」
4. 点击提交

### 3.2 配置反向代理

1. 进入站点设置 → 反向代理 → 添加反向代理
2. 填写：
   - **代理名称**：`luntai-api`
   - **目标URL**：`http://127.0.0.1:8080`
   - **发送域名**：`$host`
3. 提交保存

或者直接修改 Nginx 配置文件，点击「配置文件」，添加/修改：

```nginx
server {
    listen 80;
    server_name tires.example.com;  # 替换为你的域名

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 文件上传大小限制
        client_max_body_size 50M;
    }
}
```

### 3.3 配置 HTTPS 证书

**方式 A：Let's Encrypt 免费证书（推荐）**

1. 网站 → 你的域名 → SSL → Let's Encrypt
2. 选择域名，勾选 DNS 或 HTTP 验证
3. 申请证书
4. 开启「强制 HTTPS」

**方式 B：已有证书文件**

1. 网站 → 你的域名 → SSL → 其他证书
2. 填写：
   - 密钥（key）：粘贴 `.key` 文件内容
   - 证书（crt）：粘贴 `.pem` 或 `.crt` 文件内容
3. 保存并开启强制 HTTPS

配置完成后 Nginx 会自动添加 SSL 相关配置。

---

## 四、验证部署

### 4.1 检查容器状态

```bash
docker ps | grep luntai
docker logs luntai
```

### 4.2 测试接口

```bash
# 测试商品列表
curl https://你的域名/api/product/list

# 测试筛选接口
curl https://你的域名/api/product/filters
```

### 4.3 测试文件上传

上传一张图片，确认能通过 `https://你的域名/uploads/...` 访问到

---

## 五、常用运维命令

```bash
# 查看日志
docker logs -f luntai --tail=100

# 重启服务
docker restart luntai

# 更新版本
cd /www/wwwroot/luntai
git pull
# 如果修改尚未推送到远程仓库，请上传更新后的源码和 Dockerfile。
docker build -t luntai .
docker stop luntai && docker rm luntai
docker run -d --name luntai --restart always -p 8080:8080 -v /data/uploads:/app/uploads -e FILE_SERVER_URL=https://tire.shengjupay.com luntai

# 进入容器排查问题
docker exec -it luntai sh
```

---

## 六、注意事项

1. **防火墙**：确保服务器防火墙放行 80、443、8080 端口
2. **数据库白名单**：如果数据库不在本机，确保 MySQL 允许服务器 IP 连接
3. **文件权限**：`/data/uploads` 目录需要写权限
   ```bash
   chmod -R 755 /data/uploads
   ```
4. **微信小程序域名白名单**：需要在微信公众平台配置合法域名
   - request 合法域名：`https://你的域名.com`
   - uploadFile 合法域名：`https://你的域名.com`
