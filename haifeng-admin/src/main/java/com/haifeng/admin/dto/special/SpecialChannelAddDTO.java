package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpecialChannelAddDTO {
    @NotBlank(message = "通道代码不能为空")
    @Size(max = 30, message = "通道代码长度不能超过30")
    private String channelCode;

    @NotBlank(message = "通道名称不能为空")
    @Size(max = 50, message = "通道名称长度不能超过50")
    private String channelName;

    @Size(max = 200, message = "副标题长度不能超过200")
    private String subtitle;

    @Size(max = 30, message = "父级代码长度不能超过30")
    private String parentCode;

    @Size(max = 30, message = "筛选标签长度不能超过30")
    private String filterLabel;

    @NotBlank(message = "展示类型不能为空")
    @Size(max = 20, message = "展示类型长度不能超过20")
    private String displayType;

    private String content;
    private Integer sortOrder;
}
