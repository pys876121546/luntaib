-- 轮胎商品表
CREATE TABLE IF NOT EXISTS `tire_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category` INT NOT NULL DEFAULT '0' COMMENT '分类：0=四季轮胎，1=冬季轮胎',
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `brand` VARCHAR(100) NOT NULL COMMENT '品牌',
    `width` VARCHAR(20) NOT NULL COMMENT '胎宽',
    `ratio` VARCHAR(20) NOT NULL COMMENT '扁平比',
    `rim` VARCHAR(20) NOT NULL COMMENT '轮毂尺寸',
    `price` DECIMAL(10,2) NOT NULL COMMENT '现价',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价（特价时存在）',
    `special` TINYINT(1) DEFAULT '0' COMMENT '是否特价：0=否，1=是',
    `stock` INT NOT NULL DEFAULT '0' COMMENT '库存',
    `img` VARCHAR(500) DEFAULT NULL COMMENT '商品图片',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_brand` (`brand`),
    KEY `idx_special` (`special`),
    KEY `idx_width` (`width`),
    KEY `idx_ratio` (`ratio`),
    KEY `idx_rim` (`rim`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 插入测试数据
INSERT INTO `tire_product` (`category`, `name`, `brand`, `width`, `ratio`, `rim`, `price`, `original_price`, `special`, `stock`, `img`) VALUES
(0, '米其林 Primacy 4', '米其林', '205', '55', '16', 680.00, 520.00, 1, 12, '/static/logo.png'),
(0, '米其林 Energy XM2', '米其林', '195', '65', '15', 450.00, NULL, 0, 20, '/static/logo.png'),
(0, '固特异 Assurance', '固特异', '205', '55', '16', 580.00, NULL, 0, 15, '/static/logo.png'),
(1, '米其林 X-Ice XI3', '米其林', '205', '55', '16', 780.00, 650.00, 1, 8, '/static/logo.png'),
(1, '马牌 WinterContact', '马牌', '215', '60', '17', 920.00, NULL, 0, 10, '/static/logo.png'),
(0, '普利司通 Turanza', '普利司通', '225', '45', '18', 850.00, NULL, 0, 6, '/static/logo.png');
