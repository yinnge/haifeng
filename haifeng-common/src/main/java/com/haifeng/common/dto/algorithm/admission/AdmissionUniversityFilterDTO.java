package com.haifeng.common.dto.algorithm.admission;

import com.haifeng.common.enums.PopulationBucketEnum;
import lombok.Data;

import java.util.List;

@Data
public class AdmissionUniversityFilterDTO {

    /** 大学标签: 985/211/双一流/部委直属 等;多选 OR */
    private List<String> tags;

    /** 院校性质: 公办/民办/中外合作;多选 OR */
    private List<String> nature;

    /** 知名联盟: 985/211/双一流/C9/E9/中坚9校 等;多选 OR */
    private List<String> famousUnion;

    /** 院校类别: 综合/理工/师范/医药/农林/财经/政法/语言/艺术/民族/体育/军事;多选 OR */
    private List<String> category;

    /** 办学层次: 本科/专科/本专兼招;多选 OR */
    private List<String> educationLevel;

    /** 隶属部门: 教育部/工信部 等;多选 OR */
    private List<String> department;

    /** 省份: 北京/上海 等;多选 OR */
    private List<String> provinces;

    /** 地区: 华东/华北 等;多选 OR */
    private List<String> regions;

    /** 城市常住人口桶(单选) */
    private PopulationBucketEnum populationBucket;

    /** 学科评估等级: A+/A/A-/B+/B/B-/C+/C/C-;多选 OR */
    private List<String> evaluationGrades;
}
