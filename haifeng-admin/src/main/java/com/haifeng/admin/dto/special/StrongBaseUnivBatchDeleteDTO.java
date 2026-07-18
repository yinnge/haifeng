package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class StrongBaseUnivBatchDeleteDTO {
    @NotEmpty(message = "ID列表不能为空")
    @Size(max = 100, message = "批量删除最多100条")
    private List<Long> ids;
}
