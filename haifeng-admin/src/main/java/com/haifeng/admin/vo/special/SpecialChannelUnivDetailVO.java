package com.haifeng.admin.vo.special;

import lombok.*;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelUnivDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
