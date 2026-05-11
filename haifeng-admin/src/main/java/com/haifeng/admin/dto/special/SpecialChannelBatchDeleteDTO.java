package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class SpecialChannelBatchDeleteDTO {
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
