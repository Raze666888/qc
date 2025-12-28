-- 更新管理员账号密码
-- 账号: 17836900831 (作为手机号)
-- 密码: aa111111 (MD5加密后: 7e9a09b0486489506b2a1f5e3f949c70)

USE questionnaire_db;

-- 更新现有管理员（role=1）的账号密码
UPDATE sys_user
SET username = '17836900831',
    phone = '17836900831',
    password = '7e9a09b0486489506b2a1f5e3f949c70',
    nickname = '系统管理员'
WHERE role = 1;

-- 如果不存在管理员，则创建一个
INSERT INTO sys_user (username, password, phone, nickname, role)
VALUES ('17836900831', '7e9a09b0486489506b2a1f5e3f949c70', '17836900831', '系统管理员', 1)
ON DUPLICATE KEY UPDATE
    password = '7e9a09b0486489506b2a1f5e3f949c70',
    nickname = '系统管理员';

-- 查看更新后的管理员信息
SELECT id, username, phone, password, nickname, role
FROM sys_user
WHERE role = 1;
