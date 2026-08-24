package com.haifeng.common.enums;

/**
 * 各省市教育考试院（官方招生考试机构）映射
 * <p>数据与前端"直通考院"模块保持一致，按省份中文名匹配。
 */
public enum ProvinceExamSiteEnum {

    BEIJING("北京", "北京教育考试院", "http://www.bjeea.cn"),
    SHANGHAI("上海", "上海教育考试院", "http://www.shmeea.edu.cn"),
    TIANJIN("天津", "天津招考资讯网", "http://www.zhaokao.net"),
    CHONGQING("重庆", "重庆教育考试院", "http://www.cqksy.cn"),
    HEBEI("河北", "河北教育考试院", "http://www.hebeea.edu.cn"),
    SHANXI("山西", "山西招生考试网", "http://www.sxkszx.cn"),
    NEIMENGGU("内蒙古", "内蒙古教育招生考试中心", "http://www.nm.zsks.cn"),
    LIAONING("辽宁", "辽宁招生考试之窗", "http://www.lnzsks.com"),
    JILIN("吉林", "吉林省教育考试院", "http://www.jleea.edu.cn"),
    HEILONGJIANG("黑龙江", "黑龙江省招生考试信息港", "http://www.lzk.hl.cn"),
    JIANGSU("江苏", "江苏省教育考试院", "http://www.jseea.cn"),
    ZHEJIANG("浙江", "浙江省教育考试院", "http://www.zjzs.net"),
    ANHUI("安徽", "安徽教育招生考试院", "http://www.ahzsks.cn"),
    FUJIAN("福建", "福建省教育考试院", "http://www.fjzs.com.cn"),
    JIANGXI("江西", "江西省教育考试院", "http://www.jxeea.cn"),
    SHANDONG("山东", "山东省教育招生考试院", "http://www.sdzk.cn"),
    HENAN("河南", "河南省招生办公室", "http://www.heao.gov.cn"),
    HUBEI("湖北", "湖北省教育考试院", "http://www.hbea.edu.cn"),
    HUNAN("湖南", "湖南省教育考试院", "http://www.hneeb.cn"),
    GUANGDONG("广东", "广东省教育考试院", "http://www.eeagd.edu.cn"),
    GUANGXI("广西", "广西招生考试院", "http://www.gxeea.cn"),
    HAINAN("海南", "海南省考试局", "http://ea.hainan.gov.cn"),
    SICHUAN("四川", "四川省教育考试院", "http://www.sceea.cn"),
    GUIZHOU("贵州", "贵州省招生考试院", "http://www.gzszk.com"),
    YUNNAN("云南", "云南省招考频道", "http://www.ynzs.cn"),
    XINJIANG("新疆", "新疆招生网", "http://www.xjzk.gov.cn"),
    SHAANXI("陕西", "陕西省教育考试院", "http://www.sneac.com"),
    GANSU("甘肃", "甘肃省教育考试院", "http://www.ganseea.cn"),
    NINGXIA("宁夏", "宁夏教育考试院", "http://www.nxjyks.cn"),
    QINGHAI("青海", "青海省教育考试网", "http://www.qhjyks.com"),
    XIZANG("西藏", "西藏教育考试院", "http://www.xzedu.gov.cn"),
    HONGKONG("香港", "香港考试及评核局", "https://www.hkeaa.edu.hk"),
    MACAU("澳门", "澳门教育及青年发展局", "https://www.dsej.gov.mo");

    /** 省份中文名（匹配 planProvince） */
    private final String province;
    /** 机构名称 */
    private final String siteName;
    /** 官网地址 */
    private final String url;

    ProvinceExamSiteEnum(String province, String siteName, String url) {
        this.province = province;
        this.siteName = siteName;
        this.url = url;
    }

    public String getProvince() {
        return province;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getUrl() {
        return url;
    }

    /**
     * 按省份中文名查找（忽略前后空格），未命中返回 null
     */
    public static ProvinceExamSiteEnum findByProvince(String province) {
        if (province == null || province.isBlank()) {
            return null;
        }
        String target = province.trim();
        for (ProvinceExamSiteEnum site : values()) {
            if (site.province.equals(target)) {
                return site;
            }
        }
        return null;
    }
}
