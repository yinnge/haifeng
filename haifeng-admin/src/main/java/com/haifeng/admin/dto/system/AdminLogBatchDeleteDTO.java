package com.haifeng.admin.dto.system;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminLogBatchDeleteDTO {

    /**
     * 删除类型：ids-按ID批量删除 / lastMonth-删除最近一个月 / all-全部删除
     */
    @NotNull(message = "删除类型不能为空")
    private String type;

    /**
     * 要删除的ID列表（type=ids时必填）
     */
    private List<Long> ids;
}
