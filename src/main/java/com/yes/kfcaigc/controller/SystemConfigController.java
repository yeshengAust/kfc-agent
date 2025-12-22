package com.yes.kfcaigc.controller;

import com.yes.kfcaigc.entity.SystemConfig;
import com.yes.kfcaigc.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/system/config")
public class SystemConfigController {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    /**
     * 获取所有配置（敏感信息脱敏）
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listConfigs() {
        try {
            List<SystemConfig> configs = systemConfigRepository.list();
            List<Map<String, Object>> result = configs.stream().map(config -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", config.getId());
                map.put("configKey", config.getConfigKey());
                map.put("configType", config.getConfigType());
                map.put("description", config.getDescription());
                map.put("isEncrypted", config.getIsEncrypted());
                
                // 敏感信息脱敏
                if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
                    String value = config.getConfigValue();
                    if (value != null && value.length() > 8) {
                        map.put("configValue", value.substring(0, 8) + "********");
                    } else {
                        map.put("configValue", "********");
                    }
                } else {
                    map.put("configValue", config.getConfigValue());
                }
                
                map.put("createTime", config.getCreateTime());
                map.put("updateTime", config.getUpdateTime());
                return map;
            }).toList();
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取配置列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取单个配置（敏感信息不返回）
     */
    @GetMapping("/{configKey}")
    public ResponseEntity<Map<String, Object>> getConfig(@PathVariable String configKey) {
        try {
            String value = systemConfigRepository.getConfigValue(configKey);
            if (value == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("configKey", configKey);
            result.put("configValue", value);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取配置失败，configKey: {}", configKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新配置
     */
    @PutMapping("/{configKey}")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable String configKey,
            @RequestBody Map<String, String> request) {
        try {
            String configValue = request.get("configValue");
            if (configValue == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "配置值不能为空"));
            }
            
            boolean success = systemConfigRepository.updateConfigValue(configKey, configValue);
            
            if (success) {
                log.info("配置更新成功，configKey: {}", configKey);
                return ResponseEntity.ok(Map.of("success", true, "message", "配置更新成功"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "配置不存在"));
            }
        } catch (Exception e) {
            log.error("更新配置失败，configKey: {}", configKey, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "更新失败：" + e.getMessage()));
        }
    }
}
