# DT项目经验说明

本项目使用 `init-dt` skill 管理测试经验。

## 📁 目录结构

```
opencode/skills/init-dt/
├── SKILL.md              # init-dt主文件
└── experiences/          # 项目经验目录 ⭐
    ├── README.md         # 本文件
    ├── template.md       # 经验模板（复制使用）
    └── *.md              # 具体经验文件
```

## 🚀 如何使用

### 首次初始化

```bash
# 在项目根目录执行
/init-dt
```

skill会自动：
1. 检测项目配置
2. 交互式收集经验
3. 生成DT_AGENTS.md

### 添加新经验

**方式1：通过init-dt交互添加**

```bash
/init-dt
# 选择"添加新经验"
```

**方式2：手动添加**

```bash
# 1. 进入skill目录
cd .opencode/skills/init-dt

# 2. 复制模板
cp experiences/template.md experiences/my-experience.md

# 3. 编辑填写
vim experiences/my-experience.md

# 4. 保存生效
```

## 📝 经验类型

| 类型 | 说明 | 示例 |
|------|------|------|
| 二方件Mock | 公司内部库 | Diamond, Tair, MetaQ |
| 框架配置 | 测试框架 | SpringBootTest配置 |
| 工具类 | 工具Mock | DateUtils, JsonUtils |
| 数据库 | DAO测试 | JPA, MyBatis测试 |
| 外部服务 | RPC/HTTP | Dubbo, HTTP Client |
| 其他 | 其他经验 | 特殊配置 |

## 🎯 经验匹配

DTAgent根据以下信息自动匹配经验：

1. **tags** - 与代码import匹配
2. **type** - 经验类型
3. **文件名** - 关键词匹配

**示例：**
- 代码有 `import com.alibaba.diamond` → 匹配 `diamond-mock.md`
- 代码有 `@SpringBootTest` → 匹配 `spring-boot-test.md`

## 💡 最佳实践

1. **文件名清晰** - 使用描述性文件名，如 `diamond-mock.md`
2. **tags准确** - 包含包名、框架名、工具名
3. **代码完整** - 提供可运行的完整示例
4. **注意点详细** - 列出踩坑经验和注意事项

## 📦 分享经验

经验文件随skill一起提交到Git，团队成员共享：

```bash
# 添加新经验后提交
git add .opencode/skills/init-dt/experiences/
git commit -m "添加Diamond配置中心Mock经验"
git push
```

团队成员拉取后即可自动使用该经验。