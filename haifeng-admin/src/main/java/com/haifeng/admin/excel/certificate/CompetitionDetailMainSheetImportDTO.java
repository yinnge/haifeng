package com.haifeng.admin.excel.certificate;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.util.List;

/**
 * 竞赛详情「竞赛次表」sheet 导入实体。
 * 读取 sheet 名：竞赛次表
 * text[] 字段（奖项设置列表/竞赛目的列表/评分标准列表/注意事项列表）使用 {@link StringArrayConverter}，
 * 支持中英文逗号隔开的字符串，服务层无需再切分。
 */
@Data
public class CompetitionDetailMainSheetImportDTO {

    @ExcelProperty("竞赛名称")
    private String compName;

    @ExcelProperty(value = "奖项设置列表", converter = StringArrayConverter.class)
    private List<String> awards;

    @ExcelProperty("竞赛背景与意义")
    private String background;

    @ExcelProperty(value = "竞赛目的列表", converter = StringArrayConverter.class)
    private List<String> purposes;

    @ExcelProperty(value = "评分标准列表", converter = StringArrayConverter.class)
    private List<String> scoringCriteria;

    @ExcelProperty(value = "注意事项列表", converter = StringArrayConverter.class)
    private List<String> notices;
}
