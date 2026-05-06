package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class PlannerAddDTO {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最长50字符")
    private String name;

    @Size(max = 50, message = "职位最长50字符")
    private String position;

    @Size(max = 20, message = "地区最长20字符")
    private String region;

    @Size(max = 100, message = "头像URL最长100字符")
    private String avatar;

    @Size(max = 100, message = "专长最长100字符")
    private String specialty;

    @Size(max = 100, message = "抖音名称最长100字符")
    private String douyinName;

    @Size(max = 100, message = "抖音链接最长100字符")
    private String douyinUrl;

    private String personalDescription;
    private String experienceJob;
    private List<String> achievements;
    private List<String> expertiseAreas;
    private Integer sortOrder;
}
