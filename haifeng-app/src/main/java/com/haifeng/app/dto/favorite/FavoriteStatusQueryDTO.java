package com.haifeng.app.dto.favorite;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class FavoriteStatusQueryDTO {

    @NotBlank(message = "收藏类型不能为空")
    @Size(max = 32, message = "收藏类型长度不能超过32")
    private String targetType;

    @NotEmpty(message = "目标ID列表不能为空")
    @Size(max = 100, message = "一次最多查询100个目标")
    private List<@NotNull(message = "目标ID不能为空") @Min(value = 1, message = "目标ID必须大于0") Long> targetIds;
}
