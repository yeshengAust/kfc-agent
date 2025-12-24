package com.yes.kfcaigc.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModificationPointDTO 单元测试
 */
class ModificationPointDTOTest {

    private ModificationPointDTO modificationPoint;

    @BeforeEach
    void setUp() {
        modificationPoint = new ModificationPointDTO("原文", "修改后");
    }

    @Test
    void testConstructor_成功创建对象() {
        // when & then
        assertNotNull(modificationPoint);
        assertEquals("原文", modificationPoint.getOrigin());
        assertEquals("修改后", modificationPoint.getNow());
    }

    @Test
    void testConstructor_空字符串() {
        // when
        ModificationPointDTO dto = new ModificationPointDTO("", "");

        // then
        assertNotNull(dto);
        assertEquals("", dto.getOrigin());
        assertEquals("", dto.getNow());
    }

    @Test
    void testConstructor_null值() {
        // when
        ModificationPointDTO dto = new ModificationPointDTO(null, null);

        // then
        assertNotNull(dto);
        assertNull(dto.getOrigin());
        assertNull(dto.getNow());
    }

    @Test
    void testGetOrigin() {
        // when
        String origin = modificationPoint.getOrigin();

        // then
        assertEquals("原文", origin);
    }

    @Test
    void testGetNow() {
        // when
        String now = modificationPoint.getNow();

        // then
        assertEquals("修改后", now);
    }

    @Test
    void testSetOrigin() {
        // when
        modificationPoint.setOrigin("新原文");

        // then
        assertEquals("新原文", modificationPoint.getOrigin());
    }

    @Test
    void testSetNow() {
        // when
        modificationPoint.setNow("新修改后");

        // then
        assertEquals("新修改后", modificationPoint.getNow());
    }

    @Test
    void testDeleteOperation_nowIsEmpty() {
        // given - 删除操作，now为空字符串
        ModificationPointDTO deletePoint = new ModificationPointDTO(
            "产品包含：黄金脆皮鸡",
            ""
        );

        // then
        assertEquals("产品包含：黄金脆皮鸡", deletePoint.getOrigin());
        assertEquals("", deletePoint.getNow());
    }

    @Test
    void testReplaceOperation() {
        // given - 替换操作
        ModificationPointDTO replacePoint = new ModificationPointDTO(
            "产品包含：黄金脆皮鸡（1块装）",
            "产品包含：避风塘黄金脆皮鸡（1块装）"
        );

        // then
        assertTrue(replacePoint.getOrigin().contains("黄金脆皮鸡"));
        assertTrue(replacePoint.getNow().contains("避风塘黄金脆皮鸡"));
    }

    @Test
    void testComplexContent() {
        // given - 复杂内容
        String complexOrigin = "使用有效期：自购买之日起14天内使用有效-N列＆Q列";
        String complexNow = "使用有效期：自购买之日起30天内使用有效-N列＆Q列";
        ModificationPointDTO complexPoint = new ModificationPointDTO(complexOrigin, complexNow);

        // then
        assertEquals(complexOrigin, complexPoint.getOrigin());
        assertEquals(complexNow, complexPoint.getNow());
    }
}
