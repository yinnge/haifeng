package com.haifeng.app.dto.algorithm.wish;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 专业排序DTO
 */
@Data
public class WishMajorSortDTO {

    @NotEmpty(message = "排序列表不能为空")
    private List<MajorSortItem> items;

    @Data
    public static class MajorSortItem {
        @NotNull(message = "专业ID不能为空")
        private Long majorId;

        @NotNull(message = "排序号不能为空")
        private Integer sortOrder;
    }
}
