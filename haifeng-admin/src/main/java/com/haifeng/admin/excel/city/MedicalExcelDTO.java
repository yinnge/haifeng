package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医疗数据导入DTO (Sheet11: medical)
 */
@Data
public class MedicalExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty(value = "三甲医院数量(所)", converter = ExcelIntegerConverter.class)
    private Integer topHospitalCount;

    @ExcelProperty(value = "三级医院总数(所)", converter = ExcelIntegerConverter.class)
    private Integer tertiaryHospitalCount;

    @ExcelProperty("医生密度(人/千人)")
    private BigDecimal doctorDensity;

    @ExcelProperty(value = "医疗排名(全国)", converter = ExcelIntegerConverter.class)
    private Integer medicalRank;
}
