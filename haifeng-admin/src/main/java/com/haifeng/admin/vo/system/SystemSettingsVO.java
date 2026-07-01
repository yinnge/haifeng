package com.haifeng.admin.vo.system;

import com.haifeng.common.entity.system.BasicMessage;
import com.haifeng.common.entity.system.ContactUrl;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SystemSettingsVO {

    private Long id;

    private String siteName;

    private String siteUrl;

    private String siteIcp;

    private String siteDescription;

    private Integer universityApiNumber;

    private Integer majorApiNumber;

    private Integer cityApiNumber;

    private String providerName;

    private String modelName;

    private Integer proPrice;

    private Integer vipPrice;

    /**
     * Pro会员提成比例（0-100）
     */
    private Integer proCommissionRate;

    /**
     * VIP会员提成比例（0-100）
     */
    private Integer vipCommissionRate;

    private String seoTitle;

    private String seoKeywords;

    private String seoDescription;

    private ContactUrl contactUrl;

    private BasicMessage basicMessage;

    private OffsetDateTime updatedAt;
}
