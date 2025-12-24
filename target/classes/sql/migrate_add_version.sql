-- =====================================================
-- 数据库迁移脚本：为 coupon_info 表添加 version 字段
-- 执行前请务必备份数据库！
-- =====================================================

USE `kfc_aigc`;

-- 1. 为 coupon_info 表添加 version 字段（如果不存在）
ALTER TABLE `coupon_info` 
ADD COLUMN IF NOT EXISTS `version` INT(11) NOT NULL DEFAULT 1 COMMENT '当前版本号' 
AFTER `description`;

-- 2. 为现有数据初始化 version 字段为 1
UPDATE `coupon_info` SET `version` = 1 WHERE `version` IS NULL OR `version` = 0;

-- 3. 检查 activity_version 表是否已存在，如果不存在则创建
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

-- 4. 为现有的 coupon_info 记录在 activity_version 中创建初始版本（版本号1）
INSERT INTO `activity_version` (`activity_code`, `version`, `description`)
SELECT `activity_code`, 1, `description`
FROM `coupon_info`
WHERE `activity_code` NOT IN (
    SELECT DISTINCT `activity_code` FROM `activity_version`
);

-- =====================================================
-- 迁移完成！
-- coupon_info 表新增 version 字段
-- activity_version 表记录每个版本的完整历史
-- =====================================================
