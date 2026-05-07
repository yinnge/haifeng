package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteDTO {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
