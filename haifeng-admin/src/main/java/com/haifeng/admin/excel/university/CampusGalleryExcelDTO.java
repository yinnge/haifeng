package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

@Data
public class CampusGalleryExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty("图片类型")
    private String imageType;

    @ExcelProperty("图片URL")
    private String imageUrl;

    @ExcelProperty(value = "排序权重", converter = ExcelIntegerConverter.class)
    private Integer sortOrder;
}
