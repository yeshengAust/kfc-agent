package com.yes.kfcaigc.model;

import lombok.Data;

/**
 * 文案片段（用于分片修改）
 */
@Data
public class Segment {

    /**
     * 片段ID，如 seg1/seg2
     */
    private String id;

    /**
     * 片段类型，如：产品包含 / 替换规则 / 后备规则 / 其他
     */
    private String type;

    /**
     * 被选中的原因说明
     */
    private String reason;

    /**
     * 原始片段文本
     */
    private String text;
}


