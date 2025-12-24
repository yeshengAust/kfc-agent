package com.yes.kfcaigc.controller;

import com.yes.kfcaigc.entity.SystemConfig;
import com.yes.kfcaigc.repository.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SystemConfigController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SystemConfigControllerTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private SystemConfigController systemConfigController;

    private List<SystemConfig> mockConfigs;

    @BeforeEach
    void setUp() {
        // 准备mock数据
        SystemConfig config1 = new SystemConfig();
        config1.setId(1L);
        config1.setConfigKey("qwen.api.key");
        config1.setConfigValue("sk-test1234567890");
        config1.setConfigType("string");
        config1.setDescription("千问API密钥");
        config1.setIsEncrypted(1);
        config1.setCreateTime(LocalDateTime.now());
        config1.setUpdateTime(LocalDateTime.now());

        SystemConfig config2 = new SystemConfig();
        config2.setId(2L);
        config2.setConfigKey("qwen.api.model-name");
        config2.setConfigValue("qwen-plus");
        config2.setConfigType("string");
        config2.setDescription("千问模型名称");
        config2.setIsEncrypted(0);
        config2.setCreateTime(LocalDateTime.now());
        config2.setUpdateTime(LocalDateTime.now());

        mockConfigs = Arrays.asList(config1, config2);
    }

    @Test
    void testListConfigs_成功返回配置列表() {
        // given
        when(systemConfigRepository.list()).thenReturn(mockConfigs);

        // when
        ResponseEntity<List<Map<String, Object>>> response = systemConfigController.listConfigs();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(systemConfigRepository, times(1)).list();
    }

    @Test
    void testListConfigs_敏感信息脱敏() {
        // given
        when(systemConfigRepository.list()).thenReturn(mockConfigs);

        // when
        ResponseEntity<List<Map<String, Object>>> response = systemConfigController.listConfigs();

        // then
        assertNotNull(response.getBody());
        Map<String, Object> encryptedConfig = response.getBody().get(0);
        String configValue = (String) encryptedConfig.get("configValue");
        
        // 验证脱敏：应该显示前8位+星号
        assertTrue(configValue.contains("********"));
        assertTrue(configValue.startsWith("sk-test1"));
    }

    @Test
    void testListConfigs_非敏感信息正常显示() {
        // given
        when(systemConfigRepository.list()).thenReturn(mockConfigs);

        // when
        ResponseEntity<List<Map<String, Object>>> response = systemConfigController.listConfigs();

        // then
        assertNotNull(response.getBody());
        Map<String, Object> normalConfig = response.getBody().get(1);
        String configValue = (String) normalConfig.get("configValue");
        
        // 非加密配置应该完整显示
        assertEquals("qwen-plus", configValue);
    }

    @Test
    void testListConfigs_异常处理() {
        // given
        when(systemConfigRepository.list()).thenThrow(new RuntimeException("数据库错误"));

        // when
        ResponseEntity<List<Map<String, Object>>> response = systemConfigController.listConfigs();

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testGetConfig_成功获取配置() {
        // given
        String configKey = "qwen.api.model-name";
        when(systemConfigRepository.getConfigValue(configKey)).thenReturn("qwen-plus");

        // when
        ResponseEntity<Map<String, Object>> response = systemConfigController.getConfig(configKey);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(configKey, response.getBody().get("configKey"));
        assertEquals("qwen-plus", response.getBody().get("configValue"));
    }

    @Test
    void testGetConfig_配置不存在() {
        // given
        String configKey = "non.existent.key";
        when(systemConfigRepository.getConfigValue(configKey)).thenReturn(null);

        // when
        ResponseEntity<Map<String, Object>> response = systemConfigController.getConfig(configKey);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetConfig_异常处理() {
        // given
        String configKey = "test.key";
        when(systemConfigRepository.getConfigValue(configKey)).thenThrow(new RuntimeException("数据库错误"));

        // when
        ResponseEntity<Map<String, Object>> response = systemConfigController.getConfig(configKey);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testUpdateConfig_成功更新() {
        // given
        String configKey = "qwen.api.model-name";
        Map<String, String> request = new HashMap<>();
        request.put("configValue", "qwen-turbo");
        
        when(systemConfigRepository.updateConfigValue(configKey, "qwen-turbo")).thenReturn(true);

        // when
        ResponseEntity<Map<String, Object>> response = systemConfigController.updateConfig(configKey, request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("配置更新成功", response.getBody().get("message"));
    }

    @Test
    void testUpdateConfig_配置不存在() {
        // given
        String configKey = "non.existent.key";
        Map<String, String> request = new HashMap<>();
        request.put("configValue", "newValue");
        
        when(systemConfigRepository.updateConfigValue(configKey, "newValue")).thenReturn(false);

        // when
        ResponseEntity<Map<String, Object>> response = systemConfigController.updateConfig(configKey, request);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("配置不存在", response.getBody().get("message"));
    }

    @Test
    void testUpdateConfig_配置值为null() {
        // given
        String configKey = "test.key";
        Map<String, String> request = new HashMap<>();
        request.put("configValue", null);

        // when
        ResponseEntity<Map<String, Object>> response = systemConfigController.updateConfig(configKey, request);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("配置值不能为空", response.getBody().get("message"));
    }

    @Test
    void testUpdateConfig_异常处理() {
        // given
        String configKey = "test.key";
        Map<String, String> request = new HashMap<>();
        request.put("configValue", "testValue");
        
        when(systemConfigRepository.updateConfigValue(any(), any())).thenThrow(new RuntimeException("数据库错误"));

        // when
        ResponseEntity<Map<String, Object>> response = systemConfigController.updateConfig(configKey, request);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains("更新失败"));
    }
}
