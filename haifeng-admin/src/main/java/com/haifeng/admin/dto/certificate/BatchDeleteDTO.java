package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteDTO {

    @NotEmpty(message = "请选择要删除的记录")
    private List<Long> ids;
}
