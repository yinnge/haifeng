package com.haifeng.admin.vo.special;

import lombok.Data;

@Data
public class SpecialChannelUnivListVO {
    private Long id;
    private String channelName;
    private String universityName;
    private Short year;
    private String regionTag;
    private Boolean isActive;
}
