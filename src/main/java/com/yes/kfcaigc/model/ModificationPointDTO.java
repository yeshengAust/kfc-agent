package com.yes.kfcaigc.model;

import lombok.Data;

@Data
public class ModificationPointDTO {
    /**
     * 原始句子
     */
    private String origin;
    
    /**
     * 修改后的句子
     */
    private String now;
    
    public ModificationPointDTO(String origin, String now) {
        this.origin = origin;
        this.now = now;
    }
}
