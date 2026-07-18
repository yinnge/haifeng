package com.haifeng.admin.vo.special;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelUnivListVO {
    private Long id;
    private String channelName;
    private String universityName;
    private Short year;
    private String regionTag;
    private Boolean isActive;
}
