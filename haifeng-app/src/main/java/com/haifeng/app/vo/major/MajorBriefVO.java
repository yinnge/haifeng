package com.haifeng.app.vo.major;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MajorBriefVO {
    private String majorCode;
    private String majorName;
    private String disciplineName;
    private String majorType;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private String description;
}
