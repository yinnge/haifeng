package com.haifeng.admin.vo.special;

import lombok.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String channelCode;
    private String channelName;
    private String displayType;
    private Boolean isActive;
}
