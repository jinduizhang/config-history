# 前端项目构建指南

## 一、技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 前端框架 |
| TypeScript | 5+ | 类型安全 |
| Vite | 5+ | 构建工具 |
| Ant Design Vue | 4.x | UI组件库 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP请求 |
| Day.js | 1.x | 日期处理 |

## 二、项目初始化

### 2.1 创建项目

```bash
# 创建 Vue 3 + TypeScript 项目
npm create vite@latest frontend -- --template vue-ts

# 进入项目目录
cd frontend
```

### 2.2 安装依赖

```bash
# 安装基础依赖
npm install

# 安装 UI 框架和工具库
npm install ant-design-vue@4.x @ant-design/icons-vue vue-router@4 pinia axios dayjs
```

## 三、项目结构

```
frontend/
├── src/
│   ├── api/                    # API 接口封装
│   │   ├── request.ts          # Axios 封装
│   │   ├── config.ts           # 配置管理 API
│   │   └── history.ts          # 历史记录 API
│   ├── components/             # 公共组件
│   ├── views/                  # 页面视图
│   │   ├── ConfigList.vue      # 配置管理页面
│   │   └── HistoryList.vue     # 历史记录页面
│   ├── stores/                 # Pinia 状态管理
│   │   └── config.ts
│   ├── router/                 # 路由配置
│   │   └── index.ts
│   ├── types/                  # TypeScript 类型
│   │   └── index.ts
│   ├── App.vue                 # 根组件
│   └── main.ts                 # 入口文件
├── index.html
├── package.json
├── vite.config.ts              # Vite 配置
└── tsconfig.json               # TypeScript 配置
```

## 四、核心代码实现

### 4.1 TypeScript 类型定义

```typescript
// src/types/index.ts

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface ConfigItem {
  id: number
  configKey: string
  configName: string
  configValue: string
  description: string
  createdAt: string
  updatedAt: string
}

export interface ConfigRequest {
  configKey: string
  configName: string
  configValue: string
  description?: string
}

export interface HistoryRecord {
  id: number
  entityType: string
  entityId: number
  versionNo: number
  snapshot: string
  changeType: string
  changeFields: string[]
  operator: string
  operatorIp: string
  changeReason: string
  createdAt: string
}
```

### 4.2 Axios 封装

```typescript
// src/api/request.ts

import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import type { Result, PageResult } from '@/types'

const BASE_URL = 'http://localhost:8080/api/v1'

const instance: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export async function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.get<Result<T>>(url, config)
  return response.data.data
}

export async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.post<Result<T>>(url, data, config)
  return response.data.data
}

export async function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.put<Result<T>>(url, data, config)
  return response.data.data
}

export async function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.delete<Result<T>>(url, config)
  return response.data.data
}

export { instance as request }
export type { PageResult }
```

### 4.3 API 接口封装

```typescript
// src/api/config.ts

import { get, post, put, del } from './request'
import type { PageResult } from './request'
import type { ConfigItem, ConfigRequest, HistoryRecord, DiffResult } from '@/types'

const CONFIG_BASE = '/configs'

export const configApi = {
  list: (page = 1, pageSize = 10): Promise<PageResult<ConfigItem>> => 
    get(CONFIG_BASE, { params: { page, pageSize } }),

  get: (id: number): Promise<ConfigItem> => 
    get(`${CONFIG_BASE}/${id}`),

  create: (data: ConfigRequest): Promise<ConfigItem> => 
    post(CONFIG_BASE, data),

  update: (id: number, data: ConfigRequest): Promise<ConfigItem> => 
    put(`${CONFIG_BASE}/${id}`, data),

  delete: (id: number): Promise<void> => 
    del(`${CONFIG_BASE}/${id}`),

  getHistory: (id: number, page = 1, pageSize = 10): Promise<PageResult<HistoryRecord>> => 
    get(`${CONFIG_BASE}/${id}/history`, { params: { page, pageSize } }),

  getDiff: (id: number, from: number, to: number): Promise<DiffResult> => 
    get(`${CONFIG_BASE}/${id}/diff`, { params: { from, to } }),

  rollback: (id: number, versionId: number, operator?: string, reason?: string): Promise<void> => 
    post(`${CONFIG_BASE}/${id}/rollback/${versionId}`, null, { 
      params: { operator, reason } 
    })
}
```

### 4.4 路由配置

```typescript
// src/router/index.ts

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/config'
  },
  {
    path: '/config',
    name: 'Config',
    component: () => import('@/views/ConfigList.vue'),
    meta: { title: 'Config Management' }
  },
  {
    path: '/config/:id/history',
    name: 'ConfigHistory',
    component: () => import('@/views/HistoryList.vue'),
    meta: { title: 'History' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

### 4.5 Vite 配置

```typescript
// vite.config.ts

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 4.6 入口文件

```typescript
// src/main.ts

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import router from './router'
import App from './App.vue'
import 'ant-design-vue/dist/reset.css'
import './style.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

app.mount('#app')
```

## 五、后端配置

### 5.1 CORS 跨域配置

后端需要配置 CORS 允许前端跨域访问：

```java
// src/main/java/com/example/config/config/CorsConfig.java

package com.example.config.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

## 六、常见问题

### 6.1 CORS 403 Forbidden

**问题：** 前端请求后端 API 返回 403 Forbidden

**原因：** 跨域请求被浏览器阻止

**解决方案：** 在后端添加 CORS 配置（见 5.1）

### 6.2 TypeScript 类型导入错误

**问题：** `'XXX' is a type and must be imported using a type-only import`

**解决方案：** 使用 `import type` 导入类型

```typescript
// 错误
import { ConfigItem } from '@/types'

// 正确
import type { ConfigItem } from '@/types'
```

### 6.3 中文编码问题

**问题：** 请求体中文返回 500 错误

**解决方案：** 请求头添加 `charset=UTF-8`

```typescript
headers: {
  'Content-Type': 'application/json; charset=UTF-8'
}
```

## 七、启动命令

```bash
# 开发环境
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview
```

## 八、访问地址

| 环境 | 地址 |
|------|------|
| 前端开发 | http://localhost:3000 |
| 后端 API | http://localhost:8080 |
| API 文档 | http://localhost:8080/swagger-ui.html |

---

**文档版本：** v1.0  
**更新时间：** 2026-03-10  
**技术栈：** Vue 3.4 + TypeScript 5 + Ant Design Vue 4