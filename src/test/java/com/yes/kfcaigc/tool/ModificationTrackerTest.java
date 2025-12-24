package com.yes.kfcaigc.tool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModificationTracker 单元测试
 */
class ModificationTrackerTest {

    @BeforeEach
    void setUp() {
        // 每个测试开始前清空所有数据
        ModificationTracker.clearAll();
    }

    @AfterEach
    void tearDown() {
        // 每个测试结束后清空所有数据
        ModificationTracker.clearAll();
    }

    @Test
    void testCreateNewRequestId_生成唯一ID() {
        // when
        String requestId1 = ModificationTracker.createNewRequestId();
        String requestId2 = ModificationTracker.createNewRequestId();

        // then
        assertNotNull(requestId1);
        assertNotNull(requestId2);
        assertNotEquals(requestId1, requestId2);
        assertFalse(requestId1.isEmpty());
        assertFalse(requestId2.isEmpty());
    }

    @Test
    void testRecordChanges_成功记录单个改动点() {
        // given
        String requestId = ModificationTracker.createNewRequestId();
        String changes = "[{\"origin\":\"原文1\",\"now\":\"修改后1\"}]";

        // when
        ModificationTracker.recordChanges(requestId, changes);
        List<String> result = ModificationTracker.getChanges(requestId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(changes, result.get(0));
    }

    @Test
    void testRecordChanges_记录多个改动点() {
        // given
        String requestId = ModificationTracker.createNewRequestId();
        String changes1 = "[{\"origin\":\"原文1\",\"now\":\"修改后1\"}]";
        String changes2 = "[{\"origin\":\"原文2\",\"now\":\"修改后2\"}]";

        // when
        ModificationTracker.recordChanges(requestId, changes1);
        ModificationTracker.recordChanges(requestId, changes2);
        List<String> result = ModificationTracker.getChanges(requestId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(changes1));
        assertTrue(result.contains(changes2));
    }

    @Test
    void testRecordChanges_重复记录自动去重() {
        // given
        String requestId = ModificationTracker.createNewRequestId();
        String changes = "[{\"origin\":\"原文\",\"now\":\"修改后\"}]";

        // when
        ModificationTracker.recordChanges(requestId, changes);
        ModificationTracker.recordChanges(requestId, changes);
        ModificationTracker.recordChanges(requestId, changes);
        List<String> result = ModificationTracker.getChanges(requestId);

        // then
        assertEquals(1, result.size());
        assertEquals(changes, result.get(0));
    }

    @Test
    void testRecordChanges_requestId为null() {
        // when
        ModificationTracker.recordChanges(null, "some changes");
        List<String> result = ModificationTracker.getChanges(null);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecordChanges_requestId为空字符串() {
        // when
        ModificationTracker.recordChanges("", "some changes");
        List<String> result = ModificationTracker.getChanges("");

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecordChanges_changes为null() {
        // given
        String requestId = ModificationTracker.createNewRequestId();

        // when
        ModificationTracker.recordChanges(requestId, null);
        List<String> result = ModificationTracker.getChanges(requestId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecordChanges_changes为空字符串() {
        // given
        String requestId = ModificationTracker.createNewRequestId();

        // when
        ModificationTracker.recordChanges(requestId, "");
        List<String> result = ModificationTracker.getChanges(requestId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetChanges_不存在的requestId() {
        // when
        List<String> result = ModificationTracker.getChanges("non-existent-id");

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRemoveChanges_成功删除() {
        // given
        String requestId = ModificationTracker.createNewRequestId();
        String changes = "[{\"origin\":\"原文\",\"now\":\"修改后\"}]";
        ModificationTracker.recordChanges(requestId, changes);

        // when
        ModificationTracker.removeChanges(requestId);
        List<String> result = ModificationTracker.getChanges(requestId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRemoveChanges_删除null不报错() {
        // when & then - 不应该抛出异常
        assertDoesNotThrow(() -> ModificationTracker.removeChanges(null));
    }

    @Test
    void testClearAll_清空所有记录() {
        // given
        String requestId1 = ModificationTracker.createNewRequestId();
        String requestId2 = ModificationTracker.createNewRequestId();
        ModificationTracker.recordChanges(requestId1, "[{\"origin\":\"1\",\"now\":\"1\"}]");
        ModificationTracker.recordChanges(requestId2, "[{\"origin\":\"2\",\"now\":\"2\"}]");

        // when
        ModificationTracker.clearAll();

        // then
        assertTrue(ModificationTracker.getChanges(requestId1).isEmpty());
        assertTrue(ModificationTracker.getChanges(requestId2).isEmpty());
    }

    @Test
    void testRecordChanges_复杂JSON格式() {
        // given
        String requestId = ModificationTracker.createNewRequestId();
        String complexJson = "[{\"origin\":\"产品包含：任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）】\",\"now\":\"产品包含：任选4份【热辣香骨鸡（3块装）】\"},{\"origin\":\"在不售卖吮指原味鸡的餐厅，产品将替换为黄金脆皮鸡；\",\"now\":\"\"}]";

        // when
        ModificationTracker.recordChanges(requestId, complexJson);
        List<String> result = ModificationTracker.getChanges(requestId);

        // then
        assertEquals(1, result.size());
        assertEquals(complexJson, result.get(0));
    }

    @Test
    void testConcurrentAccess_多个请求独立记录() {
        // given
        String requestId1 = ModificationTracker.createNewRequestId();
        String requestId2 = ModificationTracker.createNewRequestId();
        String changes1 = "[{\"origin\":\"A\",\"now\":\"A1\"}]";
        String changes2 = "[{\"origin\":\"B\",\"now\":\"B1\"}]";

        // when
        ModificationTracker.recordChanges(requestId1, changes1);
        ModificationTracker.recordChanges(requestId2, changes2);

        // then
        List<String> result1 = ModificationTracker.getChanges(requestId1);
        List<String> result2 = ModificationTracker.getChanges(requestId2);

        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(changes1, result1.get(0));
        assertEquals(changes2, result2.get(0));
    }
}
