package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityChannelQueryDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "通道名称长度不能超过100")
    private String channelName;

    @Size(max = 20, message = "地区标签长度不能超过20")
    private String regionTag;
}
