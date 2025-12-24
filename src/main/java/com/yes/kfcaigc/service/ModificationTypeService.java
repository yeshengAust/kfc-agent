package com.yes.kfcaigc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yes.kfcaigc.entity.KnowledgeBase;
import com.yes.kfcaigc.entity.ModificationTypeRule;
import com.yes.kfcaigc.repository.KnowledgeBaseRepository;
import com.yes.kfcaigc.repository.ModificationTypeRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 修改类型识别服务
 */
@Slf4j
@Service
public class ModificationTypeService {
    
    @Autowired
    private ModificationTypeRuleRepository modificationTypeRuleRepository;
    
    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;
    
    /**
     * 从operation中识别修改类型（可识别多个类型）
     * @return 识别到的修改类型列表（按优先级降序）
     */
    public List<String> identifyTypes() {
        // 获取所有修改类型规则
        List<ModificationTypeRule> rules = modificationTypeRuleRepository.getAllRules();
        rules.sort(Comparator.comparingInt(ModificationTypeRule::getPriority).reversed());
        // 按优先级降序排序
        return rules.stream()
                .map(ModificationTypeRule::getType)
                .collect(Collectors.toList());

    }
    
    /**
     * 根据类型列表查询知识库
     * @param types 修改类型列表
     * @return 匹配的知识库记录列表
     */
    public List<KnowledgeBase> findKnowledgeBaseByTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<KnowledgeBase> result = new ArrayList<>();
        
        for (String type : types) {
            LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
            // 使用FIND_IN_SET或LIKE查询（因为type字段可能包含多个类型，用逗号分隔）
            wrapper.apply("FIND_IN_SET({0}, type)", type);
            List<KnowledgeBase> kbList = knowledgeBaseRepository.list(wrapper);
            result.addAll(kbList);
        }
        
        log.info("根据类型查询到的知识库记录数: types={}, count={}", types, result.size());
        return result;
    }
    
    /**
     * 根据类型列表和场景查询知识库
     * @return 匹配的知识库记录列表
     */
    public List<KnowledgeBase> findKnowledgeBaseByTypesAndScene() {
        return  knowledgeBaseRepository.findAll();


    }
    /**
     * 获取所有修改类型规则（用于提供给AI）
     * @return 所有规则的详细信息
     */
    public List<ModificationTypeRule> getAllRules() {
        return modificationTypeRuleRepository.getAllRules();
    }
}
