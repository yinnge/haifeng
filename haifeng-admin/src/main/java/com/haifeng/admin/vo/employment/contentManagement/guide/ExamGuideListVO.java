package com.haifeng.admin.vo.employment.contentManagement.guide;

import lombok.Data;

@Data
public class ExamGuideListVO {
    private Long id;
    private String guideCategory;
    private String guideType;
    private String title;
    private String subtitle;
    private Boolean isTop;
    private Boolean isRecommended;
    private Integer viewCount;
    private Integer likeCount;
    private Integer sortOrder;
}
