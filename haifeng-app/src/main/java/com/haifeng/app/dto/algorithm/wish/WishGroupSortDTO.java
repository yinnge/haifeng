package com.haifeng.app.dto.algorithm.wish;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 专业组排序DTO
 */
@Data
public class WishGroupSortDTO {

    @NotEmpty(message = "排序列表不能为空")
    @Size(max = 100, message = "排序列表最多100项")
    @Valid
    private List<GroupSortItem> items;

    @Data
    public static class GroupSortItem {
        @NotNull(message = "专业组ID不能为空")
        private Integer groupId;

        @NotNull(message = "排序号不能为空")
        private Integer sortOrder;
    }
}
