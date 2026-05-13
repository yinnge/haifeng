package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 高考档案详情 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaokaoArchiveVO {

    private Long id;

    // ========== 高考基本信息 ==========
    private Short gaokaoYear;
    private String gaokaoProvince;
    private Integer score;
    private Integer rank;

    // ========== 改革模式 ==========
    private String reformModel;

    // ========== 选科信息 ==========
    private String subjectType;
    private String secondSubjectType;
    private String thirdSubjectType;

    // ========== 各科成绩 ==========
    private Integer scoreChinese;
    private Integer scoreMath;
    private Integer scoreEnglish;
    private Integer scoreSubject1;
    private Integer scoreSubject2;
    private Integer scoreSubject3;

    // ========== 外语语种 ==========
    private String foreignLanguage;

    // ========== 身体视觉条件 ==========
    private Boolean isColorBlind;
    private Boolean isColorWeak;
    private BigDecimal visionLeft;
    private BigDecimal visionRight;
    private Boolean hasSmellDisorder;

    // ========== 身体指标 ==========
    private Integer heightCm;
    private BigDecimal weightKg;
    private Boolean isLeftHanded;
    private Boolean hasTattoo;
    private Boolean hasScar;
    private Boolean hasStutter;

    // ========== 身份条件 ==========
    private Boolean isFreshGraduate;
    private String politicalStatus;
    private String householdType;
    private Boolean isPovertyCounty;

    // ========== 批次与线差 ==========
    private String batch;
    private Short batchDataYear;
    private Integer batchLineScore;
    private Integer scoreAboveLine;
}
