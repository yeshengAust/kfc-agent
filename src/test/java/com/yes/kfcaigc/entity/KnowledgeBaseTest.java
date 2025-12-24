package com.yes.kfcaigc.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KnowledgeBase 实体类单元测试
 */
class KnowledgeBaseTest {

    private KnowledgeBase knowledgeBase;

    @BeforeEach
    void setUp() {
        knowledgeBase = new KnowledgeBase();
    }

    @Test
    void testSetAndGetId() {
        // when
        knowledgeBase.setId(1L);

        // then
        assertEquals(1L, knowledgeBase.getId());
    }

    @Test
    void testSetAndGetOperation() {
        // when
        knowledgeBase.setOperation("删除黄金脆皮鸡");

        // then
        assertEquals("删除黄金脆皮鸡", knowledgeBase.getOperation());
    }

    @Test
    void testSetAndGetType() {
        // when
        knowledgeBase.setType("删除");

        // then
        assertEquals("删除", knowledgeBase.getType());
    }

    @Test
    void testSetAndGetScene() {
        // when
        knowledgeBase.setScene("通用场景");

        // then
        assertEquals("通用场景", knowledgeBase.getScene());
    }

    @Test
    void testSetAndGetResult() {
        // given
        String result = "[{\"origin\":\"产品包含：黄金脆皮鸡\",\"now\":\"产品包含：吮指原味鸡\"}]";

        // when
        knowledgeBase.setResult(result);

        // then
        assertEquals(result, knowledgeBase.getResult());
    }

    @Test
    void testSetAndGetCreateTime() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        knowledgeBase.setCreateTime(now);

        // then
        assertEquals(now, knowledgeBase.getCreateTime());
    }

    @Test
    void testSetAndGetUpdateTime() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        knowledgeBase.setUpdateTime(now);

        // then
        assertEquals(now, knowledgeBase.getUpdateTime());
    }

    @Test
    void testSetAndGetDeleted() {
        // when
        knowledgeBase.setDeleted(0);

        // then
        assertEquals(0, knowledgeBase.getDeleted());
    }

    @Test
    void testCompleteKnowledgeBase() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        knowledgeBase.setId(1L);
        knowledgeBase.setOperation("删除黄金脆皮鸡");
        knowledgeBase.setType("删除");
        knowledgeBase.setScene("通用场景");
        knowledgeBase.setResult("[{\"origin\":\"原文\",\"now\":\"修改后\"}]");
        knowledgeBase.setCreateTime(now);
        knowledgeBase.setUpdateTime(now);
        knowledgeBase.setDeleted(0);

        // then
        assertEquals(1L, knowledgeBase.getId());
        assertEquals("删除黄金脆皮鸡", knowledgeBase.getOperation());
        assertEquals("删除", knowledgeBase.getType());
        assertEquals("通用场景", knowledgeBase.getScene());
        assertNotNull(knowledgeBase.getResult());
        assertEquals(now, knowledgeBase.getCreateTime());
        assertEquals(now, knowledgeBase.getUpdateTime());
        assertEquals(0, knowledgeBase.getDeleted());
    }

    @Test
    void testMultipleTypes() {
        // given - 多个类型用逗号分隔
        String multipleTypes = "删除,修改";

        // when
        knowledgeBase.setType(multipleTypes);

        // then
        assertEquals(multipleTypes, knowledgeBase.getType());
        assertTrue(knowledgeBase.getType().contains("删除"));
        assertTrue(knowledgeBase.getType().contains("修改"));
    }

    @Test
    void testDeletedFlag() {
        // 测试删除标识
        knowledgeBase.setDeleted(0);
        assertEquals(0, knowledgeBase.getDeleted());

        knowledgeBase.setDeleted(1);
        assertEquals(1, knowledgeBase.getDeleted());
    }

    @Test
    void testNullValues() {
        // when
        knowledgeBase.setOperation(null);
        knowledgeBase.setType(null);
        knowledgeBase.setScene(null);
        knowledgeBase.setResult(null);

        // then
        assertNull(knowledgeBase.getOperation());
        assertNull(knowledgeBase.getType());
        assertNull(knowledgeBase.getScene());
        assertNull(knowledgeBase.getResult());
    }

    @Test
    void testEmptyStrings() {
        // when
        knowledgeBase.setOperation("");
        knowledgeBase.setType("");
        knowledgeBase.setScene("");
        knowledgeBase.setResult("");

        // then
        assertEquals("", knowledgeBase.getOperation());
        assertEquals("", knowledgeBase.getType());
        assertEquals("", knowledgeBase.getScene());
        assertEquals("", knowledgeBase.getResult());
    }

    @Test
    void testSerializable() {
        // 验证实现了 Serializable 接口
        assertTrue(knowledgeBase instanceof java.io.Serializable);
    }
}
