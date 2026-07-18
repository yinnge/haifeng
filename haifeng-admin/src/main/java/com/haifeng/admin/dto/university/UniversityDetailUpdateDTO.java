package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UniversityDetailUpdateDTO {

    @Size(max = 200, message = "学校地址不能超过200个字符")
    private String address;

    @Size(max = 50, message = "招生电话不能超过50个字符")
    private String admissionPhone;

    @Size(max = 500, message = "官方网站不能超过500个字符")
    private String website;

    private Integer historyGroupScore;

    private Integer scienceGroupScore;

    private List<String> carouselImages;

    @Size(max = 5000, message = "院校详细介绍不能超过5000个字符")
    private String introduction;

    private Map<String, Integer> rankings;

    @Size(max = 10, message = "出国比例不能超过10个字符")
    private String abroadRate;

    @Size(max = 10, message = "男女比例不能超过10个字符")
    private String genderRatio;

    private Integer sortOrder;

    private Integer status;
}
