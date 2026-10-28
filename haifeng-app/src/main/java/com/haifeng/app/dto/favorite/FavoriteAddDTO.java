package com.haifeng.app.dto.favorite;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FavoriteAddDTO {

    @NotBlank(message = "收藏类型不能为空")
    @Size(max = 32, message = "收藏类型长度不能超过32")
    private String targetType;

    @NotNull(message = "收藏目标ID不能为空")
    @Min(value = 1, message = "收藏目标ID必须大于0")
    private Long targetId;
}
