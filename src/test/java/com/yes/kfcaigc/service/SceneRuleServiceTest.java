package com.yes.kfcaigc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SceneRuleService 单元测试
 */
class SceneRuleServiceTest {

    private SceneRuleService sceneRuleService;

    @BeforeEach
    void setUp() {
        sceneRuleService = new SceneRuleService();
    }

    @Test
    void testIdentifyScene_删除操作() {
        // given
        String operation = "删除黄金脆皮鸡";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
        assertNotNull(result.getActions());
        assertFalse(result.getActions().isEmpty());
        assertEquals("删除", result.getActions().get(0).getAction());
    }

    @Test
    void testIdentifyScene_替换操作_使用替换为() {
        // given
        String operation = "将黄金脆皮鸡替换为避风塘黄金脆皮鸡";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
        assertNotNull(result.getActions());
        assertFalse(result.getActions().isEmpty());
        assertEquals("替换", result.getActions().get(0).getAction());
    }

    @Test
    void testIdentifyScene_替换操作_使用改为() {
        // given
        String operation = "将原味鸡改为香辣鸡";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
        assertNotNull(result.getActions());
        assertFalse(result.getActions().isEmpty());
        assertEquals("替换", result.getActions().get(0).getAction());
    }

    @Test
    void testIdentifyScene_替换操作_使用改成() {
        // given
        String operation = "把吮指原味鸡改成香辣鸡翅";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
        assertNotNull(result.getActions());
    }

    @Test
    void testIdentifyScene_空操作() {
        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene("");

        // then
        assertNull(result);
    }

    @Test
    void testIdentifyScene_null操作() {
        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(null);

        // then
        assertNull(result);
    }

    @Test
    void testIdentifyScene_通用场景无特定关键词() {
        // given
        String operation = "修改产品说明";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
    }

    @Test
    void testSceneRule_生成操作描述() {
        // given
        String operation = "删除黄金脆皮鸡";
        SceneRuleService.SceneRule rule = sceneRuleService.identifyScene(operation);

        // when
        String description = rule.generateOperationDescription();

        // then
        assertNotNull(description);
        assertTrue(description.contains("场景：通用场景"));
        assertTrue(description.contains("请按照以下操作列表逐一执行"));
    }

    @Test
    void testRuleAction_获取器方法() {
        // given
        SceneRuleService.RuleAction action = new SceneRuleService.RuleAction(
            "删除", 
            "产品包含", 
            "黄金脆皮鸡"
        );

        // when & then
        assertEquals("删除", action.getAction());
        assertEquals("产品包含", action.getType());
        assertEquals("黄金脆皮鸡", action.getContent());
    }

    @Test
    void testSceneRule_获取器方法() {
        // given
        String operation = "删除黄金脆皮鸡";
        SceneRuleService.SceneRule rule = sceneRuleService.identifyScene(operation);

        // when & then
        assertEquals("通用场景", rule.getSceneName());
        assertEquals("通用", rule.getSubScene());
        assertNotNull(rule.getActions());
    }
}
