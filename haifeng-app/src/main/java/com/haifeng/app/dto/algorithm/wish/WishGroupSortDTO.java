package com.haifeng.app.dto.algorithm.wish;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 专业组排序DTO
 */
@Data
public class WishGroupSortDTO {

    @NotEmpty(message = "排序列表不能为空")
    private List<GroupSortItem> items;

    @Data
    public static class GroupSortItem {
        @NotNull(message = "专业组ID不能为空")
        private Integer groupId;

        @NotNull(message = "排序号不能为空")
        private Integer sortOrder;
    }
}
