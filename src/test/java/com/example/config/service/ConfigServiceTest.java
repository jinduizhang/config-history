package com.example.config.service;

import com.example.config.common.PageResult;
import com.example.config.dto.ConfigRequest;
import com.example.config.dto.ConfigResponse;
import com.example.config.dto.DiffResponse;
import com.example.config.dto.HistoryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConfigServiceTest {

    @Autowired
    private ConfigService configService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAndGetConfig() {
        ConfigRequest request = new ConfigRequest();
        request.setConfigKey("test.config.key");
        request.setConfigName("测试配置");
        request.setConfigValue("{\"name\": \"test\", \"value\": 123}");
        request.setOperator("testuser");
        request.setChangeReason("初始化测试");

        ConfigResponse created = configService.createConfig(request);
        assertNotNull(created.getId());
        assertEquals("test.config.key", created.getConfigKey());

        ConfigResponse retrieved = configService.getConfigById(created.getId());
        assertEquals(created.getConfigKey(), retrieved.getConfigKey());
    }

    @Test
    void testUpdateConfigCreatesHistory() {
        ConfigRequest createReq = new ConfigRequest();
        createReq.setConfigKey("history.test.key");
        createReq.setConfigName("历史测试");
        createReq.setConfigValue("{\"version\": 1}");
        createReq.setOperator("testuser");
        ConfigResponse created = configService.createConfig(createReq);

        ConfigRequest updateReq = new ConfigRequest();
        updateReq.setConfigValue("{\"version\": 2}");
        updateReq.setOperator("testuser");
        updateReq.setChangeReason("更新版本");
        configService.updateConfig(created.getId(), updateReq);

        PageResult<HistoryResponse> history = configService.getHistory(created.getId(), 1, 10);
        assertTrue(history.getTotal() >= 2);
    }

    @Test
    void testRollback() {
        ConfigRequest createReq = new ConfigRequest();
        createReq.setConfigKey("rollback.test.key");
        createReq.setConfigName("回退测试");
        createReq.setConfigValue("{\"version\": 1}");
        createReq.setOperator("testuser");
        ConfigResponse created = configService.createConfig(createReq);

        ConfigRequest updateReq = new ConfigRequest();
        updateReq.setConfigValue("{\"version\": 2}");
        updateReq.setOperator("testuser");
        configService.updateConfig(created.getId(), updateReq);

        PageResult<HistoryResponse> history = configService.getHistory(created.getId(), 1, 10);
        Long firstVersionId = history.getRecords().get(1).getId();

        configService.rollback(created.getId(), firstVersionId, "testuser");

        ConfigResponse current = configService.getConfigById(created.getId());
        assertTrue(current.getConfigValue().contains("version"));
    }

    @Test
    void testDiff() {
        ConfigRequest createReq = new ConfigRequest();
        createReq.setConfigKey("diff.test.key");
        createReq.setConfigName("对比测试");
        createReq.setConfigValue("{\"key1\": \"value1\"}");
        createReq.setOperator("testuser");
        ConfigResponse created = configService.createConfig(createReq);

        ConfigRequest updateReq = new ConfigRequest();
        updateReq.setConfigValue("{\"key1\": \"value2\", \"key2\": \"new\"}");
        updateReq.setOperator("testuser");
        configService.updateConfig(created.getId(), updateReq);

        PageResult<HistoryResponse> history = configService.getHistory(created.getId(), 1, 10);
        Long v1Id = history.getRecords().get(1).getId();
        Long v2Id = history.getRecords().get(0).getId();

        DiffResponse diff = configService.compareVersions(created.getId(), v1Id, v2Id);
        assertNotNull(diff.getDifferences());
    }
}
