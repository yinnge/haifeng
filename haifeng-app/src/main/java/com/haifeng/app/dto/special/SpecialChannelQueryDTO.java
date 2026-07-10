package com.haifeng.app.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialChannelQueryDTO extends BasePageQueryDTO {
    @Size(max = 20, message = "展示类型长度不能超过20")
    private String displayType;

    @Size(max = 50, message = "通道名称长度不能超过50")
    private String channelName;
}
