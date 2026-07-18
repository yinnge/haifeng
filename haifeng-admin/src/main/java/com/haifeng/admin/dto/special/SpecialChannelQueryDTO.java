package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialChannelQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "展示类型查询最长50字符")
    private String displayType;

    @Size(max = 50, message = "通道名称查询最长50字符")
    private String channelName;
}
