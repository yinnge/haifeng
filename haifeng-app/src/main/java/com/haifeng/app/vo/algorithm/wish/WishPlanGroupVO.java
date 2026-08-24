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
    /** 选科要求类型：不限/2选1/3选1/必选1/必选2/必选3 */
    private String requirementType;
    private List<String> constraintsDescription;
    private String description;
    private Integer majorCount;
    private List<String> tags;
    private Integer recommendationYear;
    private BigDecimal recommendationRate;
    private Boolean allExported;
    /** 组级安全等级（0~1），取组内专业快照 safetyLevel 最大值 */
    private BigDecimal safetyLevel;
    /** 组级等级简写（搏/冲/稳/保/垫/禁），跟随组内最大 safetyLevel 对应的专业 */
    private String levelShort;
}
