package com.haifeng.common.enums;

import lombok.Getter;

@Getter
public enum GenderEnum {
    MALE("男"),
    FEMALE("女");

    private final String desc;

    GenderEnum(String desc) {
        this.desc = desc;
    }

    public static boolean isValid(String gender) {
        if (gender == null) return true;
        for (GenderEnum g : values()) {
            if (g.desc.equals(gender)) {
                return true;
            }
        }
        return false;
    }
}
