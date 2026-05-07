package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UniversityUpdateDTO {

    @NotBlank(message = "院校名称不能为空")
    @Size(max = 50, message = "院校名称不能超过50个字符")
    private String name;

    @NotBlank(message = "院校英文名称不能为空")
    @Size(max = 50, message = "院校英文名称不能超过50个字符")
    private String nameEn;

    @NotBlank(message = "省份不能为空")
    private String provinceName;

    @NotBlank(message = "城市不能为空")
    private String cityName;

    @NotBlank(message = "所属地区不能为空")
    private String region;

    @NotBlank(message = "院校类别不能为空")
    private String category;

    private Integer majorCount;

    private String educationLevel;

    private String nature;

    private BigDecimal recommendationRate;

    private Integer recommendationYear;

    private Boolean hasDoctorate;

    private Boolean hasMaster;

    private String department;

    private List<String> tags;

    private String famousUnion;

    private String imageUrl;

    private String introduction;

    private Integer sortOrder;

    private Integer status;
}
