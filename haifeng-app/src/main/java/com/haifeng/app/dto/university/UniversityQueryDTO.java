package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端院校列表查询 DTO
 * 全部筛选字段 optional；name 走 LIKE，其余精准匹配（AND 组合）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityQueryDTO extends BasePageQueryDTO {

    /** 院校名称模糊（LIKE %name%） */
    @Size(max = 50, message = "院校名称长度不能超过50")
    private String name;

    @Size(max = 20, message = "省份名称长度不能超过20")
    private String provinceName;

    @Size(max = 20, message = "办学性质长度不能超过20")
    private String nature;

    @Size(max = 20, message = "院校类型长度不能超过20")
    private String category;

    @Size(max = 50, message = "主管部门长度不能超过50")
    private String department;

    @Size(max = 20, message = "学历层次长度不能超过20")
    private String educationLevel;

    private Boolean hasDoctorate;
    private Boolean hasMaster;
}
