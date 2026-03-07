# Tasks - 配置历史管理任务清单

## 阶段一：基础设施

- [ ] T1.1 创建数据库表（config_item, config_history）
- [ ] T1.2 创建Spring Boot项目基础结构
- [ ] T1.3 配置MyBatis-Plus和数据库连接
- [ ] T1.4 创建统一响应类（Result, PageResult）

## 阶段二：实体与Mapper

- [ ] T2.1 创建ConfigItem实体类
- [ ] T2.2 创建ConfigHistory实体类
- [ ] T2.3 创建ConfigItemMapper接口
- [ ] T2.4 创建ConfigHistoryMapper接口
- [ ] T2.5 编写Mapper XML文件

## 阶段三：核心业务（Service层）

- [ ] T3.1 创建ConfigService接口
- [ ] T3.2 实现配置CRUD功能
- [ ] T3.3 实现历史记录保存逻辑（updateConfig方法）
- [ ] T3.4 实现版本回退逻辑（rollback方法）
- [ ] T3.5 实现JSON Diff对比逻辑

## 阶段四：API接口（Controller层）

- [ ] T4.1 实现配置管理API（GET/POST/PUT/DELETE /api/v1/configs）
- [ ] T4.2 实现历史记录API（GET /api/v1/configs/{id}/history）
- [ ] T4.3 实现版本详情API（GET /api/v1/configs/{id}/history/{versionId}）
- [ ] T4.4 实现版本回退API（POST /api/v1/configs/{id}/rollback/{versionId}）
- [ ] T4.5 实现版本对比API（GET /api/v1/configs/{id}/diff）
- [ ] T4.6 添加Swagger/OpenAPI注解

## 阶段五：DTO与参数校验

- [ ] T5.1 创建请求DTO（ConfigRequest）
- [ ] T5.2 创建响应DTO（ConfigResponse, DiffResponse）
- [ ] T5.3 添加参数校验注解
- [ ] T5.4 实现分页查询

## 阶段六：测试

- [ ] T6.1 编写Service层单元测试
- [ ] T6.2 编写Controller层集成测试（可选）
- [ ] T6.3 测试JSON Diff功能
- [ ] T6.4 测试回退功能

## 阶段七：文档与优化

- [ ] T7.1 生成完整的API文档
- [ ] T7.2 配置日志记录
- [ ] T7.3 性能优化（索引检查）

---

## 任务依赖关系

```
T1.1 -> T2.1 -> T2.2 -> T3.1 -> T4.1
                              -> T4.2 -> T4.3 -> T4.4 -> T4.5
                                         -> T4.6
T3.2 -> T3.3 -> T3.4 -> T3.5
T5.1 -> T5.2 -> T5.3 -> T5.4
T3.3 -> T6.1 -> T6.2 -> T6.3 -> T6.4
T4.5 -> T7.1 -> T7.2 -> T7.3
```

---

## 优先级

**P0（必须完成）**: T1.1, T2.1-T2.5, T3.1-T3.5, T4.1-T4.5, T6.1  
**P1（建议完成）**: T5.1-T5.4, T6.3-T6.4, T7.1  
**P2（可选）**: T4.6, T6.2, T7.2-T7.3
