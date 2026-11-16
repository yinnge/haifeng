package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 培训机构分类（后端写死，App 端按此值筛选列表）。
 * <p>数据库 t_institutions.category 存中文标签，与 {@link #value} 一致。</p>
 */
@Getter
public enum InstitutionCategoryEnum {

    HIGH_SCHOOL_SPRINT("高中冲刺小班"),
    CIVIL_SERVICE("考公考编机构"),
    STATE_OWNED_ENTERPRISE("央国企培训"),
    VOCATIONAL("职业培训机构");

    @EnumValue
    private final String value;

    InstitutionCategoryEnum(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (InstitutionCategoryEnum item : values()) {
            if (item.value.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
