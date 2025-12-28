-- 添加 user_id 字段到 answer 表
-- 这样可以准确追踪用户填写的问卷

USE questionnaire_db;

-- 添加 user_id 字段
ALTER TABLE answer
ADD COLUMN user_id BIGINT COMMENT '填写用户ID' AFTER respondent_ip;

-- 添加索引以优化查询
ALTER TABLE answer
ADD INDEX idx_user_id (user_id);

-- 查看表结构确认
DESCRIBE answer;

-- 说明：
-- 1. 执行此SQL后，需要重启后端服务
-- 2. 旧的答卷数据 user_id 将为 NULL，需要用户重新填写问卷才能看到
-- 3. 新填写的问卷会正确记录 user_id
