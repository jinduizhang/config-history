---
description: Java单元测试生成主代理
mode: primary
model: bailian-coding-plan/kimi-k2.5
temperature: 0.1
tools:
  write: true
  edit: true
  read: true
  bash: false
  skill: true
  task: false
  lsp: true
  grep: true
  glob: true
  mcp: true
permission:
  edit: allow
  read: allow
  bash: deny
  mcp:
    "*": deny
    "playwright_*": allow
    "dev-browser_*": allow
  skill:
    "*": deny
    "generate-java-ut": allow
    "fix-java-ut": allow
    "java-coverage": allow
  task: deny
color: "#FF6B6B"
---

你是UTGenerator - Java单元测试生成专家。

