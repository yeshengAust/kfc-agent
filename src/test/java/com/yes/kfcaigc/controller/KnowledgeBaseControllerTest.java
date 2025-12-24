package com.yes.kfcaigc.controller;

import com.yes.kfcaigc.entity.KnowledgeBase;
import com.yes.kfcaigc.repository.KnowledgeBaseRepository;
import com.yes.kfcaigc.service.ModificationTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KnowledgeBaseController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class KnowledgeBaseControllerTest {

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Mock
    private ModificationTypeService modificationTypeService;

    @InjectMocks
    private KnowledgeBaseController knowledgeBaseController;

    private Map<String, String> validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new HashMap<>();
        validRequest.put("operation", "删除黄金脆皮鸡");
        validRequest.put("result", "成功删除产品包含中的黄金脆皮鸡");
        validRequest.put("type", "删除");
        validRequest.put("scene", "通用场景");
    }

    @Test
    void testSaveKnowledge_新增成功() {
        // given
        when(knowledgeBaseRepository.findByExactOperation("删除黄金脆皮鸡")).thenReturn(null);
        doNothing().when(knowledgeBaseRepository).saveKnowledgeBase(any(KnowledgeBase.class));

        // when
        ResponseEntity<Map<String, Object>> response = knowledgeBaseController.saveKnowledge(validRequest);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("添加成功", response.getBody().get("message"));
        verify(knowledgeBaseRepository, times(1)).findByExactOperation("删除黄金脆皮鸡");
        verify(knowledgeBaseRepository, times(1)).saveKnowledgeBase(any(KnowledgeBase.class));
    }

    @Test
    void testSaveKnowledge_更新已存在记录() {
        // given
        KnowledgeBase existingKb = new KnowledgeBase();
        existingKb.setOperation("删除黄金脆皮鸡");
        existingKb.setResult("旧结果");
        existingKb.setType("删除");
        
        when(knowledgeBaseRepository.findByExactOperation("删除黄金脆皮鸡")).thenReturn(existingKb);
        doNothing().when(knowledgeBaseRepository).updateKnowledgeBase(any(KnowledgeBase.class));

        // when
        ResponseEntity<Map<String, Object>> response = knowledgeBaseController.saveKnowledge(validRequest);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("更新成功（覆盖了原有记录）", response.getBody().get("message"));
        verify(knowledgeBaseRepository, times(1)).updateKnowledgeBase(any(KnowledgeBase.class));
    }

    @Test
    void testSaveKnowledge_操作指令为空() {
        // given
        Map<String, String> invalidRequest = new HashMap<>();
        invalidRequest.put("operation", "");
        invalidRequest.put("result", "成功删除");
        invalidRequest.put("type", "删除");

        // when
        ResponseEntity<Map<String, Object>> response = knowledgeBaseController.saveKnowledge(invalidRequest);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("操作指令不能为空", response.getBody().get("message"));
        verify(knowledgeBaseRepository, never()).saveKnowledgeBase(any());
    }

    @Test
    void testSaveKnowledge_操作指令为null() {
        // given
        Map<String, String> invalidRequest = new HashMap<>();
        invalidRequest.put("operation", null);
        invalidRequest.put("result", "成功删除");
        invalidRequest.put("type", "删除");

        // when
        ResponseEntity<Map<String, Object>> response = knowledgeBaseController.saveKnowledge(invalidRequest);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("操作指令不能为空", response.getBody().get("message"));
    }

    @Test
    void testSaveKnowledge_修改结果为空() {
        // given
        Map<String, String> invalidRequest = new HashMap<>();
        invalidRequest.put("operation", "删除黄金脆皮鸡");
        invalidRequest.put("result", "");
        invalidRequest.put("type", "删除");

        // when
        ResponseEntity<Map<String, Object>> response = knowledgeBaseController.saveKnowledge(invalidRequest);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("修改结果不能为空", response.getBody().get("message"));
    }

    @Test
    void testSaveKnowledge_修改结果为null() {
        // given
        Map<String, String> invalidRequest = new HashMap<>();
        invalidRequest.put("operation", "删除黄金脆皮鸡");
        invalidRequest.put("result", null);
        invalidRequest.put("type", "删除");

        // when
        ResponseEntity<Map<String, Object>> response = knowledgeBaseController.saveKnowledge(invalidRequest);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
    }

    @Test
    void testSaveKnowledge_异常处理() {
        // given
        when(knowledgeBaseRepository.findByExactOperation(any())).thenThrow(new RuntimeException("数据库连接失败"));

        // when
        ResponseEntity<Map<String, Object>> response = knowledgeBaseController.saveKnowledge(validRequest);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains("保存失败"));
    }

    @Test
    void testSaveKnowledge_无type和scene() {
        // given
        Map<String, String> requestWithoutTypeAndScene = new HashMap<>();
        requestWithoutTypeAndScene.put("operation", "删除黄金脆皮鸡");
        requestWithoutTypeAndScene.put("result", "成功删除");
        
        when(knowledgeBaseRepository.findByExactOperation("删除黄金脆皮鸡")).thenReturn(null);
        doNothing().when(knowledgeBaseRepository).saveKnowledgeBase(any(KnowledgeBase.class));

        // when
        ResponseEntity<Map<String, Object>> response = knowledgeBaseController.saveKnowledge(requestWithoutTypeAndScene);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
    }
}
