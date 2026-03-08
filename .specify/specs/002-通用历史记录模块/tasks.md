# 任务拆分：通用历史记录管理模块

## 阶段一：基础设施 (Phase 1: Infrastructure)

### Task 1.1: 全局异常处理
- **优先级**: P0
- **预估工时**: 1h
- **描述**: 创建全局异常处理器，统一响应格式
- **产出**:
  - `GlobalExceptionHandler.java`
  - `BusinessException.java`
  - `ErrorResponse.java`
- **验收**: 异常返回统一格式 `{code, message, timestamp}`

### Task 1.2: 历史记录注解定义
- **优先级**: P0
- **预估工时**: 0.5h
- **描述**: 定义 `@HistoryTrack` 和 `@HistoryField` 注解
- **产出**:
  - `HistoryTrack.java`
  - `HistoryField.java`
- **验收**: 注解可正确标记实体类和字段

### Task 1.3: 通用历史实体与Mapper
- **优先级**: P0
- **预估工时**: 1h
- **描述**: 创建 `GenericHistory` 实体类和对应的Mapper
- **产出**:
  - `GenericHistory.java` (实体)
  - `GenericHistoryMapper.java` (Mapper接口)
  - `GenericHistoryMapper.xml` (SQL映射)
  - 数据库表 `generic_history`
- **验收**: Mapper可正常CRUD

---

## 阶段二：核心服务 (Phase 2: Core Services)

### Task 2.1: 快照服务实现
- **优先级**: P0
- **预估工时**: 2h
- **描述**: 实现实体快照的生成和恢复
- **产出**:
  - `SnapshotService.java` (接口)
  - `SnapshotServiceImpl.java` (实现)
- **验收**: 
  - 可将任意实体序列化为JSON
  - 可从JSON恢复实体

### Task 2.2: 差异计算器实现
- **优先级**: P0
- **预估工时**: 2h
- **描述**: 计算两个版本之间的差异
- **产出**:
  - `DiffCalculator.java` (接口)
  - `DiffCalculatorImpl.java` (实现)
- **验收**: 
  - 正确识别 ADD/MODIFY/DELETE 变更
  - 支持JSON和非JSON格式

### Task 2.3: 历史记录器实现
- **优先级**: P0
- **预估工时**: 3h
- **描述**: 核心历史记录逻辑
- **产出**:
  - `HistoryRecorder.java` (接口)
  - `HistoryRecorderImpl.java` (实现)
- **验收**:
  - 可记录CREATE/UPDATE/DELETE操作
  - 版本号自动递增

---

## 阶段三：AOP集成 (Phase 3: AOP Integration)

### Task 3.1: 历史记录切面
- **优先级**: P1
- **预估工时**: 3h
- **描述**: 通过AOP自动记录实体变更
- **产出**:
  - `HistoryAspect.java`
- **验收**:
  - 自动拦截Service方法
  - 自动记录历史

### Task 3.2: 操作上下文
- **优先级**: P1
- **预估工时**: 1h
- **描述**: 管理操作人、操作IP等上下文信息
- **产出**:
  - `OperationContext.java`
  - `OperationContextHolder.java`
- **验收**: 可正确获取当前操作人和IP

---

## 阶段四：通用API (Phase 4: Generic API)

### Task 4.1: 通用历史服务
- **优先级**: P1
- **预估工时**: 2h
- **描述**: 提供通用的历史查询服务
- **产出**:
  - `HistoryService.java` (接口)
  - `HistoryServiceImpl.java` (实现)
- **验收**:
  - 支持历史列表查询
  - 支持版本对比
  - 支持版本回退

### Task 4.2: 通用历史控制器
- **优先级**: P1
- **预估工时**: 2h
- **描述**: 提供RESTful API
- **产出**:
  - `HistoryController.java`
  - `HistoryRecord.java` (DTO)
  - `DiffResult.java` (DTO)
- **验收**:
  - API文档完整
  - 接口测试通过

---

## 阶段五：迁移与测试 (Phase 5: Migration & Testing)

### Task 5.1: ConfigItem实体迁移
- **优先级**: P2
- **预估工时**: 1h
- **描述**: 为ConfigItem添加历史追踪注解
- **产出**:
  - 更新 `ConfigItem.java` 添加注解
- **验收**: 配置变更自动记录历史

### Task 5.2: 单元测试
- **优先级**: P2
- **预估工时**: 3h
- **描述**: 编写核心功能的单元测试
- **产出**:
  - `SnapshotServiceTest.java`
  - `DiffCalculatorTest.java`
  - `HistoryRecorderTest.java`
- **验收**: 测试覆盖率 > 70%

### Task 5.3: 集成测试
- **优先级**: P2
- **预估工时**: 2h
- **描述**: 编写API集成测试
- **产出**:
  - `HistoryControllerTest.java`
- **验收**: 所有API测试通过

---

## 任务依赖关系

```
Phase 1 (Infrastructure)
├── Task 1.1 (全局异常处理)
├── Task 1.2 (注解定义)
└── Task 1.3 (实体与Mapper)
        │
        ▼
Phase 2 (Core Services)
├── Task 2.1 (快照服务) ←──┐
├── Task 2.2 (差异计算)    │
└── Task 2.3 (历史记录器) ──┘
        │
        ▼
Phase 3 (AOP Integration)
├── Task 3.1 (历史切面)
└── Task 3.2 (操作上下文)
        │
        ▼
Phase 4 (Generic API)
├── Task 4.1 (通用服务)
└── Task 4.2 (通用控制器)
        │
        ▼
Phase 5 (Migration & Testing)
├── Task 5.1 (实体迁移)
├── Task 5.2 (单元测试)
└── Task 5.3 (集成测试)
```

---

## 工时估算

| 阶段 | 任务数 | 预估工时 |
|------|--------|----------|
| Phase 1 | 3 | 2.5h |
| Phase 2 | 3 | 7h |
| Phase 3 | 2 | 4h |
| Phase 4 | 2 | 4h |
| Phase 5 | 3 | 6h |
| **总计** | **13** | **23.5h** |

---

## 验收检查清单

- [ ] 全局异常处理正常工作
- [ ] 注解可正确标记实体
- [ ] 快照服务可序列化/反序列化
- [ ] 差异计算准确
- [ ] 历史记录自动保存
- [ ] 通用API可用
- [ ] ConfigItem迁移成功
- [ ] 测试覆盖率达标
- [ ] API文档完整