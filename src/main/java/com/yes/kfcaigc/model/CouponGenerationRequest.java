package com.yes.kfcaigc.model;

import lombok.Data;

@Data
public class CouponGenerationRequest {
    /**
     * 大任务名称（可选）
     */
    private String taskName;
    
    /**
     * 活动编码 / 券编码，用于从数据库查询原始券文案
     */
    private String activityCode;
    
    /**
     * 原始文案（可直接传入，不需要activityCode）
     */
    private String originalText;
    
    /**
     * 操作类型：自然语言描述，例如"删除黄金脆皮鸡"、"将黄金脆皮鸡替换为避风塘黄金脆皮鸡"
     */
    private String operation;
}

