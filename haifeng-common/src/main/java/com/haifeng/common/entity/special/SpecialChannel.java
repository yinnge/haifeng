package com.haifeng.common.entity.special;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_special_channel")
public class SpecialChannel {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String channelCode;

    private String channelName;

    private String subtitle;

    private String parentCode;

    private String filterLabel;

    private String displayType;

    private String content;

    private Integer sortOrder;

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
