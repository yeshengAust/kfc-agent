package com.yes.kfcaigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务表
 */
@Data
@TableName("task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父任务ID（大任务ID）
     */
    @TableField("parent_task_id")
    private Long parentTaskId;

    /**
     * 任务状态（0-处理中，1-处理完成，2-处理失败）
     */
    @TableField("status")
    private Integer status;

    /**
     * 原始券文案
     */
    @TableField("original_text")
    private String originalText;

    /**
     * 修改后的券文案
     */
    @TableField("coupon_text")
    private String couponText;

    /**
     * 操作场景
     */
    @TableField("scene")
    private String scene;

    /**
     * 子场景
     */
    @TableField("sub_scene")
    private String subScene;

    /**
     * 修改点列表（JSON数组字符串）
     */
    @TableField("modification_points")
    private String modificationPoints;

    /**
     * 修改类型（删除/新增/修改/替换/加回等，多个类型用逗号分隔）
     */
    @TableField("modification_type")
    private String modificationType;

    /**
     * 错误信息（处理失败时）
     */
    @TableField("error_message")
    private String errorMessage;

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
}
