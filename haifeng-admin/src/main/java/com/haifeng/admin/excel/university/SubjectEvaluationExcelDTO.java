package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class SubjectEvaluationExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty("学科代码")
    private String disciplineCode;

    @ExcelProperty("学科名称")
    private String disciplineName;

    @ExcelProperty("评估轮次")
    private String evaluationRound;

    @ExcelProperty("评估等级")
    private String evaluationGrade;

    @ExcelProperty("排序")
    private Integer sortOrder;

    @ExcelProperty("状态")
    private Integer status;
}
