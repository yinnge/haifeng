package com.haifeng.admin.tool;

import com.haifeng.common.util.SnowflakeIdGenerator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 生成 V26__seed_admin_module_data.sql
 * 使用雪花算法 ID，作为 Flyway 迁移文件
 * <p>
 * 用法：运行 main 方法，输出内容直接替换 V26__seed_admin_module_data.sql
 */
public class SeedDataSqlGenerator {

    public static void main(String[] args) {
        long roleId = SnowflakeIdGenerator.nextId();
        long adminId = SnowflakeIdGenerator.nextId();

        ModuleDef[] modules = {
                // === 系统管理 ===
                new ModuleDef("系统管理", "system", null, 1, "/system", 1),
                new ModuleDef("系统设置", "system_setting", "system", 2, "/system/setting", 1),
                new ModuleDef("模型供应商配置", "system_provider", "system", 2, "/system/provider", 2),
                new ModuleDef("操作日志记录", "system_log", "system", 2, "/system/log", 3),

                // === 权限管理 ===
                new ModuleDef("权限管理", "permission", null, 1, "/permission", 2),
                new ModuleDef("管理员账号管理", "permission_admin", "permission", 2, "/permission/admin", 1),
                new ModuleDef("角色管理", "permission_role", "permission", 2, "/permission/role", 2),
                new ModuleDef("模块菜单管理", "permission_module", "permission", 2, "/permission/module", 3),

                // === 用户管理 ===
                new ModuleDef("用户管理", "user", null, 1, "/user", 3),
                new ModuleDef("用户信息管理", "user_member", "user", 2, "/user/member", 1),
                new ModuleDef("会员订单管理", "user_order", "user", 2, "/user/order", 2),
                new ModuleDef("提现审核管理", "user_withdraw", "user", 2, "/user/withdraw", 3),
                new ModuleDef("佣金记录管理", "user_commission", "user", 2, "/user/commission", 4),
                new ModuleDef("通知消息管理", "user_notification", "user", 2, "/user/notification", 5),

                // === 首页管理 ===
                new ModuleDef("首页管理", "home", null, 1, "/home", 4),
                new ModuleDef("公告管理", "home_announcement", "home", 2, "/home/announcement", 1),
                new ModuleDef("规划师管理", "home_planner", "home", 2, "/home/planner", 2),
                new ModuleDef("培训机构管理", "home_institution", "home", 2, "/home/institution", 3),

                // === 院校管理 ===
                new ModuleDef("院校管理", "university", null, 1, "/university", 5),
                new ModuleDef("校园图册管理", "university_album", "university", 2, "/university/album", 1),
                new ModuleDef("院校管理", "university_info", "university", 2, "/university/info", 2),
                new ModuleDef("院系管理", "university_dept", "university", 2, "/university/department", 3),
                new ModuleDef("实验室管理", "university_lab", "university", 2, "/university/lab", 4),
                new ModuleDef("学科评估管理", "university_eval", "university", 2, "/university/evaluation", 5),
                new ModuleDef("院校适应指南管理", "university_guide", "university", 2, "/university/guide", 6),

                // === 专业管理 ===
                new ModuleDef("专业管理", "major", null, 1, "/major", 6),
                new ModuleDef("专业管理", "major_info", "major", 2, "/major/info", 1),
                new ModuleDef("考研专业管理", "major_subject", "major", 2, "/major/postgraduate/subject", 2),
                new ModuleDef("专业考研关联管理", "major_postgraduate", "major", 2, "/major/postgraduate", 3),
                new ModuleDef("考研专业大学关联管理", "major_univ", "major", 2, "/major/postgraduate/university", 4),

                // === 城市管理 ===
                new ModuleDef("城市管理", "city", null, 1, "/city", 7),
                new ModuleDef("城市管理", "city_info", "city", 2, "/city/info", 1),

                // === 高考算法 ===
                new ModuleDef("高考算法", "algorithm", null, 1, "/algorithm", 8),
                new ModuleDef("专业组管理", "algo_admission", "algorithm", 2, "/algorithm/admission", 1),
                new ModuleDef("专业组管理", "algo_admission_grp", "algo_admission", 3, "/algorithm/admission/group", 1),
                new ModuleDef("专业明细管理", "algo_admission_dtl", "algo_admission", 3, "/algorithm/admission/detail", 2),
                new ModuleDef("分数位次管理", "algo_score", "algorithm", 2, "/algorithm/score", 2),
                new ModuleDef("分数排名管理", "algo_score_rank", "algo_score", 3, "/algorithm/score/rank", 1),
                new ModuleDef("批次基线管理", "algo_score_baseline", "algo_score", 3, "/algorithm/score/baseline", 2),
                new ModuleDef("改革省份管理", "algo_score_prov", "algo_score", 3, "/algorithm/score/province", 3),
                new ModuleDef("算法配置管理", "algo_config", "algorithm", 2, "/algorithm/config", 3),
                new ModuleDef("算法配置管理", "algo_config_gaokao", "algo_config", 3, "/algorithm/config/gaokao", 1),
                new ModuleDef("省份配置管理", "algo_config_prov", "algo_config", 3, "/algorithm/config/prov", 2),
                new ModuleDef("约束管理", "algo_constraint", "algorithm", 2, "/algorithm/constraint", 4),
                new ModuleDef("约束字典管理", "algo_constraint_dict", "algo_constraint", 3, "/algorithm/constraint/dict", 1),
                new ModuleDef("约束专业关联管理", "algo_constraint_mjr", "algo_constraint", 3, "/algorithm/constraint/major", 2),
                new ModuleDef("安全系数管理", "algo_safety", "algorithm", 2, "/algorithm/safety", 5),
                new ModuleDef("安全系数管理", "algo_safety_level", "algo_safety", 3, "/algorithm/safety/level", 1),

                // === 特殊通道 ===
                new ModuleDef("特殊通道", "special", null, 1, "/special", 9),
                new ModuleDef("招生通道管理", "special_admission", "special", 2, "/special/admission", 1),
                new ModuleDef("通道院校关联管理", "special_adm_univ", "special", 2, "/special/admission/university", 2),
                new ModuleDef("强基计划分数管理", "special_sbs_score", "special", 2, "/special/strong-basis/score", 3),
                new ModuleDef("强基计划院校配置", "special_sbs_config", "special", 2, "/special/strong-basis/config", 4),

                // === 证书竞赛 ===
                new ModuleDef("证书竞赛", "certificate", null, 1, "/certificate", 10),
                new ModuleDef("证书管理", "certificate_info", "certificate", 2, "/certificate/info", 1),
                new ModuleDef("竞赛管理", "certificate_comp", "certificate", 2, "/certificate/competition", 2),
                new ModuleDef("竞赛专业关联管理", "cert_comp_major", "certificate", 2, "/certificate/competition/major", 3),

                // === 资源管理 ===
                new ModuleDef("资源管理", "resource", null, 1, "/resource", 11),
                new ModuleDef("资源管理", "resource_info", "resource", 2, "/resource/info", 1),

                // === 行业管理 ===
                new ModuleDef("行业管理", "industry", null, 1, "/industry", 12),
                new ModuleDef("行业管理", "industry_info", "industry", 2, "/industry/info", 1),

                // === 企业管理 ===
                new ModuleDef("企业管理", "company", null, 1, "/company", 13),
                new ModuleDef("企业管理", "company_info", "company", 2, "/company/info", 1),
                new ModuleDef("企业行业关联管理", "company_industry", "company", 2, "/company/industry", 2),

                // === 就业管理 ===
                new ModuleDef("就业管理", "employment", null, 1, "/employment", 14),
                new ModuleDef("招聘内容管理", "emp_content", "employment", 2, "/employment/content", 1),
                new ModuleDef("备考指南管理", "emp_content_guide", "emp_content", 3, "/employment/content/guide", 1),
                new ModuleDef("公告管理", "emp_content_notice", "emp_content", 3, "/employment/content/notice", 2),
                new ModuleDef("体制内招录管理", "emp_civil", "employment", 2, "/employment/civil", 2),
                new ModuleDef("公务员职位管理", "emp_civil_servant", "emp_civil", 3, "/employment/civil/servant", 1),
                new ModuleDef("事业编职位管理", "emp_civil_institution", "emp_civil", 3, "/employment/civil/institution", 2),
                new ModuleDef("部队文职岗位管理", "emp_civil_military", "emp_civil", 3, "/employment/civil/military", 3),
                new ModuleDef("选调生岗位管理", "emp_civil_selected", "emp_civil", 3, "/employment/civil/selected", 4),
                new ModuleDef("基层服务管理", "emp_grassroots", "employment", 2, "/employment/grassroots", 3),
                new ModuleDef("社区工作者岗位管理", "emp_grassroots_comm", "emp_grassroots", 3, "/employment/grassroots/community", 1),
                new ModuleDef("三支一扶西部计划", "emp_grassroots_3s", "emp_grassroots", 3, "/employment/grassroots/three-support", 2),
                new ModuleDef("公益性岗位管理", "emp_grassroots_welfare", "emp_grassroots", 3, "/employment/grassroots/welfare", 3),
                new ModuleDef("行业专项招聘管理", "emp_industry", "employment", 2, "/employment/industry", 4),
                new ModuleDef("银行/金融岗位管理", "emp_industry_bank", "emp_industry", 3, "/employment/industry/bank", 1),
                new ModuleDef("医疗卫生岗位管理", "emp_industry_medical", "emp_industry", 3, "/employment/industry/medical", 2),
                new ModuleDef("教师招聘岗位管理", "emp_industry_teacher", "emp_industry", 3, "/employment/industry/teacher", 3),
        };

        // 3. 为每个模块生成雪花 ID
        Map<String, Long> moduleIdMap = new LinkedHashMap<>();
        for (ModuleDef m : modules) {
            moduleIdMap.put(m.code, SnowflakeIdGenerator.nextId());
        }

        // 4. 输出 V26 格式 SQL
        StringBuilder sql = new StringBuilder();
        sql.append("-- V26__seed_admin_module_data.sql\n");
        sql.append("-- 初始化模块数据 + 绑定超级管理员角色到所有模块\n");
        sql.append("-- level: 1=顶级父模块, 2=子模块, 3=三级子模块\n");
        sql.append("-- 由 SeedDataSqlGenerator 自动生成（雪花算法 ID）\n\n");

        sql.append("-- ========== 角色 ==========\n");
        sql.append("INSERT INTO sys_role (id, role_name, role_code, description, status)\n");
        sql.append("VALUES (").append(roleId).append(", '超级管理员', 'super_admin', '拥有所有权限', 1)\n");
        sql.append("ON CONFLICT (role_code) DO NOTHING;\n\n");

        sql.append("-- ========== 管理员（密码：Admin123） ==========\n");
        sql.append("INSERT INTO sys_admin (id, username, password, real_name, phone, role_id, role_name, status)\n");
        sql.append("VALUES (").append(adminId).append(", 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctLkRc2xqV8k1u7QwcEVyRZCJ.', '超级管理员', '13800000000', ").append(roleId).append(", '超级管理员', 1)\n");
        sql.append("ON CONFLICT (username) DO NOTHING;\n\n");

        sql.append("-- ========== 模块菜单（81条） ==========\n");
        sql.append("INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order) VALUES\n");
        for (int idx = 0; idx < modules.length; idx++) {
            ModuleDef m = modules[idx];
            Long id = moduleIdMap.get(m.code);
            String parentIdStr = m.parentCode == null ? "NULL" : String.valueOf(moduleIdMap.get(m.parentCode));
            sql.append("  (").append(id).append(", '").append(m.name).append("', '").append(m.code).append("', ")
                    .append(parentIdStr).append(", ").append(m.level).append(", '").append(m.path).append("', ").append(m.sortOrder).append(")");
            if (idx < modules.length - 1) {
                sql.append(",\n");
            } else {
                sql.append("\nON CONFLICT (module_code) DO NOTHING;\n\n");
            }
        }

        sql.append("-- ========== 角色-模块绑定（超级管理员→所有模块） ==========\n");
        sql.append("INSERT INTO sys_role_module (id, role_id, module_id) VALUES\n");
        for (int idx = 0; idx < modules.length; idx++) {
            long bindingId = SnowflakeIdGenerator.nextId();
            Long modId = moduleIdMap.get(modules[idx].code);
            sql.append("  (").append(bindingId).append(", ").append(roleId).append(", ").append(modId).append(")");
            if (idx < modules.length - 1) {
                sql.append(",\n");
            } else {
                sql.append("\nON CONFLICT (role_id, module_id) DO NOTHING;\n\n");
            }
        }

        sql.append("-- 更新 sys_module 的 level 注释\n");
        sql.append("COMMENT ON COLUMN sys_module.level IS '模块层级: 1=顶级父模块 2=子模块 3=三级子模块';\n");

        System.out.println(sql);
    }

    private record ModuleDef(String name, String code, String parentCode, int level, String path, int sortOrder) {
    }
}
