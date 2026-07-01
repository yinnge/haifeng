package com.haifeng.common.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "system_settings", autoResultMap = true)
public class SystemSettings {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String siteName;

    private String siteUrl;

    private String siteIcp;

    private String siteDescription;

    /**
     * 默认大学API调用次数
     */
    private Integer universityApiNumber;

    /**
     * 默认专业API调用次数
     */
    private Integer majorApiNumber;

    /**
     * 默认城市API调用次数
     */
    private Integer cityApiNumber;

    private String providerName;

    private String modelName;

    private Integer proPrice;

    private Integer vipPrice;

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

    /**
     * Pro会员提成比例（0-100），代表0%到100%
     */
    private Integer proCommissionRate;

    /**
     * VIP会员提成比例（0-100），代表0%到100%
     */
    private Integer vipCommissionRate;

    private String seoTitle;

    private String seoKeywords;

    private String seoDescription;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private ContactUrl contactUrl;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private BasicMessage basicMessage;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
