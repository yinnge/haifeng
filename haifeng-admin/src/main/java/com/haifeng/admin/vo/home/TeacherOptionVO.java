package com.haifeng.admin.vo.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师身份用户下拉选项（规划师绑定 userId 用）。
 *
 * <p>只暴露 id + username：前端弹窗展示 username，提交时回传 id，
 * 后端存 Planner.userId（逻辑关联 t_member.id）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherOptionVO {

    /** 用户 ID（t_member.id，逻辑外键） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 用户名（弹窗展示用） */
    private String username;
}
