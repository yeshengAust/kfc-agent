package com.yes.kfcaigc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes.kfcaigc.model.Segment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 片段抽取服务
 * AI1：从整段券文案中选出「可能需要修改」的片段
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SegmentExtractionService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 抽取可能需要修改的片段
     */
    public List<Segment> extractSegments(String originalText, String operation) {
        try {
            String prompt = buildExtractionPrompt(originalText, operation);
            String json = chatModel.generate(prompt);
            log.info("Segment extractor raw output: {}", json);
            return parseSegmentsFromJson(json);
        } catch (Exception e) {
            log.warn("片段抽取失败，回退为不分片处理", e);
            return new ArrayList<>();
        }
    }

    private String buildExtractionPrompt(String originalText, String operation) {
        return """
                你是一个文案解析助手，需要从整段券文案中找出「本次操作最有可能会影响到的片段」。

                【操作指令】
                %s

                【原始券文案】
                %s

                请选择所有【你认为本次操作很可能需要修改或插入位置附近】的片段，要求：
                - 怎么确定一个产品？形容词加名词，北京烤鸭和南京烤鸭不是一个东西！，脆皮鸡和黄金脆皮鸡也不是一个东西！
                - 优先选择：包含操作指令中出现的产品名、规则名、关键短语的行或段落；
                - 如果操作只是“新增一句说明/规则”，只需选出你认为需要插入位置附近的 1～3 个相关片段即可；
                - 不要因为一行是“产品包含”“常规”“替换规则”就必选，只有当它和本次操作直接相关时才选；
                - 与本次操作明显无关的内容（比如其他产品、退款规则、支付方式等）不要选。
                -记住如果设计到一个产品那对这个产品的操作肯定要确定这个产品的名字是一模一样的而不是比他字少的，不要过度思考！
                                - 仅基于原文内容，判断该片段是否与操作产品直接相关。若相关，只说明‘该片段包含 [具体内容]，与操作产品直接相关’；若不相关，说明‘该片段与操作产品无关’。禁止额外联想简称对应的全称，禁止推测后续调整方案


                片段可以是单行或一小段连续的文本，但不要把整篇全文都选进来。

                请严格输出 JSON，格式如下（下面的 type/reason 只是示例，实际内容请根据你选的片段自由填写）：

                {
                  "segments": [
                    {
                      "id": "seg1",
                      "type": "片段类型示例（如：产品包含/替换规则/时间限制等）",
                      "reason": "为什么认为这个片段会被本次操作影响",
                      "text": "这里填选中的原文片段1"
                    },
                    {
                      "id": "seg2",
                      "type": "片段类型示例",
                      "reason": "选择原因示例",
                      "text": "这里填选中的原文片段2"
                    }
                  ]
                }

                只输出 JSON，不要任何解释。
                """.formatted(operation, originalText);
    }

    private List<Segment> parseSegmentsFromJson(String json) throws IOException {
        List<Segment> segments = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return segments;
        }

        JsonNode root = objectMapper.readTree(json);
        JsonNode segArray = root.get("segments");
        if (segArray == null || !segArray.isArray()) {
            return segments;
        }

        Iterator<JsonNode> iterator = segArray.elements();
        while (iterator.hasNext()) {
            JsonNode node = iterator.next();
            Segment seg = new Segment();
            seg.setId(getTextSafe(node, "id"));
            seg.setType(getTextSafe(node, "type"));
            seg.setReason(getTextSafe(node, "reason"));
            seg.setText(getTextSafe(node, "text"));

            if (seg.getText() != null && !seg.getText().isEmpty()) {
                segments.add(seg);
            }
        }

        return segments;
    }

    private String getTextSafe(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asText() : null;
    }
}


