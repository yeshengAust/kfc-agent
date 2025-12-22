package com.yes.kfcaigc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 场景规则服务
 * 将自然语言操作指令映射到结构化规则
 */
@Slf4j
@Service
public class SceneRuleService {

    /**
     * 场景规则映射（如果需要为特定业务预置结构化规则，可以在此扩展）。
     * 当前版本不内置任何与具体产品相关的规则，全部走通用逻辑。
     */
    private static final Map<String, SceneRule> SCENE_RULES = new HashMap<>();

    /**
     * 从自然语言操作指令中识别场景和规则
     */
    public SceneRule identifyScene(String operation) {
        if (operation == null || operation.isEmpty()) {
            return null;
        }

        String lowerOperation = operation.toLowerCase();

        // 识别预置场景（当前为空，预留扩展能力）
        for (Map.Entry<String, SceneRule> entry : SCENE_RULES.entrySet()) {
            SceneRule rule = entry.getValue();
            if (rule.matches(operation, lowerOperation)) {
                log.info("识别到场景：{}，操作：{}", rule.getSceneName(), operation);
                return rule;
            }
        }

        // 如果没有匹配到预定义场景，返回通用规则
        log.info("未匹配到预定义场景，使用通用规则，操作：{}", operation);
        return createGenericRule(operation);
    }

    // 之前这里内置了与具体产品（如某款脆皮鸡）强绑定的规则，现已移除。
    // 如后续需要支持特定活动/产品，可以在运行时或配置层面扩展 SCENE_RULES，而不是写死在代码里。

    /**
     * 创建通用规则（当无法匹配预定义场景时）
     */
    private SceneRule createGenericRule(String operation) {
        List<RuleAction> actions = new ArrayList<>();
        
        // 尝试从操作中提取基本信息
        if (operation.contains("删除")) {
            // 提取要删除的内容
            String target = extractTarget(operation, "删除");
            if (target != null) {
                actions.add(new RuleAction("删除", "产品包含", target));
            }
        } else if (operation.contains("替换") || operation.contains("改为") || operation.contains("改成")) {
            // 提取替换内容
            String[] parts = extractReplaceParts(operation);
            if (parts != null && parts.length == 2) {
                actions.add(new RuleAction("替换", "产品包含", parts[0] + " -> " + parts[1]));
            }
        }
        
        return new SceneRule("通用场景", Collections.emptyList(), "通用", Collections.emptyList(), actions);
    }

    /**
     * 提取目标内容
     */
    private String extractTarget(String operation, String keyword) {
        int index = operation.indexOf(keyword);
        if (index != -1) {
            String after = operation.substring(index + keyword.length()).trim();
            // 简单提取，可以进一步优化
            return after.split("[，,。]")[0].trim();
        }
        return null;
    }

    /**
     * 提取替换的源和目标
     */
    private String[] extractReplaceParts(String operation) {
        String[] keywords = {"替换为", "改为", "改成"};
        for (String keyword : keywords) {
            if (operation.contains(keyword)) {
                String[] parts = operation.split(keyword);
                if (parts.length == 2) {
                    String source = parts[0].replaceAll(".*?将|.*?把", "").trim();
                    String target = parts[1].trim();
                    return new String[]{source, target};
                }
            }
        }
        return null;
    }

    /**
     * 场景规则
     */
    public static class SceneRule {
        private final String sceneName;
        private final List<String> keywords;
        private final String subScene;
        private final List<String> subSceneKeywords;
        private final List<RuleAction> actions;

        public SceneRule(String sceneName, List<String> keywords, String subScene, 
                       List<String> subSceneKeywords, List<RuleAction> actions) {
            this.sceneName = sceneName;
            this.keywords = keywords;
            this.subScene = subScene;
            this.subSceneKeywords = subSceneKeywords;
            this.actions = actions;
        }

        /**
         * 检查操作是否匹配此场景
         */
        public boolean matches(String operation, String lowerOperation) {
            // 检查主场景关键词
            boolean matchesMain = keywords.stream()
                    .anyMatch(keyword -> lowerOperation.contains(keyword.toLowerCase()));
            
            if (!matchesMain) {
                return false;
            }

            // 检查细分场景关键词（如果有）
            if (subSceneKeywords != null && !subSceneKeywords.isEmpty()) {
                boolean matchesSub = subSceneKeywords.stream()
                        .anyMatch(keyword -> lowerOperation.contains(keyword.toLowerCase()));
                return matchesSub;
            }

            return true;
        }

        public String getSceneName() {
            return sceneName;
        }

        public String getSubScene() {
            return subScene;
        }

        public List<RuleAction> getActions() {
            return actions;
        }

        /**
         * 生成结构化操作描述
         */
        public String generateOperationDescription() {
            StringBuilder sb = new StringBuilder();
            sb.append("场景：").append(sceneName);
            if (subScene != null && !subScene.isEmpty()) {
                sb.append("（").append(subScene).append("）");
            }
            sb.append("\n\n请按照以下操作列表逐一执行：\n\n");
            
            for (int i = 0; i < actions.size(); i++) {
                RuleAction action = actions.get(i);
                sb.append("操作").append(i + 1).append("：").append(action.getAction()).append(" - ").append(action.getType()).append("\n");
                sb.append("  具体内容：").append(action.getContent()).append("\n");
                
                // 添加操作说明
                switch (action.getAction()) {
                    case "删除":
                        if ("产品包含".equals(action.getType())) {
                            sb.append("  说明：从\"产品包含\"字段中删除该产品选项\n");
                        } else if ("替换规则".equals(action.getType())) {
                            sb.append("  说明：删除与该产品相关的所有替换规则\n");
                        }
                        break;
                    case "添加":
                        if ("产品包含".equals(action.getType())) {
                            sb.append("  说明：在\"产品包含\"字段中添加该产品选项\n");
                        } else if ("替换规则".equals(action.getType())) {
                            sb.append("  说明：添加该产品的替换规则\n");
                        } else if ("后备规则".equals(action.getType())) {
                            sb.append("  说明：添加该产品的后备替换规则\n");
                        }
                        break;
                    case "替换":
                        sb.append("  说明：将文案中所有出现的旧名称替换为新名称\n");
                        break;
                    case "保留":
                        sb.append("  说明：确保该规则在修改后的文案中仍然存在\n");
                        break;
                }
                sb.append("\n");
            }
            
            sb.append("重要提示：\n");
            sb.append("- 请严格按照上述操作顺序执行\n");
            sb.append("- 每个操作都必须准确完成\n");
            sb.append("- 确保产品名称完全匹配，不要误操作其他产品\n");
            sb.append("- 保留其他所有未提及的内容不变\n");
            
            return sb.toString();
        }
    }

    /**
     * 规则动作
     */
    public static class RuleAction {
        private final String action; // 删除、添加、替换、保留
        private final String type;   // 产品包含、替换规则、后备规则、出餐规则
        private final String content; // 具体内容

        public RuleAction(String action, String type, String content) {
            this.action = action;
            this.type = type;
            this.content = content;
        }

        public String getAction() {
            return action;
        }

        public String getType() {
            return type;
        }

        public String getContent() {
            return content;
        }
    }
}

