package com.haifeng.admin.dto.employment.contentManagement.guide;

import lombok.Data;

@Data
public class ExamGuideUpdateDTO {
    private String guideCategory;
    private String guideType;
    private String title;
    private String subtitle;
    private String coverImage;
    private String iconClass;
    private String summary;
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
