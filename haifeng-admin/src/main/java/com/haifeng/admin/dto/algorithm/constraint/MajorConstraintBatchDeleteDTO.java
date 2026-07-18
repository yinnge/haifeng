package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MajorConstraintBatchDeleteDTO {

    @NotEmpty(message = "请选择要删除的记录")
    @Size(max = 100, message = "批量删除最多100条")
    private List<Long> ids;
}
