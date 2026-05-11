package com.haifeng.admin.vo.special;

import lombok.Data;

@Data
public class StrongBaseUnivListVO {
    private Long id;
    private String universityName;
    private Boolean isPilot;
    private Short pilotYear;
    private Boolean testBeforeScore;
}
