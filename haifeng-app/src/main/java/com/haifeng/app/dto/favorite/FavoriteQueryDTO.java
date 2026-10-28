package com.haifeng.app.dto.favorite;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FavoriteQueryDTO extends BasePageQueryDTO {

    @Size(max = 32, message = "收藏类型长度不能超过32")
    private String targetType;
}
