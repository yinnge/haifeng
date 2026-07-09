package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteDTO {

    @NotEmpty(message = "ID列表不能为空")
    @Size(max = 200, message = "最多支持200个ID")
    private List<Long> ids;
}
