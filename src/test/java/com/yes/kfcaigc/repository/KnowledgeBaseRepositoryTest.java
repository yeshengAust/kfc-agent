package com.yes.kfcaigc.repository;

import com.yes.kfcaigc.entity.KnowledgeBase;
import com.yes.kfcaigc.mapper.KnowledgeBaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KnowledgeBaseRepository 单元测试
 */
@ExtendWith(MockitoExtension.class)
class KnowledgeBaseRepositoryTest {

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @InjectMocks
    private KnowledgeBaseRepository knowledgeBaseRepository;

    private List<KnowledgeBase> mockKnowledgeList;

    @BeforeEach
    void setUp() {
        KnowledgeBase kb1 = new KnowledgeBase();
        kb1.setId(1L);
        kb1.setOperation("删除黄金脆皮鸡");
        kb1.setType("删除");
        kb1.setResult("成功删除");
        kb1.setDeleted(0);
        kb1.setCreateTime(LocalDateTime.now());

        KnowledgeBase kb2 = new KnowledgeBase();
        kb2.setId(2L);
        kb2.setOperation("新增避风塘炸鸡");
        kb2.setType("新增");
        kb2.setResult("成功新增");
        kb2.setDeleted(0);
        kb2.setCreateTime(LocalDateTime.now().minusDays(1));

        mockKnowledgeList = Arrays.asList(kb1, kb2);
    }

    @Test
    void testFindAll_成功返回未删除记录() {
        // given
        when(knowledgeBaseMapper.selectList(any())).thenReturn(mockKnowledgeList);

        // when
        List<KnowledgeBase> result = knowledgeBaseRepository.findAll();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(knowledgeBaseMapper, times(1)).selectList(any());
    }

    @Test
    void testFindAll_空列表() {
        // given
        when(knowledgeBaseMapper.selectList(any())).thenReturn(Collections.emptyList());

        // when
        List<KnowledgeBase> result = knowledgeBaseRepository.findAll();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByOperation_模糊查询成功() {
        // given
        String operation = "删除";
        when(knowledgeBaseMapper.selectList(any())).thenReturn(
            Collections.singletonList(mockKnowledgeList.get(0))
        );

        // when
        List<KnowledgeBase> result = knowledgeBaseRepository.findByOperation(operation);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getOperation().contains(operation));
    }

    @Test
    void testFindByOperation_无匹配结果() {
        // given
        String operation = "查询";
        when(knowledgeBaseMapper.selectList(any())).thenReturn(Collections.emptyList());

        // when
        List<KnowledgeBase> result = knowledgeBaseRepository.findByOperation(operation);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
