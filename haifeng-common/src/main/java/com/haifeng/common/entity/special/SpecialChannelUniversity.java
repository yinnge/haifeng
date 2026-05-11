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
@TableName("t_special_channel_university")
public class SpecialChannelUniversity {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String channelCode;

    private String channelName;

    private Long universityId;

    private String universityName;

    private Short year;

    private String regionTag;

    private OffsetDateTime signupStart;

    private OffsetDateTime signupEnd;

    private String officialUrl;

    private String brochureTitle;

    private String brochureContent;

    private Integer sortOrder;

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
