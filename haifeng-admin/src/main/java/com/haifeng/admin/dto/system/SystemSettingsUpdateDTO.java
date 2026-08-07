package com.haifeng.admin.dto.system;

import com.haifeng.common.entity.system.BasicMessage;
import com.haifeng.common.entity.system.ContactUrl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemSettingsUpdateDTO {

    @Size(max = 50, message = "网站名称最多50字符")
    private String siteName;

    @Size(max = 100, message = "网站Logo URL最多100字符")
    private String siteUrl;

    @Size(max = 100, message = "ICP备案号最多100字符")
    private String siteIcp;

    private String siteDescription;

    @Min(value = 1, message = "API调用次数最小为1")
    private Integer apiNumber;

    /**
     * 默认志愿表「搏」档（reach high）数量
     */
    @Min(value = 0, message = "搏档数量不能小于0")
    private Integer reachHighCount;

    /**
     * 默认志愿表「冲」档（reach）数量
     */
    @Min(value = 0, message = "冲档数量不能小于0")
    private Integer reachCount;

    /**
     * 默认志愿表「稳」档（match）数量
     */
    @Min(value = 0, message = "稳档数量不能小于0")
    private Integer matchCount;

    /**
     * 默认志愿表「保」档（safe）数量
     */
    @Min(value = 0, message = "保档数量不能小于0")
    private Integer safeCount;

    /**
     * 默认志愿表「垫」档（floor）数量
     */
    @Min(value = 0, message = "垫档数量不能小于0")
    private Integer floorCount;

    @Min(value = 0, message = "Pro会员价格不能为负")
    private Integer proPrice;

    @Min(value = 0, message = "VIP会员价格不能为负")
    private Integer vipPrice;

    @Min(value = 0, message = "Pro提成比例不能小于0")
    @Max(value = 100, message = "Pro提成比例不能大于100")
    private Integer proCommissionRate;

    @Min(value = 0, message = "VIP提成比例不能小于0")
    @Max(value = 100, message = "VIP提成比例不能大于100")
    private Integer vipCommissionRate;

    @Size(max = 200, message = "SEO标题最多200字符")
    private String seoTitle;

    @Size(max = 100, message = "SEO关键词最多100字符")
    private String seoKeywords;

    private String seoDescription;

    /**
     * 社交媒体链接（整体替换）
     */
    private ContactUrl contactUrl;

    /**
     * 基本联系信息（整体替换）
     */
    private BasicMessage basicMessage;
}
