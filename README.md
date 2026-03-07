# 配置历史管理模块

基于 Spring Boot + MyBatis-Plus + MySQL 实现的配置历史管理模块。

## 技术栈

- Java 21
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- SpringDoc OpenAPI (Swagger)

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

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/configs | 配置列表 |
| POST | /api/v1/configs | 新增配置 |
| PUT | /api/v1/configs/{id} | 更新配置 |
| DELETE | /api/v1/configs/{id} | 删除配置 |
| GET | /api/v1/configs/{id}/history | 历史记录 |
| GET | /api/v1/configs/{id}/diff?from=&to= | 版本对比 |
| POST | /api/v1/configs/{id}/rollback/{versionId} | 版本回退 |

## 项目结构

```
src/main/java/com/example/config/
├── ConfigHistoryApplication.java    # 启动类
├── controller/                      # REST控制器
├── service/                        # 业务逻辑
├── mapper/                         # 数据访问
├── entity/                         # 实体类
├── dto/                            # 数据传输对象
└── common/                         # 公共类
```
