---
name: init-dt
description: 初始化项目DT测试框架配置，自动检测测试框架、Mock框架、断言库，交互式收集用户项目经验，生成DT_AGENTS.md项目经验架构
compatibility: opencode
metadata:
  language: java
  type: project-initialization
  portable: true
---

# init-dt Skill

可移植的DT测试框架初始化工具。自动检测项目配置 + 交互式收集经验。

## 功能特性

- 🔍 **自动检测** - 测试框架、Mock框架、断言库、构建系统
- 💬 **交互收集** - 通过对话收集项目特定经验
- 📁 **经验管理** - 在skill内部管理项目经验文件
- 📝 **生成配置** - 输出DT_AGENTS.md项目经验架构

## 使用方式

```bash
# 初始化当前项目
/init-dt

# 或询问方式
"帮我初始化项目的DT配置"
```

## 执行流程

```
/init-dt
    │
    ├── 1. 检测项目配置
    │   ├── 构建系统（Maven/Gradle）
    │   ├── 测试框架（JUnit 4/5, TestNG）
    │   ├── Mock框架（Mockito, EasyMock等）
    │   ├── 断言库（AssertJ, Hamcrest等）
    │   ├── 项目特性（Spring Boot, MyBatis等）
    │   └── IDEA配置（.idea/workspace.xml中的Maven参数）
    │
    ├── 2. 扫描已有经验
    │   └── 读取 experiences/*.md
    │
    ├── 3. 交互式收集经验（可选）
    │   ├── 询问：是否有二方件需要Mock？
    │   ├── 询问：是否有自定义Mock框架？
    │   └── 询问：是否有其他特殊配置？
    │
    ├── 4. 生成经验文件（如需）
    │   └── 在 experiences/ 目录创建
    │
    └── 5. 生成DT_AGENTS.md
        └── 项目根目录
```

---

## 步骤1：自动检测项目配置

### 1.1 检测构建系统

```bash
# 检测Maven
if [ -f "pom.xml" ]; then
    BUILD_SYSTEM="Maven"
    BUILD_FILE="pom.xml"
fi

# 检测Gradle
if [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then
    BUILD_SYSTEM="Gradle"
    BUILD_FILE="build.gradle*"
fi
```

### 1.2 解析测试框架

**Maven (pom.xml)：**
```bash
# JUnit 5 (Jupiter)
grep -q "junit-jupiter" pom.xml && TEST_FRAMEWORK="JUnit 5"

# JUnit 4
grep -q "junit.*4" pom.xml && TEST_FRAMEWORK="JUnit 4"

# TestNG
grep -q "testng" pom.xml && TEST_FRAMEWORK="TestNG"
```

**版本提取：**
```bash
grep -A1 "junit-jupiter" pom.xml | grep "<version>" | sed 's/.*<version>\(.*\)<\/version>.*/\1/'
```

### 1.3 解析Mock框架

```bash
# Mockito
if grep -q "mockito" pom.xml; then
    MOCK_FRAMEWORK="Mockito"
    MOCK_VERSION=$(grep -A1 "mockito" pom.xml | grep "<version>" | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
fi

# EasyMock
if grep -q "easymock" pom.xml; then
    MOCK_FRAMEWORK="EasyMock"
fi

# PowerMock
if grep -q "powermock" pom.xml; then
    MOCK_FRAMEWORK="PowerMock"
fi
```

### 1.4 解析断言库

```bash
# AssertJ
grep -q "assertj" pom.xml && ASSERTION_LIB="AssertJ"

# Hamcrest
grep -q "hamcrest" pom.xml && ASSERTION_LIB="Hamcrest"
```

### 1.5 检测项目特性

```bash
# Spring Boot
grep -q "spring-boot" pom.xml && HAS_SPRING_BOOT=true

# MyBatis
grep -q "mybatis" pom.xml && HAS_MYBATIS=true

# Spring Cloud
grep -q "spring-cloud" pom.xml && HAS_SPRING_CLOUD=true

# 检查自定义settings.xml
find . -maxdepth 2 -name "settings.xml" -not -path "*/target/*" | head -1
```

### 1.6 检测测试目录结构

```bash
# 标准Maven结构
if [ -d "src/test/java" ]; then
    TEST_DIR="src/test/java"
fi

# 查找现有测试文件
find src/test -name "*Test.java" -type f 2>/dev/null | head -5
```

### 1.7 检测IDEA Maven配置

从 `.idea/workspace.xml` 中读取Maven运行配置参数。

**检查文件存在：**
```bash
if [ -f ".idea/workspace.xml" ]; then
    echo "✅ 发现IDEA配置"
    HAS_IDEA_CONFIG=true
fi
```

**提取Maven运行配置：**

workspace.xml 中Maven配置通常位于：
```xml
<component name="RunManager">
  <configuration name="test [test]" type="MavenRunConfiguration" ...>
    <option name="myRunnerParameters">
      <MavenRunnerParameters>
        <option name="goals">
          <list>
            <option value="test" />
          </list>
        </option>
        <option name="profilesMap">
          <map>
            <entry key="dev" value="true" />
          </map>
        </option>
      </MavenRunnerParameters>
    </option>
    <option name="myRunnerSettings">
      <MavenRunnerSettings>
        <option name="vmOptions" value="-Xmx2g -XX:+HeapDumpOnOutOfMemoryError" />
        <option name="mavenProperties">
          <map>
            <entry key="maven.wagon.http.ssl.insecure" value="true" />
            <entry key="skipTests" value="false" />
          </map>
        </option>
      </MavenRunnerSettings>
    </option>
  </configuration>
</component>
```

**解析脚本：**

```bash
#!/bin/bash

WORKSPACE_XML=".idea/workspace.xml"

if [ ! -f "$WORKSPACE_XML" ]; then
    echo "⚠️ 未找到 .idea/workspace.xml"
    exit 0
fi

echo "🔍 正在解析IDEA Maven配置..."

# 提取Maven Goals
echo "📋 Maven Goals:"
grep -oP '(?<=<option value=")[^"]*(?=")' "$WORKSPACE_XML" | grep -E "^(test|clean|package|install|verify)$" | sort -u

# 提取VM Options
VM_OPTIONS=$(grep -oP '(?<=<option name="vmOptions" value=")[^"]*(?=")' "$WORKSPACE_XML" | head -1)
if [ -n "$VM_OPTIONS" ]; then
    echo "🔧 VM Options: $VM_OPTIONS"
fi

# 提取Maven Properties
echo "🔧 Maven Properties:"
grep -A100 '<option name="mavenProperties">' "$WORKSPACE_XML" | grep -oP '(?<=<entry key=")[^"]*(?=")' | while read key; do
    value=$(grep -A1 "<entry key=\"$key\"" "$WORKSPACE_XML" | grep -oP '(?<=value=")[^"]*(?=")' | head -1)
    echo "  $key=$value"
done

# 提取Profiles
echo "📦 Active Profiles:"
grep -A50 '<option name="profilesMap">' "$WORKSPACE_XML" | grep -oP '(?<=<entry key=")[^"]*(?=")' | while read profile; do
    echo "  - $profile"
done

# 提取环境变量
echo "🌍 Environment Variables:"
grep -A50 '<option name="envMap">' "$WORKSPACE_XML" | grep -oP '(?<=<entry key=")[^"]*(?=")' | while read env; do
    value=$(grep -A1 "<entry key=\"$env\"" "$WORKSPACE_XML" | grep -oP '(?<=value=")[^"]*(?=")' | head -1)
    echo "  $env=$value"
done
```

**提取的关键参数：**

| 参数类型 | XML路径 | 用途 |
|---------|---------|------|
| VM Options | `//option[@name='vmOptions']/@value` | JVM参数（-Xmx等） |
| Maven Properties | `//option[@name='mavenProperties']/map/entry` | Maven系统属性 |
| Profiles | `//option[@name='profilesMap']/map/entry` | 激活的profile |
| Environment | `//option[@name='envMap']/map/entry` | 环境变量 |
| Goals | `//option[@name='goals']/list/option/@value` | 运行目标 |

**常见提取的参数：**

```bash
# 1. JVM内存参数
-Xmx2g
-Xms1g
-XX:+HeapDumpOnOutOfMemoryError

# 2. Maven属性（内网环境常用）
maven.wagon.http.ssl.insecure=true
maven.wagon.http.ssl.allowall=true
maven.wagon.http.ssl.ignore.validity.dates=true

# 3. 跳过测试相关
skipTests=false
maven.test.skip=false

# 4. 编码设置
project.build.sourceEncoding=UTF-8
project.reporting.outputEncoding=UTF-8

# 5. 其他常用
failIfNoTests=false
test.failure.ignore=true
```

**整合到DT_AGENTS.md的参数：**

```bash
# 从IDEA配置构建mvn test命令
build_mvn_command() {
    local cmd="mvn test"
    
    # 添加VM Options
    if [ -n "$VM_OPTIONS" ]; then
        cmd="$cmd $VM_OPTIONS"
    fi
    
    # 添加Maven Properties
    for prop in "${MAVEN_PROPERTIES[@]}"; do
        cmd="$cmd -D$prop"
    done
    
    # 添加Profiles
    if [ -n "$PROFILES" ]; then
        cmd="$cmd -P$PROFILES"
    fi
    
    echo "$cmd"
}
```

---

## 步骤2：扫描已有经验

```bash
# 获取skill所在目录
SKILL_DIR="$(dirname "$0")"
EXPERIENCES_DIR="$SKILL_DIR/experiences"

# 扫描经验文件
if [ -d "$EXPERIENCES_DIR" ]; then
    echo "📚 发现经验文件："
    ls -1 "$EXPERIENCES_DIR"/*.md 2>/dev/null | while read f; do
        echo "  - $(basename $f)"
    done
fi
```

---

## 步骤3：交互式收集经验

### 3.1 询问二方件Mock经验

```
📝 项目经验收集

1️⃣ 是否有二方件（公司内部库）需要Mock？
   例如：Diamond配置中心、Tair缓存、MetaQ等
   
   [是] / [否]
   
   如果选择"是"，继续询问：
   - 二方件名称：
   - 主要包名：
   - Mock方式：
```

### 3.2 询问自定义Mock框架

```
2️⃣ 是否有自定义Mock框架或工具？
   例如：自研Mock框架、特殊测试工具等
   
   [是] / [否]
   
   如果选择"是"，询问：
   - 框架名称：
   - 使用方式：
   - 示例代码：
```

### 3.3 询问特殊测试配置

```
3️⃣ 是否有特殊的测试配置或约定？
   例如：
   - 必须使用@SpringBootTest而不是@ExtendWith
   - 特定的测试数据库配置
   - 特殊的测试环境要求
   
   [是] / [否]
```

### 3.4 询问mvn test参数

```
4️⃣ 配置mvn test参数

   🔍 从IDEA配置自动检测到：
   {如果有 .idea/workspace.xml 中的配置，显示：}
   • VM Options: -Xmx2g -XX:+HeapDumpOnOutOfMemoryError
   • Maven Properties: maven.wagon.http.ssl.insecure=true
   • Profiles: dev, test
   • Environment: JAVA_HOME=/path/to/java
   
   {如果未检测到或需要修改：}
   
   请选择配置方式：
   [1] 使用IDEA检测到的配置 ⭐（推荐）
   [2] 使用默认参数
   [3] 内网环境（需要自定义settings）
   [4] 跳过SSL验证
   [5] 自定义输入
   
   如果选择[1]，自动应用：
   mvn test -Xmx2g -Dmaven.wagon.http.ssl.insecure=true -Pdev
   
   如果选择[3]或[4]或[5]，询问：
   - settings路径：
   - 是否跳过SSL：
   - 其他参数：
```

**从IDEA配置提取的参数优先级：**

1. **自动检测**（最高优先级）- 从`.idea/workspace.xml`读取
2. **自定义settings** - 用户指定的settings.xml
3. **手动输入** - 用户输入的其他参数

**参数合并逻辑：**

```bash
merge_mvn_params() {
    local params=""
    
    # 1. 添加IDEA配置中的VM Options
    if [ -n "$IDEA_VM_OPTIONS" ]; then
        params="$params $IDEA_VM_OPTIONS"
    fi
    
    # 2. 添加IDEA配置中的Maven Properties
    if [ ${#IDEA_MAVEN_PROPS[@]} -gt 0 ]; then
        for prop in "${IDEA_MAVEN_PROPS[@]}"; do
            params="$params -D$prop"
        done
    fi
    
    # 3. 添加IDEA配置中的Profiles
    if [ -n "$IDEA_PROFILES" ]; then
        params="$params -P$IDEA_PROFILES"
    fi
    
    # 4. 添加用户自定义settings
    if [ -n "$CUSTOM_SETTINGS" ]; then
        params="$params -s $CUSTOM_SETTINGS"
    fi
    
    # 5. 添加用户手动输入的其他参数
    if [ -n "$USER_PARAMS" ]; then
        params="$params $USER_PARAMS"
    fi
    
    echo "$params"
}
```

---

## 步骤4：生成经验文件

### 4.1 二方件经验模板

```markdown
---
title: {二方件名称}Mock
type: 二方件Mock
tags: [{tag1}, {tag2}, {tag3}]
source: user-input
---

## 适用场景

测试依赖 {二方件名称} 的类。

## 代码示例

```{用户提供的示例代码}```

## 注意事项

- {用户提供的注意点}
```

### 4.2 自定义Mock框架经验

```markdown
---
title: {框架名称}使用指南
type: 框架配置
tags: [{tag1}, {tag2}]
source: user-input
---

## 框架说明

{框架描述}

## 使用方式

{使用方式}

## 代码示例

```{示例代码}```
```

### 4.3 保存路径

```
{skill-dir}/
├── SKILL.md              # 本文件
└── experiences/          # 经验目录
    ├── README.md         # 经验说明
    ├── template.md       # 经验模板
    ├── {project-name}-1.md   # 用户输入的经验1
    ├── {project-name}-2.md   # 用户输入的经验2
    └── ...
```

---

## 步骤5：生成DT_AGENTS.md

### 5.1 文件内容模板

```markdown
# DT项目经验架构

**生成时间**: {timestamp}
**构建系统**: {build-system}
**项目路径**: {project-path}
**生成工具**: init-dt skill

---

## 测试框架配置

| 组件 | 框架 | 版本 | 检测来源 |
|------|------|------|---------|
| 构建系统 | {build-system} | - | {build-file} |
| 测试框架 | {test-framework} | {version} | {build-file} |
| Mock框架 | {mock-framework} | {version} | {build-file} |
| 断言库 | {assertion-library} | {version} | {build-file} |

### 项目特性

| 特性 | 状态 |
|------|------|
| Spring Boot | {✅/❌} |
| MyBatis | {✅/❌} |
| Spring Cloud | {✅/❌} |
| 自定义settings | {✅/❌} |

---

## 项目目录结构

```
src/
├── main/java/          # 业务代码
│   └── {package}/
└── test/java/          # 测试代码
    └── {package}/
```

## 测试配置

### 依赖配置

```{build-system} 配置（{build-file}）：```

```xml
<!-- 测试依赖示例 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```

### 测试类模板

```java
{根据检测结果生成对应模板}
```

---

## 项目特定经验

{列出收集到的所有经验}

### 经验1: {经验标题}

**类型**: {type}
**来源**: experiences/{filename}
**适用场景**: {场景描述}

```java
{代码示例}
```

**注意事项**:
- {注意点1}
- {注意点2}

---

## mvn test 参数配置

### 基础命令

```bash
# 执行所有测试
mvn test

# 执行指定测试类
mvn test -Dtest=ClassNameTest
```

### IDEA配置来源

{如果从 .idea/workspace.xml 检测到配置：}

**自动检测的IDEA Maven配置：**

| 配置项 | 值 | 来源 |
|--------|-----|------|
| VM Options | `{vm-options}` | .idea/workspace.xml |
| Maven Properties | `{maven-properties}` | .idea/workspace.xml |
| Active Profiles | `{profiles}` | .idea/workspace.xml |
| Environment | `{env-vars}` | .idea/workspace.xml |

**提取的原始配置：**

```xml
<!-- 来自 .idea/workspace.xml -->
<component name="RunManager">
  <configuration name="test" type="MavenRunConfiguration">
    <option name="vmOptions" value="{vm-options}"/>
    <option name="mavenProperties">
      <map>
        {提取的properties}
      </map>
    </option>
  </configuration>
</component>
```

### 项目推荐配置

**完整命令：**

```bash
mvn test {vm-options} {maven-properties} {profiles} {custom-settings}
```

**实际命令：**

```bash
{根据所有参数生成的实际命令}
```

### 常用参数

| 参数 | 用途 | 项目配置 |
|------|------|---------|
| `-s` | 自定义settings | {settings-path} |
| `-Dmaven.wagon.http.ssl.insecure` | 跳过SSL | {true/false} |
| `-P` | Profile | {profile-name} |
| `-Xmx` | 堆内存 | {heap-size} |
| `-Xms` | 初始堆内存 | {initial-heap} |

### 参数优先级

1. **IDEA配置**（自动检测）
2. **用户自定义**（交互输入）
3. **默认值**

---

## 快速开始

### 生成测试

```bash
# 使用DTAgent生成测试
@dtagent 生成 OrderService 的测试
```

### 执行测试

```bash
# 编译测试
mvn test-compile

# 执行测试
mvn test {推荐参数}

# 执行指定测试
mvn test -Dtest=OrderServiceTest {推荐参数}
```

---

## 经验管理

### 经验文件位置

```
.opencode/skills/init-dt/experiences/
├── README.md
├── template.md
└── {项目经验文件}.md
```

### 添加新经验

```bash
# 1. 复制模板
cp experiences/template.md experiences/my-experience.md

# 2. 编辑填写
# 3. 保存即可生效
```

### 经验匹配规则

DTAgent根据以下信息自动匹配经验：
1. **tags** - 与代码import匹配
2. **type** - 经验类型分类
3. **文件名** - 关键词匹配

---

## 注意事项

1. 本项目使用 {test-framework} + {mock-framework}
2. 测试类命名规范：{ClassName}Test
3. 测试方法命名：被测方法_场景_预期结果
4. 特殊配置：{根据收集的经验填写}

---

*此文件由 /init-dt 命令自动生成*
*更新时间: {timestamp}*
*经验数量: {count} 条*
```

---

## 交互示例

### 完整执行流程（检测到IDEA配置）

```
$ /init-dt

🔍 正在检测项目配置...

✅ 构建系统: Maven
✅ 测试框架: JUnit Jupiter 5.9.3
✅ Mock框架: Mockito 5.4.0
✅ 断言库: AssertJ 3.24.2
✅ Spring Boot: 是

🔍 检测IDEA配置...
✅ 发现 .idea/workspace.xml
✅ VM Options: -Xmx2g -XX:+HeapDumpOnOutOfMemoryError
✅ Maven Properties: maven.wagon.http.ssl.insecure=true, maven.test.skip=false
✅ Active Profiles: dev, test
✅ Environment: MAVEN_OPTS=-Xms1g

📚 扫描经验文件...
   发现 0 条经验

💬 开始收集项目经验...

1️⃣ 是否有二方件需要Mock？
   [是] / [否] > 是
   
   📦 二方件名称: Diamond配置中心
   📦 主要包名: com.alibaba.diamond
   📦 Mock方式: @Mock DiamondClient
   
   是否添加另一个二方件？ [是] / [否] > 否

2️⃣ 是否有自定义Mock框架？ [否]

3️⃣ 是否有特殊测试配置？ [否]

4️⃣ 配置mvn test参数：

   🔍 从IDEA配置自动检测到：
   • VM Options: -Xmx2g -XX:+HeapDumpOnOutOfMemoryError
   • Maven Properties: maven.wagon.http.ssl.insecure=true
   • Profiles: dev, test
   
   请选择配置方式：
   [1] 使用IDEA检测到的配置 ⭐（推荐）
   [2] 使用默认参数
   [3] 内网环境（需要自定义settings）
   [4] 跳过SSL验证
   [5] 自定义输入
   > 1
   
   ✅ 已应用IDEA配置：
   mvn test -Xmx2g -XX:+HeapDumpOnOutOfMemoryError \
            -Dmaven.wagon.http.ssl.insecure=true \
            -Pdev,test

📝 生成经验文件...
   ✅ experiences/diamond-mock.md

📝 生成 DT_AGENTS.md...
   ✅ /path/to/project/DT_AGENTS.md

📋 配置摘要:
   - 测试框架: JUnit 5 + Mockito + AssertJ
   - 项目特性: Spring Boot
   - 新增经验: 1 条（Diamond配置中心）
   - IDEA配置: 已自动检测并应用
   - mvn参数: -Xmx2g -Dmaven.wagon.http.ssl.insecure=true -Pdev,test

💡 下一步:
   1. 查看 DT_AGENTS.md 了解完整配置
   2. 使用 @dtagent 生成测试
   3. 执行 mvn test 验证（自动应用IDEA配置）
```

### 完整执行流程（未检测到IDEA配置）

```
$ /init-dt

🔍 正在检测项目配置...

✅ 构建系统: Maven
✅ 测试框架: JUnit Jupiter 5.9.3
✅ Mock框架: Mockito 5.4.0
✅ 断言库: AssertJ 3.24.2
✅ Spring Boot: 是

🔍 检测IDEA配置...
⚠️ 未找到 .idea/workspace.xml

📚 扫描经验文件...
   发现 0 条经验

💬 开始收集项目经验...

1️⃣ 是否有二方件需要Mock？ [否]
2️⃣ 是否有自定义Mock框架？ [否]
3️⃣ 是否有特殊测试配置？ [否]

4️⃣ 配置mvn test参数：

   🔍 未检测到IDEA配置
   
   请选择配置方式：
   [1] 使用默认参数
   [2] 内网环境（需要自定义settings）
   [3] 跳过SSL验证
   [4] 自定义输入
   > 2
   
   📁 settings路径: ./settings.xml
   🔒 跳过SSL: 是

📝 生成 DT_AGENTS.md...
   ✅ /path/to/project/DT_AGENTS.md

📋 配置摘要:
   - 测试框架: JUnit 5 + Mockito + AssertJ
   - IDEA配置: 未检测（建议打开项目让IDEA生成配置）
   - mvn参数: -s ./settings.xml -Dmaven.wagon.http.ssl.insecure=true

💡 提示:
   • 在IDEA中运行一次Maven测试，配置会自动保存到.idea/workspace.xml
   • 下次运行 /init-dt 可自动读取这些配置
   [3] 跳过SSL
   > 2
   
   📁 settings路径: ./settings.xml
   🔒 跳过SSL: 是

📝 生成经验文件...
   ✅ experiences/diamond-mock.md

📝 生成 DT_AGENTS.md...
   ✅ /path/to/project/DT_AGENTS.md

📋 配置摘要:
   - 测试框架: JUnit 5 + Mockito + AssertJ
   - 项目特性: Spring Boot
   - 新增经验: 1 条（Diamond配置中心）
   - mvn参数: -s ./settings.xml -Dmaven.wagon.http.ssl.insecure=true

💡 下一步:
   1. 查看 DT_AGENTS.md 了解完整配置
   2. 使用 @dtagent 生成测试
   3. 执行 mvn test -s ./settings.xml 验证
```

---

## 输出文件说明

| 文件 | 位置 | 用途 | 来源 |
|------|------|------|------|
| DT_AGENTS.md | 项目根目录 | 项目配置文档 | 自动生成 |
| experiences/*.md | skill内部 | 项目经验库 | 用户输入/经验模板 |
| .idea/workspace.xml | 项目根目录/.idea/ | IDEA配置 | 自动检测（可选） |

### 配置来源优先级

```
mvn test 参数配置
    │
    ├── 1️⃣ IDEA配置（最高优先级）
    │   └── 从 .idea/workspace.xml 读取
    │       ├── VM Options (-Xmx, -Xms等)
    │       ├── Maven Properties (-D参数)
    │       ├── Profiles (-P参数)
    │       └── Environment Variables
    │
    ├── 2️⃣ 用户自定义
    │   └── 通过交互式输入
    │       ├── 自定义settings.xml路径
    │       ├── 额外VM参数
    │       └── 其他Maven参数
    │
    └── 3️⃣ 默认值（最低优先级）
        └── mvn test
```

### IDEA配置检测说明

**自动检测条件：**
- 项目根目录存在 `.idea/workspace.xml`
- 文件中包含Maven运行配置

**如何生成IDEA配置：**
1. 在IntelliJ IDEA中打开项目
2. 打开Maven工具窗口
3. 右键点击 `test` 目标 → `Create 'test'...`
4. 配置VM Options、Parameters等
5. 运行一次，配置自动保存到workspace.xml

**手动创建workspace.xml：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="RunManager">
    <configuration name="test" type="MavenRunConfiguration">
      <option name="myRunnerParameters">
        <MavenRunnerParameters>
          <option name="goals">
            <list>
              <option value="test" />
            </list>
          </option>
        </MavenRunnerParameters>
      </option>
      <option name="myRunnerSettings">
        <MavenRunnerSettings>
          <option name="vmOptions" value="-Xmx2g" />
          <option name="mavenProperties">
            <map>
              <entry key="maven.wagon.http.ssl.insecure" value="true" />
            </map>
          </option>
        </MavenRunnerSettings>
      </option>
    </configuration>
  </component>
</project>
```

---

## 与其他Skill协作

```
init-dt (本skill)
    │
    ├── 生成 DT_AGENTS.md ──→ generate-java-ut (读取配置)
    │
    └── 管理 experiences/ ──→ 所有UT生成skill (使用经验)
```

---

## 可移植性说明

本skill设计为可移植到任意项目：

1. **无硬编码** - 所有配置通过检测或用户输入获得
2. **经验复用** - 经验文件随skill迁移
3. **独立运行** - 不依赖特定项目结构
4. **增量更新** - 可重复执行，补充新经验