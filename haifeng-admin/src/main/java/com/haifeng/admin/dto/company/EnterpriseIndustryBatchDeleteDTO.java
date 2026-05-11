package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EnterpriseIndustryBatchDeleteDTO {
    @NotEmpty(message = "请选择要删除的关联记录")
    private List<Long> ids;
}
