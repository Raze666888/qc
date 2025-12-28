-- 发布问卷ID=12
USE questionnaire_db;

-- 更新问卷状态为已发布
UPDATE questionnaire
SET status = 1,
    audit_status = 1
WHERE id = 12 AND deleted = 0;

-- 设置截止时间为30天后
UPDATE questionnaire
SET deadline = DATE_ADD(NOW(), INTERVAL 30 DAY)
WHERE id = 12 AND deadline IS NULL;

-- 查看更新结果
SELECT
    id,
    title,
    status,
    CASE status
        WHEN 0 THEN '草稿'
        WHEN 1 THEN '已发布'
        WHEN 2 THEN '已暂停'
        WHEN 3 THEN '已删除'
    END as status_text,
    deadline,
    DATE_FORMAT(deadline, '%Y-%m-%d %H:%i') as formatted_deadline,
    create_time
FROM questionnaire
WHERE id = 12;
