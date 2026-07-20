package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorListQueryDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "考研专业名称最长100个字符")
    private String name;

    @Size(max = 20, message = "考研专业代码最长20个字符")
    private String code;

    @Size(max = 20, message = "学位类型最长20个字符")
    private String degreeType;

    @Size(max = 50, message = "学科门类最长50个字符")
    private String disciplineCategory;

    @Size(max = 10, message = "热门程度最长10个字符")
    private String popularity;

    @Size(max = 10, message = "难度最长10个字符")
    private String difficulty;
}
