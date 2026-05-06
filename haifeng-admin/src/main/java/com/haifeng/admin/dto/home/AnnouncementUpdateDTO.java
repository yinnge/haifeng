package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementUpdateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 20, message = "标签最长20字符")
    private String tag;
}
