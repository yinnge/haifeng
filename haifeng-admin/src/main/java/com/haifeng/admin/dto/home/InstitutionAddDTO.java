package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class InstitutionAddDTO {
    @NotBlank(message = "机构名称不能为空")
    @Size(max = 100, message = "机构名称最长100字符")
    private String name;

    @NotBlank(message = "机构类型不能为空")
    @Size(max = 100, message = "机构类型最长100字符")
    private String type;

    @Size(max = 20, message = "联系电话最长20字符")
    private String phone;

    @Size(max = 100, message = "地址最长100字符")
    private String address;

    private String description;
    private List<String> courses;
    private List<String> images;

    @Size(max = 200, message = "Logo URL最长200字符")
    private String logo;

    private Integer sortOrder;
}
