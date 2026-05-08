package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 住房政策导入DTO (Sheet12: housing_policy)
 */
@Data
public class HousingPolicyExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("限购政策")
    private String purchaseRestriction;

    @ExcelProperty("共有产权房(万套)")
    private BigDecimal sharedPropertyHousing;

    @ExcelProperty("公租房(万套)")
    private BigDecimal publicRentalHousing;

    @ExcelProperty("首套房利率(%)")
    private BigDecimal firstHomeRate;

    @ExcelProperty("二套房利率(%)")
    private BigDecimal secondHomeRate;
}
