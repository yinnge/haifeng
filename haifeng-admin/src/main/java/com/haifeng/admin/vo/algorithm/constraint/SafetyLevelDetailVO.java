package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SafetyLevelDetailVO {
    private Short level;
    private String code;
    private String name;
    private String nameShort;
    private BigDecimal minCoefficient;
    private BigDecimal maxCoefficient;
    private String color;
    private String confidence;
    private String confidenceReason;
    private String description;
}
