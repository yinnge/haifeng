package com.haifeng.common.enums;

import lombok.Getter;

@Getter
public enum IdentityEnum {
    HIGH_SCHOOL("高中生"),
    COLLEGE("大学生"),
    GRADUATE("研究生"),
    OTHER("其他");

    private final String desc;

    IdentityEnum(String desc) {
        this.desc = desc;
    }

    public static boolean isValid(String identity) {
        if (identity == null) return true;
        for (IdentityEnum i : values()) {
            if (i.desc.equals(identity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canHaveSchool(String identity) {
        return COLLEGE.desc.equals(identity) || GRADUATE.desc.equals(identity);
    }
}
