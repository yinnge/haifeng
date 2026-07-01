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

    @Min(value = 1, message = "大学API调用次数最小为1")
    private Integer universityApiNumber;

    @Min(value = 1, message = "专业API调用次数最小为1")
    private Integer majorApiNumber;

    @Min(value = 1, message = "城市API调用次数最小为1")
    private Integer cityApiNumber;

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
