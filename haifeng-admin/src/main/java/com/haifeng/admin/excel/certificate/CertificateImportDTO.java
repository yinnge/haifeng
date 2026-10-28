package com.haifeng.admin.excel.certificate;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

/**
 * 证书批量导入 Excel 映射实体（对应 t_certificate 全部可编辑字段）。
 * <p>
 * 说明：
 * - exam_fee 使用 {@link ExcelIntegerConverter}，兼容空单元格 / "450.0" / "450" 文本数字。
 * - exam_requirements 在 Excel 中是一整列「逗号隔开的字符串」，中英文逗号均可，
 *   服务层再按 [，,] 切分为 text[] 数组，故此处用原始 String 接收。
 * - 其余为普通文本列，导入前统一 trim。
 */
@Data
public class CertificateImportDTO {

    @ExcelProperty("证书名称")
    private String certName;

    @ExcelProperty("证书分类")
    private String category;

    @ExcelProperty("证书等级")
    private String certLevel;

    @ExcelProperty("适用专业")
    private String applicableMajor;

    @ExcelProperty("报名时间")
    private String registrationTime;

    @ExcelProperty("考试时间")
    private String examTime;

    @ExcelProperty(value = "考试费用", converter = ExcelIntegerConverter.class)
    private Integer examFee;

    @ExcelProperty("证书简介")
    private String certIntro;

    @ExcelProperty("报考条件")
    private String examRequirements;

    @ExcelProperty("考试安排")
    private String examArrangement;

    @ExcelProperty("官方网站")
    private String officialWebsite;
}
