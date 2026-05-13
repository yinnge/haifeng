package com.haifeng.common.enums;

import lombok.Getter;

@Getter
public enum ProvinceEnum {
    BEIJING("北京"),
    TIANJIN("天津"),
    HEBEI("河北"),
    SHANXI("山西"),
    NEIMENGGU("内蒙古"),
    LIAONING("辽宁"),
    JILIN("吉林"),
    HEILONGJIANG("黑龙江"),
    SHANGHAI("上海"),
    JIANGSU("江苏"),
    ZHEJIANG("浙江"),
    ANHUI("安徽"),
    FUJIAN("福建"),
    JIANGXI("江西"),
    SHANDONG("山东"),
    HENAN("河南"),
    HUBEI("湖北"),
    HUNAN("湖南"),
    GUANGDONG("广东"),
    GUANGXI("广西"),
    HAINAN("海南"),
    CHONGQING("重庆"),
    SICHUAN("四川"),
    GUIZHOU("贵州"),
    YUNNAN("云南"),
    XIZANG("西藏"),
    SHAANXI("陕西"),
    GANSU("甘肃"),
    QINGHAI("青海"),
    NINGXIA("宁夏"),
    XINJIANG("新疆"),
    HONGKONG("香港"),
    MACAO("澳门"),
    TAIWAN("台湾");

    private final String desc;

    ProvinceEnum(String desc) {
        this.desc = desc;
    }

    public static boolean isValid(String province) {
        if (province == null) return true;
        for (ProvinceEnum p : values()) {
            if (p.desc.equals(province)) {
                return true;
            }
        }
        return false;
    }
}
