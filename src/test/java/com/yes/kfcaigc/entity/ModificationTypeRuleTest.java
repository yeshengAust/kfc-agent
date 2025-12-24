package com.yes.kfcaigc.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModificationTypeRule 实体类单元测试
 */
class ModificationTypeRuleTest {

    private ModificationTypeRule rule;

    @BeforeEach
    void setUp() {
        rule = new ModificationTypeRule();
    }

    @Test
    void testSetAndGetId() {
        // when
        rule.setId(1L);

        // then
        assertEquals(1L, rule.getId());
    }

    @Test
    void testSetAndGetType() {
        // when
        rule.setType("删除");

        // then
        assertEquals("删除", rule.getType());
    }

    @Test
    void testSetAndGetKeywords() {
        // when
        rule.setKeywords("删除,移除,去掉");

        // then
        assertEquals("删除,移除,去掉", rule.getKeywords());
    }

    @Test
    void testSetAndGetScene() {
        // when
        rule.setScene("通用场景");

        // then
        assertEquals("通用场景", rule.getScene());
    }

    @Test
    void testSetAndGetRuleDetail() {
        // when
        rule.setRuleDetail("删除产品或规则的操作");

        // then
        assertEquals("删除产品或规则的操作", rule.getRuleDetail());
    }

    @Test
    void testSetAndGetPriority() {
        // when
        rule.setPriority(100);

        // then
        assertEquals(100, rule.getPriority());
    }

    @Test
    void testSetAndGetCreateTime() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        rule.setCreateTime(now);

        // then
        assertEquals(now, rule.getCreateTime());
    }

    @Test
    void testSetAndGetUpdateTime() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        rule.setUpdateTime(now);

        // then
        assertEquals(now, rule.getUpdateTime());
    }

    @Test
    void testSetAndGetDeleted() {
        // when
        rule.setDeleted(0);

        // then
        assertEquals(0, rule.getDeleted());
    }

    @Test
    void testCompleteRule() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        rule.setId(1L);
        rule.setType("删除");
        rule.setKeywords("删除,移除,去掉");
        rule.setScene("通用场景");
        rule.setRuleDetail("删除产品或规则的操作");
        rule.setPriority(100);
        rule.setCreateTime(now);
        rule.setUpdateTime(now);
        rule.setDeleted(0);

        // then
        assertEquals(1L, rule.getId());
        assertEquals("删除", rule.getType());
        assertEquals("删除,移除,去掉", rule.getKeywords());
        assertEquals("通用场景", rule.getScene());
        assertEquals("删除产品或规则的操作", rule.getRuleDetail());
        assertEquals(100, rule.getPriority());
        assertEquals(now, rule.getCreateTime());
        assertEquals(now, rule.getUpdateTime());
        assertEquals(0, rule.getDeleted());
    }

    @Test
    void testDifferentTypes() {
        // 测试不同的修改类型
        rule.setType("新增");
        assertEquals("新增", rule.getType());

        rule.setType("修改");
        assertEquals("修改", rule.getType());

        rule.setType("替换");
        assertEquals("替换", rule.getType());

        rule.setType("查询");
        assertEquals("查询", rule.getType());
    }

    @Test
    void testMultipleKeywords() {
        // given
        String keywords = "修改,替换,改为,加回,恢复,还原";

        // when
        rule.setKeywords(keywords);

        // then
        assertEquals(keywords, rule.getKeywords());
        assertTrue(rule.getKeywords().contains("修改"));
        assertTrue(rule.getKeywords().contains("替换"));
        assertTrue(rule.getKeywords().contains("加回"));
    }

    @Test
    void testPriorityComparison() {
        // given
        ModificationTypeRule highPriority = new ModificationTypeRule();
        highPriority.setPriority(100);

        ModificationTypeRule lowPriority = new ModificationTypeRule();
        lowPriority.setPriority(50);

        // then
        assertTrue(highPriority.getPriority() > lowPriority.getPriority());
    }

    @Test
    void testDeletedFlag() {
        // 测试逻辑删除标识
        rule.setDeleted(0);
        assertEquals(0, rule.getDeleted());

        rule.setDeleted(1);
        assertEquals(1, rule.getDeleted());
    }

    @Test
    void testNullValues() {
        // when
        rule.setType(null);
        rule.setKeywords(null);
        rule.setScene(null);
        rule.setRuleDetail(null);
        rule.setPriority(null);

        // then
        assertNull(rule.getType());
        assertNull(rule.getKeywords());
        assertNull(rule.getScene());
        assertNull(rule.getRuleDetail());
        assertNull(rule.getPriority());
    }

    @Test
    void testEmptyStrings() {
        // when
        rule.setType("");
        rule.setKeywords("");
        rule.setScene("");
        rule.setRuleDetail("");

        // then
        assertEquals("", rule.getType());
        assertEquals("", rule.getKeywords());
        assertEquals("", rule.getScene());
        assertEquals("", rule.getRuleDetail());
    }

    @Test
    void testDetailedRuleDescription() {
        // given
        String detailedRule = "删除操作：从产品包含中删除指定产品，同时删除所有相关的替换规则。" +
                             "需要注意保留通用规则。";

        // when
        rule.setRuleDetail(detailedRule);

        // then
        assertEquals(detailedRule, rule.getRuleDetail());
        assertTrue(rule.getRuleDetail().contains("删除操作"));
        assertTrue(rule.getRuleDetail().contains("保留通用规则"));
    }

    @Test
    void testZeroPriority() {
        // when
        rule.setPriority(0);

        // then
        assertEquals(0, rule.getPriority());
    }

    @Test
    void testNegativePriority() {
        // when
        rule.setPriority(-1);

        // then
        assertEquals(-1, rule.getPriority());
    }
}
