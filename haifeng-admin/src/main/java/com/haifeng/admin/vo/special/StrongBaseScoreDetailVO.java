package com.haifeng.admin.vo.special;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class StrongBaseScoreDetailVO {
    private Long id;
    private Long universityId;
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
    private String majorName;
    private String majorCode;
    private BigDecimal entryScore;
    private String entryScoreType;
    private String entryFormula;
    private String entryRatio;
    private BigDecimal admissionScore;
    private String admissionFormula;
    private Integer planCount;
    private Integer admissionCount;
    private String remark;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
