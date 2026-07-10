package com.haifeng.app.vo.employment.contentManagement.examGuide;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamGuideDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
