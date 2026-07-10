package com.haifeng.app.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialChannelUnivQueryDTO extends BasePageQueryDTO {
    @NotBlank(message = "通道代码不能为空")
    @Size(max = 30, message = "通道代码长度不能超过30")
    private String channelCode;

    @Size(max = 50, message = "通道名称长度不能超过50")
    private String channelName;

    @Size(max = 20, message = "地区标签长度不能超过20")
    private String regionTag;

    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;
}
