package com.yes.kfcaigc.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yes.kfcaigc.entity.SystemConfig;
import com.yes.kfcaigc.mapper.SystemConfigMapper;
import org.springframework.stereotype.Repository;

/**
 * 系统配置Repository
 */
@Repository
public class SystemConfigRepository extends ServiceImpl<SystemConfigMapper, SystemConfig> {

    /**
     * 根据配置键获取配置值
     */
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 根据配置键获取配置值，带默认值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 更新配置值
     */
    public boolean updateConfigValue(String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = getOne(wrapper);
        if (config != null) {
            config.setConfigValue(configValue);
            return updateById(config);
        }
        return false;
    }
}
