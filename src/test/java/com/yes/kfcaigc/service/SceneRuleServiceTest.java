package com.yes.kfcaigc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    @Test
    void testExtractTarget_提取删除目标_带逗号() {
        // given
        String operation = "删除黄金脆皮鸡，保留其他";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
        assertEquals(1, result.getActions().size());
        assertEquals("删除", result.getActions().get(0).getAction());
        assertTrue(result.getActions().get(0).getContent().contains("黄金脆皮鸡"));
    }

    @Test
    void testExtractTarget_提取删除目标_带句号() {
        // given
        String operation = "删除香辣鸡翅。";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("删除", result.getActions().get(0).getAction());
        assertTrue(result.getActions().get(0).getContent().contains("香辣鸡翅"));
    }

    @Test
    void testExtractTarget_不包含关键词() {
        // given
        String operation = "修改产品说明";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
        assertTrue(result.getActions().isEmpty());
    }

    @Test
    void testExtractReplaceParts_使用将_替换为() {
        // given
        String operation = "将黄金脆皮鸡替换为避风堂黄金脆皮鸡";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals(1, result.getActions().size());
        assertEquals("替换", result.getActions().get(0).getAction());
        assertTrue(result.getActions().get(0).getContent().contains("\u9ec4\u91d1\u8106\u76ae\u9e21"));
        assertTrue(result.getActions().get(0).getContent().contains("->"));
        assertTrue(result.getActions().get(0).getContent().contains("避风堂黄金脆皮鸡"));
    }

    @Test
    void testExtractReplaceParts_使用把_改为() {
        // given
        String operation = "把原味鸡改为香辣鸡";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals(1, result.getActions().size());
        assertEquals("替换", result.getActions().get(0).getAction());
        assertTrue(result.getActions().get(0).getContent().contains("原味鸡"));
        assertTrue(result.getActions().get(0).getContent().contains("香辣鸡"));
    }

    @Test
    void testExtractReplaceParts_不包含关键词() {
        // given
        String operation = "将黄金脆皮鸡"; // 没有“替换为”、“改为”、“改成”

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
        assertTrue(result.getActions().isEmpty());
    }

    @Test
    void testExtractReplaceParts_多个替换为() {
        // given - 测试包含多个“替换为”的情况
        String operation = "将A替换为B替换为C";

        // when
        SceneRuleService.SceneRule result = sceneRuleService.identifyScene(operation);

        // then
        assertNotNull(result);
        assertEquals("通用场景", result.getSceneName());
        assertEquals(1, result.getActions().size());
    }

    @Test
    void testGenerateOperationDescription_删除操作_产品包含() {
        // given
        String operation = "删除黄金脆皮鸡";
        SceneRuleService.SceneRule rule = sceneRuleService.identifyScene(operation);

        // when
        String description = rule.generateOperationDescription();

        // then
        assertNotNull(description);
        assertTrue(description.contains("场景：通用场景"));
        assertTrue(description.contains("删除"));
        assertTrue(description.contains("产品包含"));
        assertTrue(description.contains("从\"产品包含\"字段中删除该产品选项"));
    }

    @Test
    void testGenerateOperationDescription_替换操作() {
        // given
        String operation = "将黄金脆皮鸡替换为避风堂黄金脆皮鸡";
        SceneRuleService.SceneRule rule = sceneRuleService.identifyScene(operation);

        // when
        String description = rule.generateOperationDescription();

        // then
        assertNotNull(description);
        assertTrue(description.contains("替换"));
        assertTrue(description.contains("将文案中所有出现的旧名称替换为新名称"));
    }

    @Test
    void testGenerateOperationDescription_包含重要提示() {
        // given
        String operation = "删除产品";
        SceneRuleService.SceneRule rule = sceneRuleService.identifyScene(operation);

        // when
        String description = rule.generateOperationDescription();

        // then
        assertTrue(description.contains("重要提示"));
        assertTrue(description.contains("严格按照上述操作顺序执行"));
        assertTrue(description.contains("确保产品名称完全匹配"));
    }

    @Test
    void testSceneRule_matches_主场景不匹配() {
        // given
        List<String> keywords = Arrays.asList("删除", "remove");
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", keywords, "子场景", Collections.emptyList(), Collections.emptyList()
        );

        // when
        boolean matches = rule.matches("新增产品", "新增产品");

        // then
        assertFalse(matches);
    }

    @Test
    void testSceneRule_matches_主场景匹配_无子场景() {
        // given
        List<String> keywords = Arrays.asList("删除");
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", keywords, "子场景", Collections.emptyList(), Collections.emptyList()
        );

        // when
        boolean matches = rule.matches("删除产品", "删除产品");

        // then
        assertTrue(matches);
    }

    @Test
    void testSceneRule_matches_主场景匹配_子场景匹配() {
        // given
        List<String> keywords = Arrays.asList("删除");
        List<String> subKeywords = Arrays.asList("黄金脆皮鸡");
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", keywords, "子场景", subKeywords, Collections.emptyList()
        );

        // when
        boolean matches = rule.matches("删除黄金脆皮鸡", "删除黄金脆皮鸡");

        // then
        assertTrue(matches);
    }

    @Test
    void testSceneRule_matches_主场景匹配_子场景不匹配() {
        // given
        List<String> keywords = Arrays.asList("删除");
        List<String> subKeywords = Arrays.asList("黄金脆皮鸡");
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", keywords, "子场景", subKeywords, Collections.emptyList()
        );

        // when
        boolean matches = rule.matches("删除香辣鸡翅", "删除香辣鸡翅");

        // then
        assertFalse(matches);
    }

    @Test
    void testSceneRule_matches_subSceneKeywords为null() {
        // given
        List<String> keywords = Arrays.asList("删除");
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", keywords, "子场景", null, Collections.emptyList()
        );

        // when
        boolean matches = rule.matches("删除产品", "删除产品");

        // then
        assertTrue(matches);
    }

    @Test
    void testGenerateOperationDescription_删除替换规则() {
        // given
        List<SceneRuleService.RuleAction> actions = Arrays.asList(
            new SceneRuleService.RuleAction("删除", "替换规则", "黄金脆皮鸡")
        );
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", Collections.emptyList(), "子场景", Collections.emptyList(), actions
        );

        // when
        String description = rule.generateOperationDescription();

        // then
        assertTrue(description.contains("删除与该产品相关的所有替换规则"));
    }

    @Test
    void testGenerateOperationDescription_添加产品包含() {
        // given
        List<SceneRuleService.RuleAction> actions = Arrays.asList(
            new SceneRuleService.RuleAction("添加", "产品包含", "香辣鸡翅")
        );
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", Collections.emptyList(), "子场景", Collections.emptyList(), actions
        );

        // when
        String description = rule.generateOperationDescription();

        // then
        assertTrue(description.contains("在\"产品包含\"字段中添加该产品选项"));
    }

    @Test
    void testGenerateOperationDescription_添加替换规则() {
        // given
        List<SceneRuleService.RuleAction> actions = Arrays.asList(
            new SceneRuleService.RuleAction("添加", "替换规则", "产品A")
        );
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", Collections.emptyList(), "子场景", Collections.emptyList(), actions
        );

        // when
        String description = rule.generateOperationDescription();

        // then
        assertTrue(description.contains("添加该产品的替换规则"));
    }

    @Test
    void testGenerateOperationDescription_添加后备规则() {
        // given
        List<SceneRuleService.RuleAction> actions = Arrays.asList(
            new SceneRuleService.RuleAction("添加", "后备规则", "产品B")
        );
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", Collections.emptyList(), "子场景", Collections.emptyList(), actions
        );

        // when
        String description = rule.generateOperationDescription();

        // then
        assertTrue(description.contains("添加该产品的后备替换规则"));
    }

    @Test
    void testGenerateOperationDescription_保留操作() {
        // given
        List<SceneRuleService.RuleAction> actions = Arrays.asList(
            new SceneRuleService.RuleAction("保留", "产品包含", "产品C")
        );
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", Collections.emptyList(), "子场景", Collections.emptyList(), actions
        );

        // when
        String description = rule.generateOperationDescription();

        // then
        assertTrue(description.contains("确保该规则在修改后的文案中仍然存在"));
    }

    @Test
    void testGenerateOperationDescription_未知操作类型() {
        // given
        List<SceneRuleService.RuleAction> actions = Arrays.asList(
            new SceneRuleService.RuleAction("未知操作", "产品包含", "产品D")
        );
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", Collections.emptyList(), "子场景", Collections.emptyList(), actions
        );

        // when
        String description = rule.generateOperationDescription();

        // then
        assertNotNull(description);
        assertTrue(description.contains("未知操作"));
    }

    @Test
    void testGenerateOperationDescription_subScene为null() {
        // given
        List<SceneRuleService.RuleAction> actions = Arrays.asList(
            new SceneRuleService.RuleAction("删除", "产品包含", "产品E")
        );
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", Collections.emptyList(), null, Collections.emptyList(), actions
        );

        // when
        String description = rule.generateOperationDescription();

        // then
        assertNotNull(description);
        assertTrue(description.contains("场景：测试场景"));
        assertFalse(description.contains("（"));
    }

    @Test
    void testGenerateOperationDescription_subScene为空字符串() {
        // given
        List<SceneRuleService.RuleAction> actions = Arrays.asList(
            new SceneRuleService.RuleAction("删除", "产品包含", "产品F")
        );
        SceneRuleService.SceneRule rule = new SceneRuleService.SceneRule(
            "测试场景", Collections.emptyList(), "", Collections.emptyList(), actions
        );

        // when
        String description = rule.generateOperationDescription();

        // then
        assertNotNull(description);
        assertTrue(description.contains("场景：测试场景"));
        assertFalse(description.contains("（）"));
    }
}
