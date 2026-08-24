package com.haifeng.app.dto.algorithm.pdf;

import lombok.Data;

/**
 * PDF 分析档案（考生画像与约束条件）保存 DTO。
 * 对应 MemberGaokao 实体「十、考生画像与约束条件（PDF报告用）」中的 PDF 导出版字段，
 * 全部选填；性别等字段属于志愿表/基础档案，由导出 xlsx 时填写，此处不涉及。
 */
@Data
public class PdfProfileDTO {

    /**
     * 发展定位（本科就业/考研深造/并行）
     */
    private String careerDevPath;

    /**
     * 性格特质
     */
    private String personalityTraits;

    /**
     * 兴趣倾向
     */
    private String interestDirection;

    /**
     * 其他疾病（用户自述，无则空）
     */
    private String otherHealthConditions;

    /**
     * 政审情况（自身及直系亲属有无犯罪记录等，大致描述）
     */
    private String politicalReviewStatus;

    /**
     * 是否必须留本省
     */
    private Boolean stayInProvince;

    /**
     * 家庭资源（体制内亲属、行业资源等）
     */
    private String familyResources;

    /**
     * 学费承受度（每年可承担上限）
     */
    private String tuitionAffordability;

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
     * 排斥行业/岗位（可为空）
     */
    private String rejectedIndustries;

    /**
     * 排斥方向（可为空）
     */
    private String rejectedDirections;
}
