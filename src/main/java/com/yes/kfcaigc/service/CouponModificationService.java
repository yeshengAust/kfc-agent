package com.yes.kfcaigc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes.kfcaigc.entity.KnowledgeBase;
import com.yes.kfcaigc.entity.ModificationTypeRule;
import com.yes.kfcaigc.repository.KnowledgeBaseRepository;
import com.yes.kfcaigc.tool.ModificationTracker;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
public class CouponModificationService {

    private final ChatLanguageModel chatModel;
    private final SceneRuleService sceneRuleService;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final ModificationTypeService modificationTypeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CouponModificationService(ChatLanguageModel chatModel,
                                     SceneRuleService sceneRuleService,
                                     KnowledgeBaseRepository knowledgeBaseRepository,
                                     ModificationTypeService modificationTypeService) {
        this.chatModel = chatModel;
        this.sceneRuleService = sceneRuleService;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.modificationTypeService = modificationTypeService;
    }

    /**
     * 修改券文案
     * 单AI模式：根据 originalText + operation 直接修改并返回完整文案和改动点
     */
    public ModificationResult modifyCouponText(String originalText, String operation) {
        // 生成请求ID（用于全局改动点）
        String requestId = ModificationTracker.createNewRequestId();
        log.info("生成请求ID：{}", requestId);

        // 识别场景规则
        SceneRuleService.SceneRule sceneRule = sceneRuleService.identifyScene(operation);
        final String structuredRules;
        final String sceneName;
        if (sceneRule != null) {
            structuredRules = sceneRule.generateOperationDescription();
            sceneName = sceneRule.getSceneName();
            log.info("识别到场景规则：{}，规则内容：\n{}", sceneName, structuredRules);
        } else {
            structuredRules = null;
            sceneName = null;
        }
        
        // 1. 从 operation 中识别修改类型（通过关键词匹配）
        List<String> identifiedTypes = modificationTypeService.identifyTypes();
        log.info("识别到的修改类型: {}", identifiedTypes);
        
        // 2. 根据类型查询对应的知识库
        List<KnowledgeBase> knowledgeList = new ArrayList<>();
        if (!identifiedTypes.isEmpty()) {
            knowledgeList = modificationTypeService.findKnowledgeBaseByTypesAndScene();
            log.info("根据类型 {} 和场景 {} 查询到知识库记录：{} 条", identifiedTypes, sceneName, knowledgeList.size());
        } else {
            log.warn("未识别到任何修改类型，不查询知识库");
        }
        
        // 3. 获取所有修改类型规则（提供给AI参考）
        List<ModificationTypeRule> allRules = modificationTypeService.getAllRules();
        log.info("获取到修改类型规则：{} 条", allRules.size());

        try {
            log.info("AI开始执行修改操作，操作指令：{}", operation);
            String modificationPrompt = buildModificationPrompt(originalText, operation, requestId, structuredRules, knowledgeList, allRules, identifiedTypes);
            String modifiedText = chatModel.generate(modificationPrompt);

            log.info("AI已生成修改后的文案");

            // 解析 AI 输出，提取工具调用（包括修改类型）
            String[] parseResult = parseToolCalls(modifiedText, requestId);
            modifiedText = parseResult[0];  // 清理后的文本
            String aiModificationType = parseResult[1];  // AI返回的修改类型

            // 从 TOOL_CALL 中取改动点JSON字符串
            List<String> changesJson = ModificationTracker.getChanges(requestId);
            ModificationTracker.removeChanges(requestId);

            // 解析JSON为ModificationPoint对象列表
            List<ModificationPoint> modificationPoints = parseModificationPoints(changesJson);

            log.info("修改完成，返回结果。改动点：{} 条，修改类型：{}", modificationPoints.size(), aiModificationType);
            ModificationResult result = new ModificationResult(modifiedText, modificationPoints);
            result.setModificationType(aiModificationType != null ? aiModificationType : String.join(",", identifiedTypes));
            return result;

        } catch (Exception e) {
            log.error("修改券文案失败", e);
            ModificationTracker.removeChanges(requestId);
            throw new RuntimeException("修改券文案失败：" + e.getMessage(), e);
        }
    }

    
    /**
     * 解析AI输出中的工具调用
     * AI可能按照格式输出：TOOL_CALL:recordChanges(requestId, "改动点1,改动点2", "修改类型")
     * 支持多种格式：
     * - TOOL_CALL:recordChanges("requestId", "改动点", "类型")
     * - TOOL_CALL:recordChanges(requestId, "改动点", "类型")
     * - recordChanges("requestId", "改动点", "类型")
     * 
     * @return String[]：[0]=清理后的文本, [1]=修改类型
     */
    private String[] parseToolCalls(String aiOutput, String requestId) {
        String cleanedText = aiOutput;
        String modificationType = null;
        
        if (aiOutput == null || aiOutput.isEmpty()) {
            return new String[]{aiOutput, null};
        }
        
        // 查找工具调用格式：TOOL_CALL:recordChanges(...) 或 recordChanges(...)
        String[] patterns = {
            "TOOL_CALL:recordChanges(",
            "recordChanges("
        };
        
        for (String pattern : patterns) {
            int startIndex = aiOutput.indexOf(pattern);
            if (startIndex == -1) {
                continue;
            }
            
            int contentStart = startIndex + pattern.length();
            // 查找匹配的右括号（考虑嵌套引号）
            int contentEnd = findMatchingParenthesis(aiOutput, contentStart);
            if (contentEnd == -1) {
                continue;
            }
            
            String toolCallContent = aiOutput.substring(contentStart, contentEnd);
            // 解析参数：可能是 (requestId, "改动点", "类型") 或 ("requestId", "改动点", "类型")
            String[] extracted = extractToolCallParams(toolCallContent);
            String changesStr = extracted[0];
            modificationType = extracted[1];
            
            if (changesStr != null && !changesStr.isEmpty()) {
                ModificationTracker.recordChanges(requestId, changesStr);
                log.info("解析到工具调用，记录改动点：{}，修改类型：{}", changesStr, modificationType);
            }
        }
        
        // 清理正文中的工具调用语句，避免出现在返回的 couponText 中
        String[] lines = aiOutput.split("\\r?\\n");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            boolean isToolLine = false;
            for (String pattern : patterns) {
                if (line.contains(pattern)) {
                    isToolLine = true;
                    break;
                }
            }
            if (!isToolLine) {
                if (cleaned.length() > 0) {
                    cleaned.append("\n");
                }
                cleaned.append(line);
            }
        }
        
        cleanedText = cleaned.toString();
        return new String[]{cleanedText, modificationType};
    }
    
    /**
     * 查找匹配的右括号
     */
    private int findMatchingParenthesis(String text, int start) {
        int depth = 1;
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == '\'' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inSingleQuote = !inSingleQuote;
            } else if (!inDoubleQuote && !inSingleQuote) {
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * 从工具调用内容中提取改动点字符串和修改类型
     * 返回：[0]=改动点JSON字符串, [1]=修改类型
     */
    private String[] extractToolCallParams(String toolCallContent) {
        // 分割参数（考虑引号内的逗号）
        List<String> params = new ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        
        for (int i = 0; i < toolCallContent.length(); i++) {
            char c = toolCallContent.charAt(i);
            
            if (c == '"' && (i == 0 || toolCallContent.charAt(i - 1) != '\\')) {
                inDoubleQuote = !inDoubleQuote;
                currentParam.append(c);
            } else if (c == '\'' && (i == 0 || toolCallContent.charAt(i - 1) != '\\')) {
                inSingleQuote = !inSingleQuote;
                currentParam.append(c);
            } else if (c == ',' && !inDoubleQuote && !inSingleQuote) {
                // 分隔符，保存当前参数
                params.add(currentParam.toString().trim());
                currentParam = new StringBuilder();
            } else {
                currentParam.append(c);
            }
        }
        // 添加最后一个参数
        if (currentParam.length() > 0) {
            params.add(currentParam.toString().trim());
        }
        
        // 提取参数：第1个是requestId，第2个是改动点JSON，第3个是修改类型
        String changesStr = null;
        String modificationType = null;
        
        if (params.size() >= 2) {
            // 第2个参数是改动点JSON
            changesStr = removeQuotes(params.get(1));
        }
        
        if (params.size() >= 3) {
            // 第3个参数是修改类型
            modificationType = removeQuotes(params.get(2));
        }
        
        return new String[]{changesStr, modificationType};
    }
    
    /**
     * 移除外层引号并去转义
     */
    private String removeQuotes(String str) {
        if (str == null) {
            return null;
        }
        str = str.trim();
        // 移除外层引号
        if (str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length() - 1);
            // 去转义：将 \\" 替换为 "
            str = str.replace("\\\"", "\"");
        } else if (str.startsWith("\'") && str.endsWith("\'")) {
            str = str.substring(1, str.length() - 1);
            // 去转义：将 \\' 替换为 '
            str = str.replace("\\\'", "\'");
        }
        return str;
    }
    
    /**
     * 解析修改点JSON为ModificationPoint对象列表
     */
    private List<ModificationPoint> parseModificationPoints(List<String> changesJson) {
        List<ModificationPoint> result = new ArrayList<>();
        if (changesJson == null || changesJson.isEmpty()) {
            return result;
        }
        
        for (String jsonStr : changesJson) {
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                continue;
            }
            
            try {
                // 尝试解析JSON数组格式: [{"origin":"...","now":"..."}, ...]
                JsonNode rootNode = objectMapper.readTree(jsonStr);
                
                if (rootNode.isArray()) {
                    // 如果是JSON数组
                    for (JsonNode node : rootNode) {
                        String origin = node.has("origin") ? node.get("origin").asText() : "";
                        String now = node.has("now") ? node.get("now").asText() : "";
                        
                        // 过滤：如果origin和now完全相同，说明没有实际修改，跳过
                        if (origin != null && now != null && origin.equals(now)) {
                            log.warn("过滤掉无效修改点：origin和now完全相同: {}", origin);
                            continue;
                        }
                        
                        result.add(new ModificationPoint(origin, now));
                    }
                } else if (rootNode.isObject()) {
                    // 如果是单个JSON对象
                    String origin = rootNode.has("origin") ? rootNode.get("origin").asText() : "";
                    String now = rootNode.has("now") ? rootNode.get("now").asText() : "";
                    
                    // 过滤：如果origin和now完全相同，说明没有实际修改，跳过
                    if (origin != null && now != null && origin.equals(now)) {
                        log.warn("过滤掉无效修改点：origin和now完全相同: {}", origin);
                    } else {
                        result.add(new ModificationPoint(origin, now));
                    }
                }
            } catch (Exception e) {
                log.warn("无法解析修改点JSON: {}", jsonStr, e);
                // 如果解析失败，尝试作为简单字符串处理（兼容旧格式）
                result.add(new ModificationPoint(jsonStr, ""));
            }
        }
        
        return result;
    }
        
    /**
     * 修改点
     */
    public static class ModificationPoint {
        private final String origin;  // 原句
        private final String now;     // 现在的句子
        
        public ModificationPoint(String origin, String now) {
            this.origin = origin;
            this.now = now;
        }
        
        public String getOrigin() {
            return origin;
        }
        
        public String getNow() {
            return now;
        }
    }
    
    /**
     * 修改结果
     */
    public static class ModificationResult {
        private final String modifiedText;
        private final List<ModificationPoint> modificationPoints;
        private String modificationType;  // 新增：修改类型
        
        public ModificationResult(String modifiedText, List<ModificationPoint> modificationPoints) {
            this.modifiedText = modifiedText;
            this.modificationPoints = modificationPoints;
        }
        
        public String getModifiedText() {
            return modifiedText;
        }
        
        public List<ModificationPoint> getModificationPoints() {
            return modificationPoints;
        }
        
        public String getModificationType() {
            return modificationType;
        }
        
        public void setModificationType(String modificationType) {
            this.modificationType = modificationType;
        }
    }

    /**
     * 构建修改提示
     */
    private String buildModificationPrompt(String originalText, String operation, String requestId, String structuredRules, List<KnowledgeBase> knowledgeList, List<ModificationTypeRule> allRules, List<String> identifiedTypes) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个专业的文案编辑助手。请根据操作指令精确修改以下肯德基兑换券文案。\n\n");
        promptBuilder.append("原始券文案：\n");
        promptBuilder.append(originalText).append("\n\n");
        promptBuilder.append("操作指令：").append(operation).append("\n\n");
        
        // 添加修改类型规则说明
        if (allRules != null && !allRules.isEmpty()) {
            promptBuilder.append("【修改类型规则 - 了解你的操作类型】\n");
            promptBuilder.append("系统定义了以下修改类型规则，帮助你理解当前操作的性质：\n\n");
            
            for (ModificationTypeRule rule : allRules) {
                promptBuilder.append("◆ 类型：").append(rule.getType()).append("\n");
                promptBuilder.append("  关键词：").append(rule.getKeywords()).append("\n");
                promptBuilder.append("  规则详情：").append(rule.getRuleDetail()).append("\n\n");
            }
        }
        
        // 说明当前识别到的修改类型
        if (identifiedTypes != null && !identifiedTypes.isEmpty()) {
            promptBuilder.append("【当前操作识别到的修改类型】\n");
            promptBuilder.append("根据操作指令的关键词分析，本次操作属于以下类型：");
            promptBuilder.append(String.join("、", identifiedTypes)).append("\n");
            promptBuilder.append("请参考对应类型的规则详情进行操作。\n\n");
        }

        // 知识库操作类型约束：仅允许新增、删除、修改、查询
        promptBuilder.append("【知识库操作类型说明】\n");
        promptBuilder.append("知识库中的操作类型仅允许以下四种：新增、删除、修改、查询。\n");
        promptBuilder.append("其中，“加回”“恢复”“还原”“放回”等本质上视为“修改”，请统一归类为“修改”。\n");
        promptBuilder.append("在调用 TOOL_CALL:recordChanges(...) 时，第三个参数必须从 {新增, 删除, 修改, 查询} 中选择；如果本次属于“加回/恢复/还原/放回”，请使用“修改”。\n\n");
        
        // 如果有知识库，显示相关案例
        if (knowledgeList != null && !knowledgeList.isEmpty()) {
            promptBuilder.append("【知识库 - 历史成功案例参考】\n");
            promptBuilder.append("以下是类似操作的历史成功案例，供你参考学习：\n\n");
            
            for (int i = 0; i < knowledgeList.size(); i++) {
                KnowledgeBase kb = knowledgeList.get(i);
                promptBuilder.append("案例").append(i + 1).append("：\n");
                promptBuilder.append("操作：").append(kb.getOperation()).append("\n");
                promptBuilder.append("修改结果：").append(kb.getResult()).append("\n\n");
            }
            
            promptBuilder.append("请参考以上案例的处理方式，但要根据当前的实际情况进行灵活调整。\n\n");
        }
        
        // 如果有结构化规则，优先使用结构化规则
        if (structuredRules != null && !structuredRules.isEmpty()) {
            promptBuilder.append("【结构化规则 - 必须严格按照以下规则执行】\n");
            promptBuilder.append(structuredRules).append("\n\n");
            promptBuilder.append("请严格按照上述结构化规则执行修改，确保每个操作都准确完成。\n\n");
        }
        
        promptBuilder.append("【关键要求 - 必须严格遵守】\n");
        promptBuilder.append("1. 产品名称识别规则（最重要）：\n");
        promptBuilder.append("   - 必须从操作指令中提取完整、准确的产品名称\n");
        promptBuilder.append("   - 只能操作与提取的产品名称完全匹配的产品，不能误操作名称相似但无关的其他产品\n");
        promptBuilder.append("   - 产品名称必须完全匹配，包括括号内的内容（如带规格的名称）\n");
        promptBuilder.append("   - 注意：'黄金脆皮鸡'和'脆皮鸡'是不同的产品，不能混淆\n");
        promptBuilder.append("   - 在\"产品包含\"中查找时，要匹配完整的产品名称，不能只匹配部分文字\n\n");
        
        promptBuilder.append("2. 删除操作的特殊要求（非常重要）：\n");
        promptBuilder.append("   当操作指令是删除某个产品时，你必须：\n");
        promptBuilder.append("   a) 在\"产品包含\"区域，删除该产品及其规格（如删除'黄金脆皮鸡（1块装）'）\n");
        promptBuilder.append("   b) 在替换规则/后备规则中，删除所有提到该产品的完整句子\n");
        promptBuilder.append("      - 例如：'在不售卖黄金脆皮鸡的餐厅，产品将替换为吮指原味鸡' 要整句删除\n");
        promptBuilder.append("      - 例如：'在不售卖吮指原味鸡的餐厅，产品将替换为黄金脆皮鸡' 也要整句删除\n");
        promptBuilder.append("   c) 必须保留通用规则（与具体产品无关的说明）\n");
        promptBuilder.append("      - 例如：'脆皮鸡/原味鸡出餐部位随机搭配，详情以实物为准' 是通用规则，要保留\n");
        promptBuilder.append("      - 判断标准：如果规则适用于产品类别（如所有脆皮鸡类产品），而不是特指某个具体产品，则为通用规则\n");
        promptBuilder.append("   d) 查找时要全面：从文案开头到结尾，逐行检查，找出所有包含该产品名称的位置\n\n");
        
        promptBuilder.append("3. 操作执行规则：\n");
        promptBuilder.append("   - 如果是删除操作（如\"删除XXX\"、\"移除XXX\"等）：\n");
        promptBuilder.append("     * 从\"产品包含\"中删除与提取的产品名称完全匹配的产品选项（包括完整的产品名称）\n");
        promptBuilder.append("     * 删除所有与该产品相关的替换规则（如\"在不售卖XXX的餐厅...\"等规则）\n");
        promptBuilder.append("     * 绝对不能删除其他产品，即使它们包含相同的字词\n");
        promptBuilder.append("     * 绝对不能删除通用规则（适用于产品类别的规则）\n");
        promptBuilder.append("   - 如果是替换操作（如\"将XXX替换为YYY\"、\"XXX改为YYY\"等）：\n");
        promptBuilder.append("     * 将文案中所有出现的完整产品名称（XXX）替换为新名称（YYY）\n");
        promptBuilder.append("     * 包括\"产品包含\"中的产品名称和替换规则中的产品名称\n");
        promptBuilder.append("     * 只替换完全匹配的产品名称，不替换其他包含相同字词的产品\n\n");
        
        promptBuilder.append("4. 操作步骤（请按此顺序执行）：\n");
        promptBuilder.append("   步骤1：从操作指令中提取要操作的产品名称（完整名称）\n");
        promptBuilder.append("   步骤2：在原始券文案中从头到尾逐行搜索，找出所有包含该产品名称的位置\n");
        promptBuilder.append("   步骤3：对每个找到的位置，判断是否应该修改（产品包含要删、替换规则要删、通用规则要保留）\n");
        promptBuilder.append("   步骤4：执行修改操作，确保没有遗漏，也没有误删\n");
        promptBuilder.append("   步骤5：再次检查修改后的文案，确认该产品已完全删除（除了通用规则）\n\n");
        
//        promptBuilder.append("5. 其他要求：\n");
//        promptBuilder.append("   - 保留其他所有内容不变，包括格式、其他产品、使用有效期、使用时间、餐厅限制、核销渠道、退款规则等\n");
//        promptBuilder.append("   - 保持原始文案的格式和结构\n");
//        promptBuilder.append("   - 不要添加或删除任何未要求修改的内容\n");
//        promptBuilder.append("   - 保持语言风格一致\n");
//        promptBuilder.append("   - 确保所有规则逻辑正确\n");
//        promptBuilder.append("   - 确保修改后的文案格式完整、逻辑清晰\n\n");
        
        promptBuilder.append("请严格按照以上要求执行，确保只操作指定的产品，不要误操作其他产品。\n\n");
        
        promptBuilder.append("【改动点记录工具】（只在系统内部使用，用户看不到工具调用本身）\n");
        promptBuilder.append("完成修改后，请使用以下工具记录你实际修改或删除的原文句子和修改后的句子，以及本次修改的类型：\n");
        promptBuilder.append("TOOL_CALL:recordChanges(\"").append(requestId).append("\", \"JSON格式的改动点\", \"修改类型\")\n");
        promptBuilder.append("格式要求：\n");
        promptBuilder.append("   - 参数1：requestId（已提供）\n");
        promptBuilder.append("   - 参数2：改动点列表，必须输出JSON数组格式：[{\\\"origin\\\":\\\"原句\\\",\\\"now\\\":\\\"修改后的句子\\\"}, ...]\n");
        promptBuilder.append("   - 参数3：修改类型，从上述【修改类型规则】中选择最匹配的类型（如：删除、新增、修改、替换、加回），如有多个类型用逗号分隔\n");
        promptBuilder.append("   - origin：从原始文案中一字不差地复制被改动的原句或原始片段，且必须保持原文行的先后顺序，不能把上下文行位置对调\\n");
        promptBuilder.append("   - now：修改后的完整句子或片段；\\n");
        promptBuilder.append("     * 如果是删除操作，now 为空字符串 \"\";\\n");
        promptBuilder.append("     * 如果是纯新增一段/一句文本（原文中不存在这句话），origin 不能为空，请这样处理：\\n");
        promptBuilder.append("       - 在原文中找到你要插入新文本的位置；\\n");
        promptBuilder.append("       - 取该位置【上面1行原文 + 下面1行原文】按【原文中出现的顺序】拼成一段，作为 origin；\\n");
        promptBuilder.append("       - 在这段 origin 中的正确位置插入新文本，生成 now；\\n");
        promptBuilder.append("       - 这样 origin 和 now 都是带上下文的完整片段，而不是空字符串。\\n");
        promptBuilder.append("     * 如果是修改已有句子（替换、调整等），origin 为原句，now 为修改后的整句；\\n");
        promptBuilder.append("   - JSON中的引号和特殊字符要正确转义\\n");
        promptBuilder.append("   - 不要写你想象中应该存在的句子，只写实际原文中存在的句子，或携带真实上下文的新增片段\\n");
//        promptBuilder.append("示例（删除操作）：\"[{\\\"origin\\\":\\\"产品包含：任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）】\\\",\\\"now\\\":\\\"产品包含：任选4份【热辣香骨鸡（3块装）】\\\"},{\\\"origin\\\":\\\"在不售卖吮指原味鸡的餐厅，产品将替换为黄金脆皮鸡；\\\",\\\"now\\\":\\\"\\\"}]\"\n\n");

        promptBuilder.append("输出格式：\n");
        promptBuilder.append("1. 先输出修改后的完整券文案\n");
        promptBuilder.append("2. 然后在新的一行输出工具调用：TOOL_CALL:recordChanges(\"").append(requestId).append("\", \"改动点列表\", \"修改类型\")\n");
        promptBuilder.append("   示例：TOOL_CALL:recordChanges(\"").append(requestId).append("\", \"[{\\\"origin\\\":\\\"...\\\",\\\"now\\\":\\\"...\\\"}]\", \"删除\")\n");
        
        return promptBuilder.toString();
    }
}
