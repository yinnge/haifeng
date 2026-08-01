package com.haifeng.admin.dto.employment.contentManagement.notice;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NoticeAddDTO {
    @NotBlank(message = "公告分类不能为空")
    private String noticeCategory;
    private String noticeType;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String summary;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String province;
    private String city;
    private String[] tags;
    private Integer year;
    private String source;
    private String sourceUrl;
    private OffsetDateTime publishDate;
    private String publishUnit;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private Integer recruitmentCount;
    private Boolean isTop;
    private Boolean isImportant;
    private Integer sortOrder;
}
