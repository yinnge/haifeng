package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 用户高考档案实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member_gaokao")
public class MemberGaokao {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联会员ID（唯一）
     */
    private Long memberId;

    // ========== 一、高考基本信息 ==========

    /**
     * 高考年份
     */
    private Short gaokaoYear;

    /**
     * 高考省份
     */
    private String gaokaoProvince;

    /**
     * 高考总分
     */
    private Integer score;

    /**
     * 位次
     */
    private Integer rank;

    // ========== 二、改革模式 ==========

    /**
     * 改革模式（3+3/3+1+2/传统文理）
     */
    private String reformModel;

    // ========== 三、选科信息 ==========

    /**
     * 第一科目
     */
    private String subjectType;

    /**
     * 第二科目
     */
    private String secondSubjectType;

    /**
     * 第三科目
     */
    private String thirdSubjectType;

    // ========== 四、各科成绩 ==========

    /**
     * 语文成绩
     */
    private Integer scoreChinese;

    /**
     * 数学成绩
     */
    private Integer scoreMath;

    /**
     * 外语成绩
     */
    private Integer scoreEnglish;

    /**
     * 第一科目分数
     */
    private Integer scoreSubject1;

    /**
     * 第二科目分数
     */
    private Integer scoreSubject2;

    /**
     * 第三科目分数
     */
    private Integer scoreSubject3;

    // ========== 五、外语语种 ==========

    /**
     * 外语语种
     */
    private String foreignLanguage;

    // ========== 六、身体视觉条件 ==========

    /**
     * 是否色盲
     */
    private Boolean isColorBlind;

    /**
     * 是否色弱
     */
    private Boolean isColorWeak;

    /**
     * 左眼视力
     */
    private BigDecimal visionLeft;

    /**
     * 右眼视力
     */
    private BigDecimal visionRight;

    /**
     * 是否嗅觉迟钝
     */
    private Boolean hasSmellDisorder;

    // ========== 七、身体指标 ==========

    /**
     * 身高（厘米）
     */
    private Integer heightCm;

    /**
     * 体重（公斤）
     */
    private BigDecimal weightKg;

    /**
     * 是否左利手
     */
    private Boolean isLeftHanded;

    /**
     * 是否有纹身
     */
    private Boolean hasTattoo;

    /**
     * 是否有面部疤痕
     */
    private Boolean hasScar;

    /**
     * 是否口吃
     */
    private Boolean hasStutter;

    // ========== 八、身份条件 ==========

    /**
     * 是否应届生
     */
    private Boolean isFreshGraduate;

    /**
     * 政治面貌
     */
    private String politicalStatus;

    /**
     * 户籍类型
     */
    private String householdType;

    /**
     * 是否贫困县户籍
     */
    private Boolean isPovertyCounty;

    // ========== 九、批次与线差 ==========

    /**
     * 所在批次名称
     */
    private String batch;

    /**
     * 批次数据来源年份
     */
    private Short batchDataYear;

    /**
     * 批次省控线
     */
    private Integer batchLineScore;

    /**
     * 线差（总分-省控线）
     */
    private Integer scoreAboveLine;

    // ========== 审计字段 ==========

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
