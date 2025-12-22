package com.yes.kfcaigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 券信息表
 */
@Data
@TableName("coupon_info")
public class CouponInfo implements Serializable {

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
     * 券文案描述
     */
    @TableField("description")
    private String description;

    /**
     * 当前版本号
     */
    @TableField("version")
    private Integer version;

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
