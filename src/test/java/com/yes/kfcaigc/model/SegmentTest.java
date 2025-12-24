package com.yes.kfcaigc.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Segment 单元测试
 */
class SegmentTest {

    private Segment segment;

    @BeforeEach
    void setUp() {
        segment = new Segment();
    }

    @Test
    void testSetAndGetId() {
        // when
        segment.setId("seg1");

        // then
        assertEquals("seg1", segment.getId());
    }

    @Test
    void testSetAndGetType() {
        // when
        segment.setType("产品包含");

        // then
        assertEquals("产品包含", segment.getType());
    }

    @Test
    void testSetAndGetReason() {
        // when
        segment.setReason("该片段包含要删除的产品");

        // then
        assertEquals("该片段包含要删除的产品", segment.getReason());
    }

    @Test
    void testSetAndGetText() {
        // when
        segment.setText("产品包含：任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）】");

        // then
        assertEquals("产品包含：任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）】", segment.getText());
    }

    @Test
    void testCompleteSegment() {
        // when
        segment.setId("seg1");
        segment.setType("产品包含");
        segment.setReason("包含黄金脆皮鸡，需要删除");
        segment.setText("产品包含：任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）】");

        // then
        assertEquals("seg1", segment.getId());
        assertEquals("产品包含", segment.getType());
        assertEquals("包含黄金脆皮鸡，需要删除", segment.getReason());
        assertEquals("产品包含：任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）】", segment.getText());
    }

    @Test
    void testNullValues() {
        // when
        segment.setId(null);
        segment.setType(null);
        segment.setReason(null);
        segment.setText(null);

        // then
        assertNull(segment.getId());
        assertNull(segment.getType());
        assertNull(segment.getReason());
        assertNull(segment.getText());
    }

    @Test
    void testEmptyValues() {
        // when
        segment.setId("");
        segment.setType("");
        segment.setReason("");
        segment.setText("");

        // then
        assertEquals("", segment.getId());
        assertEquals("", segment.getType());
        assertEquals("", segment.getReason());
        assertEquals("", segment.getText());
    }

    @Test
    void testReplaceRuleSegment() {
        // given - 替换规则片段
        Segment replaceSegment = new Segment();
        replaceSegment.setId("seg2");
        replaceSegment.setType("替换规则");
        replaceSegment.setReason("包含黄金脆皮鸡的替换规则");
        replaceSegment.setText("在不售卖黄金脆皮鸡的餐厅，产品将替换为吮指原味鸡。");

        // then
        assertEquals("seg2", replaceSegment.getId());
        assertEquals("替换规则", replaceSegment.getType());
        assertTrue(replaceSegment.getText().contains("黄金脆皮鸡"));
    }

    @Test
    void testTimeRestrictionSegment() {
        // given - 时间限制片段
        Segment timeSegment = new Segment();
        timeSegment.setId("seg3");
        timeSegment.setType("时间限制");
        timeSegment.setReason("可能需要修改使用时间");
        timeSegment.setText("堂食或自助点餐仅限09:30-23:00使用，具体以餐厅营业时间及该产品实际供应时间为准；");

        // then
        assertEquals("时间限制", timeSegment.getType());
        assertTrue(timeSegment.getText().contains("09:30-23:00"));
    }

    @Test
    void testMultilineText() {
        // given - 多行文本
        String multilineText = "使用有效期：自购买之日起14天内使用有效-N列＆Q列\n" +
                               "堂食或自助点餐仅限09:30-23:00使用，具体以餐厅营业时间及该产品实际供应时间为准；";
        
        segment.setText(multilineText);

        // then
        assertEquals(multilineText, segment.getText());
        assertTrue(segment.getText().contains("\n"));
    }
}
