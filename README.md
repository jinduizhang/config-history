# 配置历史管理模块

基于 Spring Boot + MyBatis-Plus + MySQL 实现的配置历史管理模块，支持通用实体历史记录管理。

## 技术栈

- Java 21
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- SpringDoc OpenAPI (Swagger)

## 功能特性

- **配置管理**: 配置项的增删改查
- **通用历史记录**: 支持任意实体的变更历史记录
- **版本对比**: 对比任意两个版本的差异
- **版本回退**: 支持按版本ID或时间点回退
- **时间查询**: 按时间范围筛选、排序查询历史记录

## 快速开始

### 1. 创建数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/你的数据库名
    username: 你的用户名
    password: 你的密码
```

### 3. 编译运行

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/config-history-1.0.0.jar
```

### 4. 访问

- API文档: http://localhost:8080/swagger-ui.html
- 健康检查: http://localhost:8080/actuator/health

## API接口

### 配置管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/configs | 配置列表 |
| POST | /api/v1/configs | 新增配置 |
| PUT | /api/v1/configs/{id} | 更新配置 |
| DELETE | /api/v1/configs/{id} | 删除配置 |
| GET | /api/v1/configs/{id}/history | 历史记录 |
| GET | /api/v1/configs/{id}/diff?from=&to= | 版本对比 |
| POST | /api/v1/configs/{id}/rollback/{versionId} | 版本回退 |

### 通用历史记录接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/history/{entityType}/{entityId} | 获取实体历史记录(分页) |
| GET | /api/v1/history/{entityType}/{entityId}/by-time | 按时间范围查询历史记录 |
| GET | /api/v1/history/{entityType}/{entityId}/top | 查询前N条历史记录 |
| GET | /api/v1/history/{entityType}/{entityId}/{versionId} | 获取指定版本详情 |
| GET | /api/v1/history/{entityType}/{entityId}/at-time | 获取指定时间点的版本 |
| GET | /api/v1/history/{entityType}/{entityId}/diff | 版本对比 |
| POST | /api/v1/history/{entityType}/{entityId}/rollback/{versionId} | 按版本回退 |
| POST | /api/v1/history/{entityType}/{entityId}/rollback-to-time | 按时间回退 |

### 接口参数说明

**按时间范围查询** `/by-time`:
- `startTime`: 开始时间 (格式: yyyy-MM-dd HH:mm:ss)
- `endTime`: 结束时间
- `sortBy`: 排序字段 (createdAt/versionNo)
- `sortOrder`: 排序方向 (asc/desc)

**查询前N条** `/top`:
- `limit`: 返回条数 (默认10)
- `sortOrder`: 排序方向 (asc/desc)

## 项目结构

```
src/main/java/com/example/config/
├── ConfigHistoryApplication.java    # 启动类
├── common/                          # 公共组件
├── controller/                      # REST控制器
├── history/                         # 通用历史记录模块
│   ├── aspect/                      # AOP切面
│   ├── controller/                  # 历史记录控制器
│   ├── dto/                         # 数据传输对象
│   ├── entity/                      # 实体类
│   ├── mapper/                      # 数据访问
│   └── service/                     # 业务逻辑
├── mapper/                          # 数据访问
└── service/                         # 业务逻辑
├── entity/                         # 实体类
├── dto/                            # 数据传输对象
└── common/                         # 公共类
```
