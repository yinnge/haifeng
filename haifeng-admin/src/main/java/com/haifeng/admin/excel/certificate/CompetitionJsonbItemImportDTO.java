package com.haifeng.admin.excel.certificate;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 竞赛详情 4 个 JSONB sheet 的通用导入实体。
 * 读取 sheet 名：基本信息 / 竞赛规则 / 参赛流程指南 / 奖项设置展示
 * 每一行对应一条 JSONB 数组元素 {title, content}（严格对照前端 CompetitionBasicInfoItem）。
 */
@Data
public class CompetitionJsonbItemImportDTO {

    @ExcelProperty("竞赛名称")
    private String compName;

    @ExcelProperty("标题")
    private String title;

    @ExcelProperty("内容")
    private String content;
}
