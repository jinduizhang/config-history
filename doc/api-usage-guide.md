# 通用历史记录管理模块 - 接口使用说明书

## 文档说明

| 项目 | 内容 |
|------|------|
| 文档类型 | API接口使用说明书 |
| 适用对象 | 前端开发人员、接口调用方 |
| 基础URL | `http://localhost:8080` |
| API版本 | v1 |

---

## 一、公共说明

### 1.1 统一请求头

```
Content-Type: application/json
```

### 1.2 统一响应格式

**成功响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": { ... }
}
```

**失败响应**:
```json
{
    "code": 400,
    "message": "错误描述",
    "data": null
}
```

### 1.3 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 二、配置管理接口

### 2.1 获取配置列表

**接口地址**: `GET /api/v1/configs`

**功能说明**: 分页查询配置项列表，支持关键字搜索

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |
| keyword | String | 否 | 搜索关键字，匹配配置键和配置名 |

#### 正常场景

**请求示例**:
```http
GET /api/v1/configs?page=1&pageSize=10&keyword=app
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "configKey": "app.settings",
                "configName": "应用设置",
                "configValue": "{\"theme\": \"light\", \"language\": \"zh-CN\"}",
                "description": "系统基础配置",
                "createdAt": "2026-03-08 10:00:00",
                "updatedAt": "2026-03-08 10:00:00"
            }
        ],
        "total": 1,
        "page": 1,
        "pageSize": 10
    }
}
```

#### 异常场景

| 场景 | 响应 |
|------|------|
| 参数格式错误 | `{"code": 400, "message": "参数格式错误"}` |
| 服务异常 | `{"code": 500, "message": "系统内部错误"}` |

---

### 2.2 获取配置详情

**接口地址**: `GET /api/v1/configs/{id}`

**功能说明**: 根据ID获取单个配置项详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 配置ID |

#### 正常场景

**请求示例**:
```http
GET /api/v1/configs/1
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "configKey": "app.settings",
        "configName": "应用设置",
        "configValue": "{\"theme\": \"light\", \"language\": \"zh-CN\"}",
        "description": "系统基础配置",
        "createdAt": "2026-03-08 10:00:00",
        "updatedAt": "2026-03-08 10:00:00"
    }
}
```

#### 异常场景

| 场景 | HTTP状态码 | 响应 |
|------|------------|------|
| 配置不存在 | 200 | `{"code": 500, "message": "配置不存在"}` |
| ID格式错误 | 400 | `{"code": 400, "message": "参数格式错误"}` |

---

### 2.3 创建配置

**接口地址**: `POST /api/v1/configs`

**功能说明**: 创建新的配置项，系统自动记录初始版本历史

#### 请求体

```json
{
    "configKey": "string",      // 必填，配置键，唯一标识
    "configName": "string",     // 选填，配置名称
    "configValue": "string",    // 选填，配置值，支持JSON格式
    "description": "string",    // 选填，配置描述
    "operator": "string",       // 选填，操作人
    "operatorIp": "string",     // 选填，操作IP
    "changeReason": "string"    // 选填，变更原因
}
```

#### 字段校验规则

| 字段 | 校验规则 |
|------|----------|
| configKey | 必填，不能为空字符串 |
| configValue | 建议JSON格式，但支持任意字符串 |

#### 正常场景

**请求示例**:
```http
POST /api/v1/configs
Content-Type: application/json

{
    "configKey": "system.timeout",
    "configName": "系统超时配置",
    "configValue": "{\"connectTimeout\": 5000, \"readTimeout\": 10000}",
    "description": "系统连接和读取超时配置",
    "operator": "admin"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 2,
        "configKey": "system.timeout",
        "configName": "系统超时配置",
        "configValue": "{\"connectTimeout\": 5000, \"readTimeout\": 10000}",
        "description": "系统连接和读取超时配置",
        "createdAt": "2026-03-08 11:00:00",
        "updatedAt": "2026-03-08 11:00:00"
    }
}
```

#### 异常场景

| 场景 | 响应 |
|------|------|
| configKey为空 | `{"code": 400, "message": "配置键不能为空"}` |
| configKey已存在 | `{"code": 500, "message": "配置键已存在"}` |
| JSON格式错误 | `{"code": 400, "message": "请求体格式错误"}` |

---

### 2.4 更新配置

**接口地址**: `PUT /api/v1/configs/{id}`

**功能说明**: 更新配置项，系统自动保存历史版本

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 配置ID |

#### 请求体

```json
{
    "configKey": "string",      // 必填，配置键
    "configName": "string",     // 选填，配置名称
    "configValue": "string",    // 选填，配置值
    "description": "string",    // 选填，配置描述
    "operator": "string",       // 选填，操作人
    "changeReason": "string"    // 选填，变更原因
}
```

#### 正常场景

**请求示例**:
```http
PUT /api/v1/configs/1
Content-Type: application/json

{
    "configKey": "app.settings",
    "configName": "应用设置",
    "configValue": "{\"theme\": \"dark\", \"language\": \"en-US\"}",
    "description": "更新为暗黑主题",
    "operator": "user1",
    "changeReason": "用户偏好修改"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "configKey": "app.settings",
        "configName": "应用设置",
        "configValue": "{\"theme\": \"dark\", \"language\": \"en-US\"}",
        "description": "更新为暗黑主题",
        "createdAt": "2026-03-08 10:00:00",
        "updatedAt": "2026-03-08 12:00:00"
    }
}
```

#### 异常场景

| 场景 | 响应 |
|------|------|
| 配置不存在 | `{"code": 500, "message": "配置不存在"}` |
| 配置已删除 | `{"code": 500, "message": "配置不存在"}` |
| configKey为空 | `{"code": 400, "message": "配置键不能为空"}` |

---

### 2.5 删除配置

**接口地址**: `DELETE /api/v1/configs/{id}`

**功能说明**: 软删除配置项（逻辑删除），数据仍保留在数据库

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 配置ID |

#### 正常场景

**请求示例**:
```http
DELETE /api/v1/configs/1
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

#### 异常场景

| 场景 | 响应 |
|------|------|
| 配置不存在 | `{"code": 500, "message": "配置不存在"}` |
| 配置已删除 | `{"code": 500, "message": "配置不存在"}` |

---

### 2.6 获取配置历史记录

**接口地址**: `GET /api/v1/configs/{id}/history`

**功能说明**: 分页查询配置项的历史变更记录

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 配置ID |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

#### 正常场景

**请求示例**:
```http
GET /api/v1/configs/1/history?page=1&pageSize=10
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 2,
                "configId": 1,
                "versionNo": 2,
                "configValue": "{\"theme\": \"dark\", \"language\": \"en-US\"}",
                "changeType": "UPDATE",
                "operator": "user1",
                "operatorIp": null,
                "changeReason": "用户偏好修改",
                "createdAt": "2026-03-08 12:00:00"
            },
            {
                "id": 1,
                "configId": 1,
                "versionNo": 1,
                "configValue": "{\"theme\": \"light\", \"language\": \"zh-CN\"}",
                "changeType": "INIT",
                "operator": "system",
                "operatorIp": null,
                "changeReason": "初始化配置",
                "createdAt": "2026-03-08 10:00:00"
            }
        ],
        "total": 2,
        "page": 1,
        "pageSize": 10
    }
}
```

#### changeType 说明

| 值 | 说明 |
|----|------|
| INIT | 初始化创建 |
| UPDATE | 更新操作 |
| ROLLBACK | 版本回退 |
| DELETE | 删除操作 |

---

### 2.7 获取历史版本详情

**接口地址**: `GET /api/v1/configs/{id}/history/{versionId}`

**功能说明**: 获取指定历史版本的详细信息

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 配置ID |
| versionId | Long | 是 | 历史记录ID |

#### 正常场景

**请求示例**:
```http
GET /api/v1/configs/1/history/1
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "configId": 1,
        "versionNo": 1,
        "configValue": "{\"theme\": \"light\", \"language\": \"zh-CN\"}",
        "changeType": "INIT",
        "operator": "system",
        "operatorIp": null,
        "changeReason": "初始化配置",
        "createdAt": "2026-03-08 10:00:00"
    }
}
```

#### 异常场景

| 场景 | 响应 |
|------|------|
| 历史版本不存在 | `{"code": 500, "message": "历史版本不存在"}` |

---

### 2.8 版本对比

**接口地址**: `GET /api/v1/configs/{id}/diff`

**功能说明**: 对比两个历史版本的差异

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 配置ID |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| from | Long | 是 | 源版本ID |
| to | Long | 是 | 目标版本ID |

#### 正常场景

**请求示例**:
```http
GET /api/v1/configs/1/diff?from=1&to=2
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "version1": 1,
        "version2": 2,
        "value1": "{\"theme\": \"light\", \"language\": \"zh-CN\"}",
        "value2": "{\"theme\": \"dark\", \"language\": \"en-US\"}",
        "differences": {
            "theme": {
                "type": "MODIFY",
                "oldValue": "light",
                "newValue": "dark",
                "displayName": "theme"
            },
            "language": {
                "type": "MODIFY",
                "oldValue": "zh-CN",
                "newValue": "en-US",
                "displayName": "language"
            }
        }
    }
}
```

#### 差异类型说明

| 类型 | 说明 |
|------|------|
| ADD | 新版本新增的字段 |
| MODIFY | 值发生变化的字段 |
| DELETE | 新版本删除的字段 |

#### 异常场景

| 场景 | 响应 |
|------|------|
| 版本不存在 | `{"code": 500, "message": "历史版本不存在"}` |

---

### 2.9 版本回退

**接口地址**: `POST /api/v1/configs/{id}/rollback/{versionId}`

**功能说明**: 将配置回退到指定的历史版本

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 配置ID |
| versionId | Long | 是 | 目标版本ID |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operator | String | 否 | 操作人 |

#### 正常场景

**请求示例**:
```http
POST /api/v1/configs/1/rollback/1?operator=admin
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

#### 业务说明

- 回退操作会**创建新版本**，不会覆盖现有历史记录
- 新版本的 `changeType` 为 `ROLLBACK`
- `changeReason` 记录为 "回退到版本 X"

#### 异常场景

| 场景 | 响应 |
|------|------|
| 配置不存在 | `{"code": 500, "message": "配置不存在"}` |
| 目标版本不存在 | `{"code": 500, "message": "目标版本不存在"}` |

---

## 三、通用历史记录接口

### 3.1 获取实体历史记录

**接口地址**: `GET /api/v1/history/{entityType}/{entityId}`

**功能说明**: 通用接口，获取任意实体的历史记录

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| entityType | String | 是 | 实体类型，如 ConfigItem |
| entityId | Long | 是 | 实体ID |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

#### 正常场景

**请求示例**:
```http
GET /api/v1/history/ConfigItem/1?page=1&pageSize=10
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "entityType": "ConfigItem",
                "entityId": 1,
                "versionNo": 1,
                "snapshot": "{\"configKey\":\"app.settings\",\"configName\":\"应用设置\"}",
                "changeType": "CREATE",
                "changeFields": [],
                "operator": "admin",
                "operatorIp": null,
                "changeReason": "创建",
                "createdAt": "2026-03-08 10:00:00"
            }
        ],
        "total": 1,
        "page": 1,
        "pageSize": 10
    }
}
```

---

### 3.2 获取指定版本

**接口地址**: `GET /api/v1/history/{entityType}/{entityId}/{versionId}`

**功能说明**: 获取实体的指定历史版本详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| entityType | String | 是 | 实体类型 |
| entityId | Long | 是 | 实体ID |
| versionId | Long | 是 | 历史记录ID |

#### 正常场景

**请求示例**:
```http
GET /api/v1/history/ConfigItem/1/1
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "entityType": "ConfigItem",
        "entityId": 1,
        "versionNo": 1,
        "snapshot": "{\"configKey\":\"app.settings\",\"configName\":\"应用设置\"}",
        "changeType": "CREATE",
        "changeFields": [],
        "operator": "admin",
        "operatorIp": null,
        "changeReason": "创建",
        "createdAt": "2026-03-08 10:00:00"
    }
}
```

---

### 3.3 通用版本对比

**接口地址**: `GET /api/v1/history/{entityType}/{entityId}/diff`

**功能说明**: 对比任意实体的两个历史版本

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| entityType | String | 是 | 实体类型 |
| entityId | Long | 是 | 实体ID |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| from | Long | 是 | 源版本ID |
| to | Long | 是 | 目标版本ID |

#### 正常场景

**请求示例**:
```http
GET /api/v1/history/ConfigItem/1/diff?from=1&to=2
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "version1": 1,
        "version2": 2,
        "snapshot1": "{\"theme\":\"light\"}",
        "snapshot2": "{\"theme\":\"dark\"}",
        "differences": {
            "theme": {
                "type": "MODIFY",
                "oldValue": "light",
                "newValue": "dark",
                "displayName": "theme"
            }
        }
    }
}
```

---

### 3.4 通用版本回退

**接口地址**: `POST /api/v1/history/{entityType}/{entityId}/rollback/{versionId}`

**功能说明**: 将任意实体回退到指定历史版本

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| entityType | String | 是 | 实体类型 |
| entityId | Long | 是 | 实体ID |
| versionId | Long | 是 | 目标版本ID |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operator | String | 否 | 操作人 |
| reason | String | 否 | 回退原因 |

#### 正常场景

**请求示例**:
```http
POST /api/v1/history/ConfigItem/1/rollback/1?operator=admin&reason=测试回退
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

## 四、前端集成示例

### 4.1 配置列表页面

```javascript
// 获取配置列表
async function getConfigList(page = 1, pageSize = 10, keyword = '') {
    const response = await fetch(
        `/api/v1/configs?page=${page}&pageSize=${pageSize}&keyword=${keyword}`
    );
    const result = await response.json();
    
    if (result.code === 200) {
        return result.data;
    } else {
        throw new Error(result.message);
    }
}
```

### 4.2 创建配置

```javascript
// 创建配置
async function createConfig(configData) {
    const response = await fetch('/api/v1/configs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            configKey: configData.key,
            configName: configData.name,
            configValue: JSON.stringify(configData.value),
            description: configData.description,
            operator: getCurrentUser()
        })
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
        return result.data;
    } else {
        // 异常处理
        if (result.code === 400) {
            alert('参数错误: ' + result.message);
        } else {
            alert('创建失败: ' + result.message);
        }
        throw new Error(result.message);
    }
}
```

### 4.3 查看历史记录

```javascript
// 获取历史记录
async function getHistory(configId, page = 1) {
    const response = await fetch(
        `/api/v1/configs/${configId}/history?page=${page}&pageSize=10`
    );
    const result = await response.json();
    
    if (result.code === 200) {
        return result.data.records;
    }
    return [];
}

// 版本对比
async function compareVersions(configId, version1, version2) {
    const response = await fetch(
        `/api/v1/configs/${configId}/diff?from=${version1}&to=${version2}`
    );
    const result = await response.json();
    
    if (result.code === 200) {
        // 渲染差异
        renderDiff(result.data.differences);
    }
}
```

### 4.4 版本回退

```javascript
// 回退到指定版本
async function rollbackToVersion(configId, versionId) {
    if (!confirm('确定要回退到此版本吗？')) {
        return;
    }
    
    const response = await fetch(
        `/api/v1/configs/${configId}/rollback/${versionId}?operator=${getCurrentUser()}`,
        { method: 'POST' }
    );
    const result = await response.json();
    
    if (result.code === 200) {
        alert('回退成功');
        refreshConfigList();
    } else {
        alert('回退失败: ' + result.message);
    }
}
```

---

## 五、错误码速查表

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | 正常处理响应数据 |
| 400 | 参数错误 | 检查请求参数格式 |
| 404 | 资源不存在 | 检查请求路径 |
| 500 | 服务器错误 | 联系后端排查 |

---

## 六、常见问题

### Q1: 配置值应该用什么格式？

**A**: 推荐使用JSON格式，便于版本对比。但也支持纯文本格式。

### Q2: 删除配置后还能恢复吗？

**A**: 可以。配置是软删除，历史记录保留。可以通过历史记录查看或恢复。

### Q3: 版本回退会覆盖历史吗？

**A**: 不会。回退会创建新版本，原有历史记录完整保留。

### Q4: 通用历史API和专用API有什么区别？

**A**: 
- 专用API：`/api/v1/configs/{id}/history` - 返回配置专用历史
- 通用API：`/api/v1/history/{entityType}/{entityId}` - 返回通用历史，支持任意实体

---

## 七、变更记录

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0.0 | 2026-03-08 | 初始版本，包含配置管理和通用历史记录API |