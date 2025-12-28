-- 创建数据库
CREATE DATABASE IF NOT EXISTS questionnaire_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE questionnaire_db;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    role INT DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
    points INT DEFAULT 0 COMMENT '积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 问卷表
CREATE TABLE IF NOT EXISTS questionnaire (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '问卷ID',
    user_id BIGINT NOT NULL COMMENT '创建者ID',
    title VARCHAR(200) NOT NULL COMMENT '问卷标题',
    description TEXT COMMENT '问卷描述',
    category INT COMMENT '问卷类型：1-学术科研，2-生活消费，3-心理健康，4-娱乐游戏，5-工作就业',
    deadline DATETIME COMMENT '截止时间',
    is_anonymous INT DEFAULT 1 COMMENT '是否匿名：0-否，1-是',
    status INT DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已暂停，3-已删除',
    audit_status INT DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核不通过',
    link VARCHAR(255) COMMENT '问卷链接',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷表';

-- 问题表
CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '问题ID',
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    content VARCHAR(500) NOT NULL COMMENT '问题内容',
    type INT NOT NULL COMMENT '问题类型：0-单选，1-多选，2-文本',
    is_required INT DEFAULT 0 COMMENT '是否必填：0-否，1-是',
    sort INT DEFAULT 0 COMMENT '排序',
    max_select_num INT COMMENT '最多可选数量（多选题）',
    input_type INT DEFAULT 0 COMMENT '输入框类型：0-单行，1-多行',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_questionnaire_id (questionnaire_id),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题表';

-- 问题选项表
CREATE TABLE IF NOT EXISTS question_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选项ID',
    question_id BIGINT NOT NULL COMMENT '问题ID',
    content VARCHAR(200) NOT NULL COMMENT '选项内容',
    sort INT DEFAULT 0 COMMENT '排序',
    jump_question_id BIGINT COMMENT '跳转问题ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_question_id (question_id),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题选项表';

-- 答卷表
CREATE TABLE IF NOT EXISTS answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '答卷ID',
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    respondent_name VARCHAR(50) COMMENT '答题者姓名',
    respondent_phone VARCHAR(20) COMMENT '答题者手机号',
    respondent_ip VARCHAR(50) COMMENT '答题者IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_questionnaire_id (questionnaire_id),
    INDEX idx_ip (respondent_ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答卷表';

-- 答卷详情表
CREATE TABLE IF NOT EXISTS answer_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '详情ID',
    answer_id BIGINT NOT NULL COMMENT '答卷ID',
    question_id BIGINT NOT NULL COMMENT '问题ID',
    option_id BIGINT COMMENT '选项ID（选择题）',
    text_content TEXT COMMENT '文本内容（文本题）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_answer_id (answer_id),
    INDEX idx_question_id (question_id),
    INDEX idx_option_id (option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答卷详情表';

-- 插入测试管理员账号
INSERT INTO sys_user (username, password, phone, nickname, role)
VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', '13800138000', '系统管理员', 1)
ON DUPLICATE KEY UPDATE username = username;
-- 默认密码: admin
