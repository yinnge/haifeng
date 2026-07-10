package com.haifeng.app.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseScoreQueryDTO extends BasePageQueryDTO {
    private Short year;

    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;

    @Size(max = 30, message = "入围分数类型长度不能超过30")
    private String entryScoreType;

    @Size(max = 50, message = "大学名称长度不能超过50")
    private String universityName;

    @Size(max = 100, message = "专业名称长度不能超过100")
    private String majorName;

    @Size(max = 20, message = "专业代码长度不能超过20")
    private String majorCode;
}
