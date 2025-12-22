package com.yes.kfcaigc.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yes.kfcaigc.entity.ModificationTypeRule;
import com.yes.kfcaigc.mapper.ModificationTypeRuleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 修改类型规则表 Repository
 */
@Repository
public class ModificationTypeRuleRepository extends ServiceImpl<ModificationTypeRuleMapper, ModificationTypeRule> {
    
    /**
     * 获取所有修改类型规则（按优先级降序）
     */
    public List<ModificationTypeRule> getAllRules() {
        LambdaQueryWrapper<ModificationTypeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ModificationTypeRule::getPriority);
        return list(wrapper);
    }
    
    /**
     * 根据type获取规则
     */
    public ModificationTypeRule getByType(String type) {
        LambdaQueryWrapper<ModificationTypeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModificationTypeRule::getType, type);
        return getOne(wrapper);
    }
}
