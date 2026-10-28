package com.haifeng.admin.excel.certificate;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 竞赛主表批量导入 Excel 映射实体（对应 t_competition 可编辑字段）。
 * 读取 sheet 名：竞赛主表
 */
@Data
public class CompetitionMainImportDTO {

    @ExcelProperty("竞赛名称")
    private String compName;

    @ExcelProperty("竞赛级别")
    private String compLevel;

    @ExcelProperty("报名时间")
    private String registrationTime;
}
