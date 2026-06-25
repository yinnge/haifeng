package com.haifeng.common.dto.algorithm.admission;

import lombok.Data;

import java.util.List;

@Data
public class AdmissionMajorFilterDTO {

    /** 专业大类: 工学/理学/医学/文学 等;多选 OR */
    private List<String> majorCategories;

    /** 专业父类(专业大类下的子类): 计算机类/电子信息类 等;多选 OR */
    private List<String> parentCategories;

    /** 专业标签: 热门/紧缺/新兴/基础 等;多选 OR */
    private List<String> majorTags;

    /** 专业类型: 本科/专科/职教本科;多选 OR */
    private List<String> majorTypes;
}
