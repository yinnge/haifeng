package com.haifeng.app.vo.algorithm.wish;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WishPlanGroupVO {
    private Integer id;
    private Integer groupId;
    private Integer planId;
    private Integer groupSortOrder;
    private Long universityId;
    private String universityName;
    private String cityName;
    private String category;
    private String nature;
    private String groupCode;
    private String groupName;
    private String enrollmentCode;
    private Short year;
    private String province;
    private String batch;
    private List<String> subjects;
    private List<String> constraintsDescription;
    private String description;
    private Integer majorCount;
    private List<String> tags;
    private Integer recommendationYear;
    private BigDecimal recommendationRate;
}
