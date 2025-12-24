package com.yes.kfcaigc.repository;

import com.yes.kfcaigc.entity.ModificationTypeRule;
import com.yes.kfcaigc.mapper.ModificationTypeRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ModificationTypeRuleRepository 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ModificationTypeRuleRepositoryTest {

    @Mock
    private ModificationTypeRuleMapper modificationTypeRuleMapper;

    @InjectMocks
    private ModificationTypeRuleRepository modificationTypeRuleRepository;

    private List<ModificationTypeRule> mockRules;

    @BeforeEach
    void setUp() {
        ModificationTypeRule rule1 = new ModificationTypeRule();
        rule1.setId(1L);
        rule1.setType("删除");
        rule1.setKeywords("删除,移除,去掉");
        rule1.setRuleDetail("删除产品或规则的操作");
        rule1.setPriority(100);

        ModificationTypeRule rule2 = new ModificationTypeRule();
        rule2.setId(2L);
        rule2.setType("新增");
        rule2.setKeywords("新增,添加,加入");
        rule2.setRuleDetail("新增产品或规则的操作");
        rule2.setPriority(90);

        ModificationTypeRule rule3 = new ModificationTypeRule();
        rule3.setId(3L);
        rule3.setType("修改");
        rule3.setKeywords("修改,替换,改为,加回");
        rule3.setRuleDetail("修改已有产品或规则的操作");
        rule3.setPriority(80);

        mockRules = Arrays.asList(rule1, rule2, rule3);
    }

    @Test
    void testGetAllRules_成功获取所有规则并按优先级降序() {
        // given
        when(modificationTypeRuleMapper.selectList(any())).thenReturn(mockRules);

        // when
        List<ModificationTypeRule> result = modificationTypeRuleRepository.getAllRules();

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        // 验证按优先级降序
        assertEquals("删除", result.get(0).getType());
        assertEquals(100, result.get(0).getPriority());
        assertEquals("新增", result.get(1).getType());
        assertEquals(90, result.get(1).getPriority());
        assertEquals("修改", result.get(2).getType());
        assertEquals(80, result.get(2).getPriority());
        verify(modificationTypeRuleMapper, times(1)).selectList(any());
    }

    @Test
    void testGetAllRules_空列表() {
        // given
        when(modificationTypeRuleMapper.selectList(any())).thenReturn(Collections.emptyList());

        // when
        List<ModificationTypeRule> result = modificationTypeRuleRepository.getAllRules();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByType_成功获取指定类型规则() {
        // given
        ModificationTypeRule expectedRule = mockRules.get(0);
        when(modificationTypeRuleMapper.selectOne(any())).thenReturn(expectedRule);

        // when
        ModificationTypeRule result = modificationTypeRuleRepository.getByType("删除");

        // then
        assertNotNull(result);
        assertEquals("删除", result.getType());
        assertEquals("删除,移除,去掉", result.getKeywords());
        verify(modificationTypeRuleMapper, times(1)).selectOne(any());
    }

    @Test
    void testGetByType_类型不存在返回null() {
        // given
        when(modificationTypeRuleMapper.selectOne(any())).thenReturn(null);

        // when
        ModificationTypeRule result = modificationTypeRuleRepository.getByType("不存在的类型");

        // then
        assertNull(result);
    }

    @Test
    void testGetByType_验证不同类型() {
        // 测试新增类型
        ModificationTypeRule newRule = mockRules.get(1);
        when(modificationTypeRuleMapper.selectOne(any())).thenReturn(newRule);
        
        ModificationTypeRule result = modificationTypeRuleRepository.getByType("新增");
        
        assertNotNull(result);
        assertEquals("新增", result.getType());
        assertEquals(90, result.getPriority());
    }
}
