package com.haifeng.admin.vo.system;

import com.haifeng.common.entity.system.BasicMessage;
import com.haifeng.common.entity.system.ContactUrl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String siteName;
    private String siteUrl;
    private String siteIcp;
    private String siteDescription;
    private Integer apiNumber;

    /**
     * 默认志愿表「搏」档（reach high）数量
     */
    private Integer reachHighCount;

    /**
     * 默认志愿表「冲」档（reach）数量
     */
    private Integer reachCount;

    /**
     * 默认志愿表「稳」档（match）数量
     */
    private Integer matchCount;

    /**
     * 默认志愿表「保」档（safe）数量
     */
    private Integer safeCount;

    /**
     * 默认志愿表「垫」档（floor）数量
     */
    private Integer floorCount;

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
