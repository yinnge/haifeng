package com.haifeng.app.vo.special;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrongBaseScoreListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long universityId;
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
    private String majorName;
    private String majorCode;
    private BigDecimal entryScore;
    private String entryScoreType;
    private String entryRatio;
    private BigDecimal admissionScore;
    private Integer planCount;
    private Integer admissionCount;
}
