package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;

@Data
public class ConstraintDictListVO {
    private String code;
    private String category;
    private String severity;
    private String checkField;
    private Boolean isActive;
}
