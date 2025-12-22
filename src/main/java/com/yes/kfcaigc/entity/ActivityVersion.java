package com.yes.kfcaigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动版本关系表（记录每个版本的完整信息）
 */
@Data
@TableName("activity_version")
public class ActivityVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 活动编码
     */
    @TableField("activity_code")
    private String activityCode;

    /**
     * 版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 券文案描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
