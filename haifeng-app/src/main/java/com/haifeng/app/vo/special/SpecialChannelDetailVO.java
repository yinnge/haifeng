package com.haifeng.app.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String channelCode;
    private String channelName;
    private String subtitle;
    private String filterLabel;
    private String displayType;
    private String content;
}
