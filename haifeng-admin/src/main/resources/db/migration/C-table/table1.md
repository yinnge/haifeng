## 已实现：
1. system_settings（系统设置）
2. 

## 新增表

### 二、用户资料表（新增）


```
-- ============================================================
-- 用户资料表 (t_member_profile)
-- 描述：与 t_member 一对一，存储用户的个人资料（可选填）
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_profile (

    id                      SERIAL          PRIMARY KEY,
    member_id               INTEGER         NOT NULL UNIQUE,        -- 关联会员表（一对一）

    -- ==================== 个人信息 ====================
    real_name               VARCHAR(50),                            -- 真实姓名
    email                   VARCHAR(100),                           -- 邮箱
    
    gender                  VARCHAR(10),        
        -- 性别（男/女）
    
    school_name                VARCHAR(100),                           -- 身份（如：北京大学）
    province                VARCHAR(30),                            -- 省份
    city                    VARCHAR(50),                            -- 城市
    major                   VARCHAR(100),                           -- 专业
    grade                   VARCHAR(20),                            -- 年级（如：大三、研一）
    education_level         VARCHAR(20),                            -- 学历层次（如：大学生、研究生）
    
   favorite_count INTEGER DEFAULT 0 NOT NULL,
   view_count     INTEGER DEFAULT 0 NOT NULL,

    -- ==================== 审计字段 ====================
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_profile_member_id ON t_member_profile (member_id);
CREATE INDEX idx_profile_identity  ON t_member_profile (identity);
CREATE INDEX idx_profile_city      ON t_member_profile (city);

-- 触发器
CREATE TRIGGER trg_member_profile_updated_at
    BEFORE UPDATE ON t_member_profile
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member_profile                  IS '用户资料表：与 t_member 一对一';
COMMENT ON COLUMN t_member_profile.member_id        IS '关联会员表ID';
COMMENT ON COLUMN t_member_profile.real_name        IS '真实姓名';
COMMENT ON COLUMN t_member_profile.email            IS '邮箱';
COMMENT ON COLUMN t_member_profile.identity         IS '身份（如：北京大学）';
COMMENT ON COLUMN t_member_profile.province         IS '省份';
COMMENT ON COLUMN t_member_profile.city             IS '城市';
COMMENT ON COLUMN t_member_profile.major            IS '专业';
COMMENT ON COLUMN t_member_profile.grade            IS '年级（大三/研一）';
COMMENT ON COLUMN t_member_profile.education_level  IS '学历层次（大学生/研究生）';

COMMIT;
```