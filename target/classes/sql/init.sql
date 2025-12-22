-- 创建数据库
CREATE DATABASE IF NOT EXISTS `kfc_aigc` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `kfc_aigc`;

-- 券信息表
DROP TABLE IF EXISTS `coupon_info`;
CREATE TABLE `coupon_info` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_code` VARCHAR(100) NOT NULL COMMENT '活动编码',
  `description` TEXT NOT NULL COMMENT '券文案描述',
  `version` INT(11) NOT NULL DEFAULT 1 COMMENT '当前版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_code` (`activity_code`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='券信息表';

-- 活动版本关系表（记录每个版本的完整信息：活动编码、版本号、券文案描述）
DROP TABLE IF EXISTS `activity_version`;
CREATE TABLE `activity_version` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_code` VARCHAR(100) NOT NULL COMMENT '活动编码',
  `version` INT(11) NOT NULL COMMENT '版本号',
  `description` TEXT NOT NULL COMMENT '券文案描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_version` (`activity_code`, `version`),
  KEY `idx_activity_code` (`activity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动版本关系表';

-- 知识库表
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operation` VARCHAR(500) NOT NULL COMMENT '操作指令',
  `type` VARCHAR(50) NOT NULL COMMENT '修改类型（删除/新增/修改/替换等，多个类型用逗号分隔）',
  `result` JSON NOT NULL COMMENT '修改结果（包含多个origin和now的JSON数组）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_operation` (`operation`(255)),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- 修改类型规则表
DROP TABLE IF EXISTS `modification_type_rule`;
CREATE TABLE `modification_type_rule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` VARCHAR(50) NOT NULL COMMENT '修改类型（删除/新增/修改/替换/加回等）',
  `keywords` VARCHAR(200) NOT NULL COMMENT '关键词（用于识别该类型，逗号分隔）',
  `rule_detail` TEXT NOT NULL COMMENT '规则详情（自然语言描述，AI可直接读取）',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级（数值越大优先级越高）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='修改类型规则表';

-- 大任务表（父任务）
DROP TABLE IF EXISTS `parent_task`;
CREATE TABLE `parent_task` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(200) DEFAULT NULL COMMENT '任务名称',
  `operation` VARCHAR(500) NOT NULL COMMENT '操作指令',
  `activity_codes` TEXT NOT NULL COMMENT '活动编码列表（逗号分隔）',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '子任务总数',
  `success_count` INT NOT NULL DEFAULT 0 COMMENT '成功数量',
  `fail_count` INT NOT NULL DEFAULT 0 COMMENT '失败数量',
  `processing_count` INT NOT NULL DEFAULT 0 COMMENT '处理中数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大任务表（父任务）';

-- 任务表
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_task_id` BIGINT(20) DEFAULT NULL COMMENT '父任务ID（大任务ID）',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '任务状态（0-处理中，1-处理完成，2-处理失败）',
  `original_text` TEXT DEFAULT NULL COMMENT '原始券文案',
  `coupon_text` TEXT NOT NULL COMMENT '修改后的券文案',
  `scene` VARCHAR(500) DEFAULT NULL COMMENT '操作场景',
  `sub_scene` VARCHAR(500) DEFAULT NULL COMMENT '子场景',
  `modification_points` JSON DEFAULT NULL COMMENT '修改点列表（JSON数组）',
  `modification_type` VARCHAR(100) DEFAULT NULL COMMENT '修改类型（删除/新增/修改/替换/加回等，多个类型用逗号分隔）',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息（处理失败时）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_task_id` (`parent_task_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 插入修改类型规则数据
INSERT INTO `modification_type_rule` (`type`, `keywords`, `rule_detail`, `priority`) VALUES
('删除', '删除,删掉,移除,去掉,去除', '1. 删除产品时，需从“产品包含”字段的full_name_list中删除该产品名称；2. 同步删除相关规则；3. 保留其他未提及内容不变', 100),
('新增', '新增,增加,添加,加入,加上', '1. 新增产品时，需在“产品包含”字段的full_name_list中添加该产品名称；2. 根据上下文添加相关规则（如产品替换规则）；3. 保持格式一致性', 90),
('修改', '修改,更改,调整,变更', '1. 修改指定内容为新值；2. 保持上下文语境一致；3. 同步更新所有相关引用', 80),
('替换', '替换,换成,改为,改为', '1. 将指定内容A替换为B；2. 全局替换所有出现处；3. 确保语义一致性', 85),
('加回', '加回,恢复,还原,放回', '1. 恢复之前删除的内容；2. 恢复相关规则；3. 保持与原始版本一致', 75);

-- 插入测试数据
INSERT INTO `coupon_info` (`activity_code`, `description`) VALUES
('TEST001', '产品包含：任选4份【热辣香骨鸡（3块装）/咖啙脆皮鸡（1块装）/劲爆鸡米花(小)/黄金鸡块（5块装）/香辣鸡翅（2块装）】+任选1份【百事可乐（中杯）/雪顶咖啡/醜豆浆（热饮）】\n\n常规：脆皮鸡/原味鸡出餐部位随机搭配，详情以实物为准；部分产品存在供应时间限制，如老北京鸡肉卷（香辣）等，以餐厅实际为准；在不售卖黄金脆皮鸡的餐厅，产品将替换为吮指原味鸡。在不售卖吮指原味鸡的餐厅，产品将替换为黄金脆皮鸡；在不售卖咖啙脆皮鸡的餐厅，产品将替换为避风塘黄金脆皮鸡。在不售卖避风塘黄金脆皮鸡的餐厅，产品将替换为咖啙脆皮鸡；\n\n使用有效期：自购买之日起7天内有效\n\n核销渠道：支持堂食/自助点餐(肯德基APP、肯德基微信小程序、支付宝肯德基+小程序)核销，暂不支持外送渠道核销\n\n退款规则：到期未使用支持退款，详见美团《购物须知》');

-- 插入知识库测试数据
INSERT INTO `knowledge_base` (`operation`, `type`, `result`) VALUES
('删除黄金脆皮鸡', '删除', '[{"origin":"产品包含：任选3份【黄金SPA鸡排堡（藤椒风味）/香辣鸡腿堡/劲脆鸡腿堡/老北京鸡肉卷/吮指原味鸡焖拌饭(泡菜版)/吮指原味鸡焖拌饭】+任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）/劲爆鸡米花(小)/黄金鸡块（5块装）/香辣鸡翅（2块装）】","now":"产品包含：任选3份【黄金SPA鸡排堡（藤椒风味）/香辣鸡腿堡/劲脆鸡腿堡/老北京鸡肉卷/吮指原味鸡焖拌饭(泡菜版)/吮指原味鸡焖拌饭】+任选4份【热辣香骨鸡（3块装）/劲爆鸡米花(小)/黄金鸡块（5块装）/香辣鸡翅（2块装）】"},{"origin":"在不售卖吮指原味鸡的餐厅，产品将替换为黄金脆皮鸡；","now":""}]'),
('加回黄金脆皮鸡', '加回', '[{"origin":"产品包含：任选4份【热辣香骨鸡（3块装）/劲爆鸡米花(小)/黄金鸡块（5块装）】","now":"产品包含：任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）/劲爆鸡米花(小)/黄金鸡块（5块装）】"},{"origin":"","now":"在不售卖吮指原味鸡的餐厅，产品将替换为黄金脆皮鸡；"}]'),
('黄金脆皮鸡改为AAA黄金脆皮鸡', '替换,修改', '[{"origin":"产品包含：任选4份【热辣香骨鸡（3块装）/黄金脆皮鸡（1块装）/劲爆鸡米花(小)】","now":"产品包含：任选4份【热辣香骨鸡（3块装）/AAA黄金脆皮鸡（1块装）/劲爆鸡米花(小)】"},{"origin":"在不售卖黄金脆皮鸡的餐厅，产品将替换为吮指原味鸡。在不售卖吮指原味鸡的餐厅，产品将替换为黄金脆皮鸡；","now":"在不售卖AAA黄金脆皮鸡的餐厅，产品将暿换为吮指原味鸡。在不售卖吮指原味鸡的餐厅，产品将替换为AAA黄金脆皮鸡；"}]');
