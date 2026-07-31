package com.haifeng.admin.dto.employment.contentManagement.guide;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExamGuideAddDTO {
    @NotBlank(message = "备考指南分类不能为空")
    private String guideCategory;
    private String guideType;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String subtitle;
    private String coverImage;
    private String iconClass;
    private String summary;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String[] tags;
    private String difficultyLevel;
    private String targetAudience;
    private String authorName;
    private String authorTitle;
    private Boolean isTop;
    private Boolean isRecommended;
    private Integer sortOrder;
}
