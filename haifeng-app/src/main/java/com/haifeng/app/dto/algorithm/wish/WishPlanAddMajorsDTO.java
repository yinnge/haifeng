package com.haifeng.app.dto.algorithm.wish;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class WishPlanAddMajorsDTO {

    @NotNull(message = "专业组ID不能为空")
    private Integer groupId;

    @NotEmpty(message = "请选择至少一个专业")
    private List<Long> majorIds;
}
