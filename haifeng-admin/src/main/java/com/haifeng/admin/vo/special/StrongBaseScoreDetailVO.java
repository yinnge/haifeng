package com.haifeng.admin.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrongBaseScoreDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonSerialize(using = ToStringSerializer.class)
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
