package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlannerQueryDTO extends BasePageQueryDTO {

    /** 姓名模糊查询 */
    private String name;

    /** 服务对象筛选：高考 / 考研（可选，不传查全部） */
    private String serviceTarget;

    /** 所属省份筛选（可选，ProvinceEnum 裸值） */
    private String province;

    /**
     * 上下架状态筛选（可选）：
     * <ul>
     *   <li>{@code null} = 全部（含已下线）</li>
     *   <li>{@code 1} = 仅在线（is_deleted = false）</li>
     *   <li>{@code 0} = 仅已下线（is_deleted = true）</li>
     * </ul>
     * 规划师无 status 列，上下架语义由软删除标记 is_deleted 承担。
     */
    private Short status;
}
