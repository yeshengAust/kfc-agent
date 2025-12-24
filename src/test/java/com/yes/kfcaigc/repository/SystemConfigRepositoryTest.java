package com.yes.kfcaigc.repository;

import com.yes.kfcaigc.entity.SystemConfig;
import com.yes.kfcaigc.mapper.SystemConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @InjectMocks
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
        when(systemConfigMapper.selectOne(any())).thenReturn(mockConfig);

        // when
        String result = systemConfigRepository.getConfigValue("qwen.api.key");

        // then
        assertNotNull(result);
        assertEquals("sk-test123456", result);
        verify(systemConfigMapper, times(1)).selectOne(any());
    }

    @Test
    void testGetConfigValue_配置不存在返回null() {
        // given
        when(systemConfigMapper.selectOne(any())).thenReturn(null);

        // when
        String result = systemConfigRepository.getConfigValue("non.existent.key");

        // then
        assertNull(result);
    }

    @Test
    void testGetConfigValue_带默认值_配置存在() {
        // given
        when(systemConfigMapper.selectOne(any())).thenReturn(mockConfig);

        // when
        String result = systemConfigRepository.getConfigValue("qwen.api.key", "default-value");

        // then
        assertEquals("sk-test123456", result);
    }

    @Test
    void testGetConfigValue_带默认值_配置不存在返回默认值() {
        // given
        when(systemConfigMapper.selectOne(any())).thenReturn(null);

        // when
        String result = systemConfigRepository.getConfigValue("non.existent.key", "default-value");

        // then
        assertEquals("default-value", result);
    }

    @Test
    void testUpdateConfigValue_成功更新() {
        // given
        when(systemConfigMapper.selectOne(any())).thenReturn(mockConfig);
        when(systemConfigMapper.updateById(any())).thenReturn(1);

        // when
        boolean result = systemConfigRepository.updateConfigValue("qwen.api.key", "new-value");

        // then
        assertTrue(result);
        assertEquals("new-value", mockConfig.getConfigValue());
        verify(systemConfigMapper, times(1)).updateById(any());
    }

    @Test
    void testUpdateConfigValue_配置不存在返回false() {
        // given
        when(systemConfigMapper.selectOne(any())).thenReturn(null);

        // when
        boolean result = systemConfigRepository.updateConfigValue("non.existent.key", "new-value");

        // then
        assertFalse(result);
        verify(systemConfigMapper, never()).updateById(any());
    }

    @Test
    void testUpdateConfigValue_更新失败() {
        // given
        when(systemConfigMapper.selectOne(any())).thenReturn(mockConfig);
        when(systemConfigMapper.updateById(any())).thenReturn(0);

        // when
        boolean result = systemConfigRepository.updateConfigValue("qwen.api.key", "new-value");

        // then
        assertFalse(result);
    }
}
