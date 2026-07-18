package com.haifeng.admin.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrongBaseScoreListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
    private String majorName;
    private Boolean isActive;
}
