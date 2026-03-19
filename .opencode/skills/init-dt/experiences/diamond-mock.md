---
title: Diamond配置中心Mock
type: 二方件Mock
tags: [diamond, config, alibaba, 配置中心, com.alibaba.diamond]
source: template
---

## 适用场景

测试依赖 Diamond 配置中心获取配置值的类。

Diamond 是阿里内部常用的配置中心，测试时需要模拟配置返回。

## 代码示例

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import com.alibaba.diamond.DiamondClient;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {
    
    @Mock
    private DiamondClient diamondClient;
    
    @InjectMocks
    private ConfigService configService;
    
    @BeforeEach
    void setUp() {
        // 模拟配置项返回
        when(diamondClient.get("app.name"))
            .thenReturn("test-app");
        when(diamondClient.get("app.timeout"))
            .thenReturn("5000");
        when(diamondClient.get("app.maxRetry"))
            .thenReturn("3");
    }
    
    @Test
    void shouldGetConfigValue() {
        // given
        when(diamondClient.get("test.key"))
            .thenReturn("test-value");
        
        // when
        String result = configService.getConfig("test.key");
        
        // then
        assertThat(result).isEqualTo("test-value");
        verify(diamondClient).get("test.key");
    }
}
```

## 注意事项

- **⚠️ 配置缓存问题**：Diamond客户端有本地缓存，需要在 `@BeforeEach` 中重置配置
- **⚠️ 类型转换**：数值类型配置需要转为 String 返回（如 `"5000"` 而不是 `5000`）
- **✅ 独立设置**：每个测试方法独立设置配置值，避免相互影响

## 相关类/包

- `com.alibaba.diamond.*`
- `com.taobao.diamond.*`

## 常见配置项

| 配置项 | 示例值 | 说明 |
|--------|--------|------|
| app.name | "test-app" | 应用名称 |
| app.timeout | "5000" | 超时时间（毫秒）|
| app.maxRetry | "3" | 最大重试次数 |
| feature.switch | "true" | 功能开关 |