package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class SpecialChannelUnivAddDTO {
    @NotBlank(message = "通道代码不能为空")
    @Size(max = 30, message = "通道代码长度不能超过30")
    private String channelCode;

    @NotBlank(message = "通道名称不能为空")
    @Size(max = 50, message = "通道名称长度不能超过50")
    private String channelName;

    @NotNull(message = "大学ID不能为空")
    private Long universityId;

    @NotBlank(message = "大学名称不能为空")
    @Size(max = 50, message = "大学名称长度不能超过50")
    private String universityName;

    @NotNull(message = "年份不能为空")
    private Short year;

    @Size(max = 20, message = "地区标签长度不能超过20")
    private String regionTag;

    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;

    @Size(max = 500, message = "官网URL长度不能超过500")
    private String officialUrl;

    @Size(max = 200, message = "简章标题长度不能超过200")
    private String brochureTitle;

    private String brochureContent;
    private Integer sortOrder;
}
