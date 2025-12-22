-- 迁移脚本：为task表添加modification_type字段
-- 执行时间：2025-12-22

USE `kfc_aigc`;

-- 检查并添加 modification_type 字段
ALTER TABLE `task` 
ADD COLUMN `modification_type` VARCHAR(100) DEFAULT NULL COMMENT '修改类型（删除/新增/修改/替换/加回等，多个类型用逗号分隔）' 
AFTER `modification_points`;
