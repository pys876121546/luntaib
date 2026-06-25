-- 审核测试数据

-- 1. 测试用户（如果不存在则插入）
INSERT INTO `user` (`id`, `openid`, `nickname`, `avatar_url`, `audit_status`, `admin`)
VALUES 
    (2, 'test_openid_001', '李四', 'https://thirdwx.qlogo.cn/test1.jpg', 'PENDING', 0),
    (3, 'test_openid_002', '王五', 'https://thirdwx.qlogo.cn/test2.jpg', 'APPROVED', 0),
    (4, 'test_openid_003', '赵六', 'https://thirdwx.qlogo.cn/test3.jpg', 'REJECTED', 0),
    (5, 'test_openid_004', '钱七', 'https://thirdwx.qlogo.cn/test4.jpg', 'NONE', 0)
ON DUPLICATE KEY UPDATE `nickname`=VALUES(`nickname`);

-- 2. 审批记录 - 待审核
INSERT INTO `audit_record` (`user_id`, `company_name`, `company_address`, `company_phone`, `company_photo_url`, `status`, `create_time`)
VALUES 
    (2, '北京顺达轮胎有限公司', '北京市朝阳区建国路88号', '010-88886666', '/uploads/company/photo001.jpg', 'PENDING', NOW()),
    (5, '上海通达轮胎销售部', '上海市浦东新区世纪大道100号', '021-55558888', '/uploads/company/photo002.jpg', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- 3. 审批记录 - 已通过
INSERT INTO `audit_record` (`user_id`, `company_name`, `company_address`, `company_phone`, `company_photo_url`, `status`, `auditor_id`, `audit_time`, `create_time`)
VALUES 
    (3, '广州恒通轮胎有限公司', '广州市天河区珠江新城', '020-33334444', '/uploads/company/photo003.jpg', 'APPROVED', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 4. 审批记录 - 已拒绝（可重新提交）
INSERT INTO `audit_record` (`user_id`, `company_name`, `company_address`, `company_phone`, `company_photo_url`, `status`, `audit_remark`, `auditor_id`, `audit_time`, `create_time`)
VALUES 
    (4, '深圳飞驰轮胎店', '深圳市南山区科技园路99号', '0755-22221111', '/uploads/company/photo004.jpg', 'REJECTED', '公司资质不符合要求，请补充营业执照', 1, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY));
