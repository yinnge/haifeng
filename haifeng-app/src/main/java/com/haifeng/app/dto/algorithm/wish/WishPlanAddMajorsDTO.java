package com.haifeng.app.dto.algorithm.wish;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class WishPlanAddMajorsDTO {

    /**
     * 可选：指定添加到哪个志愿方案。为空时创建一张全新的志愿表（受会员数量上限限制）。
     */
    private Integer planId;

    /**
     * 可选：新建志愿表时使用的名称。为空时后端自动命名"我的志愿方案N"。
     */
    private String planName;

    @NotNull(message = "专业组ID不能为空")
    private Integer groupId;

    /**
     * 旧接口：按数据库 ID 传入（兼容未迁移的前端）。
     */
    @Size(max = 100, message = "单次最多添加100个专业")
    private List<Long> majorIds;

    /**
     * 新接口：按专业代码传入（不受数据库 ID 变化影响，推荐使用）。
     * 优先使用 majorCodes；若为空则回退到 majorIds。
     */
    @Size(max = 100, message = "单次最多添加100个专业")
    private List<String> majorCodes;

    @AssertTrue(message = "请选择至少一个专业（majorCodes 或 majorIds 至少提供一个）")
    public boolean isMajorsNotEmpty() {
        return (majorCodes != null && !majorCodes.isEmpty())
                || (majorIds != null && !majorIds.isEmpty());
    }
}
