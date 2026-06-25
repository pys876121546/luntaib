-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
    `unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信unionid',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `audit_status` VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '审核状态: NONE-未申请, PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝',
    `company_name` VARCHAR(200) DEFAULT NULL COMMENT '公司名',
    `company_address` VARCHAR(500) DEFAULT NULL COMMENT '公司地址',
    `company_phone` VARCHAR(50) DEFAULT NULL COMMENT '公司电话',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `admin` TINYINT NOT NULL DEFAULT '0' COMMENT '是否管理员: 0-否, 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
