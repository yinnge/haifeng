package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorRankingQueryDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "专业名称最长100个字符")
    private String name;

    @Size(max = 50, message = "专业类别最长50个字符")
    private String majorCategory;

    @NotNull(message = "排序字段不能为空")
    @Pattern(regexp = "employmentRate|salaryMin|salaryMax", message = "sortBy 只支持 employmentRate、salaryMin、salaryMax")
    private String sortBy = "employmentRate";

    @NotNull(message = "排序方向不能为空")
    @Pattern(regexp = "asc|desc", message = "sortOrder 只支持 asc、desc")
    private String sortOrder = "desc";
}
