package com.yes.kfcaigc.controller;

import com.yes.kfcaigc.entity.KnowledgeBase;
import com.yes.kfcaigc.repository.KnowledgeBaseRepository;
import com.yes.kfcaigc.service.ModificationTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;
    
    @Autowired
    private ModificationTypeService modificationTypeService;

    /**
     * 保存到知识库（如果 operation 已存在则覆盖更新，不存在则新增）
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveKnowledge(@RequestBody Map<String, String> request) {
        try {
            String operation = request.get("operation");
            String result = request.get("result");
            String type = request.get("type"); // 从前端获取type

            // 参数校验
            if (operation == null || operation.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "操作指令不能为空"));
            }

            if (result == null || result.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "修改结果不能为空"));
            }
            
            // 如果前端没有传type，则自动识别
            if (type == null || type.trim().isEmpty()) {
                List<String> identifiedTypes = modificationTypeService.identifyTypes(operation);
                type = identifiedTypes.isEmpty() ? "" : String.join(",", identifiedTypes);
                log.info("自动识别修改类型: operation={}, type={}", operation, type);
            }

            // 查询是否已存在相同的 operation
            KnowledgeBase existingKnowledge = knowledgeBaseRepository.findByExactOperation(operation);

            if (existingKnowledge != null) {
                // 已存在，覆盖更新
                existingKnowledge.setResult(result);
                existingKnowledge.setType(type); // 更新type
                knowledgeBaseRepository.updateKnowledgeBase(existingKnowledge);
                
                log.info("知识库已存在，覆盖更新，operation: {}, type: {}", operation, type);
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "更新成功（覆盖了原有记录）"
                ));
            } else {
                // 不存在，新增
                KnowledgeBase knowledgeBase = new KnowledgeBase();
                knowledgeBase.setOperation(operation);
                knowledgeBase.setType(type); // 设置type
                knowledgeBase.setResult(result);
                knowledgeBaseRepository.saveKnowledgeBase(knowledgeBase);

                log.info("成功添加知识库，operation: {}, type: {}", operation, type);
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "添加成功"
                ));
            }

        } catch (Exception e) {
            log.error("保存知识库失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "保存失败：" + e.getMessage()));
        }
    }
}
