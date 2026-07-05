package com.haifeng.admin.vo.employment.contentManagement.guide;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class ExamGuideDetailVO {
    private Long id;
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
    private Integer viewCount;
    private Integer likeCount;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
