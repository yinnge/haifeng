-- V42: 修复邀请码超长问题（Hashids 生成的邀请码最长 15 位，原列 VARCHAR(8) 过短）
ALTER TABLE t_member ALTER COLUMN invite_code TYPE VARCHAR(16);
