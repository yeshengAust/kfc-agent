-- 迁移脚本：添加system_config表和task表的modification_type字段
-- 执行时间：2025-12-22

USE `kfc_aigc`;

-- 1. 创建系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_type` VARCHAR(50) NOT NULL DEFAULT 'string' COMMENT '配置类型（string/number/boolean/json）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
  `is_encrypted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否加密存储（0-否，1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 2. 插入系统配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `is_encrypted`) VALUES
('qwen.api.base-url', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'string', '千问模型API基础URL', 0),
('qwen.api.key', 'sk-eac61c69a1714ad88dcb82eef6127239', 'string', '千问模型API密钥', 1),
('qwen.api.model-name', 'qwen-plus', 'string', '千问模型名称', 0),
('qwen.api.temperature', '0.1', 'number', '模型温度参数', 0)
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- 3. 为task表添加modification_type字段（如果不存在）
ALTER TABLE `task` 
ADD COLUMN IF NOT EXISTS `modification_type` VARCHAR(100) DEFAULT NULL COMMENT '修改类型（删除/新增/修改/替换/加回等，多个类型用逗号分隔）' 
AFTER `modification_points`;
