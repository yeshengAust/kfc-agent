package com.yes.kfcaigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库表
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作指令
     */
    @TableField("operation")
    private String operation;

    /**
     * 修改类型（删除/新增/修改/替换等，多个类型用逗号分隔）
     */
    @TableField("type")
    private String type;

    /**
     * 场景标识（如：通用/删除产品/替换产品等）
     */
    @TableField("scene")
    private String scene;

    /**
     * 修改结果（包含多个origin和now的JSON数组）
     */
    @TableField("result")
    private String result;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableField("deleted")
    private Integer deleted;
}
