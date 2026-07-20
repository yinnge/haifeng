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

    @NotBlank(message = "第一科目不能为空")
    @Size(max = 20, message = "第一科目最多20个字符")
    private String subjectType;

    @Size(max = 20, message = "第二科目最多20个字符")
    private String secondSubjectType;

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

    @Min(value = 0, message = "第一科目分数不能小于0")
    @Max(value = 100, message = "第一科目分数不能大于100")
    private Integer scoreSubject1;

    @Min(value = 0, message = "第二科目分数不能小于0")
    @Max(value = 100, message = "第二科目分数不能大于100")
    private Integer scoreSubject2;

    @Min(value = 0, message = "第三科目分数不能小于0")
    @Max(value = 100, message = "第三科目分数不能大于100")
    private Integer scoreSubject3;

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
}
