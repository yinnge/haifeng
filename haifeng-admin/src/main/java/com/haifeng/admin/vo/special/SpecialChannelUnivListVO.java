package com.haifeng.admin.vo.special;

import lombok.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelUnivListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String channelName;
    private String universityName;
    private Short year;
    private String regionTag;
    private Boolean isActive;
}
