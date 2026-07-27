-- V31__fix_admin_password.sql
-- 修正管理员密码：V26 中的 BCrypt hash 与 Admin123 不匹配，重新生成正确的 hash

UPDATE sys_admin
SET password = '$2b$10$sBOFCQmfZbTPvrRIyPYQEuMbJUqwigPNO2x2SEqfUQYbCnVXVQgJ6',
    updated_at = NOW()
WHERE username = 'admin';

