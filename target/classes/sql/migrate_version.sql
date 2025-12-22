-- =====================================================
-- 数据库迁移脚本：添加版本管理功能
-- 执行前请务必备份数据库！
-- =====================================================

USE `kfc_aigc`;

-- 1. 创建活动版本关系表（记录每个版本的完整信息）
CREATE TABLE IF NOT EXISTS `activity_version` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_code` VARCHAR(100) NOT NULL COMMENT '活动编码',
  `version` INT(11) NOT NULL COMMENT '版本号',
  `description` TEXT NOT NULL COMMENT '券文案描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_version` (`activity_code`, `version`),
  KEY `idx_activity_code` (`activity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动版本关系表';

-- 2. 为现有活动初始化版本信息（版本号1，使用当前 coupon_info 的 description）
INSERT INTO `activity_version` (`activity_code`, `version`, `description`)
SELECT `activity_code`, 1, `description`
FROM `coupon_info`
WHERE `activity_code` NOT IN (SELECT `activity_code` FROM `activity_version`);

-- =====================================================
-- 迁移完成！
-- coupon_info 表保持不变，activity_version 记录每个版本的完整历史
-- =====================================================
