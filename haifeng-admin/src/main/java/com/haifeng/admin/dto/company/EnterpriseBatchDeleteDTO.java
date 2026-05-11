package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EnterpriseBatchDeleteDTO {
    @NotEmpty(message = "请选择要删除的企业")
    private List<Long> ids;
}
