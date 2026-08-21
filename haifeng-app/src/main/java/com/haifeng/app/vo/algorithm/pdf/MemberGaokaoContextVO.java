package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户高考档案上下文（PDF AI 分析用）
 * <p>从 t_member_gaokao 提取，供 AI 提示词与 PDF 封面展示使用。
 * 仅包含 AI 分析所需的精简字段，不包含完整档案信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberGaokaoContextVO {

    // ========== 必填字段 ==========

    /** 省份 */
    private String province;

    /** 总分 */
    private Integer score;

    /** 位次 */
    private Integer rank;

    /** 选科类型（物理/历史/文科/理科） */
    private String subjectType;

    // ========== 可选：各科成绩 ==========

    /** 语文成绩 */
    private Integer scoreChinese;

    /** 数学成绩 */
    private Integer scoreMath;

    /** 外语成绩 */
    private Integer scoreEnglish;

    /** 物理成绩 */
    private Integer scorePhysics;

    /** 化学成绩 */
    private Integer scoreChemistry;

    /** 生物成绩 */
    private Integer scoreBiology;

    /** 政治成绩 */
    private Integer scorePolitics;

    /** 历史成绩 */
    private Integer scoreHistory;

    /** 地理成绩 */
    private Integer scoreGeography;

    // ========== 可选：身体条件（建议性参考） ==========

    /** 是否色盲 */
    private Boolean isColorBlind;

    /** 是否色弱 */
    private Boolean isColorWeak;

    /** 左眼视力 */
    private BigDecimal visionLeft;

    /** 右眼视力 */
    private BigDecimal visionRight;

    /** 身高（厘米） */
    private Integer heightCm;

    // ========== 可选：身份条件 ==========

    /** 是否应届生 */
    private Boolean isFreshGraduate;

    /** 是否贫困县户籍 */
    private Boolean isPovertyCounty;

    // ========== 可选：批次与线差 ==========

    /** 批次名称 */
    private String batch;

    /** 批次省控线 */
    private Integer batchLineScore;

    /** 线差（总分 - 省控线） */
    private Integer scoreAboveLine;

    // ========== 可选：考生画像与约束条件 ==========

    /** 性别 */
    private String gender;

    /** 其他疾病 */
    private String otherHealthConditions;

    /** 政审情况 */
    private String politicalReviewStatus;

    /** 性格特质 */
    private String personalityTraits;

    /** 是否接受基层岗位 */
    private Boolean acceptGrassroot;

    /** 是否接受倒班 */
    private Boolean acceptShiftWork;

    /** 是否接受夜班 */
    private Boolean acceptNightWork;

    /** 是否接受长期出差 */
    private Boolean acceptBusinessTrip;

    /** 是否接受异地工作 */
    private Boolean acceptRelocation;

    /** 兴趣倾向 */
    private String interestDirection;

    /** 排斥行业/岗位 */
    private String rejectedIndustries;

    /** 学费承受度 */
    private String tuitionAffordability;

    /** 是否留本省 */
    private Boolean stayInProvince;

    /** 家庭资源 */
    private String familyResources;

    /** 发展定位 */
    private String careerDevPath;

    /** 排斥方向 */
    private String rejectedDirections;
}
