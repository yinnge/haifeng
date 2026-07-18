package com.haifeng.admin.dto.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AdminLogBatchDeleteDTO {

    /**
     * 删除类型：ids-按ID批量删除 / lastMonth-删除最近一个月 / all-全部删除
     */
    @NotBlank(message = "删除类型不能为空")
    private String type;

    /**
     * 要删除的ID列表（type=ids时必填）
     */
    @NotEmpty(message = "ID列表不能为空")
    @Size(max = 100, message = "批量删除最多100条")
    private List<Long> ids;
}
