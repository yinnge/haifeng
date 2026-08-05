package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class EnterprisePositionBatchDeleteDTO {
    @NotEmpty(message = "请选择要删除的岗位")
    @Size(max = 200, message = "单次最多删除200条记录")
    private List<Long> ids;
}
