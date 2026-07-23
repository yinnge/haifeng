package com.haifeng.admin.vo.special;

import lombok.*;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
