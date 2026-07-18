package com.haifeng.common.entity.system;

import com.baomidou.mybatisplus.annotation.FieldFill;
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

    @TableId(type = IdType.INPUT)
    private Long id;

    private String siteName;

    private String siteUrl;

    private String siteIcp;

    private String siteDescription;

    /**
     * 每日 API 调用次数上限
     */
    private Integer apiNumber;

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

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
