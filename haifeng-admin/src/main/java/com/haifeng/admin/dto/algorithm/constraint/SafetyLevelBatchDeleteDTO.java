package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SafetyLevelBatchDeleteDTO {

    @NotEmpty(message = "请选择要删除的记录")
    private List<Short> levels;
}
