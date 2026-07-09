package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteDTO {

    @NotEmpty(message = "请选择要删除的记录")
    @Size(max = 200, message = "单次最多删除200条")
    private List<Long> ids;
}
