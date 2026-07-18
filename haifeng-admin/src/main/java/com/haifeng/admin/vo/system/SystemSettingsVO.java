package com.haifeng.admin.vo.system;

import com.haifeng.common.entity.system.BasicMessage;
import com.haifeng.common.entity.system.ContactUrl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String siteName;
    private String siteUrl;
    private String siteIcp;
    private String siteDescription;
    private Integer apiNumber;
    private String providerName;
    private String modelName;
    private Integer proPrice;
    private Integer vipPrice;
    private Integer proCommissionRate;
    private Integer vipCommissionRate;
    private String seoTitle;
    private String seoKeywords;
    private String seoDescription;
    private ContactUrl contactUrl;
    private BasicMessage basicMessage;
    private OffsetDateTime updatedAt;
}
