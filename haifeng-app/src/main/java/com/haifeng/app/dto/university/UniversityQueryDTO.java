package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
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
    private String name;

    private String provinceName;
    private String nature;
    private String category;
    private String department;
    private String educationLevel;
    private Boolean hasDoctorate;
    private Boolean hasMaster;
}
