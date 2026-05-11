package com.haifeng.admin.vo.special;

import lombok.Data;

@Data
public class StrongBaseScoreListVO {
    private Long id;
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
    private String majorName;
    private Boolean isActive;
}
