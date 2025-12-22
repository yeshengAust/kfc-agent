package com.yes.kfcaigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 大任务表（父任务）
 */
@Data
@TableName("parent_task")
public class ParentTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    @TableField("name")
    private String name;

    /**
     * 操作指令
     */
    @TableField("operation")
    private String operation;

    /**
     * 活动编码列表（逗号分隔）
     */
    @TableField("activity_codes")
    private String activityCodes;

    /**
     * 子任务总数
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 成功数量
     */
    @TableField("success_count")
    private Integer successCount;

    /**
     * 失败数量
     */
    @TableField("fail_count")
    private Integer failCount;

    /**
     * 处理中数量
     */
    @TableField("processing_count")
    private Integer processingCount;

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
