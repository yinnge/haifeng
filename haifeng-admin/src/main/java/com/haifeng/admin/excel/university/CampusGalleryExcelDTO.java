package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CampusGalleryExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty("图片类型")
    private String imageType;

    @ExcelProperty("图片URL")
    private String imageUrl;

    @ExcelProperty("排序权重")
    private Integer sortOrder;
}
