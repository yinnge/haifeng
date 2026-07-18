package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseScoreQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "大学名称搜索长度不能超过50")
    private String universityName;
    private Short year;
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;
    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;
}
