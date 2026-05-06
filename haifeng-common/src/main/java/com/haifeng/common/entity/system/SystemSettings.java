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

    private Integer apiNumber;

    private Integer proPrice;

    private Integer vipPrice;

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
