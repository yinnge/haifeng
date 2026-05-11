package com.haifeng.admin.vo.special;

import lombok.Data;

@Data
public class SpecialChannelListVO {
    private Long id;
    private String channelCode;
    private String channelName;
    private String displayType;
    private Boolean isActive;
}
