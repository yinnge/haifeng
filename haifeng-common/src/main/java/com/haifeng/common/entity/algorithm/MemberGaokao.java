package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
     * 首选科目/文理类别（第一科目）
     */
    private String subjectType;

    /**
     * 第二科目（3+1+2 再选科目之一 / 3+3 选考科目之一）
     */
    @TableField("second_subject_type")
    private String secondSubjectType;

    /**
     * 第三科目（3+1+2 再选科目之一 / 3+3 选考科目之一）
     */
    @TableField("third_subject_type")
    private String thirdSubjectType;

    /**
     * 全部已选科目（首选 + 再选，自动跳过 null），供选科匹配 / 限报 / 查询过滤统一使用。
     * 命名不使用 get 前缀，避免被 MyBatis-Plus 当作实体属性映射。
     */
    public List<String> resolveAllSubjects() {
        List<String> list = new ArrayList<>();
        if (subjectType != null) list.add(subjectType);
        if (secondSubjectType != null) list.add(secondSubjectType);
        if (thirdSubjectType != null) list.add(thirdSubjectType);
        return list;
    }

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
     * 物理分数
     */
    @TableField("score_physics")
    private Integer scorePhysics;

    /**
     * 化学分数
     */
    @TableField("score_chemistry")
    private Integer scoreChemistry;

    /**
     * 生物分数
     */
    @TableField("score_biology")
    private Integer scoreBiology;

    /**
     * 政治分数
     */
    @TableField("score_politics")
    private Integer scorePolitics;

    /**
     * 历史分数
     */
    @TableField("score_history")
    private Integer scoreHistory;

    /**
     * 地理分数
     */
    @TableField("score_geography")
    private Integer scoreGeography;

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

    // ========== 十、考生画像与约束条件（PDF报告用） ==========

    /**
     * 性别（男/女）
     */
    private String gender;

    /**
     * 其他疾病（用户自述）
     */
    private String otherHealthConditions;

    /**
     * 政审情况（军校/公安/司法等特殊院校）
     */
    private String politicalReviewStatus;

    /**
     * 性格特质
     */
    private String personalityTraits;

    /**
     * 是否接受基层岗位
     */
    private Boolean acceptGrassroot;

    /**
     * 是否接受倒班
     */
    private Boolean acceptShiftWork;

    /**
     * 是否接受夜班
     */
    private Boolean acceptNightWork;

    /**
     * 是否接受长期出差
     */
    private Boolean acceptBusinessTrip;

    /**
     * 是否接受异地工作
     */
    private Boolean acceptRelocation;

    /**
     * 兴趣倾向
     */
    private String interestDirection;

    /**
     * 排斥行业/岗位
     */
    private String rejectedIndustries;

    /**
     * 学费承受度（每年可承担上限）
     */
    private String tuitionAffordability;

    /**
     * 是否必须留本省
     */
    private Boolean stayInProvince;

    /**
     * 家庭资源（体制内亲属、行业资源等）
     */
    private String familyResources;

    /**
     * 发展定位（本科就业/考研深造/并行）
     */
    private String careerDevPath;

    /**
     * 排斥方向
     */
    private String rejectedDirections;

    // ========== 审计字段 ==========

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
