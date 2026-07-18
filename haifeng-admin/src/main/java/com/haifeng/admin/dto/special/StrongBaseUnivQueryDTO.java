package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseUnivQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "大学名称搜索长度不能超过50")
    private String universityName;
    private Boolean isPilot;
    private Short pilotYear;
    private Boolean testBeforeScore;
}
