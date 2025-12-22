package com.yes.kfcaigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 券版本表
 */
@Data
@TableName("coupon_version")
public class CouponVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 券ID（关联coupon_info表）
     */
    @TableField("coupon_id")
    private Long couponId;

    /**
     * 版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 版本文案内容
     */
    @TableField("content")
    private String content;

    /**
     * 操作指令
     */
    @TableField("operation")
    private String operation;

    /**
     * 修改点记录（JSON格式）
     */
    @TableField("modification_points")
    private String modificationPoints;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableField("deleted")
    private Integer deleted;
}
