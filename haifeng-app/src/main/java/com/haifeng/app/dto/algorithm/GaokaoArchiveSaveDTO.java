package com.haifeng.app.dto.algorithm;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 保存高考档案请求 DTO
 */
@Data
public class GaokaoArchiveSaveDTO {

    // ========== 必填字段 ==========

    @NotNull(message = "高考年份不能为空")
    @Min(value = 2020, message = "高考年份不能早于2020")
    @Max(value = 2030, message = "高考年份不能晚于2030")
    private Short gaokaoYear;

    @NotBlank(message = "高考省份不能为空")
    @Size(max = 30, message = "高考省份最多30个字符")
    private String gaokaoProvince;

    @NotNull(message = "高考总分不能为空")
    @Min(value = 0, message = "高考总分不能小于0")
    @Max(value = 750, message = "高考总分不能大于750")
    private Integer score;

    @NotNull(message = "位次不能为空")
    @Min(value = 1, message = "位次必须大于0")
    private Integer rank;

    @NotBlank(message = "首选科目不能为空")
    @Size(max = 20, message = "首选科目最多20个字符")
    private String subjectType;

    /**
     * 第二科目（3+1+2 再选科目之一 / 3+3 选考科目之一）
     */
    @Size(max = 20, message = "第二科目最多20个字符")
    private String secondSubjectType;

    /**
     * 第三科目（3+1+2 再选科目之一 / 3+3 选考科目之一）
     */
    @Size(max = 20, message = "第三科目最多20个字符")
    private String thirdSubjectType;

    @NotBlank(message = "批次不能为空")
    @Size(max = 50, message = "批次最多50个字符")
    private String batch;

    @NotNull(message = "批次数据年份不能为空")
    @Min(value = 2020, message = "批次数据年份不能早于2020")
    @Max(value = 2030, message = "批次数据年份不能晚于2030")
    private Short batchDataYear;

    @NotNull(message = "批次省控线不能为空")
    @Min(value = 0, message = "批次省控线不能小于0")
    @Max(value = 750, message = "批次省控线不能大于750")
    private Integer batchLineScore;

    // ========== 可选字段：各科成绩 ==========

    @Min(value = 0, message = "语文成绩不能小于0")
    @Max(value = 150, message = "语文成绩不能大于150")
    private Integer scoreChinese;

    @Min(value = 0, message = "数学成绩不能小于0")
    @Max(value = 150, message = "数学成绩不能大于150")
    private Integer scoreMath;

    @Min(value = 0, message = "外语成绩不能小于0")
    @Max(value = 150, message = "外语成绩不能大于150")
    private Integer scoreEnglish;

    @Min(value = 0, message = "物理分数不能小于0")
    @Max(value = 100, message = "物理分数不能大于100")
    private Integer scorePhysics;

    @Min(value = 0, message = "化学分数不能小于0")
    @Max(value = 100, message = "化学分数不能大于100")
    private Integer scoreChemistry;

    @Min(value = 0, message = "生物分数不能小于0")
    @Max(value = 100, message = "生物分数不能大于100")
    private Integer scoreBiology;

    @Min(value = 0, message = "政治分数不能小于0")
    @Max(value = 100, message = "政治分数不能大于100")
    private Integer scorePolitics;

    @Min(value = 0, message = "历史分数不能小于0")
    @Max(value = 100, message = "历史分数不能大于100")
    private Integer scoreHistory;

    @Min(value = 0, message = "地理分数不能小于0")
    @Max(value = 100, message = "地理分数不能大于100")
    private Integer scoreGeography;

    // ========== 可选字段：外语语种 ==========

    @Size(max = 20, message = "外语语种最多20个字符")
    private String foreignLanguage;

    // ========== 可选字段：身体条件 ==========

    private Boolean isColorBlind;
    private Boolean isColorWeak;

    @DecimalMin(value = "0.0", message = "左眼视力不能小于0")
    @DecimalMax(value = "5.5", message = "左眼视力不能大于5.5")
    private BigDecimal visionLeft;

    @DecimalMin(value = "0.0", message = "右眼视力不能小于0")
    @DecimalMax(value = "5.5", message = "右眼视力不能大于5.5")
    private BigDecimal visionRight;

    private Boolean hasSmellDisorder;

    @Min(value = 100, message = "身高不能小于100厘米")
    @Max(value = 250, message = "身高不能大于250厘米")
    private Integer heightCm;

    @DecimalMin(value = "20.0", message = "体重不能小于20公斤")
    @DecimalMax(value = "200.0", message = "体重不能大于200公斤")
    private BigDecimal weightKg;

    private Boolean isLeftHanded;
    private Boolean hasTattoo;
    private Boolean hasScar;
    private Boolean hasStutter;

    // ========== 可选字段：身份条件 ==========

    private Boolean isFreshGraduate;

    @Size(max = 20, message = "政治面貌最多20个字符")
    private String politicalStatus;

    @Size(max = 20, message = "户籍类型最多20个字符")
    private String householdType;

    private Boolean isPovertyCounty;

    // ========== 可选字段：考生画像与约束条件 ==========

    @Size(max = 10, message = "性别最多10个字符")
    private String gender;

    @Size(max = 500, message = "其他疾病最多500个字符")
    private String otherHealthConditions;

    @Size(max = 200, message = "政审情况最多200个字符")
    private String politicalReviewStatus;

    @Size(max = 500, message = "性格特质最多500个字符")
    private String personalityTraits;

    private Boolean acceptGrassroot;
    private Boolean acceptShiftWork;
    private Boolean acceptNightWork;
    private Boolean acceptBusinessTrip;
    private Boolean acceptRelocation;

    @Size(max = 500, message = "兴趣倾向最多500个字符")
    private String interestDirection;

    @Size(max = 500, message = "排斥行业/岗位最多500个字符")
    private String rejectedIndustries;

    @Size(max = 50, message = "学费承受度最多50个字符")
    private String tuitionAffordability;

    private Boolean stayInProvince;

    @Size(max = 500, message = "家庭资源最多500个字符")
    private String familyResources;

    @Size(max = 50, message = "发展定位最多50个字符")
    private String careerDevPath;

    @Size(max = 500, message = "排斥方向最多500个字符")
    private String rejectedDirection;
}
