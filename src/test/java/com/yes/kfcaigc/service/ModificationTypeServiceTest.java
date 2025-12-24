package com.yes.kfcaigc.service;

import com.yes.kfcaigc.entity.KnowledgeBase;
import com.yes.kfcaigc.entity.ModificationTypeRule;
import com.yes.kfcaigc.repository.KnowledgeBaseRepository;
import com.yes.kfcaigc.repository.ModificationTypeRuleRepository;
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
import static org.mockito.Mockito.*;

/**
 * ModificationTypeService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ModificationTypeServiceTest {

    @Mock
    private ModificationTypeRuleRepository modificationTypeRuleRepository;

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @InjectMocks
    private ModificationTypeService modificationTypeService;

    private List<ModificationTypeRule> mockRules;
    private List<KnowledgeBase> mockKnowledgeList;

    @BeforeEach
    void setUp() {
        // 准备mock数据
        ModificationTypeRule rule1 = new ModificationTypeRule();
        rule1.setType("删除");
        rule1.setKeywords("删除,移除,去掉");
        rule1.setPriority(100);

        ModificationTypeRule rule2 = new ModificationTypeRule();
        rule2.setType("新增");
        rule2.setKeywords("新增,添加,加入");
        rule2.setPriority(90);

        ModificationTypeRule rule3 = new ModificationTypeRule();
        rule3.setType("修改");
        rule3.setKeywords("修改,替换,改为");
        rule3.setPriority(80);

        mockRules = Arrays.asList(rule1, rule2, rule3);

        // 准备知识库数据
        KnowledgeBase kb1 = new KnowledgeBase();
        kb1.setOperation("删除黄金脆皮鸡");
        kb1.setType("删除");
        kb1.setResult("成功删除产品");

        KnowledgeBase kb2 = new KnowledgeBase();
        kb2.setOperation("新增避风塘炸鸡");
        kb2.setType("新增");
        kb2.setResult("成功新增产品");

        mockKnowledgeList = Arrays.asList(kb1, kb2);
    }

    @Test
    void testIdentifyTypes_返回所有类型按优先级排序() {
        // given
        when(modificationTypeRuleRepository.getAllRules()).thenReturn(mockRules);

        // when
        List<String> result = modificationTypeService.identifyTypes();

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("删除", result.get(0));
        assertEquals("新增", result.get(1));
        assertEquals("修改", result.get(2));
        verify(modificationTypeRuleRepository, times(1)).getAllRules();
    }

    @Test
    void testIdentifyTypes_空规则列表() {
        // given
        when(modificationTypeRuleRepository.getAllRules()).thenReturn(Collections.emptyList());

        // when
        List<String> result = modificationTypeService.identifyTypes();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindKnowledgeBaseByTypes_成功查询() {
        // given
        List<String> types = Arrays.asList("删除", "新增");
        when(knowledgeBaseRepository.list(any())).thenReturn(mockKnowledgeList);

        // when
        List<KnowledgeBase> result = modificationTypeService.findKnowledgeBaseByTypes(types);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testFindKnowledgeBaseByTypes_空类型列表() {
        // when
        List<KnowledgeBase> result = modificationTypeService.findKnowledgeBaseByTypes(Collections.emptyList());

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(knowledgeBaseRepository, never()).list(any());
    }

    @Test
    void testFindKnowledgeBaseByTypes_null类型() {
        // when
        List<KnowledgeBase> result = modificationTypeService.findKnowledgeBaseByTypes(null);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindKnowledgeBaseByTypesAndScene_返回全部知识库() {
        // given
        when(knowledgeBaseRepository.findAll()).thenReturn(mockKnowledgeList);

        // when
        List<KnowledgeBase> result = modificationTypeService.findKnowledgeBaseByTypesAndScene();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(knowledgeBaseRepository, times(1)).findAll();
    }

    @Test
    void testGetAllRules_成功获取所有规则() {
        // given
        when(modificationTypeRuleRepository.getAllRules()).thenReturn(mockRules);

        // when
        List<ModificationTypeRule> result = modificationTypeService.getAllRules();

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("删除", result.get(0).getType());
        assertEquals("新增", result.get(1).getType());
        assertEquals("修改", result.get(2).getType());
    }

    @Test
    void testGetAllRules_空规则列表() {
        // given
        when(modificationTypeRuleRepository.getAllRules()).thenReturn(Collections.emptyList());

        // when
        List<ModificationTypeRule> result = modificationTypeService.getAllRules();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
