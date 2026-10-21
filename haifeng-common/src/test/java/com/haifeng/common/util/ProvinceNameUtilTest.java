package com.haifeng.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProvinceNameUtilTest {

    @Test
    void stripSuffix_shortNamesRemainUnchanged() {
        // 前端传入的都是简称：两个字为主，黑龙江/内蒙古/台湾为三字
        assertEquals("北京", ProvinceNameUtil.stripSuffix("北京"));
        assertEquals("天津", ProvinceNameUtil.stripSuffix("天津"));
        assertEquals("上海", ProvinceNameUtil.stripSuffix("上海"));
        assertEquals("重庆", ProvinceNameUtil.stripSuffix("重庆"));
        assertEquals("四川", ProvinceNameUtil.stripSuffix("四川"));
        assertEquals("河北", ProvinceNameUtil.stripSuffix("河北"));
        assertEquals("黑龙江", ProvinceNameUtil.stripSuffix("黑龙江"));
        assertEquals("内蒙古", ProvinceNameUtil.stripSuffix("内蒙古"));
        assertEquals("台湾", ProvinceNameUtil.stripSuffix("台湾"));
    }

    @Test
    void stripSuffix_municipalitiesFullNames() {
        assertEquals("北京", ProvinceNameUtil.stripSuffix("北京市"));
        assertEquals("天津", ProvinceNameUtil.stripSuffix("天津市"));
        assertEquals("上海", ProvinceNameUtil.stripSuffix("上海市"));
        assertEquals("重庆", ProvinceNameUtil.stripSuffix("重庆市"));
    }

    @Test
    void stripSuffix_provincesFullNames() {
        assertEquals("黑龙江", ProvinceNameUtil.stripSuffix("黑龙江省"));
        assertEquals("河南", ProvinceNameUtil.stripSuffix("河南省"));
        assertEquals("河北", ProvinceNameUtil.stripSuffix("河北省"));
        assertEquals("江苏", ProvinceNameUtil.stripSuffix("江苏省"));
        assertEquals("台湾", ProvinceNameUtil.stripSuffix("台湾省"));
    }

    @Test
    void stripSuffix_autonomousRegions() {
        // 民族自治区须整体剥离，不能只剩「广西壮 / 宁夏回 / 新疆吾尔」这类残缺核名
        assertEquals("内蒙古", ProvinceNameUtil.stripSuffix("内蒙古自治区"));
        assertEquals("广西", ProvinceNameUtil.stripSuffix("广西壮族自治区"));
        assertEquals("西藏", ProvinceNameUtil.stripSuffix("西藏自治区"));
        assertEquals("宁夏", ProvinceNameUtil.stripSuffix("宁夏回族自治区"));
        assertEquals("新疆", ProvinceNameUtil.stripSuffix("新疆维吾尔自治区"));
    }

    @Test
    void stripSuffix_specialAdminRegions() {
        assertEquals("香港", ProvinceNameUtil.stripSuffix("香港特别行政区"));
        assertEquals("澳门", ProvinceNameUtil.stripSuffix("澳门特别行政区"));
    }

    @Test
    void stripSuffix_trimsWhitespace() {
        assertEquals("天津", ProvinceNameUtil.stripSuffix(" 天津市 "));
        assertEquals("", ProvinceNameUtil.stripSuffix("   "));
    }

    @Test
    void stripSuffix_nullReturnsNull() {
        assertNull(ProvinceNameUtil.stripSuffix(null));
    }

    @Test
    void strippedColumnEquals_wrapsGivenColumn() {
        // 期望：库列被 7 层 replace 依次包裹（特别行政区/回族/壮族/维吾尔自治区/自治区/省/市），
        // 最后与 {0} 比较占位符对应 apply 的第一个实参。
        String expected = "replace(replace(replace(replace(replace(replace(replace(province_name"
                + ",'特别行政区',''),'回族自治区',''),'壮族自治区',''),'维吾尔自治区','')"
                + ",'自治区',''),'省',''),'市','') = {0}";
        assertEquals(expected, ProvinceNameUtil.strippedColumnEquals("province_name"));

        assertEquals("replace(replace(replace(replace(replace(replace(replace(province"
                + ",'特别行政区',''),'回族自治区',''),'壮族自治区',''),'维吾尔自治区','')"
                + ",'自治区',''),'省',''),'市','') = {0}",
                ProvinceNameUtil.strippedColumnEquals("province"));
    }
}
