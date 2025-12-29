package com.yes.kfcaigc.repository;

import com.yes.kfcaigc.entity.SystemConfig;
import com.yes.kfcaigc.mapper.SystemConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SystemConfigRepository 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SystemConfigRepositoryTest {

    @Mock
    private SystemConfigMapper systemConfigMapper;

    @Spy
    private SystemConfigRepository systemConfigRepository;

    private SystemConfig mockConfig;

    @BeforeEach
    void setUp() {
        mockConfig = new SystemConfig();
        mockConfig.setId(1L);
        mockConfig.setConfigKey("qwen.api.key");
        mockConfig.setConfigValue("sk-test123456");
        mockConfig.setConfigType("string");
        mockConfig.setDescription("千问API密钥");
        mockConfig.setIsEncrypted(1);
        mockConfig.setCreateTime(LocalDateTime.now());
        mockConfig.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testGetConfigValue_成功获取配置() {
        // given
        doReturn(mockConfig).when(systemConfigRepository).getOne(any());

        // when
        String result = systemConfigRepository.getConfigValue("qwen.api.key");

        // then
        assertNotNull(result);
        assertEquals("sk-test123456", result);
        verify(systemConfigRepository, times(1)).getOne(any());
    }

    @Test
    void testGetConfigValue_配置不存在返回null() {
        // given
        doReturn(null).when(systemConfigRepository).getOne(any());

        // when
        String result = systemConfigRepository.getConfigValue("non.existent.key");

        // then
        assertNull(result);
    }

    @Test
    void testGetConfigValue_带默认值_配置存在() {
        // given
        doReturn(mockConfig).when(systemConfigRepository).getOne(any());

        // when
        String result = systemConfigRepository.getConfigValue("qwen.api.key", "default-value");

        // then
        assertEquals("sk-test123456", result);
    }

    @Test
    void testGetConfigValue_带默认值_配置不存在返回默认值() {
        // given
        doReturn(null).when(systemConfigRepository).getOne(any());

        // when
        String result = systemConfigRepository.getConfigValue("non.existent.key", "default-value");

        // then
        assertEquals("default-value", result);
    }

    @Test
    void testUpdateConfigValue_成功更新() {
        // given
        doReturn(mockConfig).when(systemConfigRepository).getOne(any());
        doReturn(true).when(systemConfigRepository).updateById(any(SystemConfig.class));

        // when
        boolean result = systemConfigRepository.updateConfigValue("qwen.api.key", "new-value");

        // then
        assertTrue(result);
        assertEquals("new-value", mockConfig.getConfigValue());
        verify(systemConfigRepository, times(1)).updateById(any(SystemConfig.class));
    }

    @Test
    void testUpdateConfigValue_配置不存在返回false() {
        // given
        doReturn(null).when(systemConfigRepository).getOne(any());

        // when
        boolean result = systemConfigRepository.updateConfigValue("non.existent.key", "new-value");

        // then
        assertFalse(result);
        verify(systemConfigRepository, never()).updateById(any(SystemConfig.class));
    }

    @Test
    void testUpdateConfigValue_更新失败() {
        // given
        doReturn(mockConfig).when(systemConfigRepository).getOne(any());
        doReturn(false).when(systemConfigRepository).updateById(any(SystemConfig.class));

        // when
        boolean result = systemConfigRepository.updateConfigValue("qwen.api.key", "new-value");

        // then
        assertFalse(result);
    }
}
