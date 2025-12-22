package com.yes.kfcaigc.model;

import lombok.Data;

import java.util.List;

@Data
public class CouponGenerationResponse {
    /**
     * 修改后的券文案
     */
    private String couponText;
    
    /**
     * 操作类型
     */
    private String scene;
    
    /**
     * 目标内容
     */
    private String subScene;
    
    /**
     * 改动点列表
     */
    private List<ModificationPointDTO> modificationPoints;
    
    /**
     * 修改类型（删除、新增、修改、替换、加回等）
     */
    private String modificationType;
    
    /**
     * 问题列表（检查发现的问题）
     */
    private List<String> issues;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
}

