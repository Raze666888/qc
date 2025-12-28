-- 添加 user_id 字段到 answer 表
-- 必须先执行此SQL才能正常使用"我参与的问卷"功能

USE questionnaire_db;

-- 检查字段是否已存在，如果不存在则添加
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'questionnaire_db'
    AND TABLE_NAME = 'answer'
    AND COLUMN_NAME = 'user_id'
);

-- 只有在字段不存在时才添加
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE answer ADD COLUMN user_id BIGINT COMMENT ''填写用户ID'' AFTER respondent_ip',
    'SELECT ''字段 user_id 已存在，跳过添加'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加索引（如果不存在）
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'questionnaire_db'
    AND TABLE_NAME = 'answer'
    AND INDEX_NAME = 'idx_user_id'
);

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE answer ADD INDEX idx_user_id (user_id)',
    'SELECT ''索引 idx_user_id 已存在，跳过添加'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示结果
SELECT '数据库迁移完成！' AS message;

-- 验证表结构
DESCRIBE answer;
