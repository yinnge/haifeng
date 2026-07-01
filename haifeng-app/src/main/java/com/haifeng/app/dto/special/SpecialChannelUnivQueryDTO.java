package com.haifeng.app.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialChannelUnivQueryDTO extends BasePageQueryDTO {
    @NotBlank(message = "通道代码不能为空")
    private String channelCode;
    private String channelName;
    private String regionTag;
    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;
}
