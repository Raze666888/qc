-- 更新问卷状态为已发布
-- 使用说明：在MySQL中执行此SQL来发布您的问卷

USE questionnaire_db;

-- 查看当前所有问卷的状态
SELECT
    id,
    title,
    status,
    CASE status
        WHEN 0 THEN '草稿'
        WHEN 1 THEN '已发布'
        WHEN 2 THEN '已暂停'
        WHEN 3 THEN '已删除'
        ELSE '未知'
    END as status_text,
    deadline,
    create_time
FROM questionnaire
WHERE deleted = 0
ORDER BY create_time DESC;

-- 发布问卷ID为12的问卷（根据您的描述）
-- 如果需要发布其他问卷，请修改ID
UPDATE questionnaire
SET status = 1,
    audit_status = 1  -- 同时设置为审核通过
WHERE id = 12 AND deleted = 0;

-- 或者：发布所有草稿状态的问卷
-- UPDATE questionnaire
-- SET status = 1,
--     audit_status = 1
-- WHERE status = 0 AND deleted = 0;

-- 查看更新后的状态
SELECT
    id,
    title,
    status,
    CASE status
        WHEN 0 THEN '草稿'
        WHEN 1 THEN '已发布'
        WHEN 2 THEN '已暂停'
        WHEN 3 THEN '已删除'
        ELSE '未知'
    END as status_text,
    deadline,
    create_time
FROM questionnaire
WHERE deleted = 0
ORDER BY create_time DESC;
