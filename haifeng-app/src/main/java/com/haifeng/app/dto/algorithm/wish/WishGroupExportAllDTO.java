package com.haifeng.app.dto.algorithm.wish;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批量修改专业组下专业导出状态DTO
 */
@Data
public class WishGroupExportAllDTO {

    @NotNull(message = "导出状态不能为空")
    private Boolean isExported;
}
