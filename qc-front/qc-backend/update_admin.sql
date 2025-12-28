-- 更新管理员账号密码
USE questionnaire_db;

-- 先删除旧的管理员账号
DELETE FROM sys_user WHERE username = 'admin';

-- 插入新的管理员账号 (密码: qq123456 的 MD5)
INSERT INTO sys_user (username, password, phone, nickname, role)
VALUES ('123456', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', '系统管理员', 1);
