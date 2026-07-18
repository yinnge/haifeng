package com.haifeng.admin.vo.special;

import lombok.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelDetailVO {
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
