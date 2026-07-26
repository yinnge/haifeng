package com.haifeng.admin.vo.employment.contentManagement.guide;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class ExamGuideListVO {
    @JsonSerialize(using = ToStringSerializer.class)
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
