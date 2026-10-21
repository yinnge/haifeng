package com.haifeng.common.util;

/**
 * 省级行政区名称归一化工具，用于列表过滤时兼容「简称 / 全称」两种形态。
 *
 * <p>背景：库里省份字段既可能存全称（天津市、河北省、广西壮族自治区…）也可能存简称（四川、北京…），
 * 而前端筛选一般传简称（两三个字，如 天津/黑龙江/内蒙古）。两侧形态不一致时精准 eq 查不到数据。</p>
 *
 * <p>方案：输入侧 {@link #stripSuffix} 剥后缀得到核名；库列侧用 {@link #strippedColumnEquals}
 * 生成的 replace 链剥后缀，两侧按「核名」恒等比较。原先该逻辑私藏在 CityServiceImpl，
 * 此处抽取为公共方法供 app / admin 复用。</p>
 */
public final class ProvinceNameUtil {

    private ProvinceNameUtil() {
    }

    /** 省级行政区后缀，民族自治区须放在泛型「自治区」之前，否则「广西壮/宁夏回/新疆吾尔」会被误剥。 */
    private static final String[] PROVINCE_SUFFIXES = {
            "特别行政区", "回族自治区", "壮族自治区", "维吾尔自治区", "自治区", "省", "市"};

    /**
     * 剥离省级行政区后缀，得到核名；null 安全（直接返回 null）。
     * 示例：河北省↔河北、广西壮族自治区↔广西、宁夏回族自治区↔宁夏、新疆维吾尔自治区↔新疆、
     *       内蒙古自治区↔内蒙古、北京市↔北京、香港特别行政区↔香港。
     */
    public static String stripSuffix(String name) {
        if (name == null) {
            return null;
        }
        String s = name.trim();
        for (String suffix : PROVINCE_SUFFIXES) {
            if (s.endsWith(suffix)) {
                s = s.substring(0, s.length() - suffix.length());
                break;
            }
        }
        return s;
    }

    /**
     * 生成「库列剥后缀后 = 核名」的 SQL 比较片段，配合 MyBatis-Plus 的 apply 使用：
     * <pre>
     *     .apply(province != null,
     *             ProvinceNameUtil.strippedColumnEquals("province_name"),
     *             province)
     * </pre>
     * 其中 {0} 占位符对应 apply 的第一个实参（输入侧已剥后缀的核名）。
     * 注意：replace 链作用在库列上，会绕过该列上的普通索引；
     * 属于既有取舍（CityServiceImpl 同款做法），小体量表可接受。
     *
     * @param columnName 待匹配的库列名，须由代码固定传入（不可来自用户输入），并在查询表中可直接引用
     */
    public static String strippedColumnEquals(String columnName) {
        return "replace(replace(replace(replace(replace(replace(replace("
                + columnName
                + ",'特别行政区',''),'回族自治区',''),'壮族自治区',''),'维吾尔自治区',''),'自治区',''),'省',''),'市','') = {0}";
    }
}
