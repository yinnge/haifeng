package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端专业薪资/就业排行查询 DTO（spec 任务1接口4） */
@Data
@EqualsAndHashCode(callSuper = true)
public class MajorRankingQueryDTO extends BasePageQueryDTO {

    /** 模糊查询（LIKE %name%） */
    private String name;

    /** 精准查询 */
    private String majorCategory;

    /** 排序字段，默认 employmentRate */
    @Pattern(regexp = "employmentRate|salaryMin|salaryMax", message = "sortBy 只支持 employmentRate、salaryMin、salaryMax")
    private String sortBy = "employmentRate";

    /** 排序方向，默认 desc */
    @Pattern(regexp = "asc|desc", message = "sortOrder 只支持 asc、desc")
    private String sortOrder = "desc";
}
