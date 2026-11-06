# 轮胎商城 API 文档

> Base URL: `http://localhost:8080`

---

## 目录

1. [用户模块](#用户模块)
   - 1.1 [微信小程序登录](#11-微信小程序登录)
   - 1.2 [提交审核申请](#12-提交审核申请)
   - 1.3 [获取用户信息](#13-获取用户信息)
   - 1.4 [获取审核信息](#135-获取审核信息)
   - 1.5 [通过审核（管理员）](#15-通过审核管理员)
   - 1.6 [驳回审核（管理员）](#16-驳回审核管理员)
2. [审批模块](#审批模块)
   - 2.1 [获取审批列表](#21-获取审批列表)
   - 2.2 [获取待审核数量](#22-获取待审核数量)
   - 2.3 [通过审批](#23-通过审批)
   - 2.4 [驳回审批](#24-驳回审批)
3. [商品模块](#商品模块)
   - 3.1 [查询商品列表](#31-查询商品列表)
   - 3.2 [添加商品](#32-添加商品)
   - 3.3 [修改商品](#33-修改商品)
   - 3.4 [批量修改商品](#34-批量修改商品)
   - 3.5 [删除商品（单个）](#35-删除商品单个)
   - 3.6 [批量删除商品](#36-批量删除商品)
   - 3.7 [获取筛选条件列表](#37-获取筛选条件列表)
4. [文件模块](#文件模块)
   - 4.1 [通用文件上传](#41-通用文件上传)
   - 4.2 [上传公司门头照片](#42-上传公司门头照片)

---

## 用户模块

### 1.1 微信小程序登录

```
POST /api/user/login
```

**请求参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | string | 是 | 微信登录临时凭证 |
| nickname | string | 是 | 微信用户昵称（需调用 wx.getUserProfile 获取） |
| avatarUrl | string | 是 | 微信用户头像URL（需调用 wx.getUserProfile 获取） |

**请求示例:**
```json
{
  "code": "wx_login_code_from_wechat",
  "nickname": "张三",
  "avatarUrl": "https://thirdwx.qlogo.cn/..."
}
```

**前端调用示例:**
```javascript
// 1. 获取登录凭证
wx.login({
  success: (loginRes) => {
    // 2. 获取用户授权信息
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: (userRes) => {
        // 3. 调用后端登录
        wx.request({
          url: 'http://localhost:8080/api/user/login',
          method: 'POST',
          data: {
            code: loginRes.code,
            nickname: userRes.userInfo.nickName,
            avatarUrl: userRes.userInfo.avatarUrl
          }
        });
      }
    });
  }
});
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "nickname": "张三",
    "avatarUrl": "https://...",
    "auditStatus": "NONE",
    "isNewUser": true,
    "isAdmin": false
  }
}
```

**auditStatus 说明:**
- `NONE` - 未提交审核
- `PENDING` - 审核中
- `APPROVED` - 审核通过
- `REJECTED` - 审核拒绝

**isAdmin 说明:**
- `false` - 普通用户
- `true` - 管理员

---

### 1.2 提交审核申请

```
POST /api/user/audit
```

**请求头:**
```
Authorization: Bearer {token}
```

**请求参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| companyName | string | 是 | 公司名头 |
| companyAddress | string | 是 | 公司地址 |
| companyPhone | string | 是 | 公司电话 |
| companyPhotoUrl | string | 否 | 公司门头照片URL |

**请求示例:**
```json
{
  "companyName": "XX轮胎有限公司",
  "companyAddress": "北京市朝阳区xxx路xxx号",
  "companyPhone": "010-12345678",
  "companyPhotoUrl": "/uploads/company/xxx.jpg"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 1.3 获取用户信息

```
GET /api/user/info
```

**请求头:**
```
Authorization: Bearer {token}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "nickname": "张三",
    "avatarUrl": "https://...",
    "auditStatus": "APPROVED",
    "isAdmin": false
  }
}
```

---

### 1.3.5 获取审核信息

```
GET /api/user/audit/info
```

**请求头:**
```
Authorization: Bearer {token}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "status": "PENDING",
    "companyName": "XX轮胎有限公司",
    "companyAddress": "北京市朝阳区xxx路xxx号",
    "companyPhone": "010-12345678",
    "companyPhotoUrl": "/uploads/company/xxx.jpg",
    "auditRemark": null,
    "createTime": "2026-05-01 10:30:00"
  }
}
```

**说明:** 返回当前用户最新提交的审核记录，未提交过则返回 null

---

### 1.5 通过审核（管理员）

```
POST /api/user/audit/approve
```

**请求头:**
```
Authorization: Bearer {token}
```

**请求参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 要审核的用户ID |

**请求示例:**
```json
{
  "userId": 2
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）
- 用户不存在
- 当前状态无法审核（非 PENDING 状态）

---

### 1.6 驳回审核（管理员）

```
POST /api/user/audit/reject
```

**请求头:**
```
Authorization: Bearer {token}
```

**请求参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 要审核的用户ID |

**请求示例:**
```json
{
  "userId": 2
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）
- 用户不存在
- 当前状态无法审核（非 PENDING 状态）

---

## 审批模块

### 2.1 获取审批列表

```
GET /api/audit/list
```

**请求头:**
```
Authorization: Bearer {token}
```

**请求参数 (Query):**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 审批状态筛选，不传表示全部：PENDING=待审核, APPROVED=已通过, REJECTED=已驳回 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "nickname": "张三",
      "avatarUrl": "https://...",
      "companyName": "XX轮胎有限公司",
      "companyAddress": "北京市朝阳区xxx路xxx号",
      "companyPhone": "010-12345678",
      "companyPhotoUrl": "/uploads/company/xxx.jpg",
      "status": "PENDING",
      "auditRemark": null,
      "auditorId": null,
      "auditorName": null,
      "auditTime": null,
      "createTime": "2024-01-15T10:30:00"
    }
  ]
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）

---

### 2.2 获取待审核数量

```
GET /api/audit/pending/count
```

**请求头:**
```
Authorization: Bearer {token}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": 5
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）

---

### 2.3 通过审批

```
POST /api/audit/approve/{id}
```

**请求头:**
```
Authorization: Bearer {token}
```

**路径参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 审批记录ID |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）
- 审批记录不存在
- 当前状态无法审核（非 PENDING 状态）

---

### 2.4 驳回审批

```
POST /api/audit/reject/{id}
```

**请求头:**
```
Authorization: Bearer {token}
```

**路径参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 审批记录ID |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）
- 审批记录不存在
- 当前状态无法审核（非 PENDING 状态）

---

## 商品模块

### 3.1 查询商品列表

```
GET /api/product/list
```

**请求参数 (Query):**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 关键词模糊搜索（品牌、型号、规格），纯数字支持省略规格中的 R，例如 2656518 可匹配 26565R18 |
| category | string | 否 | 分类（前端传啥存啥，不做转换） |
| brand | string | 否 | 品牌筛选 |
| width | string | 否 | 胎宽筛选 |
| ratio | string | 否 | 扁平比筛选 |
| rim | string | 否 | 轮毂尺寸筛选 |
| special | boolean | 否 | 是否特价 |

**响应示例 (未登录/未审核):**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "category": "四季轮胎",
      "name": "米其林 PS4 225/45R17",
      "brand": "米其林",
      "width": "225",
      "ratio": "45",
      "rim": "17",
      "price": "",
      "originalPrice": "",
      "special": true,
      "stock": 100,
      "img": "/uploads/product/xxx.jpg"
    }
  ]
}
```

**响应示例 (已审核用户):**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "category": "四季轮胎",
      "name": "米其林 PS4 225/45R17",
      "brand": "米其林",
      "width": "225",
      "ratio": "45",
      "rim": "17",
      "price": "599.00",
      "originalPrice": "699.00",
      "special": true,
      "stock": 100,
      "img": "/uploads/product/xxx.jpg"
    }
  ]
}
```

**说明:**
- 此接口无需登录即可访问
- 未登录或未审核用户不显示价格（price 和 originalPrice 为空字符串）
- 已审核用户或管理员显示实际价格
- 列表按创建时间倒序排列

---

### 3.2 添加商品

```
POST /api/product/add
```

**请求头:**
```
Authorization: Bearer {token}
```

**请求参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 是 | 分类（如：四季轮胎、冬季轮胎，前端传啥存啥） |
| name | string | 是 | 商品名称 |
| brand | string | 是 | 品牌 |
| width | string | 是 | 胎宽 |
| ratio | string | 是 | 扁平比 |
| rim | string | 是 | 轮毂尺寸 |
| price | decimal | 是 | 现价 |
| originalPrice | decimal | 否 | 原价（特价时必填） |
| special | boolean | 是 | 是否特价 |
| stock | int | 是 | 库存 |
| img | string | 否 | 商品图片URL |

**请求示例:**
```json
{
  "category": "四季轮胎",
  "name": "米其林 PS4 225/45R17",
  "brand": "米其林",
  "width": "225",
  "ratio": "45",
  "rim": "17",
  "price": 599.00,
  "originalPrice": 699.00,
  "special": true,
  "stock": 100,
  "img": "/uploads/product/xxx.jpg"
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）

---

### 3.3 修改商品

```
POST /api/product/update/{id}
```

**请求头:**
```
Authorization: Bearer {token}
```

**路径参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 商品ID |

**请求参数:** 同添加商品，传需要修改的字段

**请求示例:**
```json
{
  "price": 549.00,
  "stock": 80
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）
- 商品ID不能为空

---

### 3.4 批量修改商品

```
POST /api/product/update/batch
```

**请求头:**
```
Authorization: Bearer {token}
```

**请求参数:** 商品数组，每个元素独立修改

**数组元素字段:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 商品ID |
| stock | int | 否 | 库存数量，null表示不修改 |
| price | decimal | 否 | 现价，null表示不修改 |
| special | boolean | 否 | 是否特价，null表示不修改 |

**价格逻辑（每个 item 独立处理）:**

| special | price | 处理方式 |
|---------|-------|---------|
| true (特价) | 有值 | 只修改 **price**，**originalPrice 不变** |
| false (非特价) | 有值 | 同时修改 **price 和 originalPrice** 为相同值 |

示例：商品原 price=599, originalPrice=699
- `{ "id": 1, "special": true, "price": 399 }` → price=**399**, originalPrice=**699**(不变)
- `{ "id": 2, "special": false, "price": 499 }` → price=**499**, originalPrice=**499**(同步)

**请求示例:**
```json
[
  {
    "id": 1,
    "price": 299.99,
    "special": true,
    "stock": 50
  },
  {
    "id": 2,
    "price": 199.99,
    "special": false
  },
  {
    "id": 3,
    "stock": 100
  }
]
```

**说明:**
- 每个商品独立处理，互不影响
- id 为必填项，其他字段按需传入
- 至少需要有一个 item

**错误情况:**
- 无权限操作（调用者不是管理员）
- 商品列表不能为空

---

### 3.5 删除商品（单个）

```
POST /api/product/delete/{id}
```

**请求头:**
```
Authorization: Bearer {token}
```

**路径参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | **商品ID**（数据库主键，非数组下标） |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）

---

### 3.6 批量删除商品

```
POST /api/product/delete/batch
```

**请求头:**
```
Authorization: Bearer {token}
```

**请求参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| - | array | 是 | 商品ID列表 |

**请求示例:**
```json
[1, 2, 3, 4, 5]
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误情况:**
- 无权限操作（调用者不是管理员）
- 删除ID列表不能为空

---

### 3.7 获取筛选条件列表

```
GET /api/product/filters
```

**说明:** 获取所有可用的筛选条件，用于前端构建筛选器。去重并排序。

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "categories": ["四季轮胎", "冬季轮胎"],
    "brands": ["固特异", "普利司通", "米其林", "马牌"],
    "widths": ["195", "205", "215", "225"],
    "ratios": ["45", "55", "60", "65"],
    "rims": ["15", "16", "17", "18"]
  }
}
```

**字段说明:**
- `categories`: 分类列表（如：四季轮胎、冬季轮胎）
- `brands`: 品牌列表
- `widths`: 胎宽列表
- `ratios`: 扁平比列表
- `rims`: 轮毂尺寸列表

---

## 文件模块

### 4.1 通用文件上传

```
POST /api/file/upload
```

**请求方式:** `multipart/form-data`

**请求参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 上传的文件 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/uploads/2024/xxx.jpg",
    "filename": "xxx.jpg",
    "size": 10240
  }
}
```

---

### 4.2 上传公司门头照片

```
POST /api/file/upload/company-photo
```

**请求方式:** `multipart/form-data`

**请求参数:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 门头照片 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/uploads/company/xxx.jpg",
    "filename": "xxx.jpg",
    "size": 20480
  }
}
```

---

## 通用响应格式

```json
{
  "code": 200,        // 200=成功，其他=错误
  "message": "success",
  "data": {}          // 具体数据
}
```

**错误码说明:**

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token无效 |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

---

## 认证方式

除登录接口外，需要认证的接口在请求头中携带:

```
Authorization: Bearer {token}
```

Token 有效期: 30 天

---

## 配置信息

### 微信小程序
- AppID: `wx71c3a0c01959fdae`

### 数据库
- Host: `120.26.19.102`
- Database: `luntai`

### 文件存储
- 本地服务器: `/data/uploads`
- 访问URL: `http://localhost:8080{url}`
