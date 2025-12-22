package com.yes.kfcaigc.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yes.kfcaigc.entity.KnowledgeBase;
import com.yes.kfcaigc.mapper.KnowledgeBaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库Repository
 */
@Slf4j
@Repository
public class KnowledgeBaseRepository extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> {

    /**
     * 查询所有知识库（未删除）
     */
    public List<KnowledgeBase> findAll() {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getDeleted, 0);
        wrapper.orderByDesc(KnowledgeBase::getCreateTime);
        return list(wrapper);
    }

    /**
     * 根据operation模糊查询
     */
    public List<KnowledgeBase> findByOperation(String operation) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getDeleted, 0);
        wrapper.like(KnowledgeBase::getOperation, operation);
        wrapper.orderByDesc(KnowledgeBase::getCreateTime);
        return list(wrapper);
    }

    /**
     * 根据operation精确查询（用于判断是否已存在）
     */
    public KnowledgeBase findByExactOperation(String operation) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getDeleted, 0);
        wrapper.eq(KnowledgeBase::getOperation, operation);
        wrapper.last("LIMIT 1");
        return getOne(wrapper);
    }

    /**
     * 保存知识库
     */
    public void saveKnowledgeBase(KnowledgeBase knowledgeBase) {
        save(knowledgeBase);
    }

    /**
     * 更新知识库
     */
    public void updateKnowledgeBase(KnowledgeBase knowledgeBase) {
        updateById(knowledgeBase);
    }
}
