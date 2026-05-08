package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医疗数据导入DTO (Sheet11: medical)
 */
@Data
public class MedicalExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("三甲医院数量(所)")
    private Integer topHospitalCount;

    @ExcelProperty("三级医院总数(所)")
    private Integer tertiaryHospitalCount;

    @ExcelProperty("医生密度(人/千人)")
    private BigDecimal doctorDensity;

    @ExcelProperty("医疗排名(全国)")
    private Integer medicalRank;
}
