-- 验证问卷表的deadline字段是否存在
-- 查询问卷表结构
DESCRIBE questionnaire;

-- 查看是否有deadline字段
SHOW COLUMNS FROM questionnaire LIKE 'deadline';

-- 测试查询：查看现有问卷的截止时间
SELECT id, title, deadline, create_time
FROM questionnaire
WHERE deleted = 0
ORDER BY create_time DESC
LIMIT 10;

-- 如果deadline字段不存在，执行以下ALTER TABLE添加字段
-- ALTER TABLE questionnaire ADD COLUMN deadline DATETIME COMMENT '截止时间' AFTER category;
