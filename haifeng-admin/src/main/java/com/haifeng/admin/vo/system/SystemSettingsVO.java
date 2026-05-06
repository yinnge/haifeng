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

    private Integer apiNumber;

    private Integer proPrice;

    private Integer vipPrice;

    private String seoTitle;

    private String seoKeywords;

    private String seoDescription;

    private ContactUrl contactUrl;

    private BasicMessage basicMessage;

    private OffsetDateTime updatedAt;
}
