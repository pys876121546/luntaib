-- 审批记录表
CREATE TABLE `audit_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `company_name` VARCHAR(255) NOT NULL COMMENT '公司名称',
  `company_address` VARCHAR(500) NOT NULL COMMENT '公司地址',
  `company_phone` VARCHAR(50) NOT NULL COMMENT '公司电话',
  `company_photo_url` VARCHAR(500) DEFAULT NULL COMMENT '公司门头照片URL',
  
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态: PENDING=待审核, APPROVED=已通过, REJECTED=已拒绝',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批备注（管理员填写）',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0=未删除, 1=已删除',
  
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批记录表';
