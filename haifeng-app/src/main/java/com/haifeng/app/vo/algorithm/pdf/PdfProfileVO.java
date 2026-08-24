package com.haifeng.app.vo.algorithm.pdf;

import lombok.Data;

/**
 * PDF 分析档案（考生画像与约束条件）查询 VO。
 * 仅包含「十、考生画像与约束条件（PDF报告用）」中 PDF 导出版需要的字段，全部可空。
 */
@Data
public class PdfProfileVO {

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
     * 政审情况
     */
    private String politicalReviewStatus;

    /**
     * 是否必须留本省
     */
    private Boolean stayInProvince;

    /**
     * 家庭资源
     */
    private String familyResources;

    /**
     * 学费承受度
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
