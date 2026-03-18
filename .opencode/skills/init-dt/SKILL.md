---
name: init-dt
description: 初始化项目DT测试框架配置，检测测试框架并生成DT_AGENTS.md项目经验架构
compatibility: opencode
metadata:
  language: java
  type: project-initialization
---

## 功能说明

初始化Java项目的DT（Development & Testing）配置，自动检测测试框架、Mock框架、断言库，并生成 `DT_AGENTS.md` 项目经验架构文件。

## 使用时机

- 新项目首次接入时
- 项目测试框架变更时
- 需要更新项目经验架构时

## 执行步骤

### 步骤1：检测项目构建系统

```bash
# 检测Maven或Gradle
if [ -f "pom.xml" ]; then echo "MAVEN"; fi
if [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then echo "GRADLE"; fi
```

### 步骤2：分析依赖文件

**Maven项目（pom.xml）：**
```bash
grep -E "(junit|testng|mockito|easymock|powermock|assertj|hamcrest)" pom.xml -A 2 -i
```

**Gradle项目（build.gradle）：**
```bash
grep -E "(junit|testng|mockito|easymock|powermock|assertj|hamcrest)" build.gradle* -A 2 -i
```

### 步骤3：检测测试框架

| 检测特征 | 框架 | 版本 |
|---------|------|------|
| `org.junit.jupiter.api.Test` | JUnit 5 | Jupiter |
| `org.junit.Test` | JUnit 4 | Vintage |
| `org.testng.annotations.Test` | TestNG | - |

### 步骤4：检测Mock框架

| 检测特征 | 框架 |
|---------|------|
| `org.mockito` | Mockito |
| `org.easymock` | EasyMock |
| `org.powermock` | PowerMock |

### 步骤5：生成DT_AGENTS.md

根据检测结果，在项目根目录生成 `DT_AGENTS.md` 文件。

---

## DT_AGENTS.md 文件格式

```markdown
# DT项目经验架构

**生成时间**: 2026-03-19
**构建系统**: Maven
**项目路径**: /path/to/project

---

## 测试框架配置

| 组件 | 框架 | 版本 | 检测来源 |
|------|------|------|---------|
| 测试框架 | JUnit 5 | 5.9.3 | pom.xml |
| Mock框架 | Mockito | 5.4.0 | pom.xml |
| 断言库 | AssertJ | 3.24.2 | pom.xml |

## 项目目录结构

```
src/
├── main/java/     # 业务代码
└── test/java/     # 测试代码
```

## 测试配置

### Maven配置（pom.xml）

```xml
<!-- 测试依赖 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.9.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

### 测试类模板

```java
@ExtendWith(MockitoExtension.class)
class {ClassName}Test {
    
    @Mock
    private Dependency dependency;
    
    @InjectMocks
    private {ClassName} target;
    
    @BeforeEach
    void setUp() {
        // 初始化
    }
    
    @Test
    void {methodName}_{scenario}_{expected}() {
        // Given
        when(dependency.method()).thenReturn(value);
        
        // When
        var result = target.method(input);
        
        // Then
        assertThat(result).isEqualTo(expected);
    }
}
```

## 项目特定经验

### 经验1: [待添加]

**类型**: 
**适用场景**: 
**代码示例**:
```java
// 待添加
```
**注意事项**:
- 

---

## 常用命令

```bash
# 编译测试
mvn test-compile

# 执行测试
mvn test

# 执行指定测试
mvn test -Dtest=ClassNameTest

# 生成覆盖率报告
mvn jacoco:report
```

## 注意事项

1. 本项目使用JUnit 5 + Mockito + AssertJ
2. 测试类命名规范：{ClassName}Test
3. 测试方法命名：被测方法_场景_预期结果

---

*此文件由 /init-dt 命令自动生成*
*更新时间: 2026-03-19*
```

---

## 生成位置

`DT_AGENTS.md` 文件生成在项目根目录，与 `pom.xml` 或 `build.gradle` 同级。

---

## 后续操作

生成 `DT_AGENTS.md` 后，用户可以：

1. **查看项目配置** - 了解当前测试框架
2. **添加项目经验** - 在文件中补充项目特定的Mock模式
3. **分享给团队** - 提交到Git，团队共享

---

## 检测示例

**执行：**
```
/init-dt
```

**输出：**
```
🔍 检测项目配置...
✅ 构建系统: Maven
✅ 测试框架: JUnit 5.9.3
✅ Mock框架: Mockito 5.4.0
✅ 断言库: AssertJ 3.24.2

📝 生成 DT_AGENTS.md...
✅ 已生成: DT_AGENTS.md

📋 项目配置:
   - JUnit 5 + Mockito + AssertJ
   - Spring Boot项目
   
💡 提示:
   - 查看 DT_AGENTS.md 了解项目配置
   - 使用 @dtagent 生成测试
```