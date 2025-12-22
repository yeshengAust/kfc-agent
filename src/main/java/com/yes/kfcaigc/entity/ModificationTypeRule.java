package com.yes.kfcaigc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改类型规则表
 */
@Data
@TableName("modification_type_rule")
public class ModificationTypeRule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 修改类型（删除/新增/修改/替换/加回等）
     */
    private String type;
    
    /**
     * 关键词（用于识别该类型，逗号分隔）
     */
    private String keywords;
    
    /**
     * 规则详情（自然语言描述，AI可直接读取）
     */
    private String ruleDetail;
    
    /**
     * 优先级（数值越大优先级越高）
     */
    private Integer priority;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}
