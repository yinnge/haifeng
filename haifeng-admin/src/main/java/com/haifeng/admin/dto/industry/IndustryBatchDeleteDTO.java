package com.haifeng.admin.dto.industry;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class IndustryBatchDeleteDTO {
    @NotEmpty(message = "请选择要删除的行业")
    @Size(max = 200, message = "单次最多删除200条记录")
    private List<Long> ids;
}
