# 配置历史管理模块 - 技术栈分析报告

## 1. 框架版本

| 框架/库 | 版本 | 说明 |
|---------|------|------|
| **Java** | 17 | 运行环境 |
| **Spring Boot** | 3.2.0 | 基础框架 |
| **MyBatis-Plus** | 3.5.7 | ORM框架 (Spring Boot 3专用) |
| **SpringDoc OpenAPI** | 2.3.0 | API文档生成 |
| **Lombok** | Managed | 代码简化工具 |
| **H2 Database** | Runtime | 内存数据库（开发测试） |
| **MySQL Connector** | Runtime | 生产数据库驱动 |

## 2. 数据库配置

### 2.1 当前配置 (H2 内存数据库)

| 配置项 | 值 |
|--------|-----|
| 驱动 | `org.h2.Driver` |
| URL | `jdbc:h2:mem:config_db;DB_CLOSE_DELAY=-1;MODE=MySQL` |
| 用户名 | `sa` |
| 密码 | 空 |
| H2 Console | 已启用，路径 `/h2-console` |

### 2.2 生产配置 (MySQL)

```yaml
driver-class-name: com.mysql.cj.jdbc.Driver
url: jdbc:mysql://localhost:3306/config_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
username: root
password: root
```

## 3. MyBatis-Plus 配置

| 配置项 | 值 |
|--------|-----|
| Mapper文件位置 | `classpath:mapper/*.xml` |
| 驼峰转换 | 开启 |
| 日志实现 | `StdOutImpl` (控制台输出SQL) |
| ID类型 | auto (自增) |
| 逻辑删除字段 | `deleted` |
| 逻辑删除值 | 1 (已删除) / 0 (未删除) |

## 4. API文档配置

| 配置项 | 值 |
|--------|-----|
| API Docs | 已启用，路径 `/v3/api-docs` |
| Swagger UI | 已启用，路径 `/swagger-ui.html` |

## 5. 构建工具

| 配置项 | 值 |
|--------|-----|
| 构建工具 | Maven |
| GroupId | `com.example` |
| ArtifactId | `config-history` |
| Version | `1.0.0` |
| Packaging | `jar` |

## 6. 访问地址

| 服务 | 地址 |
|------|------|
| 应用端口 | 8080 |
| API根路径 | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |

## 7. 待优化技术项

1. **添加缓存** - 集成 Redis 缓存
2. **添加消息队列** - 配置变更通知
3. **添加安全框架** - Spring Security 集成
4. **添加监控** - Spring Boot Actuator + Prometheus
5. **添加日志框架** - 集成 Logback/ELK
6. **添加测试框架** - JUnit5 + Mockito 完善测试