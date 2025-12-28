-- 轮播图表
CREATE TABLE IF NOT EXISTS sys_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '轮播图ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description VARCHAR(500) COMMENT '描述',
    image_url VARCHAR(500) NOT NULL COMMENT '图片地址',
    link_url VARCHAR(500) COMMENT '跳转链接',
    order_num INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 插入测试轮播图数据
INSERT INTO sys_banner (title, description, image_url, link_url, order_num, status)
VALUES
('大学生心理健康调查', '关注大学生心理健康，参与问卷赢好礼', '/banner1.jpg', '/questionnaire/1', 1, 1),
('2024年度消费习惯调研', '了解最新消费趋势，分享您的看法', '/banner2.jpg', '/questionnaire/2', 2, 1),
('AI技术应用调查', '探索人工智能在各领域的应用现状', '/banner3.jpg', '/questionnaire/3', 3, 1)
ON DUPLICATE KEY UPDATE title = title;
